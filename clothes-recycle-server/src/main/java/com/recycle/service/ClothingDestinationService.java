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
 * 衣物去向服务
 * 机构接收订单后，为每个订单单独分配去向（一对一模型）
 * 分配去向后订单状态从 7（机构已接收）变为 8（已分配去向）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClothingDestinationService {

    private final RecycleOrderMapper recycleOrderMapper;
    private final InstitutionMapper institutionMapper;
    private final UserMapper userMapper;
    private final OrderStatusLogService orderStatusLogService;

    /**
     * 校验机构身份并返回机构信息
     */
    private Institution checkAndGetInstitution(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || !"INSTITUTION".equals(user.getRole())) {
            throw new BusinessException(403, "无权限，仅机构可操作");
        }
        Institution institution = institutionMapper.selectOne(
                new LambdaQueryWrapper<Institution>().eq(Institution::getUserId, userId)
        );
        if (institution == null) {
            throw new BusinessException(404, "机构信息不存在");
        }
        if (institution.getStatus() != 1) {
            throw new BusinessException(403, "机构已被禁用");
        }
        return institution;
    }

    /**
     * 为单个订单分配去向，状态 7→8
     */
    @Transactional
    public void assignDestination(Long userId, Long orderId, String destinationType, String destinationDesc) {
        Institution institution = checkAndGetInstitution(userId);

        RecycleOrder order = recycleOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!institution.getId().equals(order.getInstitutionId())) {
            throw new BusinessException(403, "订单不属于本机构");
        }
        if (order.getStatus() != 7) {
            throw new BusinessException(400, "仅机构已接收的订单可分配去向");
        }

        int rows = recycleOrderMapper.update(null,
                new LambdaUpdateWrapper<RecycleOrder>()
                        .eq(RecycleOrder::getId, orderId)
                        .eq(RecycleOrder::getStatus, 7)
                        .set(RecycleOrder::getStatus, 8)
                        .set(RecycleOrder::getDestinationType, destinationType)
                        .set(RecycleOrder::getDestinationDesc, destinationDesc)
        );

        if (rows == 0) {
            throw new BusinessException(400, "分配失败，订单状态已变更");
        }

        String typeLabel = getDestinationTypeLabel(destinationType);
        orderStatusLogService.log(orderId, 7, 8, userId, "institution",
                "分配去向：" + typeLabel + "，" + destinationDesc);

        log.info("机构 {} 为订单 {} 分配去向：{}", institution.getId(), orderId, typeLabel);
    }

    /**
     * 查询机构已接收但尚未分配去向的订单（status=7）
     */
    public List<RecycleOrder> listUnassignedOrders(Long userId) {
        Institution institution = checkAndGetInstitution(userId);

        return recycleOrderMapper.selectList(
                new LambdaQueryWrapper<RecycleOrder>()
                        .eq(RecycleOrder::getInstitutionId, institution.getId())
                        .eq(RecycleOrder::getStatus, 7)
                        .orderByDesc(RecycleOrder::getUpdatedAt)
        );
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

    /**
     * 查询某个订单的去向信息（供用户端订单详情使用）
     * 直接从 recycle_order 字段读取，补充机构名称
     */
    public Map<String, Object> getOrderDestination(Long orderId) {
        RecycleOrder order = recycleOrderMapper.selectById(orderId);
        if (order == null || order.getDestinationType() == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("destinationType", getDestinationTypeLabel(order.getDestinationType()));
        result.put("destinationDesc", order.getDestinationDesc());

        if (order.getInstitutionId() != null) {
            Institution institution = institutionMapper.selectById(order.getInstitutionId());
            if (institution != null) {
                result.put("institutionName", institution.getName());
                result.put("institutionType", institution.getType());
            }
        }
        return result;
    }
}
