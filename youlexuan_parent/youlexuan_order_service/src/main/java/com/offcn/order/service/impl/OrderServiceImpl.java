package com.offcn.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.group.Cart;
import com.offcn.mapper.TbOrderItemMapper;
import com.offcn.mapper.TbOrderMapper;
import com.offcn.mapper.TbPayLogMapper;
import com.offcn.order.service.OrderService;
import com.offcn.pojo.TbOrder;
import com.offcn.pojo.TbOrderItem;
import com.offcn.pojo.TbPayLog;
import com.offcn.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbOrderMapper tbOrderMapper;

    @Autowired
    private TbOrderItemMapper tbOrderItemMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private TbPayLogMapper payLogMapper;


    @Override
    public void add(TbOrder order) {

        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

        //在订单生成的时候，同时产生一个支付日志对象
        //并且将该 只支付日志 对象 存储到数据库和redis中
        //方便支付的时候快速的从redis中取出
        TbPayLog payLog = new TbPayLog();

        //总金额 （元）
        double total_money=0;

        StringBuilder sb = new StringBuilder("");

        for (Cart cart : cartList) {
            long orderId = idWorker.nextId();
            sb.append(orderId+",");
            order.setOrderId(orderId);
            Double sum = 0.0;
            for (TbOrderItem tbOrderItem : cart.getOrderItemList()) {
                sum += tbOrderItem.getTotalFee().doubleValue();
                tbOrderItem.setId(idWorker.nextId());
                tbOrderItem.setOrderId(orderId);
                tbOrderItemMapper.insert(tbOrderItem);
            }
            total_money+=sum;
            order.setPayment(new BigDecimal(sum));
            order.setSellerId(cart.getSellerId());
            //一个商家对应一个订单
            tbOrderMapper.insert(order);
        }

        //支付订单号
        payLog.setOutTradeNo(idWorker.nextId()+"");
        //创建时间
        payLog.setCreateTime(new Date());
        //用户ID
        payLog.setUserId(order.getUserId());
        //支付状态
        payLog.setPayType(order.getPaymentType());
        payLog.setTradeState("0");

        payLog.setTotalFee(new BigDecimal(total_money));

        //1233,12313,123,
        //将最后一个逗号删除
        sb.deleteCharAt(sb.length()-1);
        payLog.setOrderList(sb.toString());//订单号列表，逗号分隔
        //将 支付日志 存到数据库中
        payLogMapper.insert(payLog);
        //将 支付日志 存到redis
        redisTemplate.boundHashOps("payLogList").put(order.getUserId(),payLog);

        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
    }

    @Override
    public void updateTradeStatus(String out_trade_no) {
        //支付日志 付款时间，支付状太
        TbPayLog tbPayLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        tbPayLog.setPayTime(new Date());
        tbPayLog.setTradeState("1");
        payLogMapper.updateByPrimaryKey(tbPayLog);

        //订单：付款时间，支付状态
        String orderList = tbPayLog.getOrderList();
        String[] split = orderList.split(",");

        for (String s : split) {
            TbOrder tbOrder = tbOrderMapper.selectByPrimaryKey(Long.parseLong(s));
            tbOrder.setPaymentTime(new Date());
            //表示已付款
            tbOrder.setStatus("1");
            tbOrderMapper.updateByPrimaryKey(tbOrder);
        }
    }


}
