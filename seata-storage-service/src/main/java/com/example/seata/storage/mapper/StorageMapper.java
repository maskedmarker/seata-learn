package com.example.seata.storage.mapper;

import com.example.seata.storage.entity.Storage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface StorageMapper {

    @Select("SELECT * FROM storage WHERE product_id = #{productId}")
    Storage findByProductId(@Param("productId") Long productId);

    @Update("UPDATE storage SET used = used + #{count}, residue = residue - #{count} " +
            "WHERE product_id = #{productId} AND residue >= #{count}")
    int decrease(@Param("productId") Long productId, @Param("count") Integer count);
}
