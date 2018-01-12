package com.nld.starpos.wxtrade.debug.bean.scan_batch_no;

import java.io.Serializable;

/**
 * Created by jiangrenming on 2017/10/17.
 * 批次号同步类
 */

public class AsyScanBatchNo implements Serializable{
    private String returnCode;
    private String message;
    private String batNo;
    private String terminalNo;

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

    public String getBatNo() {
        return batNo;
    }

    public void setBatNo(String batNo) {
        this.batNo = batNo;
    }

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }
}
