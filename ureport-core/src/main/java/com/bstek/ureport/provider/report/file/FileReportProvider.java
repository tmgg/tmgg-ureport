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
package com.bstek.ureport.provider.report.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

import com.bstek.ureport.UReportProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.bstek.ureport.exception.ReportException;
import com.bstek.ureport.provider.report.ReportFile;
import com.bstek.ureport.provider.report.ReportProvider;

/**
 * @author Jacky.gao
 * @since 2017年2月11日
 */
@Component
public class FileReportProvider implements ReportProvider {
    private String prefix = "file:";

    @Resource
    private UReportProperties props;


    public String getStoreDir() {
        return props.getFileStoreDir();
    }


    @Override
    public String loadReport(String file) {
        if (file.startsWith(prefix)) {
            file = file.substring(prefix.length(), file.length());
        }
        String fullPath = getStoreDir() + "/" + file;
        try {
            return FileUtils.readFileToString(new File(fullPath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ReportException(e);
        }
    }

    @Override
    public void deleteReport(String file) {
        if (file.startsWith(prefix)) {
            file = file.substring(prefix.length(), file.length());
        }
        String fullPath = getStoreDir() + "/" + file;
        File f = new File(fullPath);
        if (f.exists()) {
            f.delete();
        }
    }

    @Override
    public List<ReportFile> getReportFiles() {
        File file = new File(getStoreDir());
        List<ReportFile> list = new ArrayList<ReportFile>();
        for (File f : file.listFiles()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(f.lastModified());
            list.add(new ReportFile(f.getName(), calendar.getTime()));
        }
        Collections.sort(list, (f1, f2) -> f2.getUpdateDate().compareTo(f1.getUpdateDate()));
        return list;
    }

    @Override
    public String getName() {
        return "服务器文件系统";
    }

    @Override
    public void saveReport(String file, String content) {
        if (file.startsWith(prefix)) {
            file = file.substring(prefix.length(), file.length());
        }
        String fullPath = getStoreDir() + "/" + file;
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(new File(fullPath));
            IOUtils.write(content, outStream, "utf-8");
        } catch (Exception ex) {
            throw new ReportException(ex);
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public boolean disabled() {
        return !props.isFileStoreEnable();
    }




    @Override
    public String getPrefix() {
        return prefix;
    }
}
