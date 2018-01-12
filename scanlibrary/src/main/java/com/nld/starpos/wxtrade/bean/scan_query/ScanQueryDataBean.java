package com.nld.starpos.wxtrade.bean.scan_query;

import java.io.Serializable;

/**
 * Created by jiangrenming on 2017/9/30.
 */

public class ScanQueryDataBean implements Serializable{

    private String cseq_no;
    private String txn_typ;
    private String rn;
    private String corg_no;
    private String txn_amt;
    private  String txn_cd;
    private  String paychannel;
    private  String log_no;
    private  String txn_sts;
    private  String max_ref_amt;
    private  String ac_dt;

    public String getCseq_no() {
        return cseq_no;
    }

    public void setCseq_no(String cseq_no) {
        this.cseq_no = cseq_no;
    }

    public String getTxn_typ() {
        return txn_typ;
    }

    public void setTxn_typ(String txn_typ) {
        this.txn_typ = txn_typ;
    }

    public String getRn() {
        return rn;
    }

    public void setRn(String rn) {
        this.rn = rn;
    }

    public String getCorg_no() {
        return corg_no;
    }

    public void setCorg_no(String corg_no) {
        this.corg_no = corg_no;
    }

    public String getTxn_amt() {
        return txn_amt;
    }

    public void setTxn_amt(String txn_amt) {
        this.txn_amt = txn_amt;
    }

    public String getTxn_cd() {
        return txn_cd;
    }

    public void setTxn_cd(String txn_cd) {
        this.txn_cd = txn_cd;
    }

    public String getPaychannel() {
        return paychannel;
    }

    public void setPaychannel(String paychannel) {
        this.paychannel = paychannel;
    }

    public String getLog_no() {
        return log_no;
    }

    public void setLog_no(String log_no) {
        this.log_no = log_no;
    }

    public String getTxn_sts() {
        return txn_sts;
    }

    public void setTxn_sts(String txn_sts) {
        this.txn_sts = txn_sts;
    }

    public String getMax_ref_amt() {
        return max_ref_amt;
    }

    public void setMax_ref_amt(String max_ref_amt) {
        this.max_ref_amt = max_ref_amt;
    }

    public String getAc_dt() {
        return ac_dt;
    }

    public void setAc_dt(String ac_dt) {
        this.ac_dt = ac_dt;
    }
}
