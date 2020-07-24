package com.offcn.listen;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.offcn.util.SmsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SmsListen {

    @Autowired
    private SmsUtil smsUtil;

    @JmsListener(destination = "offcn_sms")
    public void sendSms(Map<String, String> map) {
        try {
            String mobile = map.get("mobile");
            String sign_name = map.get("sign_name");
            String template_code = map.get("template_code");
            String param = map.get("param");

            System.out.println("mobile: " + mobile + ", sign_name: " + sign_name + ", template_code: " + template_code + ", param: " + param);

            SendSmsResponse response = smsUtil.sendSms(mobile, sign_name, template_code, param);
            System.out.println("Code=" + response.getCode());
            System.out.println("Message=" + response.getMessage());

        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

}
