package com.jinmu.xinda.util;

import java.util.List;

/**
 * ClassName:R
 * Package:com.jinmu.xinda.util
 * Description:
 * Date:2022/6/14 10:16
 * author:ck
 *
 * 统一的应答结果类
 */
public class RespResult {

    //应答码，自定义数字
    private int code;

    //code的文字说明，一般做提示给用户看
    private String msg;

    //访问token
    private String accessToken;

    //单个数据
    private Object obj;

//    //集合数据
//    private List list;




    //成功
    public static RespResult ok(){
        RespResult respResult = new RespResult();
        respResult.setRCode(RCode.SUCC);

        return respResult;
    }



    //失败
    public static RespResult fail(){
        RespResult respResult = new RespResult();
        respResult.setRCode(RCode.UNKNOWN);

        return respResult;
    }

    public void setRCode(RCode rCode){
        this.code = rCode.getCode();
        this.msg = rCode.getText();
    }


    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }


//    public List getList() {
//        return list;
//    }
//
//    public void setList(List list) {
//        this.list = list;
//    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }



}
