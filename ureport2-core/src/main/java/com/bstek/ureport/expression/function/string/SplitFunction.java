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
package com.bstek.ureport.expression.function.string;

import com.bstek.ureport.build.Context;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.model.Cell;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
新增函数： split
 */
@Component
public class SplitFunction extends StringFunction {

	@Override
	public Object execute(List<ExpressionData<?>> dataList, Context context,Cell currentCell) {
		String text=buildString(dataList);

		String[] arr = text.split(",");

		List<Object> list = new ArrayList<>();
		for (String obj : arr) {
			list.add(obj);
		}
		return list;
	}

	@Override
	public String name() {
		return "split";
	}
}
