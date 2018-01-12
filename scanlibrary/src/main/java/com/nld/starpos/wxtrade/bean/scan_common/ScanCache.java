package com.nld.starpos.wxtrade.bean.scan_common;

/**
 * Created by jiangrenming on 2017/10/30.
 */

public class ScanCache {
    private static ScanCache instance;
    private String transCode; //交易类型
    private String errCode;
    private String errMesage;
    private ResultStatus resultStatus;

    private ScanCache() {}
    public synchronized static ScanCache getInstance() {
        if (null == instance) {
            instance = new ScanCache();
        }
        return instance;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMesage() {
        return errMesage;
    }

    public void setErrMesage(String errMesage) {
        this.errMesage = errMesage;
    }

    public ResultStatus getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(ResultStatus resultStatus) {
        this.resultStatus = resultStatus;
    }
}
