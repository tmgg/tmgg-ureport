package com.bstek.ureport;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ureport")
@Data
public class UReportProperties {

    /**
     * 报表存储路径
     */
    String storePath = "src/main/resources/report";





}
