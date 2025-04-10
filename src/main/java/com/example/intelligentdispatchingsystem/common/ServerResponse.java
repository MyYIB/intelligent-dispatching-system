package com.example.intelligentdispatchingsystem.common;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;


import java.io.Serializable;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "通用返回实体")
public class ServerResponse<T> implements Serializable {
    @Schema(description = "返回状态码")
    private int status;
    @Schema(description = "返回消息")
    private String msg;
    @Schema(description = "返回数据")
    private T data;
    private ServerResponse(int status){
        this.status=status;
    }
    private ServerResponse(int status,String msg){
        this.status=status;
        this.msg=msg;
    }
    private ServerResponse(int status,T data){
        this.status=status;
        this.data=data;
    }
    private ServerResponse(int status,String msg,T data){
        this.status=status;
        this.msg=msg;
        this.data=data;
    }


    @JsonIgnore
    //让他不再序列化结果中
    public boolean isSuccess(){
        return  this.status==ResponseCode.SUCCESS.getCode();
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
    //成功返回status 0
    public static <T> ServerResponse<T> createSuccess(){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }
    //成功返回status 0 还有成功的信息
    public static <T> ServerResponse<T> createBySuccessMsg(String msg){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }
    //成功返回status 0返回泛型T的对象
    public static <T> ServerResponse<T> createSuccess(T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }
    //成功返回status 0返回泛型T的对象
    public static <T> ServerResponse<T> createSuccess(String msg,T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg, data);
    }
    //成功返回status 0返回泛型T的对象
    public static <T> ServerResponse<T> createSuccess(int code,String msg,T data){
        return new ServerResponse<T>(code,msg, data);
    }

    //失败返回code，和错误信息
    public static <T> ServerResponse<T> createByErrorMsg(int errorCode, String msg) {
        return new ServerResponse<T>(errorCode, msg);
    }

    //失败返回status 1 ，和错误信息
    public static <T> ServerResponse<T> createError(String msg) {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(), msg);
    }

    public static <T> ServerResponse<T> fail(String message) {
        return new ServerResponse<>(ResponseCode.ERROR.getCode(), message, null);
    }
}

