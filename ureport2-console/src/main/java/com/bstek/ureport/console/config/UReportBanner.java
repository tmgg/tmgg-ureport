package com.bstek.ureport.console.config;

import com.bstek.ureport.UReportCoreConfig;
import com.bstek.ureport.UReportProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UReportBanner implements ApplicationRunner {

    @Resource
    Environment env;

    @Resource
    UReportProperties reportProperties;

    @Resource
    UReportConsoleProperties consoleProperties;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.err.println("################# ureport2 模块 ###################");
        String port = env.getProperty("server.port", "8080");
        System.err.println("请求地址为：http://localhost:" + port + "/ureport/designer");

        System.err.println(reportProperties);
        System.err.println(consoleProperties);

        String key = "spring.jackson.date-format";
        System.err.println(key + ":" + env.getProperty(key));


        System.err.println("修改时间" + UReportCoreConfig.UPDATE_TIME);
    }
}
