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

package com.ufukuzun.myth.dialect.builder;

import com.ufukuzun.myth.dialect.processor.MythEventAttrProcessor;
import com.ufukuzun.myth.dialect.processor.MythProcessAttrProcessor;
import com.ufukuzun.myth.dialect.processor.MythUpdateAttrProcessor;
import com.ufukuzun.myth.dialect.processor.MythUrlAttrProcessor;
import com.ufukuzun.myth.dialect.util.ElementAndAttrUtils;
import com.ufukuzun.myth.dialect.util.ExpressionUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;

import java.util.List;

public class AjaxEventBindingBuilder {

    public final static String requestUpdateFormat = "{\"renderFragment\":%s, \"updates\":[%s]}";

    public final static String ajaxEventBindingFormat = "Myth.ajax(this, {\"update\":[%s], \"process\":[%s], \"url\":\"%s\"}); return false;";

    public static String[] build(Arguments arguments, Element element, String attributeName) {
        String[] binding = new String[2];

        binding[0] = getEventString(arguments, element);
        binding[1] = String.format(
                ajaxEventBindingFormat,
                getUpdateString(arguments, element),
                getProcessString(arguments, element),
                getUrlString(arguments, element)
        );

        return binding;
    }

    private static String getEventString(Arguments arguments, Element element) {
        String event = ElementAndAttrUtils.getProcessedAttributeValue(arguments, element, MythEventAttrProcessor.ATTR_NAME_WITH_PREFIX);
        return "on" + (StringUtils.isNotBlank(event) ? event : "click");
    }

    private static String getProcessString(Arguments arguments, Element element) {
        return getProcessedFragmentIdsWithQuote(arguments, element, MythProcessAttrProcessor.ATTR_NAME_WITH_PREFIX);
    }

    private static String getUrlString(Arguments arguments, Element element) {
        return ElementAndAttrUtils.getProcessedAttributeValue(arguments, element, MythUrlAttrProcessor.ATTR_NAME_WITH_PREFIX);
    }

    private static String getUpdateString(Arguments arguments, Element element) {
        StringBuilder stringBuilder = new StringBuilder();

        String attributeValue = element.getAttributeValue(MythUpdateAttrProcessor.ATTR_NAME_WITH_PREFIX);

        List<String> renderExpressions = ExpressionUtils.extractRenderExpressions(attributeValue);
        for (String each : renderExpressions) {
            stringBuilder.append(getRequestUpdateString(arguments, ExpressionUtils.splitRenderFragmentAndUpdates(each)) + ", ");
        }

        List<String> updates = ExpressionUtils.splitIdFragments(ExpressionUtils.removeRenderExpressions(attributeValue));
        for (String each : updates) {
            stringBuilder.append(getRequestUpdateString(arguments, each) + ", ");
        }

        return StringUtils.removeEnd(stringBuilder.toString(), ", ");
    }

    private static String getRequestUpdateString(Arguments arguments, String value) {
        return getRequestUpdateString(arguments, new String[]{value, value});
    }

    private static String getRequestUpdateString(Arguments arguments, String[] array) {
        return String.format(
                requestUpdateFormat,
                getIdsWithQuote(arguments, array[0], false, true),
                getProcessedFragmentIdsWithQuote(arguments, array[1])
        );
    }

    private static String getProcessedFragmentIdsWithQuote(Arguments arguments, Element element, String attributeName) {
        return getIdsWithQuote(arguments, element.getAttributeValue(attributeName), true, false);
    }

    private static String getProcessedFragmentIdsWithQuote(Arguments arguments, String attributeValue) {
        return getIdsWithQuote(arguments, attributeValue, true, false);
    }

    private static String getIdsWithQuote(Arguments arguments, String attributeValue, boolean process, boolean encode) {
        List<String> ids = ExpressionUtils.splitIdFragments(attributeValue);

        StringBuilder stringBuilder = new StringBuilder();
        for (String each : ids) {
            String id = each;
            if (process) {
                id = ElementAndAttrUtils.getProcessedAttributeValue(arguments, each);
            }
            if (encode) {
                id = new String(Base64.encodeBase64(each.getBytes()));
            }
            stringBuilder.append("\"" + id + "\", ");
        }

        return StringUtils.removeEnd(stringBuilder.toString(), ", ");
    }

}
