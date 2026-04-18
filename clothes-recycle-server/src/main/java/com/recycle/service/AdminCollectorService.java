package com.recycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recycle.common.BusinessException;
import com.recycle.dto.CollectorVO;
import com.recycle.dto.CreateCollectorRequest;
import com.recycle.entity.Collector;
import com.recycle.entity.User;
import com.recycle.mapper.CollectorMapper;
import com.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员回收员管理服务
 * 提供管理员创建回收员账号、查询回收员列表等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCollectorService {

    private final UserMapper userMapper;
    private final CollectorMapper collectorMapper;

    /**
     * 管理员创建回收员账号
     * 在 user 表插入一条 role=COLLECTOR 的记录，同时在 collector 表插入扩展记录
     * 新创建的回收员 status=0（待完善资质），需要登录小程序上传身份证照片后再审核
     *
     * @param request 创建回收员请求（手机号+密码+姓名）
     */
    @Transactional
    public void createCollector(CreateCollectorRequest request) {
        String phone = request.getPhone();
        String password = request.getPassword();
        String name = request.getName();

        // 1. 校验手机号格式
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new BusinessException(400, "手机号格式不正确");
        }

        // 2. 校验密码
        if (password == null || password.length() < 6) {
            throw new BusinessException(400, "密码至少需要6位");
        }

        // 3. 校验姓名
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(400, "姓名不能为空");
        }

        // 4. 检查手机号是否已注册
        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
        );
        if (existUser != null) {
            throw new BusinessException(400, "该手机号已注册");
        }

        // 5. 插入 user 表（角色为 COLLECTOR）
        User newUser = new User();
        newUser.setPhone(phone);
        newUser.setPasswordHash(password);
        newUser.setName(name);
        newUser.setRole("COLLECTOR");
        newUser.setStatus(1);
        newUser.setPointsBalance(0);
        userMapper.insert(newUser);

        // 6. 插入 collector 扩展表（status=0 待完善资质）
        Collector collector = new Collector();
        collector.setUserId(newUser.getId());
        collector.setName(name);
        collector.setStatus(0);
        collectorMapper.insert(collector);

        log.info("管理员创建回收员成功：phone={}, name={}", phone, name);
    }

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
            CollectorVO vo = new CollectorVO();
            vo.setId(c.getId());
            vo.setUserId(c.getUserId());
            vo.setName(c.getName());
            vo.setIdCardPhoto(c.getIdCardPhoto());
            vo.setCollectorStatus(c.getStatus());
            vo.setCreatedAt(c.getCreatedAt());

            User user = userMap.get(c.getUserId());
            if (user != null) {
                vo.setPhone(user.getPhone());
            }
            voList.add(vo);
        }

        return voList;
    }
}
