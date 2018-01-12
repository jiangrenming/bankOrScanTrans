package com.nld.starpos.wxtrade.debug.bean.password;

import java.io.Serializable;

/**
 * Created by wqz on 2017/10/10.
 */

public class CheckPwRsp implements Serializable{

    private String returnCode;
    private String message;
    private String rspCd;

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRspCd() {
        return rspCd;
    }

    public void setRspCd(String rspCd) {
        this.rspCd = rspCd;
    }
}
