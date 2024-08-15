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
package com.bstek.ureport.console.importexcel;

import com.bstek.ureport.console.BaseAction;
import com.bstek.ureport.console.cache.TempObjectCache;
import com.bstek.ureport.definition.ReportDefinition;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2017年5月25日
 */
@RestController
@RequestMapping("ureport/import")
public class ImportExcelAction extends BaseAction {


    @Resource
    ExcelParser excelParser;


    /**
     * （注：前端似乎时页面访问，有些框架会返回xml，这里强制返回json）
     * @param file
     * @return
     * @throws Exception
     */
    @RequestMapping(produces = "application/json; charset=UTF-8")
    public Map<String, Object> execute(@RequestParam(name = "_excel_file") MultipartFile file) throws Exception {
        Map<String, Object> result = new HashMap<>();
        if(!file.getOriginalFilename().endsWith(".xlsx")){
            result.put("result", false);
            result.put("errorInfo", "只支持.xlsx");
            return result;
        }

        InputStream inputStream = file.getInputStream();
        ReportDefinition report = excelParser.parse(inputStream);
        inputStream.close();


        if (report == null) {
            result.put("result", false);
            result.put("errorInfo", "解析错误");
        }else {
            result.put("result", true);
            TempObjectCache.putObject("classpath:template/template.ureport.xml", report);
        }


        return result;
    }

}
