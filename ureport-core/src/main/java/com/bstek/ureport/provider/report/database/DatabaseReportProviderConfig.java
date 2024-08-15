package com.bstek.ureport.provider.report.database;

import com.bstek.ureport.UConst;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = UConst.DATASOURCE_EXIST_KEY)
public class DatabaseReportProviderConfig {



    @Bean
    @ConditionalOnMissingBean(JdbcTemplate.class)
    public JdbcTemplate jdbcTemplate(DataSource dataSource){
        return  new JdbcTemplate(dataSource);
    }

    @Bean
    public SysReportDao sysReportDao(){
        return  new SysReportDao();
    }

    @Bean
    public DatabaseReportProvider databaseReportProvider(){
        return  new DatabaseReportProvider();
    }
}
