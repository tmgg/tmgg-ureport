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

import java.io.Serializable;
import java.util.List;


import com.bstek.ureport.build.Context;
import com.bstek.ureport.build.paging.HeaderFooter;
import com.bstek.ureport.expression.model.Expression;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.expression.model.data.ObjectExpressionData;
import com.bstek.ureport.expression.model.data.ObjectListExpressionData;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class HeaderFooterDefinition implements Serializable{
	private static final long serialVersionUID = -7239528314017768029L;
	private String left;
	private String center;
	private String right;
	private String fontFamily="宋体";
	private int fontSize=10;
	private String forecolor="0,0,0";
	private boolean bold;
	private boolean italic;
	private boolean underline;
	private int height=30;
	private int margin=30;
	@JsonIgnore
	private Expression leftExpression;
	@JsonIgnore
	private Expression centerExpression;
	@JsonIgnore
	private Expression rightExpression;
	public HeaderFooter buildHeaderFooter(int pageIndex,Context context){
		HeaderFooter hf=new HeaderFooter();
		hf.setBold(bold);
		hf.setFontFamily(fontFamily);
		hf.setFontSize(fontSize);
		hf.setForecolor(forecolor);
		hf.setHeight(height);
		hf.setItalic(italic);
		hf.setUnderline(underline);
		hf.setMargin(margin);
		context.setPageIndex(pageIndex);
		if(leftExpression!=null){
			Object obj = buildExpression(context,leftExpression);
			if(obj!=null){
				hf.setLeft(obj.toString());
			}
		}
		if(centerExpression!=null){
			Object obj = buildExpression(context,centerExpression);
			if(obj!=null){
				hf.setCenter(obj.toString());
			}
		}
		if(rightExpression!=null){
			Object obj = buildExpression(context,rightExpression);
			if(obj!=null){
				hf.setRight(obj.toString());
			}
		}
		return hf;
	}
	private Object buildExpression(Context context,Expression expr) {
		ExpressionData<?> data=expr.execute(context.getRootCell(), context.getRootCell(), context);
		Object obj=null;
		if(data instanceof ObjectExpressionData){
			obj=((ObjectExpressionData)data).getData();
		}else if(data instanceof ObjectListExpressionData){
			ObjectListExpressionData listData=(ObjectListExpressionData)data;
			if(listData!=null){
				List<?> list=listData.getData();
				if(list!=null && list.size()>0){
					obj="";
					for(Object o:list){
						if(o==null){
							continue;
						}
						if(!obj.equals("")){
							obj+=",";
						}
						obj+=o.toString();
					}
				}
			}
		}
		return obj;
	}

}
