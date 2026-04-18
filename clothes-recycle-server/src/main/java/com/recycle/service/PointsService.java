package com.recycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recycle.common.BusinessException;
import com.recycle.entity.PointsRule;
import com.recycle.entity.PointsTransaction;
import com.recycle.entity.User;
import com.recycle.mapper.PointsRuleMapper;
import com.recycle.mapper.PointsTransactionMapper;
import com.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 积分服务
 * 统一管理积分的增加、扣减、查询和计算
 * 所有积分变动都会记录流水，保证余额和流水的一致性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsService {

    private final PointsTransactionMapper pointsTransactionMapper;
    private final PointsRuleMapper pointsRuleMapper;
    private final UserMapper userMapper;

    /**
     * 增加积分（回收订单完成时调用）
     * 记录积分流水 + 更新用户余额，在同一个事务中执行
     *
     * @param userId      用户ID
     * @param amount      增加的积分数（必须为正数）
     * @param orderId     关联的回收订单ID
     * @param description 变动描述
     */
    @Transactional
    public void addPoints(Long userId, int amount, Long orderId, String description) {
        if (amount <= 0) {
            throw new BusinessException(400, "积分增加数量必须为正数");
        }

        // 查询用户当前余额
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 计算变动后余额
        int balanceAfter = user.getPointsBalance() + amount;

        // 记录积分流水
        PointsTransaction transaction = new PointsTransaction();
        transaction.setUserId(userId);
        transaction.setType("EARN");
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRelatedOrderId(orderId);
        transaction.setDescription(description);
        pointsTransactionMapper.insert(transaction);

        // 更新用户积分余额
        user.setPointsBalance(balanceAfter);
        userMapper.updateById(user);

        log.info("用户 {} 获得 {} 积分，当前余额 {}，关联订单ID {}", userId, amount, balanceAfter, orderId);
    }

    /**
     * 扣减积分（积分商城兑换时调用）
     * 余额不足时抛出异常，记录流水 + 更新余额在同一事务中
     *
     * @param userId      用户ID
     * @param amount      扣减的积分数（传入正数，内部取负）
     * @param exchangeId  关联的兑换订单ID
     * @param description 变动描述
     */
    @Transactional
    public void deductPoints(Long userId, int amount, Long exchangeId, String description) {
        if (amount <= 0) {
            throw new BusinessException(400, "积分扣减数量必须为正数");
        }

        // 查询用户当前余额
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 校验余额是否充足
        if (user.getPointsBalance() < amount) {
            throw new BusinessException(400, "积分余额不足");
        }

        // 计算变动后余额
        int balanceAfter = user.getPointsBalance() - amount;

        // 记录积分流水（金额为负数表示扣减）
        PointsTransaction transaction = new PointsTransaction();
        transaction.setUserId(userId);
        transaction.setType("EXCHANGE");
        transaction.setAmount(-amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setRelatedExchangeId(exchangeId);
        transaction.setDescription(description);
        pointsTransactionMapper.insert(transaction);

        // 更新用户积分余额
        user.setPointsBalance(balanceAfter);
        userMapper.updateById(user);

        log.info("用户 {} 扣减 {} 积分，当前余额 {}，关联兑换订单ID {}", userId, amount, balanceAfter, exchangeId);
    }

    /**
     * 查询用户积分流水列表
     * 按创建时间倒序排列，最新的流水在前
     *
     * @param userId 用户ID
     * @return 积分流水列表
     */
    public List<PointsTransaction> getPointsHistory(Long userId) {
        return pointsTransactionMapper.selectList(
                new LambdaQueryWrapper<PointsTransaction>()
                        .eq(PointsTransaction::getUserId, userId)
                        .orderByDesc(PointsTransaction::getCreatedAt)
        );
    }

    /**
     * 根据积分规则计算本次回收应发放的积分
     * 解析订单中的衣物分类（JSON数组），查询每个分类的积分规则，
     * 按各分类的 pointsPerKg 加权平均后乘以实际重量
     * 如果某个分类没有对应规则或规则被禁用，使用默认值 10 积分/kg
     *
     * @param actualWeight      实际称重重量(kg)
     * @param clothesCategories 衣物分类JSON数组字符串，如 ["外套","裤子"]
     * @return 应发放的积分数（向下取整）
     */
    public int calculatePoints(BigDecimal actualWeight, String clothesCategories) {
        if (actualWeight == null || actualWeight.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        // 默认积分比例：10 积分/kg（兜底值）
        int defaultPointsPerKg = 10;

        // 查询所有启用的积分规则，构建 分类→规则 映射
        List<PointsRule> rules = pointsRuleMapper.selectList(
                new LambdaQueryWrapper<PointsRule>()
                        .eq(PointsRule::getStatus, 1)
        );
        Map<String, Integer> ruleMap = rules.stream()
                .collect(Collectors.toMap(PointsRule::getClothesCategory, PointsRule::getPointsPerKg));

        // 解析衣物分类JSON数组
        List<String> categories = parseCategories(clothesCategories);

        if (categories.isEmpty()) {
            // 没有分类信息，使用默认比例
            return actualWeight.multiply(new BigDecimal(defaultPointsPerKg)).intValue();
        }

        // 计算所有分类的平均积分比例
        int totalPointsPerKg = 0;
        for (String category : categories) {
            totalPointsPerKg += ruleMap.getOrDefault(category, defaultPointsPerKg);
        }
        int avgPointsPerKg = totalPointsPerKg / categories.size();

        // 最终积分 = 实际重量 × 平均积分比例（向下取整）
        return actualWeight.multiply(new BigDecimal(avgPointsPerKg)).intValue();
    }

    /**
     * 解析衣物分类JSON数组字符串
     * 输入格式如 ["外套","裤子"]，解析为 List<String>
     * 解析失败时返回空列表（使用默认积分比例）
     *
     * @param clothesCategories JSON数组字符串
     * @return 分类列表
     */
    private List<String> parseCategories(String clothesCategories) {
        if (clothesCategories == null || clothesCategories.isBlank()) {
            return List.of();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(clothesCategories, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("解析衣物分类JSON失败：{}，将使用默认积分比例", clothesCategories);
            return List.of();
        }
    }
}
