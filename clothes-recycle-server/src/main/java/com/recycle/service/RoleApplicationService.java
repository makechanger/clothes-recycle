package com.recycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recycle.common.BusinessException;
import com.recycle.dto.RoleApplicationRequest;
import com.recycle.entity.Collector;
import com.recycle.entity.Institution;
import com.recycle.entity.RoleApplication;
import com.recycle.entity.User;
import com.recycle.mapper.CollectorMapper;
import com.recycle.mapper.InstitutionMapper;
import com.recycle.mapper.RoleApplicationMapper;
import com.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色申请服务
 * 处理用户提交资质申请、管理员审批通过/拒绝
 *
 * 重构后的审批逻辑：
 * - 审批通过时：UPDATE user.role 为对应角色 + INSERT 扩展信息到 collector/institution 表
 * - 不再删除 user 表记录，保证外键引用（recycle_order.user_id 等）不断裂
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleApplicationService {

    private final RoleApplicationMapper roleApplicationMapper;
    private final UserMapper userMapper;
    private final CollectorMapper collectorMapper;
    private final InstitutionMapper institutionMapper;

    /**
     * 提交资质申请
     * 校验：不能重复提交待审核的申请
     *
     * @param userId  当前登录用户 ID
     * @param request 申请信息
     */
    public void apply(Long userId, RoleApplicationRequest request) {
        String applyRole = request.getApplyRole();

        // 1. 校验申请角色合法性
        if (!"COLLECTOR".equals(applyRole) && !"INSTITUTION".equals(applyRole)) {
            throw new BusinessException(400, "申请角色不合法，只能申请回收员或机构");
        }

        // 2. 校验必填字段（根据申请角色不同）
        if ("COLLECTOR".equals(applyRole)) {
            if (request.getName() == null || request.getName().isEmpty()) {
                throw new BusinessException(400, "请填写真实姓名");
            }
            if (request.getIdCardPhoto() == null || request.getIdCardPhoto().isEmpty()) {
                throw new BusinessException(400, "请上传身份证照片");
            }
        } else {
            // 机构申请
            if (request.getName() == null || request.getName().isEmpty()) {
                throw new BusinessException(400, "请填写机构名称");
            }
            if (request.getAddress() == null || request.getAddress().isEmpty()) {
                throw new BusinessException(400, "请填写机构地址");
            }
            if (request.getContactPerson() == null || request.getContactPerson().isEmpty()) {
                throw new BusinessException(400, "请填写联系人");
            }
        }

        // 3. 检查是否已有待审核的申请（不区分角色，有任意待审核申请就不能再提交）
        Long pendingCount = roleApplicationMapper.selectCount(
                new LambdaQueryWrapper<RoleApplication>()
                        .eq(RoleApplication::getUserId, userId)
                        .eq(RoleApplication::getStatus, 0)
        );
        if (pendingCount > 0) {
            throw new BusinessException(400, "您已有一条待审核的申请，请等待审核结果后再提交");
        }

        // 4. 插入申请记录
        RoleApplication application = new RoleApplication();
        application.setUserId(userId);
        application.setApplyRole(applyRole);
        application.setName(request.getName());
        application.setIdCardPhoto(request.getIdCardPhoto());
        application.setAddress(request.getAddress());
        application.setContactPerson(request.getContactPerson());
        application.setStatus(0);  // 待审核
        roleApplicationMapper.insert(application);

        log.info("用户 {} 提交了 {} 角色申请", userId, applyRole);
    }

    /**
     * 查询用户的申请记录列表
     *
     * @param userId 用户 ID
     * @return 申请记录列表
     */
    public List<RoleApplication> getMyApplications(Long userId) {
        return roleApplicationMapper.selectList(
                new LambdaQueryWrapper<RoleApplication>()
                        .eq(RoleApplication::getUserId, userId)
                        .orderByDesc(RoleApplication::getCreatedAt)
        );
    }

    /**
     * 查询所有待审核的申请（管理员用）
     * 支持按申请角色过滤
     *
     * @param applyRole 申请角色（COLLECTOR/INSTITUTION），为 null 时返回全部
     * @return 待审核申请列表
     */
    public List<RoleApplication> getPendingApplications(String applyRole) {
        LambdaQueryWrapper<RoleApplication> wrapper = new LambdaQueryWrapper<RoleApplication>()
                .eq(RoleApplication::getStatus, 0)
                .orderByAsc(RoleApplication::getCreatedAt);
        if (applyRole != null && !applyRole.isEmpty()) {
            wrapper.eq(RoleApplication::getApplyRole, applyRole);
        }
        return roleApplicationMapper.selectList(wrapper);
    }

    /**
     * 查询所有已处理的申请记录（管理员审批历史）
     * 包含已通过（status=1）和已拒绝（status=2）的记录
     * 支持按申请角色过滤
     *
     * @param applyRole 申请角色（COLLECTOR/INSTITUTION），为 null 时返回全部
     * @return 已处理申请列表，按处理时间倒序
     */
    public List<RoleApplication> getProcessedApplications(String applyRole) {
        LambdaQueryWrapper<RoleApplication> wrapper = new LambdaQueryWrapper<RoleApplication>()
                .in(RoleApplication::getStatus, 1, 2)
                .orderByDesc(RoleApplication::getUpdatedAt);
        if (applyRole != null && !applyRole.isEmpty()) {
            wrapper.eq(RoleApplication::getApplyRole, applyRole);
        }
        return roleApplicationMapper.selectList(wrapper);
    }

    /**
     * 管理员审批通过
     * 核心逻辑（重构后）：
     * 1. 更新 user 表的 role 字段为对应角色
     * 2. 在 collector/institution 扩展表中插入记录（通过 user_id 关联）
     * 3. 不删除 user 表记录，保证外键引用完整
     *
     * @param applicationId 申请记录 ID
     */
    @Transactional
    public void approve(Long applicationId) {
        // 1. 查询申请记录
        RoleApplication application = roleApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(400, "申请记录不存在");
        }
        if (application.getStatus() != 0) {
            throw new BusinessException(400, "该申请已处理");
        }

        // 2. 查询用户信息
        User user = userMapper.selectById(application.getUserId());
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        // 3. 根据申请角色，更新 user.role 并插入扩展信息
        if ("COLLECTOR".equals(application.getApplyRole())) {
            // 检查是否已有回收员扩展记录（防止重复）
            Collector existing = collectorMapper.selectOne(
                    new LambdaQueryWrapper<Collector>().eq(Collector::getUserId, user.getId())
            );
            if (existing != null) {
                throw new BusinessException(400, "该用户已是回收员，无法重复升级");
            }

            // 更新用户角色为回收员
            user.setRole("COLLECTOR");
            userMapper.updateById(user);

            // 插入回收员扩展信息（通过 user_id 关联）
            Collector collector = new Collector();
            collector.setUserId(user.getId());
            collector.setName(application.getName());
            collector.setIdCardPhoto(application.getIdCardPhoto());
            collector.setStatus(2);  // 直接设为已认证（审批通过即认证）
            collectorMapper.insert(collector);

        } else if ("INSTITUTION".equals(application.getApplyRole())) {
            // 检查是否已有机构扩展记录
            Institution existing = institutionMapper.selectOne(
                    new LambdaQueryWrapper<Institution>().eq(Institution::getUserId, user.getId())
            );
            if (existing != null) {
                throw new BusinessException(400, "该用户已是机构，无法重复升级");
            }

            // 更新用户角色为机构
            user.setRole("INSTITUTION");
            userMapper.updateById(user);

            // 插入机构扩展信息（通过 user_id 关联）
            Institution institution = new Institution();
            institution.setUserId(user.getId());
            institution.setName(application.getName());
            institution.setAddress(application.getAddress());
            institution.setContactPerson(application.getContactPerson());
            institution.setStatus(1);  // 正常状态
            institutionMapper.insert(institution);
        }

        // 4. 更新申请状态为已通过（不再删除 user 表记录）
        application.setStatus(1);
        roleApplicationMapper.updateById(application);

        log.info("管理员审批通过：用户 {} 升级为 {}", user.getId(), application.getApplyRole());
    }

    /**
     * 管理员审批拒绝
     *
     * @param applicationId 申请记录 ID
     * @param rejectReason  拒绝原因
     */
    public void reject(Long applicationId, String rejectReason) {
        RoleApplication application = roleApplicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(400, "申请记录不存在");
        }
        if (application.getStatus() != 0) {
            throw new BusinessException(400, "该申请已处理");
        }

        // 更新状态为已拒绝
        application.setStatus(2);
        application.setRejectReason(rejectReason);
        roleApplicationMapper.updateById(application);

        log.info("管理员拒绝了用户 {} 的 {} 角色申请，原因：{}", application.getUserId(), application.getApplyRole(), rejectReason);
    }
}
