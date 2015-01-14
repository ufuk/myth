package com.ufukuzun.myth.controller.jsonbinding.model;

public class ParentModel {

    private String name;

    private ChildModel child = new ChildModel();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChildModel getChild() {
        return child;
    }

    public void setChild(ChildModel child) {
        this.child = child;
    }

    @Override
    public String toString() {
        return "ParentModel{" +
                "name='" + name + '\'' +
                ", child=" + child +
                '}';
    }

}
