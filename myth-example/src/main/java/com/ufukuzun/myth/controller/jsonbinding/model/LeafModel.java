package com.ufukuzun.myth.controller.jsonbinding.model;

public class LeafModel {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "LeafModel{" +
                "name='" + name + '\'' +
                '}';
    }

}
