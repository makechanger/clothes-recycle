package com.recycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.recycle.common.BusinessException;
import com.recycle.entity.Institution;
import com.recycle.entity.RecycleOrder;
import com.recycle.entity.User;
import com.recycle.mapper.InstitutionMapper;
import com.recycle.mapper.RecycleOrderMapper;
import com.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机构订单服务
 * 处理机构端的订单操作：扫码接收、查看已接收订单列表
 * 机构扫描回收员生成的溯源二维码后，完成订单接收并触发积分发放
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstitutionOrderService {

    private final RecycleOrderMapper recycleOrderMapper;
    private final UserMapper userMapper;
    private final InstitutionMapper institutionMapper;
    private final PointsService pointsService;
    private final OrderStatusLogService orderStatusLogService;

    /**
     * 校验当前用户是否为机构角色
     * 非机构用户调用机构接口时抛出异常
     *
     * @param userId 当前用户ID
     */
    private void checkInstitutionRole(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !"INSTITUTION".equals(user.getRole())) {
            throw new BusinessException(403, "无权限，仅机构可操作");
        }
    }

    /**
     * 根据用户ID查询机构扩展信息
     * 同时校验机构状态是否正常（status=1）
     *
     * @param userId 当前用户ID
     * @return 机构扩展信息
     */
    private Institution getInstitutionByUserId(Long userId) {
        Institution institution = institutionMapper.selectOne(
                new LambdaQueryWrapper<Institution>()
                        .eq(Institution::getUserId, userId)
        );
        if (institution == null) {
            throw new BusinessException(404, "机构信息不存在");
        }
        if (institution.getStatus() != 1) {
            throw new BusinessException(403, "机构已被禁用，无法操作");
        }
        return institution;
    }

    /**
     * 机构扫码接收订单（核心方法）
     * 流程：校验机构身份 → 查询订单 → 校验状态 → 计算积分 → 更新订单 → 发放积分 → 记录日志
     * 使用乐观更新（WHERE status=4）防止重复扫码
     *
     * @param userId  当前机构用户ID
     * @param orderNo 订单编号（从二维码扫描获得）
     */
    @Transactional
    public void scanReceive(Long userId, String orderNo) {
        // 1. 校验机构角色和机构状态
        checkInstitutionRole(userId);
        Institution institution = getInstitutionByUserId(userId);

        // 2. 根据订单编号查询订单
        RecycleOrder order = recycleOrderMapper.selectOne(
                new LambdaQueryWrapper<RecycleOrder>()
                        .eq(RecycleOrder::getOrderNo, orderNo)
        );
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 3. 校验订单状态必须为 4（用户已确认）
        if (order.getStatus() != 4) {
            throw new BusinessException(400, "订单状态不正确，无法接收（当前状态：" + order.getStatus() + "）");
        }

        // 4. 根据积分规则计算应发放的积分
        int points = pointsService.calculatePoints(order.getActualWeight(), order.getClothesCategories());

        // 5. 乐观更新订单：设置状态为7（机构已接收）、机构ID、积分数
        //    WHERE status=4 防止并发重复扫码
        int rows = recycleOrderMapper.update(null,
                new LambdaUpdateWrapper<RecycleOrder>()
                        .eq(RecycleOrder::getId, order.getId())
                        .eq(RecycleOrder::getStatus, 4)
                        .set(RecycleOrder::getStatus, 7)
                        .set(RecycleOrder::getInstitutionId, institution.getId())
                        .set(RecycleOrder::getPointsAwarded, points)
        );

        if (rows == 0) {
            throw new BusinessException(400, "接收失败，订单已被处理或状态已变更");
        }

        // 6. 发放积分给用户（记录积分流水 + 更新用户余额）
        if (points > 0) {
            pointsService.addPoints(order.getUserId(), points, order.getId(),
                    "回收订单完成，订单号：" + orderNo);
        }

        // 7. 记录状态变更日志
        orderStatusLogService.log(order.getId(), 4, 7, userId, "institution",
                "机构接收，发放积分" + points);

        log.info("机构用户 {} 接收订单 {}，发放积分 {}", userId, orderNo, points);
    }

    /**
     * 查询机构已接收的订单列表
     * 支持按状态筛选，按更新时间倒序排列
     *
     * @param userId 当前机构用户ID
     * @param status 订单状态（null 表示查全部已接收订单）
     * @return 订单列表
     */
    public List<RecycleOrder> listReceivedOrders(Long userId, Integer status) {
        // 校验机构角色
        checkInstitutionRole(userId);
        Institution institution = getInstitutionByUserId(userId);

        // 查询该机构接收的订单
        LambdaQueryWrapper<RecycleOrder> wrapper = new LambdaQueryWrapper<RecycleOrder>()
                .eq(RecycleOrder::getInstitutionId, institution.getId())
                .orderByDesc(RecycleOrder::getUpdatedAt);

        if (status != null) {
            wrapper.eq(RecycleOrder::getStatus, status);
        } else {
            wrapper.in(RecycleOrder::getStatus, 7, 8);
        }

        return recycleOrderMapper.selectList(wrapper);
    }

    /**
     * 查询机构订单详情
     * 校验订单归属本机构，返回订单信息 + 状态日志
     */
    public Map<String, Object> getOrderDetail(Long userId, Long orderId) {
        checkInstitutionRole(userId);
        Institution institution = getInstitutionByUserId(userId);

        RecycleOrder order = recycleOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!institution.getId().equals(order.getInstitutionId())) {
            throw new BusinessException(403, "无权查看该订单");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("statusLogs", orderStatusLogService.listByOrderId(orderId));

        if (order.getDestinationType() != null) {
            Map<String, Object> destination = new HashMap<>();
            destination.put("destinationType", getDestinationTypeLabel(order.getDestinationType()));
            destination.put("destinationDesc", order.getDestinationDesc());
            destination.put("institutionName", institution.getName());
            result.put("destination", destination);
        }

        return result;
    }

    /**
     * 去向类型英文转中文
     */
    private String getDestinationTypeLabel(String type) {
        if (type == null) return "";
        switch (type) {
            case "DONATION": return "捐赠";
            case "RECYCLE": return "再生利用";
            case "ENVIRONMENTAL": return "环保处理";
            default: return type;
        }
    }
}
