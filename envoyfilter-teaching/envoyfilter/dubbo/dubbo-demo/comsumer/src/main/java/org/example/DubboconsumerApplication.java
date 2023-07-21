package org.example;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.example.api.IGroupService;
import org.example.api.ITestService;
import org.example.api.IVersionService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@EnableDubboConfig
@Controller
public class DubboconsumerApplication {
    @DubboReference(url="dubbo://dubbo-hello-provider:20880",check = false)
    private ITestService testService;
    @DubboReference(url="dubbo://dubbo-hello-provider:20880",check = false,group = "group1")
    private IGroupService iGroupService;
    @DubboReference(url="dubbo://dubbo-hello-provider:20880",check = false,group = "group2")
    private IGroupService iGroupService2;
    @DubboReference(url="dubbo://dubbo-hello-provider:20880",check = false,version = "1.0.0")
    private IVersionService iVersionService;
    @DubboReference(url="dubbo://dubbo-hello-provider:20880",check = false,version = "2.0.0")
    private IVersionService iVersionService2;

    public static void main(String[] args) {
        SpringApplication.run(DubboconsumerApplication.class, args);
    }

    /**
     * 测试
     * @param id 消息
     * @return 结果
     */
    @RequestMapping("/test")
    @ResponseBody
    public String test(@RequestParam("id") int id) {
        String result = testService.dubboCallProiderService(String.valueOf(id));
        return result;
    }

    @RequestMapping("/test/group1")
    @ResponseBody
    public String testgroup(@RequestParam("id") int id) {
        String result = iGroupService.dubboCallProiderService(String.valueOf(id));
        return result;
    }

    @RequestMapping("/test/group2")
    @ResponseBody
    public String testgroup2(@RequestParam("id") int id) {
        String result = iGroupService2.dubboCallProiderService(String.valueOf(id));
        return result;
    }

    @RequestMapping("/test/version1")
    @ResponseBody
    public String testversion(@RequestParam("id") int id) {
        String result = iVersionService.dubboCallProiderService(String.valueOf(id));
        return result;
    }

    @RequestMapping("/test/version2")
    @ResponseBody
    public String testversion2(@RequestParam("id") int id) {
        String result = iVersionService2.dubboCallProiderService(String.valueOf(id));
        return result;
    }
}