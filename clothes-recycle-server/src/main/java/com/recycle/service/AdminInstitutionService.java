package com.recycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recycle.dto.InstitutionVO;
import com.recycle.entity.Institution;
import com.recycle.entity.User;
import com.recycle.mapper.InstitutionMapper;
import com.recycle.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员机构管理服务
 * 提供查询机构列表等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminInstitutionService {

    private final UserMapper userMapper;
    private final InstitutionMapper institutionMapper;

    /**
     * 查询所有机构列表
     * 关联 user 表获取手机号，合并为 InstitutionVO 返回
     */
    public List<InstitutionVO> listInstitutions() {
        List<Institution> institutions = institutionMapper.selectList(
                new LambdaQueryWrapper<Institution>().orderByDesc(Institution::getCreatedAt)
        );
        if (institutions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> userIds = institutions.stream()
                .map(Institution::getUserId)
                .collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<InstitutionVO> voList = new ArrayList<>();
        for (Institution inst : institutions) {
            InstitutionVO vo = new InstitutionVO();
            vo.setId(inst.getId());
            vo.setUserId(inst.getUserId());
            vo.setName(inst.getName());
            vo.setAddress(inst.getAddress());
            vo.setContactPerson(inst.getContactPerson());
            vo.setType(inst.getType());
            vo.setStatus(inst.getStatus());
            vo.setCreatedAt(inst.getCreatedAt());

            User user = userMap.get(inst.getUserId());
            if (user != null) {
                vo.setPhone(user.getPhone());
            }
            voList.add(vo);
        }

        return voList;
    }
}
