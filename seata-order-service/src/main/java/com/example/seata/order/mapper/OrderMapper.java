package com.example.seata.order.mapper;

import com.example.seata.order.entity.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;

public interface OrderMapper {

    @Insert("INSERT INTO `order` (user_id, product_id, count, money, status) " +
            "VALUES (#{userId}, #{productId}, #{count}, #{money}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Update("UPDATE `order` SET status = #{status} WHERE id = #{id}")
    int updateStatus(Long id, Integer status);
}
