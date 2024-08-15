package com.bstek.ureport.console.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ureport.console")
@ConfigurationPropertiesBinding
@Data
public class UReportConsoleProperties {

    boolean disableHttpSessionReportCache = false;

}
