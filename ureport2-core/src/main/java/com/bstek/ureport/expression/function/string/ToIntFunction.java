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

import java.util.List;


/**
 * toInt函数 将字符串转换为数字
 * 会自动把前面得0去掉，比如月份 “08” 会变成 8
 *
 *
 * @author deepls
 */
@Component
public class ToIntFunction extends StringFunction {
	@Override
	public Object execute(List<ExpressionData<?>> dataList, Context context,Cell currentCell) {
		String text=buildString(dataList);

		return Integer.parseInt(text.trim());
	}

	@Override
	public String name() {
		return "toInt";
	}
}
