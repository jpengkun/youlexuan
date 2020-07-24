package com.offcn.order.service;

import com.offcn.pojo.TbOrder;
import com.offcn.pojo.TbPayLog;

public interface OrderService {

    public void  add(TbOrder order);


    void updateTradeStatus(String out_trade_no);

}
