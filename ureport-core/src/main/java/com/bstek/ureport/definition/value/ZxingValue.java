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
package com.bstek.ureport.definition.value;

import com.bstek.ureport.expression.model.Expression;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ZxingValue implements Value {
	private int width;
	private int height;
	private Source source;
	private String text;
	private String expr;
	private String format;
	private Expression expression;
	private ZxingCategory category;
	private boolean codeDisplay;
	@Override
	public String getValue() {
		// 2019年1月23日 修复表达式时无法获取value数据
		return source == Source.expression ? expr : text;
	}

	@Override
	public ValueType getType() {
		return ValueType.zxing;
	}


}
