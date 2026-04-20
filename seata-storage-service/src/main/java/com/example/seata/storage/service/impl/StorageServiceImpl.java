package com.example.seata.storage.service.impl;

import com.example.seata.storage.mapper.StorageMapper;
import com.example.seata.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {

    @Resource
    private StorageMapper storageMapper;

    @Override
    public void decrease(Long productId, Integer count) {
        log.info("开始扣减库存, productId: {}, count: {}", productId, count);

        int result = storageMapper.decrease(productId, count);
        if (result <= 0) {
            throw new RuntimeException("库存不足或扣减失败");
        }

        log.info("库存扣减成功");
    }
}
