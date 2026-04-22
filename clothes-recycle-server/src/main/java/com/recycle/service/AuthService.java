package com.recycle.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recycle.common.BusinessException;
import com.recycle.dto.ChangePasswordRequest;
import com.recycle.dto.LoginRequest;
import com.recycle.dto.LoginResponse;
import com.recycle.dto.RegisterRequest;
import com.recycle.entity.Collector;
import com.recycle.entity.Institution;
import com.recycle.entity.User;
import com.recycle.mapper.CollectorMapper;
import com.recycle.mapper.InstitutionMapper;
import com.recycle.mapper.UserMapper;
import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一认证服务
 * 重构后所有角色（用户/回收员/机构）统一存储在 user 表中
 * 通过 user.role 字段区分角色，collector/institution 表为扩展信息表
 * 登录只查 user 表，统一使用 StpUtil 鉴权
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final CollectorMapper collectorMapper;
    private final InstitutionMapper institutionMapper;

    /**
     * 统一登录接口
     * 所有角色都从 user 表查询，通过 role 字段判断角色
     * 如果是回收员或机构，额外查询扩展表返回扩展信息
     *
     * @param request 登录请求（手机号+密码）
     * @return 登录响应（token + 角色 + 用户信息）
     */
    public LoginResponse login(LoginRequest request) {
        String phone = request.getPhone();
        String password = request.getPassword();

        // 1. 从 user 表按手机号查询（所有角色都在 user 表中）
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
        );
        if (user == null) {
            throw new BusinessException(400, "手机号未注册");
        }

        // 2. 验证密码（BCrypt）
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new BusinessException(400, "密码错误");
        }

        // 3. 检查账号状态
        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用或注销");
        }

        // 4. 统一使用 StpUtil 登录
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        String role = user.getRole();

        // 5. 根据角色构建用户信息（包含扩展表数据）
        Map<String, Object> userInfo = buildUserInfoByRole(user);

        log.info("用户 {} 登录成功，角色：{}", phone, role);
        return buildResponse(token, role, userInfo);
    }

    /**
     * 用户注册
     * 所有角色先注册为普通用户（role=USER），后续通过资质申请升级角色
     *
     * @param request 注册请求（手机号+密码+姓名）
     * @return 登录响应（注册成功后自动登录）
     */
    public LoginResponse register(RegisterRequest request) {
        String phone = request.getPhone();
        String password = request.getPassword();
        String name = request.getName();

        // 1. 校验手机号格式（11位数字，1开头）
        if (phone == null || !phone.matches("^1\\d{10}$")) {
            throw new BusinessException(400, "手机号格式不正确");
        }

        // 2. 校验密码不为空
        if (password == null || password.isEmpty()) {
            throw new BusinessException(400, "密码不能为空");
        }

        // 3. 检查手机号是否已注册（只需查 user 表，所有用户都在这里）
        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)
        );
        if (existUser != null) {
            throw new BusinessException(400, "该手机号已注册");
        }

        // 4. 插入 user 表，默认角色为 USER
        User newUser = new User();
        newUser.setPhone(phone);
        newUser.setPasswordHash(BCrypt.hashpw(password));
        newUser.setName(name);
        newUser.setRole("USER");  // 默认普通用户角色
        newUser.setStatus(1);     // 正常状态
        newUser.setPointsBalance(0);
        userMapper.insert(newUser);

        // 5. 自动登录并返回
        StpUtil.login(newUser.getId());
        log.info("新用户注册成功：{}", phone);
        return buildResponse(StpUtil.getTokenValue(), "USER", buildUserInfo(newUser));
    }

    /**
     * 统一修改密码（所有角色通用）
     * 所有角色的密码都存储在 user 表中，统一从 user 表修改
     *
     * @param userId  当前登录用户 ID
     * @param request 修改密码请求（旧密码+新密码）
     */
    public void changePassword(Long userId, ChangePasswordRequest request) {
        // 1. 校验新密码不为空且至少6位
        validateNewPassword(request.getNewPassword());

        // 2. 查询用户记录
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        // 3. 验证旧密码（BCrypt）
        if (!BCrypt.checkpw(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "旧密码错误");
        }

        // 4. 更新密码（BCrypt 加密）
        user.setPasswordHash(BCrypt.hashpw(request.getNewPassword()));
        userMapper.updateById(user);
        log.info("用户 {} 修改密码成功", userId);
    }

    /**
     * 校验新密码格式（至少6位）
     */
    private void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException(400, "新密码至少需要6位");
        }
    }

    /**
     * 根据用户角色构建完整的用户信息
     * 如果是回收员或机构，额外查询扩展表并合并信息
     */
    private Map<String, Object> buildUserInfoByRole(User user) {
        Map<String, Object> info = buildUserInfo(user);

        if ("COLLECTOR".equals(user.getRole())) {
            // 查询回收员扩展信息
            Collector collector = collectorMapper.selectOne(
                    new LambdaQueryWrapper<Collector>().eq(Collector::getUserId, user.getId())
            );
            if (collector != null) {
                info.put("collectorStatus", collector.getStatus());
                info.put("idCardPhoto", collector.getIdCardPhoto());
            }
        } else if ("INSTITUTION".equals(user.getRole())) {
            // 查询机构扩展信息
            Institution institution = institutionMapper.selectOne(
                    new LambdaQueryWrapper<Institution>().eq(Institution::getUserId, user.getId())
            );
            if (institution != null) {
                info.put("address", institution.getAddress());
                info.put("contactPerson", institution.getContactPerson());
                info.put("institutionName", institution.getName());
            }
        }

        return info;
    }

    /**
     * 构建登录响应
     */
    private LoginResponse buildResponse(String token, String role, Map<String, Object> userInfo) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRole(role);
        response.setUserInfo(userInfo);
        return response;
    }

    /**
     * 构建基础用户信息（不返回密码）
     */
    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("name", user.getName());
        info.put("phone", user.getPhone());
        info.put("pointsBalance", user.getPointsBalance());
        return info;
    }
}
