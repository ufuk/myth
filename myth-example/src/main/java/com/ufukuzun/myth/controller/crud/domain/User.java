package com.ufukuzun.myth.controller.crud.domain;

import com.ufukuzun.myth.controller.crud.enums.UserType;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class User {

    private Integer id;

    @NotEmpty
    @Size(max = 10)
    private String firstname;

    @NotEmpty
    @Size(max = 10)
    private String lastname;

    @NotNull
    private UserType type = UserType.STANDARD_USER;

    public User() {
    }

    public User(Integer id, String firstname, String lastname, UserType type) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        setType(type);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public UserType getType() {
        return type;
    }

    public void setType(UserType type) {
        this.type = (type != null) ? type : UserType.STANDARD_USER;
    }

}
