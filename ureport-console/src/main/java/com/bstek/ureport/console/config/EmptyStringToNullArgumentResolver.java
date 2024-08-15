package com.bstek.ureport.console.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;

/**
 * 将前端请求空字符串转换为null
 */
public class EmptyStringToNullArgumentResolver extends AbstractNamedValueMethodArgumentResolver {
    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        return  new NamedValueInfo("", false, ValueConstants.DEFAULT_NONE);
    }
    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
        String[] param = request.getParameterValues(name);
        if(param==null){
            return null;
        }
        if(StringUtils.isEmpty(param[0])){
            return null;
        }
        return param[0];
    }
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(String.class);
    }
}