package com.gonsin.androidweb.samples;

/**
 * Created by Administrator on 2017/8/31.
 */

public class BeanInTestBean {

    private String name = "hjx";
    private int sex = 1;

    public BeanInTestBean(String name, int sex) {
        this.name = name;
        this.sex = sex;
    }

    public String getName() {
        return name;
    }

    public int getSex() {
        return sex;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }
}
