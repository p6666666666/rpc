package com.czp.enumeration;

public enum RequestType {


    REQUEST((byte)1,"普通请求"),HEART_BRAT((byte)2,"心跳检测请求");
    private byte id;
    private String type;

    RequestType(byte id, String type) {
        this.id=id;
        this.type=type;
    }

    public byte getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
