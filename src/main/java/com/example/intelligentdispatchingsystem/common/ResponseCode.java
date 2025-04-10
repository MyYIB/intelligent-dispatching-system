package com.example.intelligentdispatchingsystem.common;

public enum ResponseCode {
    SUCCESS(200,"操作成功"),
    ERROR(204,"操作失败");
    private final int code;
    private final String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}