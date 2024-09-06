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
package com.bstek.ureport.console.pdf;

import com.bstek.ureport.build.ReportBuilder;
import com.bstek.ureport.console.BaseAction;
import com.bstek.ureport.console.cache.TempObjectCache;
import com.bstek.ureport.console.exception.ReportDesignException;
import com.bstek.ureport.definition.Paper;
import com.bstek.ureport.definition.ReportDefinition;
import com.bstek.ureport.exception.ReportComputeException;
import com.bstek.ureport.exception.ReportException;
import com.bstek.ureport.export.ExportConfigure;
import com.bstek.ureport.export.ExportConfigureImpl;
import com.bstek.ureport.export.ExportManager;
import com.bstek.ureport.export.ReportRender;
import com.bstek.ureport.export.pdf.PdfProducer;
import com.bstek.ureport.model.Report;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2017年3月20日
 */
@RestController
@RequestMapping("ureport/pdf")
public class ExportPdfAction extends BaseAction {

	@Resource
	private ReportBuilder reportBuilder;

	@Resource
	private ExportManager exportManager;

	@Resource
	private ReportRender reportRender;
	private PdfProducer pdfProducer=new PdfProducer();

	@RequestMapping
	public void execute(@RequestParam("_u")String file ,@RequestParam(name = "_n",required = false) String fileName,HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			buildPdf(file, fileName,req, resp,false);
	}


	@RequestMapping("show")
	public void show(@RequestParam("_u")String file , @RequestParam(name = "_n",required = false) String fileName, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		buildPdf(file, fileName, req, resp,true);
	}


	public void buildPdf(String file , String fileName, HttpServletRequest req, HttpServletResponse resp, boolean forPrint) throws IOException {
		file=decode(file);
		if(StringUtils.isBlank(file)){
			throw new ReportComputeException("Report file can not be null.");
		}
		OutputStream outputStream=null;
		try {

			fileName=buildDownloadFileName(file, fileName, ".pdf");
			fileName=new String(fileName.getBytes("UTF-8"),"ISO8859-1");
			if(forPrint){
				resp.setContentType("application/pdf");
				resp.setHeader("Content-Disposition","inline;filename=\"" + fileName + "\"");
			}else{
				resp.setContentType("application/octet-stream;charset=ISO8859-1");
				resp.setHeader("Content-Disposition","attachment;filename=\"" + fileName + "\"");
			}
			outputStream=resp.getOutputStream();
			Map<String, Object> parameters = buildParameters(req);
			if(file.equals(PREVIEW_KEY)){
				ReportDefinition reportDefinition=(ReportDefinition)TempObjectCache.getObject(PREVIEW_KEY);
				if(reportDefinition==null){
					throw new ReportDesignException("Report data has expired,can not do export pdf.");
				}
				Report report=reportBuilder.buildReport(reportDefinition, parameters);
				pdfProducer.produce(report, outputStream);
			}else{
				ExportConfigure configure=new ExportConfigureImpl(file,parameters,outputStream);
				exportManager.exportPdf(configure);
			}
		}catch(Exception ex) {
			throw new ReportException(ex);
		}finally {
			outputStream.flush();
			outputStream.close();
		}
	}

	@RequestMapping("newPaging")
	public void newPaging(@RequestParam("_u") String file, @RequestParam("_paper")	String paper,HttpServletRequest req) throws IOException {
		if(StringUtils.isBlank(file)){
			throw new ReportComputeException("Report file can not be null.");
		}
		Report report=null;
		Map<String, Object> parameters = buildParameters(req);
		if(file.equals(PREVIEW_KEY)){
			ReportDefinition reportDefinition=(ReportDefinition)TempObjectCache.getObject(PREVIEW_KEY);
			if(reportDefinition==null){
				throw new ReportDesignException("Report data has expired,can not do export pdf.");
			}
			report=reportBuilder.buildReport(reportDefinition, parameters);
		}else{
			ReportDefinition reportDefinition=reportRender.getReportDefinition(file);
			report=reportRender.render(reportDefinition, parameters);
		}

		ObjectMapper mapper=new ObjectMapper();
		Paper newPaper=mapper.readValue(paper, Paper.class);
		report.rePaging(newPaper);
	}


}
