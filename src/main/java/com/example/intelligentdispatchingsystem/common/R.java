package com.example.intelligentdispatchingsystem.common;
import lombok.Data;

import java.util.ArrayList;

@Data
public class R<T> {
    private Integer code; //编码：1成功，0和其它数字为失败
    private String msg; //错误信息
    private T data;     //第1个对象
    private T data2;    //第 2个对象
    private ArrayList<T> array; //  对象数组1
    private ArrayList<T> array2;    //对象数组2

    public static <T> R<T> ArraysSuccess(ArrayList<T> object,ArrayList<T> object2) {  //  传入两个个对象数组
        R<T> r = new R<T>();
        r.array = object;
        r.array2=object2;
        r.code = 1;
        return r;
    }

    public static <T> R<T> ArraySuccess(ArrayList<T> object) {  //  传入一个对象数组
        R<T> r = new R<T>();
        r.array = object;
        r.code = 1;
        return r;
    }

    public static <T> R<T> OneSuccess(T object) {  //  传入一个对象
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    public static <T> R<T> error(String msg) {     //   传入一个错误语句提示
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public static <T> R<T> success(String msg) {     //   传入一个错误语句提示
        R r = new R();
        r.msg = msg;
        r.code = 200;
        return r;
    }
}
