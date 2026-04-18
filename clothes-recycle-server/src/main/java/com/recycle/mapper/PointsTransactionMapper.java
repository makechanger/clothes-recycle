package com.recycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recycle.entity.PointsTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分流水 Mapper
 * 继承 MyBatis-Plus BaseMapper，提供单表 CRUD 操作
 */
@Mapper
public interface PointsTransactionMapper extends BaseMapper<PointsTransaction> {
}
