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

package com.ufukuzun.myth.dialect.spec;

import org.thymeleaf.Configuration;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.NestableNode;
import org.thymeleaf.dom.Node;
import org.thymeleaf.fragment.IFragmentSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AttributeNameAndValueFragmentSpec implements IFragmentSpec {

    private final List<String> attributeNames;

    private final String attributeValue;

    public AttributeNameAndValueFragmentSpec(String attributeValue, String... attributeNames) {
        this.attributeNames = Arrays.asList(attributeNames);
        this.attributeValue = attributeValue;
    }

    @Override
    public List<Node> extractFragment(Configuration configuration, List<Node> nodes) {
        List<Node> extraction = new ArrayList<Node>();

        for (String eachAttributeName : attributeNames) {
            extraction = extractFragmentByAttributeNameAndValue(nodes, eachAttributeName, attributeValue);
            if (!extraction.isEmpty()) {
                break;
            }
        }

        return extraction.isEmpty() ? extraction : Arrays.asList(extraction.get(0));
    }

    private List<Node> extractFragmentByAttributeNameAndValue(List<Node> nodes, String attributeName, String attributeValue) {
        String normalizedAttributeName = Element.normalizeElementName(attributeName);

        List<Node> fragmentNodes = new ArrayList<Node>();
        for (Node node : nodes) {
            List<Node> extraction = extractFragmentFromNode(node, normalizedAttributeName, attributeValue);
            if (extraction != null) {
                fragmentNodes.addAll(extraction);
            }
        }

        return fragmentNodes;
    }

    private List<Node> extractFragmentFromNode(Node node, String attributeName, String attributeValue) {
        if (node instanceof NestableNode) {
            NestableNode nestableNode = (NestableNode) node;
            if (nestableNode instanceof Element) {
                Element element = (Element) nestableNode;
                if (attributeName != null && element.hasNormalizedAttribute(attributeName)) {
                    String elementAttrValue = element.getAttributeValue(attributeName);
                    if (elementAttrValue != null && elementAttrValue.trim().equals(attributeValue)) {
                        return Collections.singletonList((Node) nestableNode);
                    }
                }
            }

            List<Node> extraction = new ArrayList<Node>();
            List<Node> children = nestableNode.getChildren();
            for (Node child : children) {
                List<Node> childResult = extractFragmentFromNode(child, attributeName, attributeValue);
                extraction.addAll(childResult);
            }
            return extraction;
        }

        return Collections.emptyList();
    }

}
