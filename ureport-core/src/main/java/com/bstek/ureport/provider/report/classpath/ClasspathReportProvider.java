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
package com.bstek.ureport.provider.report.classpath;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.bstek.ureport.UReportProperties;
import com.bstek.ureport.provider.report.ReportFile;
import com.bstek.ureport.provider.report.ReportProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jacky.gao
 * @since 2016年12月4日
 */
@Component
@Slf4j
public class ClasspathReportProvider implements ReportProvider {
    @Resource
    private UReportProperties props;

    private boolean isDevMode = new File("pom.xml").exists();


    @Override
    public String loadReport(String file) {
        if (isDevMode && !ReportFile.isTemplateFile(file)) {
            File realFile = getRealFile(file);
            return FileUtil.readUtf8String(realFile);
        }
        return ResourceUtil.readUtf8Str(file);
    }


    @Override
    public String getPrefix() {
        return "classpath:";
    }

    @Override
    public void deleteReport(String file) {
        Assert.state(isDevMode, "只能在开发时操作");
        File realFile = getRealFile(file);
        Assert.state(realFile.exists(), "文件不存在");
        realFile.delete();
    }

    @Override
    public void saveReport(String file, String content) {
        Assert.state(isDevMode, "只能在开发时操作");
        File storeFile = getRealFile(file);
        FileUtil.writeString(content, storeFile, StandardCharsets.UTF_8);
    }

    private File getRealFile(String file) {
        file = StrUtil.removePrefix(file, getPrefix());
        File root = getStoreRoot();
        String fileName = FileUtil.getName(file);
        File storeFile = new File(root, fileName);
        return storeFile;
    }

    @Override
    public List<ReportFile> getReportFiles() {
        String dirName = getDirName();
        ArrayList<ReportFile> list = new ArrayList<>();

        if (!isDevMode) {
            String storePath = getStoreDir();
            File file = new File(storePath);
            String dir = file.getName();

            String classPath = "classpath*:" + dir + "/*";

            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            try {
                org.springframework.core.io.Resource[] resources = resolver.getResources(classPath);

                for (org.springframework.core.io.Resource resource : resources) {
                    String filename = resource.getFilename();

                    ReportFile reportFile = new ReportFile(dirName + "/" + filename, new Date(file.lastModified()));
                    list.add(reportFile);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // 开发模式下
            File storeRoot = getStoreRoot();

            File[] files = storeRoot.listFiles();

            if (files == null) {
                return Collections.emptyList();
            }

            for (File file : files) {
                String fileName = file.getName();
                ReportFile reportFile = new ReportFile(dirName + "/" + fileName, new Date(file.lastModified()));

                list.add(reportFile);
            }

        }

        return list.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public boolean disabled() {
        return !props.isClasspathStoreEnable();
    }

    @Override
    public String getName() {
        return "classpath:" + getStoreDir();
    }

    private String getStoreDir() {
        return props.getClasspathStoreDir();
    }


    private File getStoreRoot() {
        String path = getStoreDir();
        File file = new File(path);

        if (file.exists()) {
            file.mkdirs();
        }
        return file;
    }


    private String getDirName() {
        String path = getStoreDir();
        File file = new File(path);
        return file.getName();
    }
}
