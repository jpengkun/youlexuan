package com.offcn.pay.controllerr;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AlipayService;
import com.offcn.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private AlipayService alipayService;

    @Autowired
    private RedisTemplate redisTemplate;


    @Reference
    private OrderService orderService;


    @RequestMapping("/createCode")
    public Map createCode (){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        TbPayLog payLog = (TbPayLog) redisTemplate.boundHashOps("payLogList").get(userId);
                                                                   //利用string的方法只保留小数点两位小数
        return alipayService.createNative(payLog.getOutTradeNo(),String.format("%.2f",payLog.getTotalFee()));
    }


    /**
     * 查询支付状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;
        int x = 0;
        while (true) {
            // 调用查询接口
            Map<String, String> map = null;
            try {
                map = alipayService.queryPayStatus(out_trade_no);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (map == null) {// 出错
                result = new Result(false, "查询支付状态异常");
                break;
            }
            // 如果成功
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_SUCCESS")) {

                //支付成功，跟新订单，支付状态改变
                orderService.updateTradeStatus(out_trade_no);

                result = new Result(true, "支付成功");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_CLOSED")) {
                result = new Result(false, "交易超时二维码过期");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_FINISHED")) {
                result = new Result(false, "交易结束");
                break;
            }
            try {
                Thread.sleep(3000);// 间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 如果变量超过设定值退出循环，超时为3分钟
            x++;
            if (x >= 20) {
                result = new Result(false, "交易超时二维码过期");
                break;
            }
        }

        return result;
    }


}
