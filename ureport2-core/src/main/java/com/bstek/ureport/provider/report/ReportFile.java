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
package com.bstek.ureport.provider.report;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author Jacky.gao
 * @since 2017年2月11日
 */
@Getter
@Setter
public class ReportFile {

	public static final String TEMPLATE_FILE = "classpath:template/template.ureport.xml";
	public static boolean isTemplateFile(String file) {
		return file.endsWith(ReportFile.TEMPLATE_FILE);
	}

	public static String getTemplateFileContent(){
		return ResourceUtil.readUtf8Str(TEMPLATE_FILE);
	}

	private String name;
	private Date updateDate;
	
	public ReportFile(String name, Date updateDate) {
		this.name = name;
		this.updateDate = updateDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ReportFile that = (ReportFile) o;

		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}




}
