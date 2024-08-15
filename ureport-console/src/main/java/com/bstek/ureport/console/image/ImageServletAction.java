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
package com.bstek.ureport.console.image;

import com.bstek.ureport.cache.ResourceCache;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Jacky.gao
 * @since 2016年6月6日
 */

@Controller
@RequestMapping("ureport/image")
public class ImageServletAction {

	@RequestMapping
	public void execute(String _key, HttpServletResponse resp) throws ServletException, IOException {
		if(StringUtils.isNotBlank(_key)){
			byte[] bytes=(byte[])ResourceCache.getObject(_key);
			InputStream input=new ByteArrayInputStream(bytes);
			OutputStream output=resp.getOutputStream();
			resp.setContentType("image/png");
			try{
				IOUtils.copy(input, output);			
			}finally{
				IOUtils.closeQuietly(input);
				IOUtils.closeQuietly(output);
			}
		}else{
			//processImage(req, resp);			
		}
	}

}
