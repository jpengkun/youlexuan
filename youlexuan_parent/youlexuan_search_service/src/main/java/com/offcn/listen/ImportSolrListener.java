package com.offcn.listen;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class ImportSolrListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage text = (TextMessage)message;
            String srt = text.getText();
            List<TbItem> tbItems = JSON.parseArray(srt, TbItem.class);
            itemSearchService.importList(tbItems);
            System.out.println("收到了import的solr消息");

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
