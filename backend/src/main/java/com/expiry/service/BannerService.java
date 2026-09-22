package com.expiry.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.entity.Banner;
import com.expiry.mapper.BannerMapper;
import org.springframework.stereotype.Service;

@Service
public class BannerService extends ServiceImpl<BannerMapper, Banner> {
}
