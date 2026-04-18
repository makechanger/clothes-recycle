package com.recycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recycle.entity.PointsRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分规则 Mapper
 * 继承 MyBatis-Plus BaseMapper，提供单表 CRUD 操作
 */
@Mapper
public interface PointsRuleMapper extends BaseMapper<PointsRule> {
}
