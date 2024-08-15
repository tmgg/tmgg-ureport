package com.bstek.ureport.parser;

import cn.hutool.core.util.StrUtil;
import org.dom4j.CDATA;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.function.Function;

public class ParserTool<T> {

    public ParserTool(Element el, T data, T defaultData) {
        this.el = el;
        this.data = data;
        this.defaultData = defaultData;
    }

    Element el;

    T data;
    T defaultData;

    public void attr(String name, Function<T, Object> fn) {
        String value = getValue(fn);
        if (value == null) {
            return;
        }
        el.addAttribute(name, value);
    }

    public void attrF(String name, Function<T, Object> fn) {
        Object value = fn.apply(data);
        if(value == null){
            value = "";
        }
        el.addAttribute(name, String.valueOf(value));
    }

    public void childText(String name, Function<T, Object> fn) {
        String value = getValue(fn);
        if (value == null) {
            return;
        }

        Element child = DocumentHelper.createElement(name);
        child.setText(value);

        el.add(child);
    }

    private  String getValue(Function<T, Object> fn) {
        Object value = fn.apply(data);
        Object defaultValue = defaultData != null ? fn.apply(defaultData): null;
        if (value == null ||  StrUtil.isBlankIfStr(value) || value.equals(defaultValue) || isBaseDefaultValue(value)) {
            return null;
        }
        return String.valueOf(value);
    }


    /**
     * 判断是否为基本类型的默认值
     *
     * @param object
     * @return
     */
    private static boolean isBaseDefaultValue(Object object) {
        Class className = object.getClass();
        if (className.equals(java.lang.Integer.class)) {
            return (int) object == 0;
        } else if (className.equals(java.lang.Byte.class)) {
            return (byte) object == 0;
        } else if (className.equals(java.lang.Long.class)) {
            return (long) object == 0L;
        } else if (className.equals(java.lang.Double.class)) {
            return (double) object == 0.0d;
        } else if (className.equals(java.lang.Float.class)) {
            return (float) object == 0.0f;
        } else if (className.equals(java.lang.Character.class)) {
            return (char) object == '\u0000';
        } else if (className.equals(java.lang.Short.class)) {
            return (short) object == 0;
        } else if (className.equals(java.lang.Boolean.class)) {
            return (boolean) object == false;
        }
        return false;
    }

}
