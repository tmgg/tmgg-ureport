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
package com.bstek.ureport.parser.impl.value;

import java.util.ArrayList;
import java.util.List;

import com.bstek.ureport.expression.model.expr.dataset.DatasetExpression;
import com.bstek.ureport.parser.ParserTool;
import com.bstek.ureport.utils.DomTool;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.bstek.ureport.definition.Order;
import com.bstek.ureport.definition.mapping.MappingItem;
import com.bstek.ureport.definition.mapping.MappingType;
import com.bstek.ureport.definition.value.AggregateType;
import com.bstek.ureport.definition.value.DatasetValue;
import com.bstek.ureport.definition.value.GroupItem;
import com.bstek.ureport.definition.value.Value;
import com.bstek.ureport.expression.ExpressionUtils;
import com.bstek.ureport.expression.model.Condition;
import com.bstek.ureport.expression.model.Op;
import com.bstek.ureport.expression.model.condition.Join;
import com.bstek.ureport.expression.model.condition.PropertyExpressionCondition;


public class DatasetValueParser extends ValueParser {
	@Override
	public Value parse(Element element) {
		DatasetValue value=new DatasetValue();
		value.setAggregate(AggregateType.valueOf(element.attributeValue("aggregate")));
		value.setDatasetName(element.attributeValue("dataset-name"));
		value.setProperty(element.attributeValue("property"));
		String order=element.attributeValue("order");
		if(StringUtils.isNotBlank(order)){
			value.setOrder(Order.valueOf(order));
		}
		String mappingType=element.attributeValue("mapping-type");
		if(StringUtils.isNotBlank(mappingType)){
			value.setMappingType(MappingType.valueOf(mappingType));
		}
		value.setMappingDataset(element.attributeValue("mapping-dataset"));
		value.setMappingKeyProperty(element.attributeValue("mapping-key-property"));
		value.setMappingValueProperty(element.attributeValue("mapping-value-property"));
		List<GroupItem> groupItems=null;
		List<MappingItem> mappingItems=null;
		List<Condition> conditions=new ArrayList<Condition>();
		PropertyExpressionCondition topCondition=null;
		PropertyExpressionCondition prevCondition=null;
		value.setConditions(conditions);
		for(Object obj:element.elements()){
			if(obj==null || !(obj instanceof Element)){
				continue;
			}
			Element ele=(Element)obj;
			if(ele.getName().equals("condition")){
				PropertyExpressionCondition condition = parseCondition(ele);
				conditions.add(condition);
				if(topCondition==null){
					topCondition=condition;
					prevCondition=topCondition;
				}else{
					prevCondition.setNextCondition(condition);
					prevCondition.setJoin(condition.getJoin());
					prevCondition=condition;
				}				
			}else if(ele.getName().equals("group-item")){
				if(groupItems==null){
					groupItems=new ArrayList<GroupItem>();
					value.setGroupItems(groupItems);
				}
				GroupItem item=new GroupItem();
				item.setName(ele.attributeValue("name"));
				groupItems.add(item);
				PropertyExpressionCondition groupItemTopCondition=null;
				List<Condition> itemConditions=new ArrayList<Condition>();
				for(Object o:ele.elements()){
					if(o==null || !(o instanceof Element)){
						continue;
					}
					PropertyExpressionCondition itemCondition=parseCondition((Element)o);
					itemConditions.add(itemCondition);
					if(groupItemTopCondition==null){
						groupItemTopCondition=itemCondition;
					}else{
						groupItemTopCondition.setNextCondition(itemCondition);
					}
				}
				item.setCondition(groupItemTopCondition);
				item.setConditions(itemConditions);
			}else if(ele.getName().equals("mapping-item")){
				MappingItem item=new MappingItem();
				item.setLabel(ele.attributeValue("label"));
				item.setValue(ele.attributeValue("value"));
				if(mappingItems==null){
					mappingItems=new ArrayList<MappingItem>();
				}
				mappingItems.add(item);
			}
		}
		if(topCondition!=null){
			value.setCondition(topCondition);
		}
		if(mappingItems!=null){
			value.setMappingItems(mappingItems);
		}
		return value;
	}



	private PropertyExpressionCondition parseCondition(Element ele) {
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
	}

	@Override
	public void convert(Object o, Element el) {
		DatasetValue value= (DatasetValue) o;
		ParserTool<DatasetValue> tool = new ParserTool<>(el, value, new DatasetValue());
		tool.attr("aggregate", DatasetExpression::getAggregate);
		tool.attr("dataset-name", DatasetExpression::getDatasetName);
		tool.attr("property", DatasetExpression::getProperty);
		tool.attr("order", DatasetExpression::getOrder);
		el.addAttribute("mapping-type", value.getMappingType().name());
		tool.attr("mapping-dataset", DatasetExpression::getMappingDataset);
		tool.attr("mapping-key-property", DatasetExpression::getMappingKeyProperty);
		tool.attr("mapping-value-property", DatasetExpression::getMappingValueProperty);
		tool.attr("aggregate", DatasetExpression::getAggregate);


		if(value.getConditions() != null){
			for (Condition condition : value.getConditions()) {
				Element child = DocumentHelper.createElement("condition");
				convertCondition(child, (PropertyExpressionCondition) condition);

				el.add(child);
			}
		}

		if(value.getGroupItems() != null){
			for (GroupItem groupItem : value.getGroupItems()) {
				Element child = DocumentHelper.createElement("group-item");
				child.addAttribute("name", groupItem.getName());

				if(groupItem.getConditions() != null){
					for (Condition condition : groupItem.getConditions()) {
						Element child2 = DocumentHelper.createElement("condition");
						convertCondition(child2, (PropertyExpressionCondition) condition);
						child.add(child2);
					}
				}
				el.add(child);
			}
		}

		if(value.getMappingItems() != null){
			for (MappingItem mappingItem : value.getMappingItems()) {
				Element child = DocumentHelper.createElement("mapping-item");
				child.addAttribute("label", mappingItem.getLabel());
				child.addAttribute("value", mappingItem.getValue());

				el.add(child);
			}
		}
	}

	private void convertCondition(Element ele, PropertyExpressionCondition condition) {
		ele.addAttribute("property", condition.getLeftProperty());
		ele.addAttribute("op", condition.getOperation());
		ele.addAttribute("type", condition.getType().name());



		Element child = DomTool.createChild(ele, "value");
		child.addCDATA(condition.getRight());

		if(condition.getJoin() != null){
			ele.addAttribute("join", condition.getJoin().name());
		}
	}
}
