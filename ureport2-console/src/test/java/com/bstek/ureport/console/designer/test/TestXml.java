package com.bstek.ureport.console.designer.test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.bstek.ureport.definition.Paper;
import com.bstek.ureport.definition.ReportDefinition;
import com.bstek.ureport.parser.ReportParser;
import com.bstek.ureport.utils.DomTool;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.*;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.io.File;


@Slf4j
public class TestXml {

    ReportParser reportParser = new ReportParser();

    @Test
    public void test() throws Exception {
        File dir = new File("D:\\ws2\\ureport2-spring\\ureport2-console\\src\\test\\resources");
        for (String file : dir.list()) {
            System.err.println(file);
            this.testXml(file);
        }


    }


    public void testXml(String file) throws Exception {
        String xml1 = ResourceUtil.readUtf8Str(file);

        ReportDefinition def = reportParser.parse(xml1, "test");
        String xml2 = reportParser.convert(def);


        check(xml1, xml2);
    }


    private void check(String a, String b) throws DocumentException {
        a = DomTool.format(a);

        System.out.println(a);

        System.out.println("转换后结果");
        System.out.println(b);

        FileUtil.writeUtf8String(a, new File("D:/a.xml"));

        FileUtil.writeUtf8String(b, new File("D:/b.xml"));

        Element doc1 = DocumentHelper.parseText(a).getRootElement();
        Element doc2 = DocumentHelper.parseText(b).getRootElement();




        compare(doc1, doc2);


    }


    private static void compare(Element e1, Element e2) {
        log.debug("Comparing element: {}", e1.getName());

        {
            if(e1.getName().equals("search-form")){
                return;
            }

            String[] ignoreAttr = new String[0];
            if(e2.getName().equals("condition")){
                ignoreAttr = new String[]{"id","type"};

            }


            boolean equals = DomTool.equals(e1, e2, true, ignoreAttr);

            if(!equals){
                return;
            }
        }



        for (Object o : e1.elements()) {
            if (!(o instanceof Element)) {
                continue;
            }


            Element e11 = (Element) o;

            if(e11.getName().equals("search-form")){
                continue;
            }

            String[] ignoreAttr = new String[0];
            if(e11.getName().equals("condition")){
                ignoreAttr = new String[]{"id","type"};

            }

            log.debug("比较 {}的子元素{}", e1.getName(), e11.getName());
            Element e22 = DomTool.findChild(e11, e2,false, ignoreAttr);
            if(e22 == null){
                DomTool.findChild(e11, e2,true,ignoreAttr);
            }

            Assert.notNull(e22, "元素2不存在");

            compare(e11, e22);

        }


        log.info("通过\n");

    }
}
