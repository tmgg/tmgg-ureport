package com.bstek.ureport.parser.impl;

import com.bstek.ureport.definition.*;
import com.bstek.ureport.parser.Parser;
import com.bstek.ureport.parser.ParserTool;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;


public class PaperParser implements Parser<Paper> {
    @Override
    public Paper parse(Element element) {
        Paper paper = new Paper();

        String orientation = element.attributeValue("orientation");
        paper.setOrientation(Orientation.valueOf(orientation));

        paper.setPaperType(PaperType.valueOf(element.attributeValue("type")));
        if (paper.getPaperType().equals(PaperType.CUSTOM)) {
            paper.setWidth(Integer.valueOf(element.attributeValue("width")));
            paper.setHeight(Integer.valueOf(element.attributeValue("height")));
        } else {
            PaperSize size = paper.getPaperType().getPaperSize();
            paper.setWidth(size.getWidth());
            paper.setHeight(size.getHeight());
        }

        String leftMargin = element.attributeValue("left-margin");
        if (StringUtils.isNotBlank(leftMargin)) {
            paper.setLeftMargin(Integer.valueOf(leftMargin));
        }
        String rightMargin = element.attributeValue("right-margin");
        if (StringUtils.isNotBlank(rightMargin)) {
            paper.setRightMargin(Integer.valueOf(rightMargin));
        }
        String topMargin = element.attributeValue("top-margin");
        if (StringUtils.isNotBlank(topMargin)) {
            paper.setTopMargin(Integer.valueOf(topMargin));
        }
        String bottomMargin = element.attributeValue("bottom-margin");
        if (StringUtils.isNotBlank(bottomMargin)) {
            paper.setBottomMargin(Integer.valueOf(bottomMargin));
        }
        paper.setPagingMode(PagingMode.valueOf(element.attributeValue("paging-mode")));
        if (paper.getPagingMode().equals(PagingMode.fixrows)) {
            paper.setFixRows(Integer.valueOf(element.attributeValue("fixrows")));
        }
        String columnEnabled = element.attributeValue("column-enabled");
        if (StringUtils.isNotBlank(columnEnabled)) {
            paper.setColumnEnabled(Boolean.valueOf(columnEnabled));
        }
        if (paper.isColumnEnabled()) {
            paper.setColumnCount(Integer.valueOf(element.attributeValue("column-count")));
            paper.setColumnMargin(Integer.valueOf(element.attributeValue("column-margin")));
        }
        String htmlReportAlign = element.attributeValue("html-report-align");
        if (StringUtils.isNotBlank(htmlReportAlign)) {
            paper.setHtmlReportAlign(HtmlReportAlign.valueOf(htmlReportAlign));
        }
        String htmlIntervalRefreshValue = element.attributeValue("html-interval-refresh-value");
        if (StringUtils.isNotBlank(htmlIntervalRefreshValue)) {
            paper.setHtmlIntervalRefreshValue(Integer.valueOf(htmlIntervalRefreshValue));
        }
        paper.setBgImage(element.attributeValue("bg-image"));
        return paper;
    }

    @Override
    public void convert(Object data, Element el) {
        Paper paper = (Paper) data;

        ParserTool<Paper> tool = new ParserTool<>(el, paper, new Paper());
        tool.attr("orientation", Paper::getOrientation);

        tool.attr("type", Paper::getPaperType);


        tool.attr("width", Paper::getWidth);
        tool.attr("height", Paper::getHeight);
        tool.attrF("left-margin", Paper::getLeftMargin);
        tool.attrF("right-margin", Paper::getRightMargin);
        tool.attrF("top-margin", Paper::getTopMargin);
        tool.attrF("bottom-margin", Paper::getBottomMargin);
        tool.attr("paging-mode", Paper::getPagingMode);
        tool.attrF("fixrows", Paper::getFixRows);
        tool.attrF("column-enabled", Paper::isColumnEnabled);
        tool.attr("column-count", Paper::getColumnCount);
        tool.attr("column-margin", Paper::getColumnMargin);
        tool.attrF("html-report-align", Paper::getHtmlReportAlign);
        tool.attrF("html-interval-refresh-value", Paper::getHtmlIntervalRefreshValue);
        tool.attrF("bg-image", Paper::getBgImage);
    }
}
