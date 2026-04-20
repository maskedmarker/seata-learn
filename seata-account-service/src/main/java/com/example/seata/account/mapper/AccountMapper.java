package com.example.seata.account.mapper;

import com.example.seata.account.entity.Account;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

public interface AccountMapper {

    @Select("SELECT * FROM account WHERE user_id = #{userId}")
    Account findByUserId(@Param("userId") Long userId);

    @Update("UPDATE account SET used = used + #{money}, residue = residue - #{money} WHERE user_id = #{userId} AND residue >= #{money}")
    int decrease(@Param("userId") Long userId, @Param("money") BigDecimal money);
}
