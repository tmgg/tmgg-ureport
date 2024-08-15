package com.bstek.ureport.parser;

import cn.hutool.core.io.IoUtil;
import com.bstek.ureport.definition.*;
import com.bstek.ureport.definition.datasource.DatasourceDefinition;
import com.bstek.ureport.definition.searchform.SearchForm;
import com.bstek.ureport.exception.ReportException;
import com.bstek.ureport.exception.ReportParseException;
import com.bstek.ureport.parser.impl.*;
import com.bstek.ureport.parser.impl.searchform.SearchFormParser;
import com.bstek.ureport.utils.DomTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Slf4j
@Component
public class ReportParser {
	private Map<String,Parser<?>> parsers= new HashMap<>();


	public ReportParser() {
		parsers.put("row",new RowParser());
		parsers.put("column",new ColumnParser());
		parsers.put("cell",new CellParser());
		parsers.put("datasource",new DatasourceParser());
		parsers.put("paper",new PaperParser());
		parsers.put("header",new HeaderFooterParser());
		parsers.put("footer",new HeaderFooterParser());
		parsers.put("search-form",new SearchFormParser());
	}
	public ReportDefinition parse(String text,String file) {
		ReportDefinition report=new ReportDefinition();
		report.setReportFullName(file);
		SAXReader saxReader = new SAXReader();
		try{
			ByteArrayInputStream is = IoUtil.toStream(text, StandardCharsets.UTF_8);
			Document document = saxReader.read(is);
			Element element = document.getRootElement();
			if(!element.getName().equals("ureport")){
				throw new ReportParseException("Unknow report file.");
			}
			List<RowDefinition> rows= new ArrayList<>();
			List<ColumnDefinition> columns= new ArrayList<>();
			List<CellDefinition> cells= new ArrayList<>();
			List<DatasourceDefinition> datasources= new ArrayList<>();
			report.setRows(rows);
			report.setColumns(columns);
			report.setCells(cells);
			report.setDatasources(datasources);
			for(Object obj:element.elements()){
				if(obj==null || !(obj instanceof Element)){
					continue;
				}
				Element ele=(Element)obj;
				Parser<?> parser=parsers.get(ele.getName());
				if(parser!=null){
					Object target=parser.parse(ele);
					if(target instanceof RowDefinition){
						rows.add((RowDefinition)target);
					}else if(target instanceof ColumnDefinition){
						columns.add((ColumnDefinition)target);
					}else if(target instanceof CellDefinition){
						cells.add((CellDefinition)target);
					}else if(target instanceof DatasourceDefinition){
						datasources.add((DatasourceDefinition)target);
					}else if(target instanceof Paper){
						Paper paper=(Paper)target;
						report.setPaper(paper);
					}else if(target instanceof HeaderFooterDefinition){
						HeaderFooterDefinition hf=(HeaderFooterDefinition)target;
						if(ele.getName().equals("header")){
							report.setHeader(hf);
						}else{
							report.setFooter(hf);
						}
					}else if(target instanceof SearchForm){
						SearchForm form=(SearchForm)target;
						report.setSearchForm(form);
						report.setSearchFormXml(ele.asXML());
					}
				}else{
					throw new ReportParseException("Unknow element :"+ele.getName());
				}
			}
			Collections.sort(rows);
			Collections.sort(columns);

			IoUtil.close(is);
		}catch(Exception ex){
			throw new ReportParseException(ex);
		}
		rebuild(report);

		return report;
	}
	private void rebuild(ReportDefinition report) {
		List<CellDefinition> cells=report.getCells();
		Map<String,CellDefinition> cellsMap=new HashMap<String,CellDefinition>();
		Map<String,CellDefinition> cellsRowColMap=new HashMap<String,CellDefinition>();
		for(CellDefinition cell:cells){
			cellsMap.put(cell.getName(), cell);
			int rowNum=cell.getRowNumber(),colNum=cell.getColumnNumber(),rowSpan=cell.getRowSpan(),colSpan=cell.getColSpan();
			rowSpan = rowSpan>0 ? rowSpan-- : 1;
			colSpan = colSpan>0 ? colSpan-- : 1;
			int rowStart=rowNum,rowEnd=rowNum+rowSpan,colStart=colNum,colEnd=colNum+colSpan;
			for(int i=rowStart;i<rowEnd;i++){
				cellsRowColMap.put(i+","+colNum, cell);				
			}
			for(int i=colStart;i<colEnd;i++){
				cellsRowColMap.put(rowNum+","+i, cell);								
			}
		}
		for(CellDefinition cell:cells){
			int rowNumber=cell.getRowNumber();
			int colNumber=cell.getColumnNumber();
			String leftParentCellName=cell.getLeftParentCellName();
			if(StringUtils.isNotBlank(leftParentCellName)){
				if(!leftParentCellName.equals("root")){
					CellDefinition targetCell=cellsMap.get(leftParentCellName);
					if(targetCell==null){
						throw new ReportException("Cell ["+cell.getName()+"] 's left parent cell ["+leftParentCellName+"] not exist.");
					}
					cell.setLeftParentCell(targetCell);					
				}
			}else{
				if(colNumber>1){
					CellDefinition targetCell=cellsRowColMap.get(rowNumber+","+(colNumber-1));
					cell.setLeftParentCell(targetCell);
				}
			}
			String topParentCellName=cell.getTopParentCellName();
			if(StringUtils.isNotBlank(topParentCellName)){
				if(!topParentCellName.equals("root")){
					CellDefinition targetCell=cellsMap.get(topParentCellName);
					if(targetCell==null){
						throw new ReportException("Cell ["+cell.getName()+"] 's top parent cell ["+topParentCellName+"] not exist.");
					}
					cell.setTopParentCell(targetCell);					
				}
			}else{
				if(rowNumber>1){
					CellDefinition targetCell=cellsRowColMap.get((rowNumber-1)+","+colNumber);
					cell.setTopParentCell(targetCell);
				}
			}
		}
	}


	public String convert(ReportDefinition def) {
		Document doc = DocumentHelper.createDocument();
		Element ureport = DocumentHelper.createElement("ureport");
		doc.add(ureport);

		for (CellDefinition cell : def.getCells()) {
			this.convertAndAddElement(ureport,"cell",cell);
		}


		for (RowDefinition row : def.getRows()) {
			this.convertAndAddElement(ureport,"row", row);
		}

		for (ColumnDefinition column : def.getColumns()) {
			this.convertAndAddElement(ureport,"column",column);
		}

		for (DatasourceDefinition datasource : def.getDatasources()) {
			this.convertAndAddElement(ureport,"datasource",datasource);
		}

		this.convertAndAddElement(ureport,"paper", def.getPaper());
		this.convertAndAddElement(ureport,"header", def.getHeader());
		this.convertAndAddElement(ureport,"footer", def.getFooter());
		this.convertAndAddElement(ureport,"search-form", def.getSearchForm());


		return DomTool.format(doc);
	}

	private void convertAndAddElement(Element doc, String name, Object data){
		if(data == null){
			return;
		}
		log.info("开始转换 {} {}", name, data);
		Parser<?> parser = parsers.get(name);
		Element el = DocumentHelper.createElement(name);
		doc.add(el);

		parser.convert(data, el);
	}

}
