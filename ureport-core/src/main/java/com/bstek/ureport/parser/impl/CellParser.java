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
package com.bstek.ureport.parser.impl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bstek.ureport.definition.*;
import com.bstek.ureport.definition.value.*;
import com.bstek.ureport.parser.ParserTool;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.bstek.ureport.exception.ReportException;
import com.bstek.ureport.exception.ReportParseException;
import com.bstek.ureport.expression.ExpressionUtils;
import com.bstek.ureport.expression.model.Expression;
import com.bstek.ureport.parser.Parser;
import com.bstek.ureport.parser.impl.value.ChartValueParser;
import com.bstek.ureport.parser.impl.value.DatasetValueParser;
import com.bstek.ureport.parser.impl.value.ExpressionValueParser;
import com.bstek.ureport.parser.impl.value.ImageValueParser;
import com.bstek.ureport.parser.impl.value.SimpleValueParser;
import com.bstek.ureport.parser.impl.value.SlashValueParser;
import com.bstek.ureport.parser.impl.value.ZxingValueParser;
import org.springframework.util.Assert;


public class CellParser implements Parser<CellDefinition>{
	private Map<String,Parser<?>> parsers= new HashMap<>();
	public CellParser() {
		parsers.put("simple-value",new SimpleValueParser());
		parsers.put("image-value",new ImageValueParser());
		parsers.put("expression-value",new ExpressionValueParser());
		parsers.put("dataset-value",new DatasetValueParser());
		parsers.put("slash-value",new SlashValueParser());
		parsers.put("zxing-value",new ZxingValueParser());
		parsers.put("chart-value",new ChartValueParser());
		parsers.put("cell-style",new CellStyleParser());
		parsers.put("link-parameter",new LinkParameterParser());
		parsers.put("condition-property-item",new ConditionParameterItemParser());
	}
	@Override
	public CellDefinition parse(Element element) {
		CellDefinition cell=new CellDefinition();
		cell.setName(element.attributeValue("name"));
		cell.setColumnNumber(Integer.valueOf(element.attributeValue("col")));
		cell.setRowNumber(Integer.valueOf(element.attributeValue("row")));
		cell.setLeftParentCellName(element.attributeValue("left-cell"));
		cell.setTopParentCellName(element.attributeValue("top-cell"));
		String rowSpan=element.attributeValue("row-span");
		if(StringUtils.isNotBlank(rowSpan)){
			cell.setRowSpan(Integer.valueOf(rowSpan));
		}
		String colSpan=element.attributeValue("col-span");
		if(StringUtils.isNotBlank(colSpan)){
			cell.setColSpan(Integer.valueOf(colSpan));
		}
		String expand=element.attributeValue("expand");
		if(StringUtils.isNotBlank(expand)){
			cell.setExpand(Expand.valueOf(expand));			
		}
		String fillBlankRows=element.attributeValue("fill-blank-rows");
		if(StringUtils.isNotBlank(fillBlankRows)){
			cell.setFillBlankRows(Boolean.valueOf(fillBlankRows));
			String multiple=element.attributeValue("multiple");
			if(StringUtils.isNotBlank(multiple)){
				cell.setMultiple(Integer.valueOf(multiple));
			}
		}
		cell.setLinkTargetWindow(element.attributeValue("link-target-window"));
		String linkUrl=element.attributeValue("link-url");
		cell.setLinkUrl(linkUrl);
		if(StringUtils.isNotBlank(linkUrl)){
			if(linkUrl.startsWith(ExpressionUtils.EXPR_PREFIX) && linkUrl.endsWith(ExpressionUtils.EXPR_SUFFIX)){
				String expr=linkUrl.substring(2,linkUrl.length()-1);
				Expression urlExpression=ExpressionUtils.parseExpression(expr);
				cell.setLinkUrlExpression(urlExpression);
			}
		}
		List<LinkParameter> linkParameters=null;
		List<ConditionPropertyItem> conditionPropertyItems=null;
		for(Object obj:element.elements()){
			if(!(obj instanceof Element)){
				continue;
			}
			Element ele=(Element)obj;
			Object parseData=parseValue(ele);
			if(parseData instanceof Value){
				Value value=(Value)parseData;
				cell.setValue(value);
			}else if(parseData instanceof CellStyle){
				CellStyle cellStyle=(CellStyle)parseData;
				cell.setCellStyle(cellStyle);
			}else if(parseData instanceof LinkParameter){
				if(linkParameters==null){
					linkParameters= new ArrayList<>();
				}
				linkParameters.add((LinkParameter)parseData);
			}else if(parseData instanceof ConditionPropertyItem){
				if(conditionPropertyItems==null){
					conditionPropertyItems= new ArrayList<>();
				}
				conditionPropertyItems.add((ConditionPropertyItem)parseData);
			}
		}
		if(linkParameters!=null){
			cell.setLinkParameters(linkParameters);
		}
		cell.setConditionPropertyItems(conditionPropertyItems);
		if(cell.getValue()==null){
			throw new ReportException("Cell ["+cell.getName()+"] value not define.");
		}
		return cell;
	}



	private Object parseValue(Element element){
		Parser<?> parser=parsers.get(element.getName());
		if(parser!=null){
			return parser.parse(element);			
		}
		throw new ReportParseException("Unknow element :"+element.getName());
	}

	@Override
	public void convert(Object o, Element el) {
		CellDefinition cell= (CellDefinition) o;
		ParserTool<CellDefinition> tool = new ParserTool<>(el, cell, new CellDefinition());

		tool.attr("name", CellDefinition::getName);
		tool.attr("col",CellDefinition::getColumnNumber);
		tool.attr("row",CellDefinition::getRowNumber);
		tool.attr("left-cell",CellDefinition::getLeftParentCellName);
		tool.attr("top-cell",CellDefinition::getTopParentCellName);
		tool.attr("row-span",CellDefinition::getRowSpan);
		tool.attr("col-span",CellDefinition::getColSpan);
		tool.attrF("expand",CellDefinition::getExpand);
		tool.attr("fill-blank-rows",CellDefinition::isFillBlankRows);
		if(cell.isFillBlankRows()){
			tool.attr("multiple", CellDefinition::getMultiple);
		}
		tool.attr("link-target-windo",CellDefinition::getLinkTargetWindow);
		tool.attr("link-url",CellDefinition::getLinkUrl);

		Value value = cell.getValue();
		convertValue(el,value);
		convertValue(el,cell.getCellStyle());
		List<LinkParameter> linkParameters = cell.getLinkParameters();
		if(linkParameters != null) {
			for (LinkParameter linkParameter : linkParameters) {
				convertValue(el, linkParameter);
			}
		}
		List<ConditionPropertyItem> conditionPropertyItems = cell.getConditionPropertyItems();
		if(conditionPropertyItems != null) {
			for (ConditionPropertyItem conditionPropertyItem : conditionPropertyItems) {
				convertValue(el, conditionPropertyItem);
			}

		}
	}


	private void convertValue(Element element, Object value){
		String name = null;
		if(value instanceof SimpleValue){
			name = "simple-value";
		}else if(value instanceof ImageValue){
			name = "image-value";
		}else if(value instanceof ExpressionValue){
			name = "expression-value";
		}else if(value instanceof DatasetValue){
			name = "dataset-value";
		}else if(value instanceof SlashValue){
			name = "slash-value";
		}else if(value instanceof ZxingValue){
			name = "zxing-value";
		}else if(value instanceof ChartValue){
			name = "chart-value";
		}else if(value instanceof CellStyle){
			name = "cell-style";
		}else if(value instanceof LinkParameter){
			name = "link-parameter";
		}


		Parser<?> parser=parsers.get(name);
		Assert.state(parser != null,"解析器未定义"+ name);

		Element child = DocumentHelper.createElement(name);
		parser.convert(value,child);

		element.add(child);
	}
}
