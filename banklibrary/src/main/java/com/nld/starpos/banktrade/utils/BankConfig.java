package com.nld.starpos.banktrade.utils;

import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.starpos.banktrade.R;

/**
 * Created by jiangrenming on 2017/12/6.
 */

public class BankConfig {

    public static String getIpAndPortUnionPay(){
        return HttpConnetionHelper.getmContext().getResources().getString(R.string.address_set_unionpay_proc);
    }


    public final static String TPDU_UNIONPAY="6005010000";//蚂蚁企服
    public final static String UNIONPAY_PATH="cer/cupcert.pem";// 银联授权用蚂蚁企服
    public final static String LOG_LEVEL="INFO";
}
