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

package com.ufukuzun.myth.dialect.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.Test;

public class ExpressionUtilsTest {

	@Test
	public void shouldReturnEmptyListIfAttributeValueContainsOnlySpaces() {
		assertThat(ExpressionUtils.splitIdFragments("      ").size(), equalTo(0));
	}
	
	@Test
	public void shouldReturnEmptyListIfAttributeValueEmpty() {
		assertThat(ExpressionUtils.splitIdFragments("").size(), equalTo(0));
	}
	
	@Test
	public void shouldReturnEmptyListIfAttributeValueNull() {
		assertThat(ExpressionUtils.splitIdFragments(null).size(), equalTo(0));
	}
	
	@Test
	public void shouldSplitNormalIdFragment() {
		List<String> idFragments = ExpressionUtils.splitIdFragments("abcForm xyzButton");
		
		assertThat(idFragments.size(), equalTo(2));
		assertThat(idFragments, hasItem("abcForm"));
		assertThat(idFragments, hasItem("xyzButton"));
	}
	
	@Test
	public void shouldSplitExpressionIdFragments() {
		List<String> idFragments = ExpressionUtils.splitIdFragments("${'user_' + user.id} ${deneme.id}");
		
		assertThat(idFragments.size(), equalTo(2));
		assertThat(idFragments, hasItem("${'user_' + user.id}"));
		assertThat(idFragments, hasItem("${deneme.id}"));
	}
	
	@Test
	public void shouldIgnoreEmptyExpressionIdFragments() {
		List<String> idFragments = ExpressionUtils.splitIdFragments("${} ${deneme.id}");
		
		assertThat(idFragments.size(), equalTo(1));
		assertThat(idFragments, not(hasItem("${}")));
		assertThat(idFragments, hasItem("${deneme.id}"));
	}
	
	@Test
	public void shouldSplitMixOfExpressionAndNormalIdFragments() {
		List<String> idFragments = ExpressionUtils.splitIdFragments("${'user_' + user.id} abcForm ${deneme.id} xyzButton dfgDiv");
		
		assertThat(idFragments.size(), equalTo(5));
		assertThat(idFragments, hasItem("${'user_' + user.id}"));
		assertThat(idFragments, hasItem("${deneme.id}"));
		assertThat(idFragments, hasItem("abcForm"));
		assertThat(idFragments, hasItem("xyzButton"));
		assertThat(idFragments, hasItem("dfgDiv"));
	}
	
	@Test
	public void shouldReturnTrueIfValueExpression() {
		assertThat(ExpressionUtils.isDollarExpression("${'user_' + user.id}"), equalTo(true));
	}
	
	@Test
	public void shouldReturnFalseIfValueNotExpression() {
		assertThat(ExpressionUtils.isDollarExpression("abcForm"), equalTo(false));
	}
	
	@Test
	public void shouldReturnFalseIfValueContainsMoreThanOneExpression() {
		assertThat(ExpressionUtils.isDollarExpression("${'user_' + user.id} ${deneme.id}"), equalTo(false));
	}
	
	@Test
	public void shouldReturnFalseIfValueEmptyExpression() {
		assertThat(ExpressionUtils.isDollarExpression("${}"), equalTo(false));
	}
	
	@Test
	public void shouldReturnFalseIfValueBlank() {
		assertThat(ExpressionUtils.isDollarExpression(""), equalTo(false));
		assertThat(ExpressionUtils.isDollarExpression(null), equalTo(false));
	}
	
	@Test
	public void shouldSplitRenderFragmentAndUpdates() {
		String[] result = ExpressionUtils.splitRenderFragmentAndUpdates("[[ renderFragment >> update1 update2 ]]");
		assertThat(result[0], equalTo("renderFragment"));
		assertThat(result[1], equalTo("update1 update2"));
	}
	
	@Test
	public void shouldSplitRenderFragmentAndUpdatesWhichContainsDollarExpression() {
		String[] result = ExpressionUtils.splitRenderFragmentAndUpdates("[[ ${'newUser' + 'form'} >> update1 ${user.id} ]]");
		assertThat(result[0], equalTo("${'newUser' + 'form'}"));
		assertThat(result[1], equalTo("update1 ${user.id}"));
	}
	
	@Test
	public void shouldReturnTrueIfValueRenderExpression() {
		assertThat(ExpressionUtils.isRenderExpression("[[ renderFragment >> update1 update2 ]]"), equalTo(true));
		assertThat(ExpressionUtils.isRenderExpression("[[ ${'newUser' + 'form'} >> update1 ${user.id} ]]"), equalTo(true));
	}
	
	@Test
	public void shouldReturnFalseIfValueNotRenderExpression() {
		assertThat(ExpressionUtils.isRenderExpression("[[ >> update1 update2 ]]"), equalTo(false));
		assertThat(ExpressionUtils.isRenderExpression("[[ ${'newUser' + 'form'} >> ]]"), equalTo(false));
		assertThat(ExpressionUtils.isRenderExpression("${'newUser' + 'form'}"), equalTo(false));
		assertThat(ExpressionUtils.isRenderExpression("usersTable"), equalTo(false));
		assertThat(ExpressionUtils.isRenderExpression(""), equalTo(false));
		assertThat(ExpressionUtils.isRenderExpression(null), equalTo(false));
	}
	
	@Test
	public void shouldExtractRenderExpressions() {
		List<String> result = ExpressionUtils.extractRenderExpressions("[[abc>>def]] ${user.id} [[ render >> ${update}]] update1");
		assertThat(result.size(), equalTo(2));
		assertThat(result.get(0), equalTo("[[abc>>def]]"));
		assertThat(result.get(1), equalTo("[[ render >> ${update}]]"));
	}
	
	@Test
	public void shouldReturnEmptyListIfValueBlankWhileExtractingRenderExpressions() {
		assertThat(ExpressionUtils.extractRenderExpressions("").size(), equalTo(0));
		assertThat(ExpressionUtils.extractRenderExpressions(null).size(), equalTo(0));
	}
	
	@Test
	public void shouldRemoveRenderExpressions() {
		assertThat(ExpressionUtils.removeRenderExpressions("[[abc>>def]] ${user.id} [[ render >> ${update}]] update1"), equalTo(" ${user.id}  update1"));
	}
	
	@Test
	public void shouldReturnBlankIfValueBlankWhileRemovingRenderExpressions() {
		assertThat(ExpressionUtils.removeRenderExpressions(""), equalTo(""));
		assertThat(ExpressionUtils.removeRenderExpressions(null), equalTo(null));
	}
	
}
