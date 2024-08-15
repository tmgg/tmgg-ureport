package com.bstek.ureport.parser.impl;

import com.bstek.ureport.definition.dataset.*;
import com.bstek.ureport.definition.datasource.*;
import com.bstek.ureport.expression.ExpressionUtils;
import com.bstek.ureport.expression.model.Expression;
import com.bstek.ureport.parser.Parser;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;


public class DatasourceParser implements Parser<DatasourceDefinition> {
    @Override
    public DatasourceDefinition parse(Element element) {
        String type = element.attributeValue("type");
        switch (type) {
            case "jdbc": {
                JdbcDatasourceDefinition ds = new JdbcDatasourceDefinition();
                ds.setName(element.attributeValue("name"));
                ds.setDriver(element.attributeValue("driver"));
                ds.setUrl(element.attributeValue("url"));
                ds.setUsername(element.attributeValue("username"));
                ds.setPassword(element.attributeValue("password"));
                ds.setDatasets(parseDatasets(element));
                return ds;
            }
            case "spring": {
                SpringBeanDatasourceDefinition ds = new SpringBeanDatasourceDefinition();
                ds.setName(element.attributeValue("name"));
                ds.setBeanId(element.attributeValue("bean"));
                ds.setDatasets(parseDatasets(element));
                return ds;
            }
            case "buildin": {
                BuildinDatasourceDefinition ds = new BuildinDatasourceDefinition();
                ds.setName(element.attributeValue("name"));
                ds.setDatasets(parseDatasets(element));
                return ds;
            }
        }
        return null;
    }


    private List<DatasetDefinition> parseDatasets(Element element) {
        List<DatasetDefinition> list = new ArrayList<DatasetDefinition>();
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            String type = ele.attributeValue("type");
            switch (type) {
                case "sql": {
                    SqlDatasetDefinition dataset = new SqlDatasetDefinition();
                    dataset.setName(ele.attributeValue("name"));
                    dataset.setSql(parseSql(ele, dataset));
                    dataset.setFields(parseFields(ele));
                    dataset.setParameters(parseParameters(ele));
                    list.add(dataset);
                    break;
                }
                case "bean": {
                    BeanDatasetDefinition dataset = new BeanDatasetDefinition();
                    dataset.setName(ele.attributeValue("name"));
                    dataset.setMethod(ele.attributeValue("method"));
                    dataset.setFields(parseFields(ele));
                    dataset.setClazz(ele.attributeValue("clazz"));
                    list.add(dataset);
                    break;
                }
            }
        }
        return list;
    }

    private List<Parameter> parseParameters(Element element) {
        List<Parameter> parameters = new ArrayList<Parameter>();
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            if (!ele.getName().equals("parameter")) {
                continue;
            }
            Parameter param = new Parameter();
            param.setName(ele.attributeValue("name"));
            param.setDefaultValue(ele.attributeValue("default-value"));
            param.setType(DataType.valueOf(ele.attributeValue("type")));
            parameters.add(param);
        }
        return parameters;
    }

    private List<Field> parseFields(Element element) {
        List<Field> fields = new ArrayList<Field>();
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            if (!ele.getName().equals("field")) {
                continue;
            }
            Field field = new Field(ele.attributeValue("name"));
            fields.add(field);
        }
        return fields;
    }

    private String parseSql(Element element, SqlDatasetDefinition dataset) {
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            if (ele.getName().equals("sql")) {
                String sql = ele.getText().trim();
                if (sql.startsWith(ExpressionUtils.EXPR_PREFIX) && sql.endsWith(ExpressionUtils.EXPR_SUFFIX)) {
                    String s = sql.substring(2, sql.length() - 1);
                    Expression expr = ExpressionUtils.parseExpression(s);
                    dataset.setSqlExpression(expr);
                }
                return ele.getText();
            }
        }
        return null;
    }

    @Override
    public void convert(Object data, Element el) {
        DatasourceDefinition def = (DatasourceDefinition) data;

        el.addAttribute("name", def.getName());
        el.addAttribute("type", def.getType().name());
        switch (def.getType()) {
            case jdbc: {
                JdbcDatasourceDefinition jdbc = (JdbcDatasourceDefinition) def;
                el.addAttribute("driver", jdbc.getDriver());
                el.addAttribute("url", jdbc.getUrl());
                el.addAttribute("username", jdbc.getUsername());
                el.addAttribute("password", jdbc.getPassword());
                break;
            }
            case spring: {
                el.addAttribute("bean", ((SpringBeanDatasourceDefinition) def).getBeanId());
                break;
            }

            case buildin: {
                break;
            }
        }


        List<DatasetDefinition> datasets = def.getDatasets();

        convertDatasets(el,  datasets);
    }


    private void convertDatasets(Element element,  List<DatasetDefinition> list) {
        for (DatasetDefinition def : list) {

            if(def instanceof SqlDatasetDefinition){
                SqlDatasetDefinition dataset = (SqlDatasetDefinition) def;
                Element dsEl = DocumentHelper.createElement("dataset");
                dsEl.addAttribute("type", "sql" );
                dsEl.addAttribute("name", def.getName());
                convertSql(dsEl, dataset);
                convertFields(dsEl, dataset.getFields());
                convertParameters(dsEl, dataset.getParameters());
                element.add(dsEl);
            }else if(def instanceof BeanDatasetDefinition){
                BeanDatasetDefinition dataset = (BeanDatasetDefinition) def;
                Element dsEl = DocumentHelper.createElement("dataset");
                dsEl.addAttribute("type", "bean" );
                dsEl.addAttribute("name", def.getName());
                dsEl.addAttribute("method", dataset.getMethod());
                convertFields(dsEl, dataset.getFields());
                dsEl.addAttribute("clazz", dataset.getClazz());
                element.add(dsEl);
            }else {
                throw  new IllegalStateException();
            }


        }


    }

    private void  convertParameters(Element element,List<Parameter> list) {
        if(list == null){
            return;
        }
        for (Parameter param : list) {
            Element child = DocumentHelper.createElement("parameter");
            child.addAttribute("name", param.getName());
            child.addAttribute("default-value", param.getDefaultValue());
            child.addAttribute("type", param.getType().name());
            element.add(child);
        }
    }

    private void convertFields(Element element,List<Field> list) {
        for (Field field : list) {
            Element child = DocumentHelper.createElement("field");
            child.addAttribute("name", field.getName());
            element.add(child);
        }

        List<Field> fields = new ArrayList<>();
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            if (!ele.getName().equals("field")) {
                continue;
            }
            Field field = new Field(ele.attributeValue("name"));
            fields.add(field);
        }
    }

    private void convertSql(Element element, SqlDatasetDefinition dataset) {
        Element child = DocumentHelper.createElement("sql");
        child.addCDATA(dataset.getSql());
        element.add(child);
    }
}
