package com.gonsin.androidweb.samples;

/**
 * Created by monday on 2017/8/10.
 */

public class TestBean {

    // some info

    private String info;

    private int roomId;

    public TestBean(String info, int roomId) {
        this.info = info;
        this.roomId = roomId;
    }

    public String getInfo() {
        return info;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
}
