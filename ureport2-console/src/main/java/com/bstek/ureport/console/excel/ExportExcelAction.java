/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.bstek.ureport.console.excel;

import com.bstek.ureport.build.ReportBuilder;
import com.bstek.ureport.console.BaseAction;
import com.bstek.ureport.console.cache.TempObjectCache;
import com.bstek.ureport.console.exception.ReportDesignException;
import com.bstek.ureport.definition.ReportDefinition;
import com.bstek.ureport.exception.ReportComputeException;
import com.bstek.ureport.exception.ReportException;
import com.bstek.ureport.export.ExportConfigure;
import com.bstek.ureport.export.ExportConfigureImpl;
import com.bstek.ureport.export.ExportManager;
import com.bstek.ureport.export.excel.high.ExcelProducer;
import com.bstek.ureport.model.Report;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2017年4月17日
 */
@RestController
@RequestMapping("ureport/excel")
public class ExportExcelAction extends BaseAction {
    @Resource
    private ReportBuilder reportBuilder;

    @Resource
    private ExportManager exportManager;
    private ExcelProducer excelProducer = new ExcelProducer();

    @RequestMapping
    public void index(@RequestParam("_u")String file, @RequestParam(name = "_n",required = false) String fileName, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        buildExcel(file,fileName,req, resp, false, false);
    }

    @RequestMapping("paging")
    public void paging(@RequestParam("_u")String file,@RequestParam(name = "_n",required = false) String fileName,HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        buildExcel(file,fileName,req, resp, true, false);
    }

    @RequestMapping("sheet")
    public void sheet(@RequestParam("_u")String file,@RequestParam(name = "_n",required = false) String fileName,HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        buildExcel(file,fileName,req, resp, false, true);
    }

    public void buildExcel(String file,String fileName,HttpServletRequest req, HttpServletResponse resp, boolean withPage, boolean withSheet) throws IOException {
        file = decode(file);
        if (StringUtils.isBlank(file)) {
            throw new ReportComputeException("Report file can not be null.");
        }
        OutputStream outputStream = resp.getOutputStream();
        try {
            fileName = buildDownloadFileName(file, fileName, ".xlsx");
            resp.setContentType("application/octet-stream;charset=ISO8859-1");
            fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
            resp.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
            Map<String, Object> parameters = buildParameters(req);
            if (file.equals(PREVIEW_KEY)) {
                ReportDefinition reportDefinition = (ReportDefinition) TempObjectCache.getObject(PREVIEW_KEY);
                if (reportDefinition == null) {
                    throw new ReportDesignException("Report data has expired,can not do export excel.");
                }
                Report report = reportBuilder.buildReport(reportDefinition, parameters);
                if (withPage) {
                    excelProducer.produceWithPaging(report, outputStream);
                } else if (withSheet) {
                    excelProducer.produceWithSheet(report, outputStream);
                } else {
                    excelProducer.produce(report, outputStream);
                }
            } else {
                ExportConfigure configure = new ExportConfigureImpl(file, parameters, outputStream);
                if (withPage) {
                    exportManager.exportExcelWithPaging(configure);
                } else if (withSheet) {
                    exportManager.exportExcelWithPagingSheet(configure);
                } else {
                    exportManager.exportExcel(configure);
                }
            }
        } catch (Exception ex) {
            throw new ReportException(ex);
        } finally {
            outputStream.flush();
            outputStream.close();
        }
    }

}
