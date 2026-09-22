package com.expiry.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.expiry.entity.Announcement;
import com.expiry.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementService extends ServiceImpl<AnnouncementMapper, Announcement> {
}
