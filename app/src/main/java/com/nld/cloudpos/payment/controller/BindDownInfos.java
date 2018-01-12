package com.nld.cloudpos.payment.controller;

import java.io.Serializable;

/**
 * Created by jiangrenming on 2017/9/23.
 * 绑定终端下发的信息
 */

public class BindDownInfos implements Serializable {
    /**
     * 终端查询返回的字段
     */
    private String mercId;    //内部商户号
    private String mercNm; //商户名
    private String terminalNo; //终端编号
    private String storeNo;    //门店号
    private String payMercId;    //扫码商户号
    private String posMercId;    //银行卡收单商户号
    private String payStlAc;    //扫码结算账号
    private String posStlAc;    //银行卡收单结算账号
    private String trmSts;    //绑定状态
    private String unionTrmNo; //银联终端号
    private String unionMercId; //银联商户号

    /**
     * 终端绑定 多 返回的子段
     */
    private String TMK; //主密钥
    private String TmkCkvalue; //主密钥校验值
    private String md5Key; //MD5密钥

    public String getUnionTrmNo() {
        return unionTrmNo;
    }

    public void setUnionTrmNo(String unionTrmNo) {
        this.unionTrmNo = unionTrmNo;
    }

    public String getUnionMercId() {
        return unionMercId;
    }

    public void setUnionMercId(String unionMercId) {
        this.unionMercId = unionMercId;
    }

    public String getMercNm() {
        return mercNm;
    }

    public void setMercNm(String mercNm) {
        this.mercNm = mercNm;
    }

    public String getTerminalNo() {
        return terminalNo;
    }

    public void setTerminalNo(String terminalNo) {
        this.terminalNo = terminalNo;
    }

    public String getMercId() {
        return mercId;
    }

    public void setMercId(String mercId) {
        this.mercId = mercId;
    }

    public String getStoreNo() {
        return storeNo;
    }

    public void setStoreNo(String storeNo) {
        this.storeNo = storeNo;
    }

    public String getPayMercId() {
        return payMercId;
    }

    public void setPayMercId(String payMercId) {
        this.payMercId = payMercId;
    }

    public String getPosMercId() {
        return posMercId;
    }

    public void setPosMercId(String posMercId) {
        this.posMercId = posMercId;
    }

    public String getPayStlAc() {
        return payStlAc;
    }

    public void setPayStlAc(String payStlAc) {
        this.payStlAc = payStlAc;
    }

    public String getPosStlAc() {
        return posStlAc;
    }

    public void setPosStlAc(String posStlAc) {
        this.posStlAc = posStlAc;
    }

    public String getMd5Key() {
        return md5Key;
    }

    public void setMd5Key(String md5Key) {
        this.md5Key = md5Key;
    }

    public String getTMK() {
        return TMK;
    }

    public void setTMK(String TMK) {
        this.TMK = TMK;
    }

    public String getTrmSts() {
        return trmSts;
    }

    public void setTrmSts(String trmSts) {
        this.trmSts = trmSts;
    }

    public String getTmkCkvalue() {
        return TmkCkvalue;
    }

    public void setTmkCkvalue(String tmkCkvalue) {
        TmkCkvalue = tmkCkvalue;
    }
}
