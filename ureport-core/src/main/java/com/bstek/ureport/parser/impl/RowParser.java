package com.bstek.ureport.parser.impl;

import com.bstek.ureport.definition.Band;
import com.bstek.ureport.definition.RowDefinition;
import com.bstek.ureport.parser.BuildUtils;
import com.bstek.ureport.parser.Parser;
import com.bstek.ureport.parser.ParserTool;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;


public class RowParser implements Parser<RowDefinition> {
    @Override
    public RowDefinition parse(Element element) {
        RowDefinition row = new RowDefinition();

        row.setRowNumber(Integer.valueOf(element.attributeValue("row-number")));
        String height = element.attributeValue("height");
        if (StringUtils.isNotBlank(height)) {
            row.setHeight(Integer.valueOf(height));
        }
        String band = element.attributeValue("band");
        if (StringUtils.isNotBlank(band)) {
            row.setBand(Band.valueOf(band));
        }
        return row;
    }



    @Override
    public void convert(Object data, Element el) {
        RowDefinition row = (RowDefinition) data;
        ParserTool<RowDefinition> tool = new ParserTool<>(el, row, new RowDefinition());

        tool.attr( "row-number", RowDefinition::getRowNumber);
        tool.attr( "height", RowDefinition::getHeight);
        tool.attr( "band", RowDefinition::getBand);
    }
}
