package com.recycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recycle.entity.Community;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社区 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供社区表的基础 CRUD 操作
 */
@Mapper
public interface CommunityMapper extends BaseMapper<Community> {
}
