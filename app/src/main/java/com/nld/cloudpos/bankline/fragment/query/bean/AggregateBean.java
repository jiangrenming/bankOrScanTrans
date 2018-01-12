package com.nld.cloudpos.bankline.fragment.query.bean;

/**
 * Created by Terrence on 2017/2/20.
 */

public class AggregateBean {

    public String transType;
    public String totalTrans;
    public String totalAmount;
    public int type;
    public int total;
    public long amount;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getTotalTrans() {
        return totalTrans;
    }

    public void setTotalTrans(String totalTrans) {
        this.totalTrans = totalTrans;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

}
