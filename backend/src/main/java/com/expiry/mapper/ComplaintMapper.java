package com.expiry.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.expiry.entity.Complaint;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ComplaintMapper extends BaseMapper<Complaint> {
}
