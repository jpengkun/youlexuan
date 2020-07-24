package com.offcn.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.seckill.service.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * seckill_ordercontroller
 * @author senqi
 *
 */
@RestController
@RequestMapping("/seckillOrder")
public class SeckillOrderController {

	@Reference
	private SeckillOrderService seckillOrderService;

	/**
	 * 测试
	 * @param skId
	 * @param userId
	 */
	@RequestMapping("/submitOrderTest")
	public void submitOrder(Long skId,String userId){
		seckillOrderService.submitOrder(skId,userId);
	}


	/**
	 * 提交秒杀订单
	 * @return
	 */
	@RequestMapping("/submitOrder")
	public Result submitOrder(Long skId){
		try {

			String userId = SecurityContextHolder.getContext().getAuthentication().getName();

			seckillOrderService.submitOrder(skId,userId);
			return new Result(true,"抢购成功");
		}catch (Exception e){
			e.printStackTrace();
			return new Result(false,"抢购失败");
		}
	}
	

	
}
