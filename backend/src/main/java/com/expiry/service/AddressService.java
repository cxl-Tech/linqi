package com.expiry.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.entity.Address;
import com.expiry.mapper.AddressMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService extends ServiceImpl<AddressMapper, Address> {

    public List<Address> listByUser(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId).orderByDesc(Address::getIsDefault).orderByDesc(Address::getCreateTime);
        return this.list(wrapper);
    }

    public void setDefault(Long userId, Long addressId) {
        LambdaUpdateWrapper<Address> resetWrapper = new LambdaUpdateWrapper<>();
        resetWrapper.eq(Address::getUserId, userId).set(Address::getIsDefault, 0);
        this.update(resetWrapper);

        Address address = this.getById(addressId);
        address.setIsDefault(1);
        this.updateById(address);
    }
}
