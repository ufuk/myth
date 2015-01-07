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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;

import com.ufukuzun.myth.dialect.processor.MythEventAttrProcessor;
import com.ufukuzun.myth.dialect.processor.MythProcessAttrProcessor;
import com.ufukuzun.myth.dialect.processor.MythUpdateAttrProcessor;
import com.ufukuzun.myth.dialect.processor.MythUrlAttrProcessor;
import com.ufukuzun.myth.dialect.util.ElementAndAttrUtils;
import com.ufukuzun.myth.dialect.util.ExpressionUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Base64.class, ElementAndAttrUtils.class, ExpressionUtils.class, Element.class, Arguments.class})
public class AjaxEventBindingBuilderTest {

	private Element element;
	
	@Before
	public void before() {
		mockStatic(Base64.class, ElementAndAttrUtils.class, ExpressionUtils.class);
		element = mock(Element.class);
	}
	
	@Test
	public void shouldBuildAjaxEventBinding() {
		byte[] renderFragmentEncoded = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
		when(Base64.encodeBase64("newUserForm".getBytes())).thenReturn(renderFragmentEncoded);
		
		when(ElementAndAttrUtils.getPrefixedName(MythEventAttrProcessor.ATTR_NAME)).thenReturn("myth:event");
		when(ElementAndAttrUtils.getPrefixedName(MythUrlAttrProcessor.ATTR_NAME)).thenReturn("myth:url");
		when(ElementAndAttrUtils.getPrefixedName(MythUpdateAttrProcessor.ATTR_NAME)).thenReturn("myth:update");
		when(ElementAndAttrUtils.getPrefixedName(MythProcessAttrProcessor.ATTR_NAME)).thenReturn("myth:process");
		
		when(ElementAndAttrUtils.getProcessedAttributeValue(null, element, MythEventAttrProcessor.ATTR_NAME_WITH_PREFIX)).thenReturn("click");
		when(ElementAndAttrUtils.getProcessedAttributeValue(null, element, MythUrlAttrProcessor.ATTR_NAME_WITH_PREFIX)).thenReturn("/save");
		
		when(ElementAndAttrUtils.getProcessedAttributeValue(null, "newUserForm")).thenReturn("newUserForm");
		when(element.getAttributeValue(MythUpdateAttrProcessor.ATTR_NAME_WITH_PREFIX)).thenReturn("newUserForm");
		when(ExpressionUtils.splitIdFragments("newUserForm")).thenReturn(Arrays.asList("newUserForm"));
		when(ExpressionUtils.removeRenderExpressions("newUserForm")).thenReturn("newUserForm");
		
		when(ElementAndAttrUtils.getProcessedAttributeValue(null, "newUserForm")).thenReturn("newUserForm");
		when(element.getAttributeValue(MythProcessAttrProcessor.ATTR_NAME_WITH_PREFIX)).thenReturn("newUserForm");
		when(ExpressionUtils.splitIdFragments("newUserForm")).thenReturn(Arrays.asList("newUserForm"));
		
		String[] binding = AjaxEventBindingBuilder.build(null, element, MythUpdateAttrProcessor.ATTR_NAME_WITH_PREFIX);
		
		assertThat(binding[0], equalTo("onclick"));
		assertThat(binding[1], 
			equalTo(String.format(
					"Myth.ajax(this, {\"update\":[{\"renderFragment\":\"%s\", \"updates\":[\"newUserForm\"]}], \"process\":[\"newUserForm\"], \"url\":\"/save\"}); return false;",
					new String(renderFragmentEncoded)
				)
			)
		);
	}
	
}
