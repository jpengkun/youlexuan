package com.offcn.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbSeckillGoodsMapper;
import com.offcn.mapper.TbSeckillOrderMapper;
import com.offcn.pojo.TbSeckillGoods;
import com.offcn.pojo.TbSeckillOrder;
import com.offcn.pojo.TbSeckillOrderExample;
import com.offcn.pojo.TbSeckillOrderExample.Criteria;
import com.offcn.seckill.service.SeckillOrderService;
import com.offcn.util.IdWorker;
import com.offcn.util.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.List;

/**
 * seckill_order服务实现层
 * @author senqi
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private RedisLock redisLock;


	@Override
	public void submitOrder(Long skId, String userId) {

		String lockKey = "createSecKillOrder";
		// 过期时间：1秒
		long ex = 1000;
		String lockVal = String.valueOf(System.currentTimeMillis() + ex);
		boolean lock = redisLock.lock(lockKey, lockVal);

		if (lock) {
			//从缓存中查询秒杀商品
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(skId);
			if (seckillGoods == null) {
				throw new RuntimeException("商品不存在");
			}
			if (seckillGoods.getStockCount() == 0) {
				throw new RuntimeException("商品已抢完");
			}
			//扣减（redis）库存
			seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
			//放回缓存 跟新redis
			redisTemplate.boundHashOps("seckillGoods").put(skId, seckillGoods);

			//如果已经被秒光
			if (seckillGoods.getStockCount() == 0) {
				seckillGoodsMapper.updateByPrimaryKey(seckillGoods);//同步到数据库
				redisTemplate.boundHashOps("seckillGoods").delete(skId);
			}

			//保存（redis）订单
			long orderId = idWorker.nextId();
			TbSeckillOrder seckillOrder = new TbSeckillOrder();
			seckillOrder.setId(orderId);
			seckillOrder.setCreateTime(new Date());
			seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
			seckillOrder.setSeckillId(skId);
			seckillOrder.setSellerId(seckillGoods.getSellerId());
			seckillOrder.setUserId(userId);//设置用户ID
			seckillOrder.setStatus("0");//状态
			redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);

			// 释放锁
			redisLock.unlock(lockKey, lockVal);
		}
	}

	@Override
	public void saveOrderFromRedisToDb(String userId) {

		TbSeckillOrder order = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);

		order.setPayTime(new Date());

		//已支付
		order.setStatus("1");
		seckillOrderMapper.insert(order);

		//保存订单成功后，从redis中移除该订单
		redisTemplate.boundHashOps("seckillOrder").delete(userId);
	}

	@Override
	public void deleteOrderFromRedis(String userId) {

		//还原库存
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);

		TbSeckillGoods goods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
		if (goods == null) {
			goods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
		}
		goods.setStockCount(goods.getStockCount() + 1);
		redisTemplate.boundHashOps("seckillGoods").put(goods.getId(),goods);

		//从redis中该订单移除
		redisTemplate.boundHashOps("seckillOrder").delete(userId);

	}


}
