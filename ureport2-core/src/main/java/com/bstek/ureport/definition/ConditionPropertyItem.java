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
package com.bstek.ureport.definition;

import java.util.List;


import com.bstek.ureport.expression.model.Condition;
import com.bstek.ureport.expression.model.Expression;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ConditionPropertyItem {
	private String name;
	
	@JsonIgnore
	private Condition condition;
	/**
	 * 此属性给设计器使用，引擎不使用该属性
	 */
	private List<Condition> conditions;
	
	private int rowHeight=-1;
	private int colWidth=-1;
	
	private String newValue;
	private String linkUrl;
	private String linkTargetWindow;
	private List<LinkParameter> linkParameters;
	
	private ConditionCellStyle cellStyle;
	
	private ConditionPaging paging;
	
	@JsonIgnore
	private Expression expression;
	
	private String expr;


}
