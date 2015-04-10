package com.ufukuzun.myth.controller.jsonbinding;

import com.ufukuzun.myth.controller.jsonbinding.model.ParentModel;
import com.ufukuzun.myth.dialect.handler.annotation.AjaxRequestBody;
import com.ufukuzun.myth.dialect.handler.annotation.AjaxResponseBody;
import com.ufukuzun.myth.dialect.model.AjaxRequest;
import com.ufukuzun.myth.dialect.model.AjaxResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/jsonBindingExamples/complexJsonObjects")
public class ComplexObjectExampleController {

    private static final String VIEW_NAME = "jsonBindingExamples/complexJsonObjects";

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView showForm() {
        ModelAndView modelAndView = new ModelAndView(VIEW_NAME);
        modelAndView.addObject("parentModel", new ParentModel());
        return modelAndView;
    }

    @RequestMapping(value = "/bind", method = RequestMethod.POST)
    @AjaxResponseBody
    public AjaxResponse bindModel(@AjaxRequestBody AjaxRequest<ParentModel> ajaxRequest, ModelAndView modelAndView) {
        modelAndView.setViewName(VIEW_NAME);
        modelAndView.addObject("parentModel", ajaxRequest.getModel());
        return new AjaxResponse(ajaxRequest, modelAndView);
    }

}