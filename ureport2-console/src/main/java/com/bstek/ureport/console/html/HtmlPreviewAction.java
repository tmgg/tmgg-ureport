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
package com.bstek.ureport.console.html;

import com.bstek.ureport.build.Context;
import com.bstek.ureport.build.ReportBuilder;
import com.bstek.ureport.build.paging.Page;
import com.bstek.ureport.cache.CacheUtils;
import com.bstek.ureport.chart.ChartData;
import com.bstek.ureport.console.BaseAction;
import com.bstek.ureport.console.MobileUtils;
import com.bstek.ureport.console.cache.TempObjectCache;
import com.bstek.ureport.console.exception.ReportDesignException;
import com.bstek.ureport.definition.Paper;
import com.bstek.ureport.definition.ReportDefinition;
import com.bstek.ureport.definition.searchform.FormPosition;
import com.bstek.ureport.exception.ReportComputeException;
import com.bstek.ureport.export.*;
import com.bstek.ureport.export.html.HtmlProducer;
import com.bstek.ureport.export.html.HtmlReport;
import com.bstek.ureport.export.html.SearchFormData;
import com.bstek.ureport.model.Report;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * @author Jacky.gao
 * @since 2017年2月15日
 */
@RestController
@RequestMapping("ureport/preview")
public class HtmlPreviewAction extends BaseAction {

    @Resource
    private ExportManager exportManager;

    @Resource
    private ReportBuilder reportBuilder;

    @Resource
    private ReportRender reportRender;

    private HtmlProducer htmlProducer = new HtmlProducer();

    @RequestMapping
    public void execute( @RequestParam(name = "_title",required = false) String title,@RequestParam(name = "_t", required = false) String toolsInfo, @RequestParam("_u") String file, @RequestParam(name = "_i",required = false) String pageIndex,HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        VelocityContext context = new VelocityContext();
        HtmlReport htmlReport = null;
        String errorMsg = null;
        try {
            htmlReport = loadReport(file,pageIndex,req);
        } catch (Exception ex) {
            if (!(ex instanceof ReportDesignException)) {
                ex.printStackTrace();
            }
            errorMsg = buildExceptionMessage(ex);
        }
        title = buildTitle(title, file);
        context.put("title", title);
        if (htmlReport == null) {
            context.put("content", "<div style='color:red'><strong>报表计算出错，错误信息如下：</strong><br><div style=\"margin:10px\">" + errorMsg + "</div></div>");
            context.put("error", true);
            context.put("searchFormJs", "");
            context.put("downSearchFormHtml", "");
            context.put("upSearchFormHtml", "");
        } else {
            SearchFormData formData = htmlReport.getSearchFormData();
            if (formData != null) {
                context.put("searchFormJs", formData.getJs());
                if (formData.getFormPosition().equals(FormPosition.up)) {
                    context.put("upSearchFormHtml", formData.getHtml());
                    context.put("downSearchFormHtml", "");
                } else {
                    context.put("downSearchFormHtml", formData.getHtml());
                    context.put("upSearchFormHtml", "");
                }
            } else {
                context.put("searchFormJs", "");
                context.put("downSearchFormHtml", "");
                context.put("upSearchFormHtml", "");
            }
            context.put("content", htmlReport.getContent());
            context.put("style", htmlReport.getStyle());
            context.put("reportAlign", htmlReport.getReportAlign());
            context.put("totalPage", htmlReport.getTotalPage());
            context.put("totalPageWithCol", htmlReport.getTotalPageWithCol());
            context.put("pageIndex", htmlReport.getPageIndex());
            context.put("chartDatas", convertJson(htmlReport.getChartDatas()));
            context.put("error", false);

            context.put("file", file);
            context.put("intervalRefreshValue", htmlReport.getHtmlIntervalRefreshValue());
            String customParameters = buildCustomParameters(req);
            context.put("customParameters", customParameters);
            context.put("_t", "");
            Tools tools = null;
            if (MobileUtils.isMobile(req)) {
                tools = new Tools(false);
                tools.setShow(false);
            } else {

                if (StringUtils.isNotBlank(toolsInfo)) {
                    tools = new Tools(false);
                    if (toolsInfo.equals("0")) {
                        tools.setShow(false);
                    } else {
                        String[] infos = toolsInfo.split(",");
                        for (String name : infos) {
                            tools.doInit(name);
                        }
                    }
                    context.put("_t", toolsInfo);
                    context.put("hasTools", true);
                } else {
                    tools = new Tools(true);
                }
            }
            context.put("tools", tools);
        }
        context.put("contextPath", req.getContextPath());
        resp.setContentType("text/html");
        resp.setCharacterEncoding("utf-8");
        Template template = ve.getTemplate("ureport-html/html-preview.html", "utf-8");
        PrintWriter writer = resp.getWriter();
        template.merge(context, writer);
        writer.close();
    }

    private String buildTitle(String title, String file) {
        if (StringUtils.isBlank(title)) {
            title = file;
            title = decode(title);
            int point = title.lastIndexOf(".ureport.xml");
            if (point > -1) {
                title = title.substring(0, point);
            }
            if (title.equals("p")) {
                title = "设计中报表";
            }
        } else {
            title = decode(title);
        }
        return title + "-ureport";
    }

    private String convertJson(Collection<ChartData> data) {
        if (data == null || data.size() == 0) {
            return "";
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(data);
            return json;
        } catch (Exception e) {
            throw new ReportComputeException(e);
        }
    }

    @RequestMapping("loadData")
    public HtmlReport loadData( @RequestParam("_u") String file, @RequestParam(name = "_i",required = false) String pageIndex,HttpServletRequest req) throws ServletException, IOException {
        HtmlReport htmlReport = loadReport(file, pageIndex,req);
        return htmlReport;
    }


    @RequestMapping("loadPrintPages")
    public Map<String, String> loadPrintPages(@RequestParam(name = "_u", required = false) String file, HttpServletRequest req) {

        file = decode(file);
        if (StringUtils.isBlank(file)) {
            throw new ReportComputeException("Report file can not be null.");
        }
        Map<String, Object> parameters = buildParameters(req);
        ReportDefinition reportDefinition = null;
        if (file.equals(PREVIEW_KEY)) {
            reportDefinition = (ReportDefinition) TempObjectCache.getObject(PREVIEW_KEY);
            if (reportDefinition == null) {
                throw new ReportDesignException("Report data has expired,can not do export excel.");
            }
        } else {
            reportDefinition = reportRender.getReportDefinition(file);
        }
        Report report = reportBuilder.buildReport(reportDefinition, parameters);
        Map<String, ChartData> chartMap = report.getContext().getChartDataMap();
        if (chartMap.size() > 0) {
            CacheUtils.storeChartDataMap(chartMap);
        }
        FullPageData pageData = PageBuilder.buildFullPageData(report);
        StringBuilder sb = new StringBuilder();
        List<List<Page>> list = pageData.getPageList();
        Context context = report.getContext();
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                List<Page> columnPages = list.get(i);
                if (i == 0) {
                    String html = htmlProducer.produce(context, columnPages, pageData.getColumnMargin(), false);
                    sb.append(html);
                } else {
                    String html = htmlProducer.produce(context, columnPages, pageData.getColumnMargin(), false);
                    sb.append(html);
                }
            }
        } else {
            List<Page> pages = report.getPages();
            for (int i = 0; i < pages.size(); i++) {
                Page page = pages.get(i);
                if (i == 0) {
                    String html = htmlProducer.produce(context, page, false);
                    sb.append(html);
                } else {
                    String html = htmlProducer.produce(context, page, true);
                    sb.append(html);
                }
            }
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("html", sb.toString());
        return map;
    }

    @RequestMapping("loadPagePaper")
    public Paper loadPagePaper(@RequestParam("_u") String file) {
        file = decode(file);
        if (StringUtils.isBlank(file)) {
            throw new ReportComputeException("Report file can not be null.");
        }
        ReportDefinition report = null;
        if (file.equals(PREVIEW_KEY)) {
            report = (ReportDefinition) TempObjectCache.getObject(PREVIEW_KEY);
            if (report == null) {
                throw new ReportDesignException("Report data has expired.");
            }
        } else {
            report = reportRender.getReportDefinition(file);
        }
        Paper paper = report.getPaper();
        return paper;
    }

    private HtmlReport loadReport( String file, String pageIndex, HttpServletRequest req) {
        Map<String, Object> parameters = buildParameters(req);
        HtmlReport htmlReport = null;

        file = decode(file);

        if (StringUtils.isBlank(file)) {
            throw new ReportComputeException("Report file can not be null.");
        }
        if (file.equals(PREVIEW_KEY)) {
            ReportDefinition reportDefinition = (ReportDefinition) TempObjectCache.getObject(PREVIEW_KEY);
            if (reportDefinition == null) {
                throw new ReportDesignException("Report data has expired,can not do preview.");
            }
            Report report = reportBuilder.buildReport(reportDefinition, parameters);
            Map<String, ChartData> chartMap = report.getContext().getChartDataMap();
            if (chartMap.size() > 0) {
                CacheUtils.storeChartDataMap(chartMap);
            }
            htmlReport = new HtmlReport();
            String html = null;
            if (StringUtils.isNotBlank(pageIndex) && !pageIndex.equals("0")) {
                Context context = report.getContext();
                int index = Integer.valueOf(pageIndex);
                SinglePageData pageData = PageBuilder.buildSinglePageData(index, report);
                List<Page> pages = pageData.getPages();
                if (pages.size() == 1) {
                    Page page = pages.get(0);
                    html = htmlProducer.produce(context, page, false);
                } else {
                    html = htmlProducer.produce(context, pages, pageData.getColumnMargin(), false);
                }
                htmlReport.setTotalPage(pageData.getTotalPages());
                htmlReport.setPageIndex(index);
            } else {
                html = htmlProducer.produce(report);
            }
            if (report.getPaper().isColumnEnabled()) {
                htmlReport.setColumn(report.getPaper().getColumnCount());
            }
            htmlReport.setChartDatas(report.getContext().getChartDataMap().values());
            htmlReport.setContent(html);
            htmlReport.setTotalPage(report.getPages().size());
            htmlReport.setStyle(reportDefinition.getStyle());
            htmlReport.setSearchFormData(reportDefinition.buildSearchFormData(report.getContext().getDatasetMap(), parameters));
            htmlReport.setReportAlign(report.getPaper().getHtmlReportAlign().name());
            htmlReport.setHtmlIntervalRefreshValue(report.getPaper().getHtmlIntervalRefreshValue());
        } else {
            if (StringUtils.isNotBlank(pageIndex) && !pageIndex.equals("0")) {
                int index = Integer.valueOf(pageIndex);
                htmlReport = exportManager.exportHtml(file, req.getContextPath(), parameters, index);
            } else {
                htmlReport = exportManager.exportHtml(file, req.getContextPath(), parameters);
            }
        }
        return htmlReport;
    }


    private String buildCustomParameters(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder();
        Enumeration<?> enumeration = req.getParameterNames();
        while (enumeration.hasMoreElements()) {
            Object obj = enumeration.nextElement();
            if (obj == null) {
                continue;
            }
            String name = obj.toString();
            String value = req.getParameter(name);
            if (name == null || value == null || (name.startsWith("_") && !name.equals("_n"))) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(name);
            sb.append("=");
            sb.append(value);
        }
        return sb.toString();
    }

    private String buildExceptionMessage(Throwable throwable) {
        Throwable root = buildRootException(throwable);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        root.printStackTrace(pw);
        String trace = sw.getBuffer().toString();
        trace = trace.replaceAll("\n", "<br>");
        pw.close();
        return trace;
    }


}
