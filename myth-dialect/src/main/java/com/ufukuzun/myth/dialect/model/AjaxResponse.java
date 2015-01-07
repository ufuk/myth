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

package com.ufukuzun.myth.dialect.model;

import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

public class AjaxResponse {

    private transient AjaxRequest<?> ajaxRequest;

    private transient ModelAndView modelAndView;

    private final List<ResponseUpdate> updates = new ArrayList<ResponseUpdate>();

    public AjaxResponse() {
    }

    public AjaxResponse(AjaxRequest<?> ajaxRequest, ModelAndView modelAndView) {
        this.ajaxRequest = ajaxRequest;
        this.modelAndView = modelAndView;
    }

    public void add(String id, String content) {
        updates.add(new ResponseUpdate(id, content));
    }

    public List<ResponseUpdate> getUpdates() {
        return updates;
    }

    public AjaxRequest<?> getAjaxRequest() {
        return ajaxRequest;
    }

    public ModelAndView getModelAndView() {
        return modelAndView;
    }

}
