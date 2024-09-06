package com.bstek.ureport.console.config;

import com.bstek.ureport.definition.datasource.BuildinDatasource;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 内置数据源
 */

public class BuildinDatasourceImpl implements BuildinDatasource {

    @Resource
    private DataSource dataSource;


    @Override
    public String name() {
        return "内置数据源";
    }


    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
