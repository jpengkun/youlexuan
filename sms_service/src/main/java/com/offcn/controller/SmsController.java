package com.offcn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SmsController {

    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    @RequestMapping("/sendsms")
    public void sendSms() {
        Map<String, String> map = new HashMap<>();
        map.put("mobile", "15638229363");
        map.put("sign_name", "优乐选");
        map.put("template_code", "SMS_162522110");
        map.put("param", "{\"code\":\"666888\"}");

        jmsMessagingTemplate.convertAndSend("sms", map);
    }

}
