package com.example.seata.order.controller;

import com.example.seata.order.entity.Order;
import com.example.seata.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @PostMapping("/create")
    public String create(Order order) {
        try {
            orderService.create(order);
            return "订单创建成功";
        } catch (Exception e) {
            log.error("订单创建失败", e);
            return "订单创建失败: " + e.getMessage();
        }
    }
}
