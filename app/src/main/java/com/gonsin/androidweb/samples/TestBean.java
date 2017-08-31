package com.gonsin.androidweb.samples;

import java.util.logging.StreamHandler;

/**
 * Created by monday on 2017/8/10.
 */

public class TestBean {


    private String info;

    private int roomId;

    private BeanInTestBean beanInTestBean;

    public TestBean(String info,int roomId) {
        this.info = info;
        this.roomId = roomId;
    }

    public String getInfo() {
        return info;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String show(){
        return info;
    }

    public void setBeanInTestBean(BeanInTestBean beanInTestBean) {
        this.beanInTestBean = beanInTestBean;
    }

    public BeanInTestBean getBeanInTestBean() {
        return beanInTestBean;
    }

}
