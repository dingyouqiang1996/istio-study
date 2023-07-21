package org.example.impl;

import org.apache.dubbo.config.annotation.DubboService;
import org.example.api.IGroupService;
import org.example.api.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Component
@DubboService(interfaceClass = IGroupService.class,group = "group2")
public class Group2ServiceImpl implements IGroupService {
    @Autowired
    private Environment env;

    @Override
    public String dubboCallProiderService(String params) {
        return  env.getProperty("version")+"hello group2:"+" , " + params;
    }
}