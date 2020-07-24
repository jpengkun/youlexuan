package com.offcn.seckill.service;

import com.offcn.entity.PageResult;
import com.offcn.pojo.TbSeckillOrder;

import java.util.List;

/**
 * seckill_order服务层接口
 * @author jinpengkun
 *
 */
public interface SeckillOrderService {


	void submitOrder(Long skId, String userId);


	/**
	 * 支付成功保存订单
	 * @param userId
	 * @param orderId
	 */
	public void saveOrderFromRedisToDb(String userId);


	/**
	 * 从缓存中删除订单
	 * @param userId
	 * @param orderId
	 */
	public void deleteOrderFromRedis(String userId);


}
