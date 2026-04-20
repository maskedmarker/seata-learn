package com.example.seata.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "account-service", url = "${feign.account.url}")
public interface AccountFeignClient {

    @PostMapping("/account/decrease")
    void decrease(@RequestParam("userId") Long userId, @RequestParam("money") java.math.BigDecimal money);
}
