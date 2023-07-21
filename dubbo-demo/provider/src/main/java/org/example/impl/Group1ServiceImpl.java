package org.example.impl;

import org.apache.dubbo.config.annotation.DubboService;
import org.example.api.IGroupService;
import org.example.api.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@DubboService(interfaceClass = IGroupService.class,group = "group1")
public class Group1ServiceImpl implements IGroupService {
    @Autowired
    private Environment env;

    @Override
    public String dubboCallProiderService(String params) {
        return  env.getProperty("version")+"hello group1:"+" , " + params;
    }
}
