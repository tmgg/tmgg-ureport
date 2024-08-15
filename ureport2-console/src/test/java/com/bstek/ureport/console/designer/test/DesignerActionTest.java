package com.bstek.ureport.console.designer.test;


import cn.hutool.core.bean.BeanUtil;
import com.bstek.ureport.console.designer.ReportDefinitionWrapper;
import com.bstek.ureport.definition.CellDefinition;
import com.bstek.ureport.definition.ReportDefinition;
import com.bstek.ureport.export.ReportRender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.List;

@SpringBootTest
public class DesignerActionTest {

    @Resource
    ReportRender reportRender;

    @Test
    public void testLoad() throws JsonProcessingException {
        ReportDefinition reportDef = reportRender.parseReport("classpath:报送表01-年累实际收支情况表.ureport.xml");






        toJson(reportDef);



    }

    private void toJson(Object obj) throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        String s = om.writeValueAsString(obj);
      //  System.out.println(s);
    }
}
