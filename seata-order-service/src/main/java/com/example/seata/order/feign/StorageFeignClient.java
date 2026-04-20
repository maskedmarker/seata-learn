package com.example.seata.order.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "storage-service", url = "${feign.storage.url}")
public interface StorageFeignClient {

    @PostMapping("/storage/decrease")
    void decrease(@RequestParam("productId") Long productId, @RequestParam("count") Integer count);
}
