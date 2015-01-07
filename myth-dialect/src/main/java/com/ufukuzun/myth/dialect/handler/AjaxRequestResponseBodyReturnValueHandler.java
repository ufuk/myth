/*
 * Copyright 2012, Ufuk Uzun (http://www.ufukuzun.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ufukuzun.myth.dialect.handler;

import com.ufukuzun.myth.dialect.bean.Myth;
import com.ufukuzun.myth.dialect.handler.annotation.AjaxRequestBody;
import com.ufukuzun.myth.dialect.handler.annotation.AjaxResponseBody;
import com.ufukuzun.myth.dialect.model.AjaxRequest;
import com.ufukuzun.myth.dialect.model.AjaxResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AjaxRequestResponseBodyReturnValueHandler extends AbstractMessageConverterMethodProcessor {

    @Autowired
    private Myth myth;

    public AjaxRequestResponseBodyReturnValueHandler(List<HttpMessageConverter<?>> messageConverters) {
        super(messageConverters);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.getMethodAnnotation(AjaxResponseBody.class) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
        mavContainer.setRequestHandled(true);

        if (returnValue != null && AjaxResponse.class.isInstance(returnValue)) {
            AjaxResponse ajaxResponse = (AjaxResponse) returnValue;
            HttpServletResponse response = (HttpServletResponse) webRequest.getNativeResponse();
            HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
            ModelAndView modelAndView = ajaxResponse.getModelAndView();

            modelAndView.getModelMap().mergeAttributes((HashMap<String, ?>) mavContainer.getModel().clone());
            returnValue = myth.response(ajaxResponse.getAjaxRequest(), modelAndView, response, request);

            writeWithMessageConverters(returnValue, returnType, webRequest);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> void writeWithMessageConverters(T returnValue, MethodParameter returnType, ServletServerHttpRequest inputMessage, ServletServerHttpResponse outputMessage) throws IOException, HttpMediaTypeNotAcceptableException {
        Class<?> returnValueClass = returnValue.getClass();

        for (HttpMessageConverter<?> messageConverter : messageConverters) {
            if (messageConverter.canWrite(returnValueClass, MediaType.APPLICATION_JSON)) {
                ((HttpMessageConverter<T>) messageConverter).write(returnValue, MediaType.APPLICATION_JSON, outputMessage);
                return;
            }
        }

        throw new HttpMediaTypeNotAcceptableException(allSupportedMediaTypes);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AjaxRequestBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        AjaxRequestBody ajaxRequestBody = parameter.getParameterAnnotation(AjaxRequestBody.class);
        boolean performValidation = ajaxRequestBody.validate();
        String targetName = ajaxRequestBody.targetName();
        Assert.notNull(targetName);

        Object argument = readWithMessageConverters(webRequest, parameter, parameter.getGenericParameterType());

        if (argument != null && AjaxRequest.class.isInstance(argument) && !StringUtils.isEmpty(targetName)) {
            AjaxRequest<?> ajaxRequest = (AjaxRequest<?>) argument;

            mavContainer.addAttribute(targetName, ajaxRequest.getModel());

            if (performValidation) {
                boolean modelValid = myth.validate(mavContainer.getModel(), ajaxRequest.getModel(), targetName);
                if (!modelValid) {
                    Field modelValidField = ReflectionUtils.findField(AjaxRequest.class, "modelValid");
                    modelValidField.setAccessible(true);
                    ReflectionUtils.setField(modelValidField, ajaxRequest, false);
                }
            }
        }

        return argument;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected <T> Object readWithMessageConverters(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType) throws IOException, HttpMediaTypeNotSupportedException {
        Class<?> contextClass = parameter.getDeclaringClass();
        Map<TypeVariable, Type> map = GenericTypeResolver.getTypeVariableMap(contextClass);
        Class<T> targetClass = (Class<T>) GenericTypeResolver.resolveType(targetType, map);

        for (HttpMessageConverter<?> converter : this.messageConverters) {
            if (converter.canRead(targetClass, MediaType.APPLICATION_JSON)) {
                return ((HttpMessageConverter<T>) converter).read(targetClass, inputMessage);
            }
        }

        throw new HttpMediaTypeNotSupportedException(MediaType.APPLICATION_JSON, allSupportedMediaTypes);
    }

}
