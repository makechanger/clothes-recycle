package com.recycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recycle.dto.CollectorVO;
import com.recycle.entity.Collector;
import com.recycle.entity.User;
import com.recycle.mapper.CollectorMapper;
import com.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员回收员管理服务
 * 提供查询回收员列表等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCollectorService {

    private final UserMapper userMapper;
    private final CollectorMapper collectorMapper;

    /**
     * 查询所有回收员列表
     * 关联 user 表获取手机号，合并为 CollectorVO 返回
     *
     * @return 回收员列表
     */
    public List<CollectorVO> listCollectors() {
        // 1. 查询所有回收员扩展记录
        List<Collector> collectors = collectorMapper.selectList(
                new LambdaQueryWrapper<Collector>().orderByDesc(Collector::getCreatedAt)
        );
        if (collectors.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 批量查询关联的 user 记录（获取手机号）
        List<Long> userIds = collectors.stream()
                .map(Collector::getUserId)
                .collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 3. 组装 VO
        List<CollectorVO> voList = new ArrayList<>();
        for (Collector c : collectors) {
            User user = userMap.get(c.getUserId());

            // 只显示当前角色仍然是回收员的用户
            if (user == null || !"COLLECTOR".equals(user.getRole())) {
                continue;
            }

            CollectorVO vo = new CollectorVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setName(c.getName());
            vo.setIdCardPhoto(c.getIdCardPhoto());
            vo.setCollectorStatus(c.getStatus());
            vo.setCreatedAt(c.getCreatedAt());

            if (user != null) {
                vo.setPhone(user.getPhone());
            }
            voList.add(vo);
        }

        return voList;
    }
}
