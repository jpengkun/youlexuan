package com.offcn.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pojo.TbOrder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference
    private OrderService orderService;

    @RequestMapping("/add")
    public Result add(@RequestBody TbOrder order){
        try {
            //刚下单 未支付
            order.setStatus("0");
            order.setCreateTime(new Date());
            order.setUpdateTime(new Date());

            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            order.setUserId(userId);

            orderService.add(order);
            return new Result(true,"保存订单成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"保存订单失败");
        }
    }

}
