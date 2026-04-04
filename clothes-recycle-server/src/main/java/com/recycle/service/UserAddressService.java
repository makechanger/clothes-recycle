package com.recycle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.recycle.common.BusinessException;
import com.recycle.dto.UserAddressRequest;
import com.recycle.entity.UserAddress;
import com.recycle.mapper.UserAddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户地址服务
 * 提供地址的增删改查和设置默认地址功能
 * 所有操作都会校验地址归属，防止越权访问
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserAddressMapper userAddressMapper;

    /**
     * 查询当前用户的所有地址
     * 默认地址排在最前面，其余按创建时间倒序
     *
     * @param userId 当前登录用户ID
     * @return 地址列表
     */
    public List<UserAddress> listByUserId(Long userId) {
        return userAddressMapper.selectList(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .orderByDesc(UserAddress::getIsDefault)
                        .orderByDesc(UserAddress::getCreatedAt)
        );
    }

    /**
     * 根据ID查询地址详情（含归属校验）
     *
     * @param addressId 地址ID
     * @param userId    当前登录用户ID
     * @return 地址详情
     */
    public UserAddress getById(Long addressId, Long userId) {
        UserAddress address = userAddressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(404, "地址不存在");
        }
        return address;
    }

    /**
     * 新增地址
     * 如果设为默认地址，先取消该用户其他默认地址
     *
     * @param userId  当前登录用户ID
     * @param request 地址信息
     */
    @Transactional
    public void create(Long userId, UserAddressRequest request) {
        // 如果设为默认地址，先清除该用户的其他默认地址
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefault(userId);
        }

        UserAddress address = new UserAddress();
        address.setUserId(userId);
        copyProperties(request, address);
        userAddressMapper.insert(address);
        log.info("用户 {} 新增地址成功，地址ID：{}", userId, address.getId());
    }

    /**
     * 编辑地址（含归属校验）
     * 如果设为默认地址，先取消该用户其他默认地址
     *
     * @param addressId 地址ID
     * @param userId    当前登录用户ID
     * @param request   地址信息
     */
    @Transactional
    public void update(Long addressId, Long userId, UserAddressRequest request) {
        // 校验地址归属
        UserAddress address = getById(addressId, userId);

        // 如果设为默认地址，先清除该用户的其他默认地址
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefault(userId);
        }

        copyProperties(request, address);
        userAddressMapper.updateById(address);
        log.info("用户 {} 编辑地址 {} 成功", userId, addressId);
    }

    /**
     * 删除地址（含归属校验）
     *
     * @param addressId 地址ID
     * @param userId    当前登录用户ID
     */
    public void delete(Long addressId, Long userId) {
        // 校验地址归属
        getById(addressId, userId);
        userAddressMapper.deleteById(addressId);
        log.info("用户 {} 删除地址 {} 成功", userId, addressId);
    }

    /**
     * 设置默认地址
     * 先取消该用户所有默认地址，再将指定地址设为默认
     *
     * @param addressId 地址ID
     * @param userId    当前登录用户ID
     */
    @Transactional
    public void setDefault(Long addressId, Long userId) {
        // 校验地址归属
        getById(addressId, userId);

        // 清除该用户所有默认地址
        clearDefault(userId);

        // 设置指定地址为默认
        UserAddress address = new UserAddress();
        address.setId(addressId);
        address.setIsDefault(1);
        userAddressMapper.updateById(address);
        log.info("用户 {} 设置地址 {} 为默认地址", userId, addressId);
    }

    /**
     * 清除该用户所有默认地址标记
     */
    private void clearDefault(Long userId) {
        userAddressMapper.update(null,
                new LambdaUpdateWrapper<UserAddress>()
                        .eq(UserAddress::getUserId, userId)
                        .eq(UserAddress::getIsDefault, 1)
                        .set(UserAddress::getIsDefault, 0)
        );
    }

    /**
     * 将请求 DTO 的属性复制到实体对象
     */
    private void copyProperties(UserAddressRequest request, UserAddress address) {
        address.setName(request.getName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }
    }
}
