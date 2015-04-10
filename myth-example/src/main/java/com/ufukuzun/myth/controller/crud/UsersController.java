package com.ufukuzun.myth.controller.crud;

import com.ufukuzun.myth.controller.crud.domain.User;
import com.ufukuzun.myth.controller.crud.enums.UserType;
import com.ufukuzun.myth.dialect.handler.annotation.AjaxRequestBody;
import com.ufukuzun.myth.dialect.handler.annotation.AjaxResponseBody;
import com.ufukuzun.myth.dialect.model.AjaxRequest;
import com.ufukuzun.myth.dialect.model.AjaxResponse;
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
@RequestMapping("/crudExamples/userControlPanel")
public class UsersController {

    private static final List<User> USERS = new ArrayList<User>();

    private static final String VIEW_NAME = "crudExamples/userControlPanel";

    static {
        USERS.add(new User(1, "Daniel", "Fernández", UserType.ADMIN));
        USERS.add(new User(2, "Ufuk", "Uzun", UserType.MODERATOR));
        USERS.add(new User(3, "Mr. Nobody", "Someone", UserType.STANDARD_USER));
    }

    @RequestMapping(method = RequestMethod.GET)
    public String listUsers(ModelMap modelMap) {
        modelMap.addAttribute("user", new User());
        return VIEW_NAME;
    }

    @ModelAttribute("userList")
    public List<User> getUserList() {
        return USERS;
    }

    @ModelAttribute("userTypes")
    public List<UserType> getUserTypes() {
        return Arrays.asList(UserType.values());
    }

    @RequestMapping(value = "/saveUser", method = RequestMethod.POST)
    @AjaxResponseBody
    public AjaxResponse saveUser(@AjaxRequestBody(validate = true, targetName = "user") AjaxRequest<User> ajaxRequest, ModelAndView modelAndView) {
        modelAndView.setViewName(VIEW_NAME);
        if (ajaxRequest.isValid()) {
            ajaxRequest.getModel().setId(getNextId());
            USERS.add(ajaxRequest.getModel());
            modelAndView.addObject("user", new User());
        }
        return new AjaxResponse(ajaxRequest, modelAndView);
    }

    @RequestMapping(value = "/resetForm", method = RequestMethod.POST)
    @AjaxResponseBody
    public AjaxResponse resetForm(@AjaxRequestBody AjaxRequest<?> ajaxRequest, ModelAndView modelAndView) {
        modelAndView.setViewName(VIEW_NAME);
        modelAndView.addObject("user", new User());
        return new AjaxResponse(ajaxRequest, modelAndView);
    }

    @RequestMapping(value = "/deleteUser/{id}", method = RequestMethod.POST)
    @AjaxResponseBody
    public AjaxResponse deleteUser(@AjaxRequestBody AjaxRequest<?> ajaxRequest, @PathVariable Integer id, ModelAndView modelAndView) {
        modelAndView.setViewName(VIEW_NAME);
        deleteUser(id);
        return new AjaxResponse(ajaxRequest, modelAndView);
    }

    private Integer getNextId() {
        if (USERS.isEmpty()) {
            return 1;
        }
        return USERS.get(USERS.size() - 1).getId() + 1;
    }

    private void deleteUser(Integer id) {
        User deleted = null;
        for (User user : USERS) {
            if (user.getId().equals(id)) {
                deleted = user;
            }
        }
        if (deleted != null) {
            USERS.remove(deleted);
        }
    }

}