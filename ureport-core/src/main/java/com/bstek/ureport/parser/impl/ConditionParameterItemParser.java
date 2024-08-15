package com.bstek.ureport.parser.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bstek.ureport.expression.model.condition.*;
import com.bstek.ureport.parser.ParserTool;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.bstek.ureport.definition.ConditionCellStyle;
import com.bstek.ureport.definition.ConditionPaging;
import com.bstek.ureport.definition.ConditionPropertyItem;
import com.bstek.ureport.definition.LinkParameter;
import com.bstek.ureport.expression.ExpressionUtils;
import com.bstek.ureport.expression.model.Condition;
import com.bstek.ureport.expression.model.Expression;
import com.bstek.ureport.expression.model.Op;
import com.bstek.ureport.parser.Parser;


public class ConditionParameterItemParser implements Parser<ConditionPropertyItem>{
	private Map<String,Parser<?>> parsers=new HashMap<String,Parser<?>>();
	public ConditionParameterItemParser() {
		parsers.put("cell-style",new CellStyleParser());
		parsers.put("link-parameter",new LinkParameterParser());
		parsers.put("paging",new ConditionPagingParser());
	}
	@Override
	public ConditionPropertyItem parse(Element element) {
		ConditionPropertyItem item=new ConditionPropertyItem();
		String rowHeight=element.attributeValue("row-height");
		if(StringUtils.isNotBlank(rowHeight)){
			item.setRowHeight(Integer.valueOf(rowHeight));
		}
		String colWidth=element.attributeValue("col-width");
		if(StringUtils.isNotBlank(colWidth)){
			item.setColWidth(Integer.valueOf(colWidth));
		}
		item.setName(element.attributeValue("name"));
		item.setNewValue(element.attributeValue("new-value"));
		item.setLinkUrl(element.attributeValue("link-url"));
		item.setLinkTargetWindow(element.attributeValue("link-target-window"));
		List<LinkParameter> parameters=null;
		List<Condition> conditions=new ArrayList<Condition>();
		item.setConditions(conditions);
		BaseCondition topCondition=null;
		BaseCondition prevCondition=null;
		for(Object obj:element.elements()){
			if(obj==null || !(obj instanceof Element)){
				continue;
			}
			Element ele=(Element)obj;
			String name=ele.getName();
			if(name.equals("condition")){
				BaseCondition condition = parseCondition(ele);
				conditions.add(condition);
				if(topCondition==null){
					topCondition=condition;
					prevCondition=condition;
				}else{
					prevCondition.setNextCondition(condition);
					prevCondition.setJoin(condition.getJoin());
					prevCondition=condition;
				}
				continue;
			}
			Parser<?> parser=parsers.get(name);
			if(parser==null){
				continue;
			}
			Object data=parser.parse(ele);
			if(data instanceof ConditionCellStyle){
				item.setCellStyle((ConditionCellStyle)data);
			}else if(data instanceof LinkParameter){
				if(parameters==null){
					parameters= new ArrayList<>();
				}
				parameters.add((LinkParameter)data);
			}else if(data instanceof ConditionPaging){
				item.setPaging((ConditionPaging)data);
			}
		}
		item.setCondition(topCondition);
		item.setLinkParameters(parameters);
		return item;
	}



	private BaseCondition parseCondition(Element ele) {
		String type=ele.attributeValue("type");
		if(type==null || type.equals("property")){
			PropertyExpressionCondition condition=new PropertyExpressionCondition();
			String property=ele.attributeValue("property");
			condition.setLeftProperty(property);
			condition.setLeft(property);
			String operation=ele.attributeValue("op");
			condition.setOperation(operation);
			condition.setOp(Op.parse(operation));
			for(Object o:ele.elements()){
				if(o==null || !(o instanceof Element)){
					continue;
				}
				Element e=(Element)o;
				if(!e.getName().equals("value")){
					continue;
				}
				String expr=e.getTextTrim();
				condition.setRightExpression(ExpressionUtils.parseExpression(expr));
				condition.setRight(expr);
				break;
			}
			String join=ele.attributeValue("join");
			if(StringUtils.isNotBlank(join)){
				condition.setJoin(Join.valueOf(join));
			}
			return condition;
		}else{
			BothExpressionCondition exprCondition=new BothExpressionCondition();
			exprCondition.setOperation(ele.attributeValue("op"));
			exprCondition.setOp(Op.parse(ele.attributeValue("op")));
			for(Object o:ele.elements()){
				if(o==null || !(o instanceof Element)){
					continue;
				}
				Element e=(Element)o;
				String name=e.getName();
				if(name.equals("left")){
					String left=e.getText();
					Expression leftExpr=ExpressionUtils.parseExpression(left);
					exprCondition.setLeft(left);
					exprCondition.setLeftExpression(leftExpr);
				}else if(name.equals("right")){
					String right=e.getText();
					Expression rightExpr=ExpressionUtils.parseExpression(right);
					exprCondition.setRight(right);
					exprCondition.setRightExpression(rightExpr);
				}
			}
			String join=ele.attributeValue("join");
			if(StringUtils.isNotBlank(join)){
				exprCondition.setJoin(Join.valueOf(join));
			}
			return exprCondition;
		}
	}

	@Override
	public void convert(Object o, Element element) {
		ConditionPropertyItem item= (ConditionPropertyItem) o;

		ParserTool<ConditionPropertyItem> tool = new ParserTool<>(element, item, new ConditionPropertyItem());

		tool.attr("row-height",ConditionPropertyItem::getRowHeight);
		tool.attr("col-width",ConditionPropertyItem::getColWidth);
		tool.attr("name",ConditionPropertyItem::getName);
		tool.attr("new-value",ConditionPropertyItem::getNewValue);
		tool.attr("link-url", ConditionPropertyItem::getLinkUrl);
		tool.attr("link-target-window", ConditionPropertyItem::getLinkTargetWindow);


		if(item.getConditions() != null){
			for (Condition condition : item.getConditions()) {
				Element child = DocumentHelper.createElement("condition");
				convertCondition(child, (BaseCondition) condition);

				element.add(child);
			}
		}

		ConditionCellStyle style = item.getCellStyle();
		if(style != null){
			Element child = DocumentHelper.createElement("cell-style");

			parsers.get("cell-style").convert(style, child);

			element.add(child);
		}

		if(item.getLinkParameters() != null){
			for (LinkParameter linkParameter : item.getLinkParameters()) {
				Element child = DocumentHelper.createElement("link-parameter");

				parsers.get("link-parameter").convert(linkParameter, child);

				element.add(child);

			}
		}

		if(item.getPaging() != null) {
			Element child = DocumentHelper.createElement("paging");

			parsers.get("paging").convert(item.getPaging(), child);

			element.add(child);
		}
	}


	private void convertCondition(Element ele,BaseCondition baseCondition) {
		if(baseCondition instanceof  PropertyExpressionCondition){
			ele.addAttribute("type","property");
			PropertyExpressionCondition condition = (PropertyExpressionCondition) baseCondition;
			ele.addAttribute("property", condition.getLeftProperty());
			ele.addAttribute("op", condition.getOperation());

			Element child = DocumentHelper.createElement("value");
			child.setText(condition.getRight());
			ele.add(child);

			if(condition.getJoin() != null){
				ele.addAttribute("join", condition.getJoin().name());
			}

			return;
		}

		BothExpressionCondition exprCondition=new BothExpressionCondition();

		ele.addAttribute("op", exprCondition.getOperation());

		{
			Element child = DocumentHelper.createElement("left");
			child.setText(exprCondition.getLeft());
			ele.add(child);
		}
		{
			Element child = DocumentHelper.createElement("right");
			child.setText(exprCondition.getRight());
			ele.add(child);
		}

		if(exprCondition.getJoin() != null){
			ele.addAttribute("join", exprCondition.getJoin().name());
		}

	}
}
