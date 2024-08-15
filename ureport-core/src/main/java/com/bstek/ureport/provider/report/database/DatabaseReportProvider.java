package com.bstek.ureport.provider.report.database;

import cn.hutool.core.util.StrUtil;
import com.bstek.ureport.provider.report.ReportFile;
import com.bstek.ureport.provider.report.ReportProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DatabaseReportProvider implements ReportProvider {


    public static final String PREFIX = "db:";

    @Resource
    SysReportDao dao;


    @Override
    public String getReport(String file) {
        SysReport report = dao.findByFile(file);
        if (report != null) {
            return report.getContent();
        }
        return null;
    }

    @Override
    public void deleteReport(String file) {
        SysReport report = dao.findByFile(file);
        if(report == null){
            log.error("删除报表失败,报表不存在 {}", file);
        }else {
            dao.deleteById(report.getId());
        }

    }

    @Override
    public List<ReportFile> getReportFiles() {
        List<SysReport> list = dao.findAll();

        return list.stream().map(r -> {
            String file = r.getFile();

            String pureFile = StrUtil.removePrefix(file, getPrefix());


            return new ReportFile(pureFile, r.getUpdateTime());
        }).collect(Collectors.toList());
    }

    @Override
    public void saveReport(String file, String content) {
        SysReport r = dao.findByFile(file);
        if (r == null) {
            r = new SysReport();
        }

        r.setFile(file);
        r.setContent(content);
        dao.save(r);
    }

    @Override
    public String getName() {
        return "数据库存储";
    }

    @Override
    public boolean disabled() {
        return false;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
