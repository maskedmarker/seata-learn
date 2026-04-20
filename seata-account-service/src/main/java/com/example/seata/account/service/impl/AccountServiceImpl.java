package com.example.seata.account.service.impl;

import com.example.seata.account.mapper.AccountMapper;
import com.example.seata.account.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    @Resource
    private AccountMapper accountMapper;

    @Override
    public void decrease(Long userId, BigDecimal money) {
        log.info("开始扣减账户余额, userId: {}, money: {}", userId, money);

        int result = accountMapper.decrease(userId, money);
        if (result <= 0) {
            throw new RuntimeException("账户余额不足或扣减失败");
        }

        log.info("账户扣减成功");
    }
}
