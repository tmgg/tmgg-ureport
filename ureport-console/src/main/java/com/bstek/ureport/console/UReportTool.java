package com.bstek.ureport.console;

import cn.hutool.core.io.IoUtil;
import com.bstek.ureport.utils.DomTool;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * 提供给框架使用者的一些工具类
 */
public class UReportTool {

    public static String getDesignerUrl(String file) {
        return "ureport/designer?_u=" + file;
    }

    public static String getViewUrl(String file) {
        return "ureport/preview?_u=" + file;
    }

    public static String addBuildinDataset(String xml, String name, String sql, List<String> fields) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        ByteArrayInputStream is = IoUtil.toStream(xml, StandardCharsets.UTF_8);
        Document doc = saxReader.read(is);

        Element root = doc.getRootElement();

        List datasourceList = root.elements("datasource");
        Element datasource = null;
        for (Object o : datasourceList) {
            if (o instanceof Element) {
                Element el = (Element) o;
                String type = el.attributeValue("type");
                if (Objects.equals(type, "buildin")) {
                    datasource = el;
                    break;
                }
            }
        }


        if (datasource == null) {
            datasource = root.addElement("datasource");
            datasource.addAttribute("name", "内置数据源");
            datasource.addAttribute("type", "buildin");
        } else {
            List datasetList = datasource.elements();
            for (Object el : datasetList) {
                if (el instanceof Element && Objects.equals(((Element) el).attributeValue("name"), name)) {
                    // 删除旧的
                    datasource.remove((Element) el);
                    break;
                }
            }
        }

        Element dataset  = datasource.addElement("dataset");
        dataset.addAttribute("name", name);
        dataset.addAttribute("type", "sql");
        Element sqlEl = dataset.addElement("sql");
        sqlEl.addCDATA(sql);

        for (String field : fields) {
            Element el = dataset.addElement("field");
            el.addAttribute("name", field);
        }


        IoUtil.close(is);



        //return  doc.asXML();

        return DomTool.format(doc);
    }




}
