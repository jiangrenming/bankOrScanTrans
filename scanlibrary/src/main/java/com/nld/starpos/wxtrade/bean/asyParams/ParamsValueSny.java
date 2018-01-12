package com.nld.starpos.wxtrade.bean.asyParams;

import java.io.Serializable;

/**
 * Created by jiangrenming on 2017/10/16.
 * 业务参数返回的数据
 */

public class ParamsValueSny implements Serializable{

    private String returnCode;
    private String message;
    private String trmTyp;
    private String timeOut;
    private String retryNum;
    private String phNo1;
    private String phNo2;
    private String phNo3;
    private String phNoMng;
    private String supTip;
    private String tipPer;
    private String supInputCard;
    private String supAutOut;
    private String merNam;
    private String retranNm;
    private String keyIndex;
    private String txnList;
    private String servList;

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

    public String getTrmTyp() {
        return trmTyp;
    }

    public void setTrmTyp(String trmTyp) {
        this.trmTyp = trmTyp;
    }

    public String getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }

    public String getRetryNum() {
        return retryNum;
    }

    public void setRetryNum(String retryNum) {
        this.retryNum = retryNum;
    }

    public String getPhNo1() {
        return phNo1;
    }

    public void setPhNo1(String phNo1) {
        this.phNo1 = phNo1;
    }

    public String getPhNo2() {
        return phNo2;
    }

    public void setPhNo2(String phNo2) {
        this.phNo2 = phNo2;
    }

    public String getPhNo3() {
        return phNo3;
    }

    public void setPhNo3(String phNo3) {
        this.phNo3 = phNo3;
    }

    public String getPhNoMng() {
        return phNoMng;
    }

    public void setPhNoMng(String phNoMng) {
        this.phNoMng = phNoMng;
    }

    public String getSupTip() {
        return supTip;
    }

    public void setSupTip(String supTip) {
        this.supTip = supTip;
    }

    public String getTipPer() {
        return tipPer;
    }

    public void setTipPer(String tipPer) {
        this.tipPer = tipPer;
    }

    public String getSupInputCard() {
        return supInputCard;
    }

    public void setSupInputCard(String supInputCard) {
        this.supInputCard = supInputCard;
    }

    public String getSupAutOut() {
        return supAutOut;
    }

    public void setSupAutOut(String supAutOut) {
        this.supAutOut = supAutOut;
    }

    public String getMerNam() {
        return merNam;
    }

    public void setMerNam(String merNam) {
        this.merNam = merNam;
    }

    public String getRetranNm() {
        return retranNm;
    }

    public void setRetranNm(String retranNm) {
        this.retranNm = retranNm;
    }

    public String getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(String keyIndex) {
        this.keyIndex = keyIndex;
    }

    public String getTxnList() {
        return txnList;
    }

    public void setTxnList(String txnList) {
        this.txnList = txnList;
    }

    public String getServList() {
        return servList;
    }

    public void setServList(String servList) {
        this.servList = servList;
    }
}
