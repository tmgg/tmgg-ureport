package com.bstek.ureport.parser.impl;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.bstek.ureport.definition.LinkParameter;
import com.bstek.ureport.expression.ExpressionUtils;
import com.bstek.ureport.expression.model.Expression;
import com.bstek.ureport.parser.Parser;


public class LinkParameterParser implements Parser<LinkParameter> {
	@Override
	public LinkParameter parse(Element element) {
		LinkParameter param=new LinkParameter();
		param.setName(element.attributeValue("name"));
		for(Object obj:element.elements()){
			if(obj==null || !(obj instanceof Element)){
				continue;
			}
			Element ele=(Element)obj;
			if(ele.getName().equals("value")){
				param.setValue(ele.getText());
				Expression expr=ExpressionUtils.parseExpression(ele.getText());
				param.setValueExpression(expr);;
				break;
			}
		}
		return param;
	}


	@Override
	public void convert(Object data, Element el) {
		LinkParameter param= (LinkParameter) data;
		el.addAttribute("name", param.getName());

		Element value = DocumentHelper.createElement("value");
		value.setText(param.getValue());
		el.add(value);
	}
}
