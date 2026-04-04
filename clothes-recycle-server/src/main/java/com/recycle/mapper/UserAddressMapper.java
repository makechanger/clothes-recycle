package com.recycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recycle.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户地址 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供地址表的基础 CRUD 操作
 */
@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {
}
