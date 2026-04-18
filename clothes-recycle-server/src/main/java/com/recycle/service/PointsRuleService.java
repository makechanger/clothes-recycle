package com.recycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.recycle.common.BusinessException;
import com.recycle.entity.PointsRule;
import com.recycle.mapper.PointsRuleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 积分规则管理服务
 * 提供积分规则的查询和更新功能，供管理后台使用
 * 管理员可以调整不同衣物分类的积分兑换比例
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsRuleService {

    private final PointsRuleMapper pointsRuleMapper;

    /**
     * 查询所有积分规则
     * 按衣物分类名称排序
     *
     * @return 积分规则列表
     */
    public List<PointsRule> listRules() {
        return pointsRuleMapper.selectList(
                new LambdaQueryWrapper<PointsRule>()
                        .orderByAsc(PointsRule::getId)
        );
    }

    /**
     * 更新积分规则
     * 管理员可修改每公斤积分数、最低重量和启用/禁用状态
     *
     * @param id           规则ID
     * @param pointsPerKg  每公斤积分数（可为null表示不修改）
     * @param minWeight    最低回收重量(kg)（可为null表示不修改）
     * @param status       状态：0-禁用 1-启用（可为null表示不修改）
     */
    public void updateRule(Long id, Integer pointsPerKg, BigDecimal minWeight, Integer status) {
        // 校验规则是否存在
        PointsRule rule = pointsRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(404, "积分规则不存在");
        }

        // 构建更新条件，只更新非null字段
        LambdaUpdateWrapper<PointsRule> wrapper = new LambdaUpdateWrapper<PointsRule>()
                .eq(PointsRule::getId, id);

        if (pointsPerKg != null) {
            if (pointsPerKg < 0) {
                throw new BusinessException(400, "每公斤积分数不能为负数");
            }
            wrapper.set(PointsRule::getPointsPerKg, pointsPerKg);
        }
        if (minWeight != null) {
            if (minWeight.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(400, "最低重量不能为负数");
            }
            wrapper.set(PointsRule::getMinWeight, minWeight);
        }
        if (status != null) {
            wrapper.set(PointsRule::getStatus, status);
        }

        pointsRuleMapper.update(null, wrapper);
        log.info("更新积分规则 {}：pointsPerKg={}, minWeight={}, status={}", id, pointsPerKg, minWeight, status);
    }

    /**
     * 按衣物分类查询积分规则
     * 仅返回启用状态的规则
     *
     * @param category 衣物分类名称
     * @return 积分规则，不存在则返回null
     */
    public PointsRule getRuleByCategory(String category) {
        return pointsRuleMapper.selectOne(
                new LambdaQueryWrapper<PointsRule>()
                        .eq(PointsRule::getClothesCategory, category)
                        .eq(PointsRule::getStatus, 1)
        );
    }
}
