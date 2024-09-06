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
     * 文件存储 启用
     */
    boolean fileStoreEnable = true;
    /**
     * 文件存储目录
     */
    String fileStoreDir = "/ureport-files";


    /**
     * class 启用
     */
    boolean classpathStoreEnable = true;

    /**
     *
     */
    String classpathStoreDir = "ureport-files";



}
