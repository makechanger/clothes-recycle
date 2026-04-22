package com.recycle.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recycle.common.BusinessException;
import com.recycle.entity.Admin;
import com.recycle.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 管理员管理服务
 * 提供管理员的增删改查功能（仅超级管理员可操作）
 */
@Service
@RequiredArgsConstructor
public class AdminManagerService {

    private final AdminMapper adminMapper;

    private static final String DEFAULT_PASSWORD = "admin123";

    public Page<Admin> listAdmins(String keyword, Integer page, Integer size) {
        Page<Admin> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<Admin>()
                .orderByDesc(Admin::getCreatedAt);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Admin::getUsername, keyword);
        }
        Page<Admin> result = adminMapper.selectPage(pageParam, wrapper);
        // 清除密码字段
        result.getRecords().forEach(a -> a.setPasswordHash(null));
        return result;
    }

    public void createAdmin(String username, String password, String role) {
        if (username == null || username.isEmpty()) {
            throw new BusinessException(400, "用户名不能为空");
        }
        // 检查用户名是否已存在
        Admin existing = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username)
        );
        if (existing != null) {
            throw new BusinessException(400, "用户名已存在");
        }
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPasswordHash(BCrypt.hashpw(password != null && !password.isEmpty() ? password : DEFAULT_PASSWORD));
        admin.setRole(role != null && !role.isEmpty() ? role : "operator");
        admin.setStatus(1);
        adminMapper.insert(admin);
    }

    public void toggleStatus(Long id, Integer status) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        // 不允许禁用自己（由 Controller 层判断）
        admin.setStatus(status);
        adminMapper.updateById(admin);
    }

    public void resetPassword(Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        admin.setPasswordHash(BCrypt.hashpw(DEFAULT_PASSWORD));
        adminMapper.updateById(admin);
    }

    public void deleteAdmin(Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        adminMapper.deleteById(id);
    }
}
