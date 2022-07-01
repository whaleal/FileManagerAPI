package com.jinmu.xinda.util;


public enum RCode {

    UNKNOWN(0,"未知错误"),
    SUCC(200,"请求成功"),
    SUCC_LONGIN(200,"登陆成功"),
    SUCC_QUERY(200,"查询成功"),
    SUCC_DOWNLOAD(200,"查询成功"),
    SUCC_DELETE(200,"删除成功"),

    FAIL_LOGIN(2000,"登录失败"),
    FAIL_UPLOAD(0,"上传失败"),
    FAIL_DOWNLOAD(0,"获取失败"),
    TOKEN_INVALID(0,"Token已失效"),
    NOT_FOUNT(1001,"未找到该id下的成功URL"),
    FILE_NOT_FOUNT(1001,"文件未找到"),
    PASSWORD_ERR(2001,"密码错误"),
    LOGIN_SUCCESS(2000,"密码正确"),
    NOT_FOUND_USER(2002,"该账号还未注册"),
    ;


    RCode(int c, String t){
        this.code = c;
        this.text = t;
    }


    private int code;
    private String text;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
