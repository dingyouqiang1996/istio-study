package org.example.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.dubbo.config.annotation.DubboService;
import org.example.api.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@DubboService(interfaceClass = ITestService.class)
public class TestServiceImpl  implements ITestService {
    @Autowired
    private Environment env;

    @Override
    public String dubboCallProiderService(String params) {
        System.out.println("TestService dubboCallProiderService : " + params);
        return "hello"+env.getProperty("version")+" ： " + params;
    }
}
