package com.bstek.ureport.utils;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

@Slf4j
public class DomTool {
    public static String format(String xml) throws DocumentException {
        SAXReader reader = new SAXReader();

        StringReader r = new StringReader(xml);
        Document doc = reader.read(r);

        r.close();

        return format(doc);
    }
    public static String format(Document doc){
        OutputFormat fmt = new OutputFormat();
        fmt.setEncoding("UTF-8");
        fmt.setNewlines(true);
        fmt.setIndent(true);
        fmt.setIndentSize(4);
        fmt.setTrimText(true);
        fmt.setPadText(true);


        StringWriter writer = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(writer,fmt);
        try {
            xmlWriter.write(doc);
            xmlWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return writer.toString();
    }

    public static boolean equals(Element element1, Element element2, boolean showLog,String... ignoreAttrs){
        if(showLog) {
            log.info("开始比较元素{}，{}", element1, element2);
        }
        if (!element1.getName().equals(element2.getName())) {
            return false;
        }

        if (!element1.getText().equals(element2.getText())) {
            if(showLog){
                log.warn("文本内容不一致");
                log.info(element1.asXML());
                log.info(element2.asXML());
            }

            return false;
        }
        Map<String, String> map1 = getAttrMap(element1);
        Map<String, String> map2 = getAttrMap(element2);
        for (String ignoreAttr : ignoreAttrs) {
            map1.remove(ignoreAttr);
            map2.remove(ignoreAttr);
        }
        if (map1.size() != map1.size()) {
            Collection<String> subtract = CollUtil.subtract(map1.keySet(), map2.keySet());
            Collection<String> subtract2 = CollUtil.subtract(map2.keySet(), map1.keySet());
            if(showLog){
                log.info("{} 元素属性不一致{}, {}", element1.getName(), element1.attributeCount(), element2.attributeCount());
                log.info("差集为：{} {}",subtract, subtract2);
                log.info(element1.asXML());
                log.info(element2.asXML());
            }
            return false;
        }

        if(showLog){
            System.out.println("元素1的属性");
            System.out.println(map1);
            System.out.println("元素2的属性");
            System.out.println(map2);
        }


        for (Map.Entry<String, String> e : map1.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();

            String v2 = map2.get(k);
            if(!Objects.equals(v, v2)){
                if(showLog){
                    log.info("属性{}的值不一致{} {}", k, v,v2);
                }
                return false;
            }

        }

        return true;
    }

    public static Element findChild(Element el, Element target, boolean showLog,String... ignoreAttrs){
        List list = target.elements(el.getName());

        for (Object o : list) {
            if(o instanceof  Element){
                Element cur = (Element) o;
                if(equals(el, cur, showLog, ignoreAttrs)){
                    return  cur;
                }
            }
        }


        return null;
    }

    public static void printAttr(Element el){
        Map<String, String> map = getAttrMap(el);

        for (Map.Entry<String, String> e : map.entrySet()) {
            System.out.println(e.getKey() +"="+e.getValue());
        }
    }

    public static Element createChild(Element parent, String name){
        Element element = DocumentHelper.createElement(name);
        parent.add(element);
        return element;
    }

    private static Map<String, String> getAttrMap(Element el) {
        List attributes = el.attributes();
        Map<String,String> map = new TreeMap<>();
        for (Object attribute : attributes) {
            org.dom4j.Attribute a = (org.dom4j.Attribute) attribute;
            map.put(a.getName(), a.getValue());
        }
        return map;
    }
}
