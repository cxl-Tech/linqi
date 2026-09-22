package com.expiry.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.expiry.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper extends BaseMapper<Product> {

    @Update("UPDATE product SET stock = stock - #{quantity}, sales = sales + #{quantity} WHERE id = #{id} AND stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") int quantity);
}
