package com.nld.starpos.wxtrade.http;
import android.util.Log;
import com.nld.netlibrary.xutils.*;
import  com.nld.netlibrary.xutils.AsyncRequestCallBack;
import com.nld.starpos.wxtrade.bean.scan_common.CommonBean;
import com.nld.starpos.wxtrade.utils.params.CommonParams;
import com.nld.starpos.wxtrade.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;
import java.util.Map;
import java.util.TreeMap;

import common.MD5Util;
import common.StringUtil;

import static android.content.ContentValues.TAG;

/**
 * @author jiangrenming
 */
public class AsyncHttpUtil {

    /**
     * 请求入口
     * @param params
     * @param callBack
     */
    public static void  httpPostXutils(TreeMap<String, String> params, CommonBean commonBean,AsyncRequestCallBack<String> callBack){
        Log.i("请求url路径",commonBean.getRequestUrl());
        commonBean.init(commonBean.getProjectType());
        TreeMap<String, String> sendParams = sendParams(params,commonBean);
        AsyHttpXUtilsHelper.getInstance().asyXUtils(commonBean.getRequestUrl(),sendParams,callBack);
    }

    /**
     * 拼接参数
     * @param sendParms
     * @param commonBean
     * @return
     */
    private static TreeMap<String ,String> sendParams(TreeMap<String,String> sendParms, CommonBean commonBean){

        if (EncodingEmun.maCaoProject.getType().equals(commonBean.getProjectType())){
            sendParms.put(CommonParams.TRANS_CURRENCY, commonBean.getCurrency());
            sendParms.put(CommonParams.TRANS_DATE, commonBean.getChlDate());
        }
        sendParms.put(CommonParams.CHARACTER_SET, commonBean.getCharacterSet());
        sendParms.put(CommonParams.IP_ADDRESS, commonBean.getIpAddress());
        sendParms.put(CommonParams.REQEUST_ID, commonBean.getRequestId());
        sendParms.put(CommonParams.SIGN_TYPE, commonBean.getSignType());
        sendParms.put(CommonParams.VERSION, commonBean.getVersion());
        sendParms.put(CommonParams.OPR_ID, commonBean.getOprId());

        if (!TransParamsValue.AntCompanyInterfaceType.SCAN_SHOP_ID.equals(commonBean.getTransType())){

            sendParms.put(CommonParams.MERCHANT_ID, commonBean.getMerchantId());
            String md5_key= commonBean.getMd5_key();
            StringBuffer sb = new StringBuffer();
            for (Map.Entry<String, String> item : sendParms.entrySet()) {
                sb.append(item.getKey()).append("=").append(item.getValue()).append("&");
            }
            try {
                if (!StringUtil.isEmpty(md5_key)) {
                    sb.append("key=" + md5_key);
                }
                Log.e(TAG, "原文：  " + sb.toString());
                String hmac = MD5Util.addHmac(sb.toString());
                sendParms.put(CommonParams.H_MAC, hmac);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }else {
            sendParms.put(CommonParams.MERCHANT_ID, "000000000000000");
        }
        return sendParms;
    }
}
