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

package com.ufukuzun.myth.dialect.bean;

import com.ufukuzun.myth.dialect.model.AjaxRequest;
import com.ufukuzun.myth.dialect.model.AjaxResponse;
import com.ufukuzun.myth.dialect.model.RequestUpdate;
import com.ufukuzun.myth.dialect.spec.AttributeNameAndValueFragmentSpec;
import com.ufukuzun.myth.dialect.util.ExpressionUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.fragment.IFragmentSpec;
import org.thymeleaf.spring4.view.ThymeleafView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Myth {

    private static final String THYMELEAF_ID_FRAGMENT_SELECTOR_ATTR_NAME = "th:id";

    private static final String STANDARD_ID_FRAGMENT_SELECTOR_ATTR_NAME = "id";

    private final static Method RENDER_FRAGMENT_METHOD;

    static {
        RENDER_FRAGMENT_METHOD = ReflectionUtils.findMethod(ThymeleafView.class, "renderFragment", IFragmentSpec.class, Map.class, HttpServletRequest.class, HttpServletResponse.class);
        RENDER_FRAGMENT_METHOD.setAccessible(true);
    }

    @Autowired
    private SessionLocaleResolver localeResolver;

    @Autowired
    private ViewResolver viewResolver;

    @Autowired
    private Validator validator;

    public <T> AjaxResponse response(AjaxRequest<T> form, ModelAndView modelAndView, HttpServletResponse response, HttpServletRequest request) {
        return response(form, modelAndView.getViewName(), modelAndView.getModelMap(), response, request);
    }

    public <T> AjaxResponse response(AjaxRequest<T> form, String viewName, ModelMap modelMap, HttpServletResponse response, HttpServletRequest request) {
        AjaxResponse ajaxResponse = new AjaxResponse();

        for (RequestUpdate eachRequestUpdate : form.getUpdate()) {
            if (StringUtils.isNotBlank(eachRequestUpdate.getRenderFragment())) {
                String decodedRenderFragment = new String(Base64.decodeBase64(eachRequestUpdate.getRenderFragment()));
                if (StringUtils.isNotBlank(decodedRenderFragment) && !eachRequestUpdate.getUpdates().isEmpty()) {
                    String processResult = process(viewName, decodedRenderFragment, modelMap, request, response);
                    Document document = parse(processResult);
                    for (String eachId : eachRequestUpdate.getUpdates()) {
                        Element element = document.getElementById(eachId);
                        String content = element != null ? element.toString() : "";
                        ajaxResponse.add(eachId, content);
                    }
                }
            }
        }

        return ajaxResponse;
    }

    private Document parse(String html) {
        Document document = new Document("");

        List<Node> nodes = Parser.parseXmlFragment(html, "");
        if (!nodes.isEmpty()) {
            document.appendChild(nodes.get(0));
        }

        return document;
    }

    private String process(final String viewName, final String fragmentSelectorAttrValue, ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
        final StringWriter htmlStringWriter = new StringWriter();

        ThymeleafView view = new ThymeleafView() {

            @Override
            public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
                HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response) {

                    @Override
                    public PrintWriter getWriter() throws IOException {
                        return new PrintWriter(htmlStringWriter);
                    }

                };

                ThymeleafView realView = (ThymeleafView) viewResolver.resolveViewName(viewName, localeResolver.resolveLocale(request));

                String selectorAttrName = ExpressionUtils.isDollarExpression(fragmentSelectorAttrValue) ? THYMELEAF_ID_FRAGMENT_SELECTOR_ATTR_NAME : STANDARD_ID_FRAGMENT_SELECTOR_ATTR_NAME;
                IFragmentSpec fragmentSpec = new AttributeNameAndValueFragmentSpec(fragmentSelectorAttrValue, selectorAttrName);

                RENDER_FRAGMENT_METHOD.invoke(realView, fragmentSpec, model, request, wrapper);
            }

        };

        try {
            view.render(modelMap, request, response);
        } catch (Exception e) {
            // TODO ufuk: log the exception, do not use "e.printStackTrace()"
            e.printStackTrace();
        }

        return htmlStringWriter.toString();
    }

    public <T> boolean validate(ModelMap modelMap, T targetBean, String targetName) {
        Set<ConstraintViolation<T>> errors = validator.validate(targetBean);
        if (errors.isEmpty()) {
            return true;
        } else {
            addBindingResultToModelMap(modelMap, errors, targetBean, targetName);
            return false;
        }
    }

    private <T> BindingResult addBindingResultToModelMap(ModelMap modelMap, Set<ConstraintViolation<T>> errors, T targetBean, String targetName) {
        BindingResult br = new BeanPropertyBindingResult(targetBean, targetName);
        for (ConstraintViolation<T> cv : errors) {
            br.rejectValue(cv.getPropertyPath().toString(), getErrorCode(cv), cv.getMessage());
        }
        modelMap.addAttribute("org.springframework.validation.BindingResult." + targetName, br);
        return br;
    }

    private <T> String getErrorCode(ConstraintViolation<T> cv) {
        String errorCode = StringUtils.replaceEach(
                cv.getMessageTemplate(),
                new String[]{"{", "org.hibernate.validator.constraints.", ".message}", "javax.validation.constraints."},
                new String[]{"", "", "", ""}
        );
        return errorCode + "." + toCamelCase(cv.getLeafBean().getClass().getSimpleName());
    }

    private String toCamelCase(String className) {
        return new StringBuilder(className).replace(0, 1, className.substring(0, 1).toLowerCase()).toString();
    }

}
