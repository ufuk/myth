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

public class AjaxEventBinding {

    private String eventAttributeName;

    private String eventAttributeValue;

    public AjaxEventBinding(String eventAttributeName, String eventAttributeValue) {
        this.eventAttributeName = eventAttributeName;
        this.eventAttributeValue = eventAttributeValue;
    }

    public String getEventAttributeName() {
        return eventAttributeName;
    }

    public String getEventAttributeValue() {
        return eventAttributeValue;
    }

}
