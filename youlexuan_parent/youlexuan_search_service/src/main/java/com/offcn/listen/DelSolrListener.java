package com.offcn.listen;

import com.alibaba.fastjson.JSON;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.util.List;

@Component
public class DelSolrListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage obj = (ObjectMessage)message;
            Long[] ids = (Long[]) obj.getObject();
            itemSearchService.deleteList(ids);
            System.out.println("收到了del的solr消息");

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
