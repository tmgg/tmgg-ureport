package com.bstek.ureport;

import com.bstek.ureport.provider.report.classpath.ClasspathReportProvider;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Configuration
@ComponentScan(basePackageClasses = UReportCoreConfig.class)
@Getter
@Setter
public class UReportCoreConfig {

    public static final String UPDATE_TIME = "2023-12-18 11:32";

    public static void main(String[] args) {
        System.out.println(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm"));
    }

}
