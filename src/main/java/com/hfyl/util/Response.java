package com.hfyl.util;

/**
 * Created by xyj on 2016/12/30.
 */
public class Response<T> {

    private String code;
    private String msg;
    private T t;
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public T getT() {
        return t;
    }
    public void setT(T t) {
        this.t = t;
    }
    @Override
    public String toString() {
        return "Response [code=" + code + ", msg=" + msg + ", t=" + t + "]";
    }

}
