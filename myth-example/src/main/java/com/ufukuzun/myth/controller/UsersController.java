package com.ufukuzun.myth.controller;

import com.ufukuzun.myth.dialect.handler.annotation.AjaxRequestBody;
import com.ufukuzun.myth.dialect.handler.annotation.AjaxResponseBody;
import com.ufukuzun.myth.dialect.model.AjaxRequest;
import com.ufukuzun.myth.dialect.model.AjaxResponse;
import com.ufukuzun.myth.domain.User;
import com.ufukuzun.myth.enums.UserType;
import com.ufukuzun.myth.request.UserAjaxRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/")
public class UsersController {

    private static final List<User> userList = new ArrayList<User>();

    private static final String viewName = "userControlPanel";

    static {
        userList.add(new User(1, "Daniel", "Fernández", UserType.ADMIN));
        userList.add(new User(2, "Ufuk", "Uzun", UserType.MODERATOR));
        userList.add(new User(3, "Mr. Nobody", "Someone", UserType.STANDARD_USER));
    }

    @RequestMapping(method = RequestMethod.GET)
    public String listUsers(ModelMap modelMap) {
        modelMap.addAttribute("user", new User());
        return viewName;
    }

    @ModelAttribute("userList")
    public List<User> getUserList() {
        return userList;
    }

    @ModelAttribute("userTypes")
    public List<UserType> getUserTypes() {
        return Arrays.asList(UserType.values());
    }

    @RequestMapping(value = "/saveUser", method = RequestMethod.POST)
    @AjaxResponseBody
    public AjaxResponse saveUser(@AjaxRequestBody(validate = true, targetName = "user") UserAjaxRequest ajaxRequest, ModelAndView modelAndView) {
        modelAndView.setViewName(viewName);
        if (ajaxRequest.isModelValid()) {
            ajaxRequest.getModel().setId(getNextId());
            userList.add(ajaxRequest.getModel());
            modelAndView.addObject("user", new User());
        }
        return new AjaxResponse(ajaxRequest, modelAndView);
    }

    @RequestMapping(value = "/resetForm", method = RequestMethod.POST)
    @AjaxResponseBody
    public AjaxResponse resetForm(@AjaxRequestBody AjaxRequest<?> ajaxRequest, ModelAndView modelAndView) {
        modelAndView.setViewName(viewName);
        modelAndView.addObject("user", new User());
        return new AjaxResponse(ajaxRequest, modelAndView);
    }

    @RequestMapping(value = "/deleteUser/{id}", method = RequestMethod.POST)
    @AjaxResponseBody
    public AjaxResponse deleteUser(@AjaxRequestBody AjaxRequest<?> ajaxRequest, @PathVariable Integer id, ModelAndView modelAndView) {
        modelAndView.setViewName(viewName);
        deleteUser(id);
        return new AjaxResponse(ajaxRequest, modelAndView);
    }

    private Integer getNextId() {
        if (userList.isEmpty()) {
            return 1;
        }
        return userList.get(userList.size() - 1).getId() + 1;
    }

    private void deleteUser(Integer id) {
        User deleted = null;
        for (User user : userList) {
            if (user.getId().equals(id)) {
                deleted = user;
            }
        }
        if (deleted != null) {
            userList.remove(deleted);
        }
    }

}