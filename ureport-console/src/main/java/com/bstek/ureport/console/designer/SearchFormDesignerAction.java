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
package com.bstek.ureport.console.designer;

import com.bstek.ureport.console.BaseAction;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Jacky.gao
 * @since 2017年10月24日
 */
@RestController
@RequestMapping("ureport/searchFormDesigner")
public class SearchFormDesignerAction extends BaseAction {

	@RequestMapping
	public void index(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		VelocityContext context = new VelocityContext();
		context.put("contextPath", req.getContextPath());
		resp.setContentType("text/html");
		resp.setCharacterEncoding("utf-8");
		Template template=ve.getTemplate("ureport-html/searchform.html","utf-8");
		PrintWriter writer=resp.getWriter();
		template.merge(context, writer);
		writer.close();
	}

}
