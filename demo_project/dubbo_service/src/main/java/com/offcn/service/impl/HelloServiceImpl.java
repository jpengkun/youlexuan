package com.offcn.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.service.HelloService;
@Service
public class HelloServiceImpl implements HelloService {
    @Override
    public String getName() {
        return "hello, Dubbo";
    }
}
