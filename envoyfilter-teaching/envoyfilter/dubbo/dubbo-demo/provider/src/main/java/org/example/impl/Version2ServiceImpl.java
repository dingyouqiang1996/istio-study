package org.example.impl;

import org.apache.dubbo.config.annotation.DubboService;
import org.example.api.IGroupService;
import org.example.api.ITestService;
import org.example.api.IVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Component
@DubboService(interfaceClass = IVersionService.class,version = "2.0.0")
public class Version2ServiceImpl implements IVersionService {
    @Autowired
    private Environment env;

    @Override
    public String dubboCallProiderService(String params) {
        return  env.getProperty("version")+"hello version2:"+" , " + params;
    }
}
