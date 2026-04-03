package com.recycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recycle.entity.RoleApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色申请 Mapper
 * 操作 role_application 表
 */
@Mapper
public interface RoleApplicationMapper extends BaseMapper<RoleApplication> {
}
