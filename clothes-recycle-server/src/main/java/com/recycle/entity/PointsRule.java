package com.recycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分规则实体类
 * 对应数据库 points_rule 表，配置不同衣物分类的积分兑换比例
 * 每个衣物分类对应一条规则，管理员可在后台调整积分比例
 */
@Data
@TableName("points_rule")
public class PointsRule {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 衣物分类（如：外套、裤子、鞋子、床品、其他） */
    private String clothesCategory;

    /** 每公斤可获得的积分数 */
    private Integer pointsPerKg;

    /** 最低回收重量(kg)，低于此重量不计积分 */
    private BigDecimal minWeight;

    /** 规则状态：0-禁用 1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
