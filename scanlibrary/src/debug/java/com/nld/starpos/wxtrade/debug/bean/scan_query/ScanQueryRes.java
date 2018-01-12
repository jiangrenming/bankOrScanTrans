package com.nld.starpos.wxtrade.debug.bean.scan_query;

import java.io.Serializable;
import java.util.List;

/**
 * Created by jiangrenming on 2017/9/30.
 */

public class ScanQueryRes implements Serializable{

    private String returnCode;
    private String message;
    private String type;
    private String signType;
    private String merchantId;
    private String version;
    private int pagNo;
    private int pagNum;
    private int totCnt;
    private long ordfee;
    private long amount;
    private List<ScanQueryDataBean> datas;
    private String hmac;

    public int getPagNum() {
        return pagNum;
    }

    public void setPagNum(int pagNum) {
        this.pagNum = pagNum;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getPagNo() {
        return pagNo;
    }

    public void setPagNo(int pagNo) {
        this.pagNo = pagNo;
    }

    public int getTotCnt() {
        return totCnt;
    }

    public void setTotCnt(int totCnt) {
        this.totCnt = totCnt;
    }

    public long getOrdfee() {
        return ordfee;
    }

    public void setOrdfee(long ordfee) {
        this.ordfee = ordfee;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public List<ScanQueryDataBean> getDatas() {
        return datas;
    }

    public void setDatas(List<ScanQueryDataBean> datas) {
        this.datas = datas;
    }

    public String getHmac() {
        return hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }
}
