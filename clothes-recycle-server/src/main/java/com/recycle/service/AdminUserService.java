package com.recycle.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recycle.common.BusinessException;
import com.recycle.entity.*;
import com.recycle.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 管理员用户管理服务
 * 提供用户列表查询、启用/禁用、重置密码、修改角色功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserMapper userMapper;
    private final CollectorMapper collectorMapper;
    private final InstitutionMapper institutionMapper;
    private final UserAddressMapper userAddressMapper;
    private final RecycleOrderMapper recycleOrderMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final RoleApplicationMapper roleApplicationMapper;
    private final PointsTransactionMapper pointsTransactionMapper;

    /**
     * 管理员新建用户
     * 创建角色为 USER、状态为正常的用户
     */
    public void createUser(String phone, String password, String name) {
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new BusinessException(400, "手机号格式不正确");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException(400, "密码至少需要6位");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(400, "姓名不能为空");
        }

        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
        );
        if (existUser != null) {
            throw new BusinessException(400, "该手机号已注册");
        }

        User newUser = new User();
        newUser.setPhone(phone);
        newUser.setPasswordHash(BCrypt.hashpw(password));
        newUser.setName(name.trim());
        newUser.setRole("USER");
        newUser.setStatus(1);
        newUser.setPointsBalance(0);
        userMapper.insert(newUser);

        log.info("管理员新建用户：phone={}, name={}", phone, name.trim());
    }

    /**
     * 分页查询用户列表
     * 支持按角色、状态筛选，按手机号/姓名模糊搜索
     */
    public Map<String, Object> listUsers(String role, Integer status, String keyword, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(role != null && !role.isEmpty(), User::getRole, role)
                .eq(status != null, User::getStatus, status)
                .and(keyword != null && !keyword.isEmpty(),
                        w -> w.like(User::getPhone, keyword).or().like(User::getName, keyword))
                .orderByDesc(User::getCreatedAt);

        IPage<User> pageResult = userMapper.selectPage(new Page<>(page, size), wrapper);

        // 排除密码字段
        for (User u : pageResult.getRecords()) {
            u.setPasswordHash(null);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        return result;
    }

    /**
     * 启用/禁用用户
     */
    public void toggleUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        log.info("管理员修改用户 {} 状态为 {}", userId, status);
    }

    /**
     * 重置用户密码为默认值 123456
     */
    public void resetPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPasswordHash(BCrypt.hashpw("123456"));
        userMapper.updateById(user);
        log.info("管理员重置用户 {} 的密码", userId);
    }

    /**
     * 修改用户角色
     * 升级为 COLLECTOR/INSTITUTION 时自动创建扩展记录（如不存在）
     * 降级时只更新 role，不删除扩展记录（保留审计痕迹）
     */
    @Transactional
    public void changeRole(Long userId, String newRole) {
        if (!"USER".equals(newRole) && !"COLLECTOR".equals(newRole) && !"INSTITUTION".equals(newRole)) {
            throw new BusinessException(400, "角色不合法，只能设置为 USER / COLLECTOR / INSTITUTION");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (newRole.equals(user.getRole())) {
            return;
        }

        String oldRole = user.getRole();
        user.setRole(newRole);
        userMapper.updateById(user);

        // 升级为回收员时，自动创建扩展记录
        if ("COLLECTOR".equals(newRole)) {
            Collector existing = collectorMapper.selectOne(
                    new LambdaQueryWrapper<Collector>().eq(Collector::getUserId, userId)
            );
            if (existing == null) {
                Collector collector = new Collector();
                collector.setUserId(userId);
                collector.setName(user.getName());
                collector.setStatus(2); // 已认证
                collectorMapper.insert(collector);
            }
        }

        // 升级为机构时，自动创建扩展记录
        if ("INSTITUTION".equals(newRole)) {
            Institution existing = institutionMapper.selectOne(
                    new LambdaQueryWrapper<Institution>().eq(Institution::getUserId, userId)
            );
            if (existing == null) {
                Institution institution = new Institution();
                institution.setUserId(userId);
                institution.setName(user.getName());
                institution.setStatus(1); // 正常
                institutionMapper.insert(institution);
            }
        }

        log.info("管理员修改用户 {} 角色：{} → {}", userId, oldRole, newRole);
    }

    /**
     * 删除用户（硬删除，同时清理所有关联数据）
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 1. 删除该用户订单关联的状态流转日志
        List<Long> orderIds = recycleOrderMapper.selectList(
                new LambdaQueryWrapper<RecycleOrder>().eq(RecycleOrder::getUserId, userId).select(RecycleOrder::getId)
        ).stream().map(RecycleOrder::getId).toList();
        if (!orderIds.isEmpty()) {
            orderStatusLogMapper.delete(
                    new LambdaQueryWrapper<OrderStatusLog>().in(OrderStatusLog::getOrderId, orderIds)
            );
        }

        // 2. 删除回收订单（用户下单的 + 作为回收员/机构关联的）
        recycleOrderMapper.delete(
                new LambdaQueryWrapper<RecycleOrder>().eq(RecycleOrder::getUserId, userId)
        );
        recycleOrderMapper.delete(
                new LambdaQueryWrapper<RecycleOrder>().eq(RecycleOrder::getCollectorId, userId)
        );
        recycleOrderMapper.delete(
                new LambdaQueryWrapper<RecycleOrder>().eq(RecycleOrder::getInstitutionId, userId)
        );

        // 3. 删除用户地址
        userAddressMapper.delete(
                new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId, userId)
        );

        // 4. 删除积分流水
        pointsTransactionMapper.delete(
                new LambdaQueryWrapper<PointsTransaction>().eq(PointsTransaction::getUserId, userId)
        );

        // 5. 删除角色申请记录
        roleApplicationMapper.delete(
                new LambdaQueryWrapper<RoleApplication>().eq(RoleApplication::getUserId, userId)
        );

        // 6. 删除扩展表记录
        collectorMapper.delete(
                new LambdaQueryWrapper<Collector>().eq(Collector::getUserId, userId)
        );
        institutionMapper.delete(
                new LambdaQueryWrapper<Institution>().eq(Institution::getUserId, userId)
        );

        // 7. 删除用户本身
        userMapper.deleteById(userId);

        log.info("管理员硬删除用户 {}，已清理所有关联数据", userId);
    }
}
