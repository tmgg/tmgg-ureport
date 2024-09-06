package com.bstek.ureport.console.config;

import com.bstek.ureport.UConst;
import com.bstek.ureport.parser.ReportParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.sql.DataSource;

@Configuration
@PropertySource("classpath:ureport.properties")
@Slf4j
public class UReportConfig  {


    @Bean
    public VelocityEngine velocityEngine() {
        VelocityEngine ve = new VelocityEngine();

        ve.setProperty(Velocity.RESOURCE_LOADER, "class");
        ve.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
        ve.init();
        return ve;
    }


//    /**
//     * 允许前端开发时的前端跨域请求
//     * @return
//     */
//    @Bean
//    @ConditionalOnMissingBean(CorsFilter.class)
//    public CorsFilter corsFilter() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.addAllowedOrigin("http://localhost:3000");
//        config.setAllowCredentials(true);
//        config.addAllowedMethod("*");
//        config.addAllowedHeader("*");
//
//        config.addExposedHeader("*");
//
//        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
//        configSource.registerCorsConfiguration("/**", config);
//
//        return new CorsFilter(configSource);
//    }

    @Bean
    @ConditionalOnProperty(name = UConst.DATASOURCE_EXIST_KEY)
    public BuildinDatasourceImpl buildinDatasource() {
        return new BuildinDatasourceImpl();
    }
}
