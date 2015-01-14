package com.ufukuzun.myth.controller.jsonbinding.model;

public class ChildModel {

    private String name;

    private LeafModel leaf = new LeafModel();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LeafModel getLeaf() {
        return leaf;
    }

    public void setLeaf(LeafModel leaf) {
        this.leaf = leaf;
    }

    @Override
    public String toString() {
        return "ChildModel{" +
                "name='" + name + '\'' +
                ", leaf=" + leaf +
                '}';
    }

}
