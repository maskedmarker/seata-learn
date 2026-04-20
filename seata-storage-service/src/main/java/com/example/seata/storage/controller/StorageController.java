package com.example.seata.storage.controller;

import com.example.seata.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/storage")
public class StorageController {

    @Resource
    private StorageService storageService;

    @PostMapping("/decrease")
    public String decrease(@RequestParam("productId") Long productId, @RequestParam("count") Integer count) {
        try {
            storageService.decrease(productId, count);
            return "库存扣减成功";
        } catch (Exception e) {
            log.error("库存扣减失败", e);
            return "库存扣减失败: " + e.getMessage();
        }
    }
}
