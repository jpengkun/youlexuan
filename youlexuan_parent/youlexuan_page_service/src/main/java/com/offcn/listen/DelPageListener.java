package com.offcn.listen;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
@Component
public class DelPageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage obj = (ObjectMessage)message;
            Long[] ids = (Long[]) obj.getObject();
            itemPageService.deleteHtml(ids);
            System.out.println("receive del page msg");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}