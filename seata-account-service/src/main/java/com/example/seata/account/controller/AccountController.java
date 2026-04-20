package com.example.seata.account.controller;

import com.example.seata.account.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/account")
public class AccountController {

    @Resource
    private AccountService accountService;

    @PostMapping("/decrease")
    public String decrease(@RequestParam("userId") Long userId, @RequestParam("money") BigDecimal money) {
        try {
            accountService.decrease(userId, money);
            return "账户扣减成功";
        } catch (Exception e) {
            log.error("账户扣减失败", e);
            return "账户扣减失败: " + e.getMessage();
        }
    }
}
