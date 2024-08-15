package com.bstek.ureport.provider.report.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
public class SysReportDao {

    @Resource
    JdbcTemplate jdbcTemplate;


    @PostConstruct
    public void initDDL() {
        String sql = " CREATE TABLE IF NOT EXISTS sys_report (\n" +
                     "     id varchar(32) PRIMARY KEY,\n" +
                     "     update_time datetime(6) DEFAULT NULL,\n" +
                     "     content longtext NOT NULL,\n" +
                     "     file varchar(255) NOT NULL\n" +
                     ")";

        log.info("执行建表语句 {}", sql);
        jdbcTemplate.execute(sql);
    }


    public SysReport findByFile(String file) {
        String sql = "select * from sys_report where file=?";


        BeanPropertyRowMapper<SysReport> rowMapper = new BeanPropertyRowMapper<>(SysReport.class);
        List<SysReport> list = jdbcTemplate.query(sql, rowMapper, file);

        if (list != null && list.size() > 0) {
            return list.get(0);
        }

        return null;
    }

    public void deleteById(String id) {
        String sql = "DELETE FROM sys_report WHERE id = ?";
        jdbcTemplate.execute(sql);
    }

    public List<SysReport> findAll() {
        String sql = "select * from sys_report";


        BeanPropertyRowMapper<SysReport> rowMapper = new BeanPropertyRowMapper<>(SysReport.class);
        List<SysReport> list = jdbcTemplate.query(sql, rowMapper);


        return list;
    }

    public void save(SysReport r) {
        if (r.getId() == null) {
            String id = UUID.randomUUID().toString().replace("-", "");
            String sql = "INSERT INTO sys_report (id, file, content, update_time) VALUES (?, ?, ?,?)";
            jdbcTemplate.update(sql, id, r.getFile(), r.getContent(), new Date());
        } else {
            String sql = "UPDATE sys_report SET file = ?, content = ?, update_time=? WHERE id = ?";
            jdbcTemplate.update(sql, r.getFile(), r.getContent(), new Date(), r.getId());
        }

    }


}
