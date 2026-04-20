package com.example.seata.order.service.impl;

import com.example.seata.order.entity.Order;
import com.example.seata.order.feign.AccountFeignClient;
import com.example.seata.order.feign.StorageFeignClient;
import com.example.seata.order.mapper.OrderMapper;
import com.example.seata.order.service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private StorageFeignClient storageFeignClient;

    @Resource
    private AccountFeignClient accountFeignClient;

    /**
     * 创建订单 - 全局事务入口
     * 流程: 创建订单 -> 扣减库存 -> 扣减账户余额
     */
    @Override
    @GlobalTransactional(name = "order-create-tx", rollbackFor = Exception.class)
    public void create(Order order) {
        log.info("开始创建订单, userId: {}, productId: {}, count: {}", order.getUserId(), order.getProductId(), order.getCount());

        // 1. 创建订单
        order.setStatus(0);
        orderMapper.insert(order);
        log.info("订单创建成功, orderId: {}", order.getId());

        // 2. 扣减库存
        storageFeignClient.decrease(order.getProductId(), order.getCount());
        log.info("库存扣减成功, productId: {}, count: {}", order.getProductId(), order.getCount());

        // 3. 扣减账户余额
        accountFeignClient.decrease(order.getUserId(), order.getMoney());
        log.info("账户扣减成功, userId: {}, money: {}", order.getUserId(), order.getMoney());

        // 4. 更新订单状态
        orderMapper.updateStatus(order.getId(), 1);
        log.info("订单状态更新为已完成, orderId: {}", order.getId());

        log.info("全局事务执行完成");
    }
}
