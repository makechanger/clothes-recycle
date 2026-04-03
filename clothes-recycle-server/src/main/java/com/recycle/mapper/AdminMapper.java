package com.recycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recycle.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员 Mapper
 * 操作 admin 表
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}
