package com.nld.starpos.wxtrade.debug.bean.scan_common;

import com.nld.starpos.wxtrade.debug.http.ApiTools;
import com.nld.starpos.wxtrade.debug.utils.params.EncodingEmun;

import java.io.Serializable;

import common.DateTimeUtil;


/**
 * Created by jiangrenming on 2017/9/22.
 * 所有请求的共同体
 */

public class CommonBean implements Serializable {
    //开启扫码时需要
    private String scanResult;
    private boolean isSucess;
    //请求时的请求体
    private String characterSet;
    private String ipAddress; //客户机IP
    private String requestId;
    private String signType;
    private String version;
    private String hmac;
    private String oprId;
    private String telNo;
    private String merchantId;
    private String currency;
    private String chlDate; //交易日期
    private String requestUrl; //请求路径
    private String projectType ; //项目区别号
    private String md5_key;
    private String TransType;  //接口类型

    public void init(String type){
        setCharacterSet(EncodingEmun.GBK.getType()); //字符集
        setIpAddress(ApiTools.SCAN_API_IP);  //ip地址
        setSignType("MD5");
        setOprId("001");   //现在是默认的，数据库可改
        if (EncodingEmun.maCaoProject.getType().equals(type)){  //--->澳门项目
            setVersion("1.0.0");
            setCurrency(EncodingEmun.USCURRENCY.getType());
            setChlDate((DateTimeUtil.getCurrentDate(DateTimeUtil.YYYY)+DateTimeUtil.getCurrentDate()));
        }else {  //-->蚂蚁企服
            setVersion("1.0.2");
        }
    }

    public String getTransType() {
        return TransType;
    }

    public void setTransType(String transType) {
        TransType = transType;
    }

    public String getMd5_key() {
        return md5_key;
    }

    public void setMd5_key(String md5_key) {
        this.md5_key = md5_key;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getChlDate() {
        return chlDate;
    }

    public void setChlDate(String chlDate) {
        this.chlDate = chlDate;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getScanResult() {
        return scanResult;
    }

    public void setScanResult(String scanResult) {
        this.scanResult = scanResult;
    }

    public boolean isSucess() {
        return isSucess;
    }

    public void setSucess(boolean sucess) {
        isSucess = sucess;
    }

    public String getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHmac() {
        return hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }

    public String getOprId() {
        return oprId;
    }

    public void setOprId(String oprId) {
        this.oprId = oprId;
    }

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }
}
