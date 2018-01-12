package com.nld.starpos.wxtrade.debug.utils.params;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import com.lidroid.xutils.util.LogUtils;
import com.nld.starpos.wxtrade.R;
import com.nld.starpos.wxtrade.debug.bean.scan_query.ScanQueryDataBean;
import com.nld.starpos.wxtrade.debug.bean.scan_query.ScanQueryWater;
import com.nld.starpos.wxtrade.debug.local.db.ScanTransDao;
import com.nld.starpos.wxtrade.debug.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.debug.local.db.imp.ScanTransDaoImp;
import com.nld.starpos.wxtrade.debug.utils.ShareScanPreferenceUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import common.MD5Util;
import common.StringUtil;

/**
 * Created by jiangrenming on 2017/11/2.
 */

public class ScanTransUtils {


    /**
     * 获取引起结算中断的步骤标志。
     *
     * @return
     */
    public static String getSettleHaltStep(Context context) {
        String settleHaltStep = "";
        if (ShareScanPreferenceUtils.getBoolean( context, TransParamsValue.SettleConts.SETTLE_ALL_FLAG,false)) {
            if (ShareScanPreferenceUtils.getBoolean(context, TransParamsValue.SettleConts.PARAMS_IS_SCAN_SETTLT_HALT,false)) {
                settleHaltStep = "[1]";
            } else if (ShareScanPreferenceUtils.getBoolean(context, TransParamsValue.SettleConts.PARAMS_IS_PRINT_SETTLE_HALT,false)) {
                settleHaltStep = "[2]";
            } else if (ShareScanPreferenceUtils.getBoolean(context, TransParamsValue.SettleConts.PARAMS_IS_PRINT_ALLWATER_HALT,false)) {
                settleHaltStep = "[3]";
            } else if (ShareScanPreferenceUtils.getBoolean(context, TransParamsValue.SettleConts.PARAMS_IS_CLEAR_SETTLT_HLAT,false)) {
                settleHaltStep = "[4]";
            }
        }
        return settleHaltStep;
    }

    /**
     * 扫码批结后的操作
     */
    public  static  void clearWaterForScanTrans(Context context){
        try{
            //清除扫码相关数据
            ScanTransDao dao = new ScanTransDaoImp();
            dao.clearScanWater();
            ShareScanPreferenceUtils.clearData(context, TransParamsValue.SettleConts.PARAMS_SETTLE_DATA);
            ShareScanPreferenceUtils.clearData(context, TransParamsValue.SettleConts.PARAMS_SETTLE_TIME);
            //清除结算数据的标志
            ShareScanPreferenceUtils.putBoolean(context, TransParamsValue.SettleConts.PARAMS_IS_CLEAR_SETTLT_HLAT,false);
            //中断总标志
            ShareScanPreferenceUtils.putBoolean(context, TransParamsValue.SettleConts.SETTLE_ALL_FLAG,false);
        } catch (Exception e){
            e.printStackTrace();
            LogUtils.e("清除流水数据异常:"+e.getMessage());
        }
    }

    @SuppressLint("DefaultLocale")
    public static  boolean checkHmac(String response) {
        LogUtils.i("进入返回值mac验证");
        String[] strings = response.split("&hmac=");
        String error;
        if (strings.length < 2) {
            error = "没有返回hmac校验值";
            return false;
        }
        String data = strings[0];
        String hmac = strings[1];
        if (TextUtils.isEmpty(hmac)) {
            error = "没有返回hmac校验值";
            return false;
        }
        strings = data.split("&");
        Arrays.sort(strings);
        data = "";
        for (String str : strings) {
            data += str + "&";
        }
        data = data.substring(0, data.length() - 1);
        data = data + "&key=" + ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.MD5_KEY);
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bResult = md5.digest(data.getBytes());
            String result = MD5Util.bcdToString(bResult).toUpperCase();
            LogUtils.d(data + "\n--MD5加密-->" + result);
            if (hmac.equals(result)) {
                return true;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        error = "返回数据hmac验证失败";
        return false;
    }

    //交易结果类型定义
    public static final String S = "S";       //交易成功
    public static final String F = "F";       //交易失败
    public static final String A = "A";       //等待授权
    public static final String D = "D";       //交易撤销
    public static final String Z = "";        //交易未知
    public static String setTextStyle(Context context, TextView txt, Object obj) {
        if (obj instanceof ScanQueryWater) {
            ScanQueryWater scanWater = (ScanQueryWater) obj;
            if (scanWater != null && !StringUtil.isEmpty(scanWater.getTxnSts())) {
                String txnSts = scanWater.getTxnSts();
                if (S.equals(txnSts)) {
                    if (scanWater.getMAX_REF_AMT() == 0) {
                        txt.setTextColor(context.getResources().getColor(R.color.green_83C561));
                        return "已撤销";
                    }
                    txt.setTextColor(context.getResources().getColor(R.color.green_83C561));
                    return "交易成功";
                } else if (F.equals(txnSts)) {
                    txt.setTextColor(context.getResources().getColor(R.color.red_FB5D5D));
                    return "交易失败";
                } else if (A.equals(txnSts)) {
                    txt.setTextColor(context.getResources().getColor(R.color.gray_8F8F8F));
                    return "等待授权";
                } else if (D.equals(txnSts)) {
                    txt.setTextColor(context.getResources().getColor(R.color.gray_8F8F8F));
                    return "交易撤销";
                } else {
                    txt.setTextColor(context.getResources().getColor(R.color.gray_8F8F8F));
                    return "交易未知";
                }
            }
        } else if (obj instanceof ScanQueryDataBean) {
            ScanQueryDataBean scanQuery = (ScanQueryDataBean) obj;
            if (scanQuery != null && !StringUtil.isEmpty(scanQuery.getTxn_sts())) {
                String txnSts = scanQuery.getTxn_sts();
                if (S.equals(txnSts)) {
                    if (scanQuery.getMax_ref_amt().equals("0")) {
                        txt.setTextColor(context.getResources().getColor(R.color.green_83C561));
                        return "已撤销";
                    }
                    txt.setTextColor(context.getResources().getColor(R.color.green_83C561));
                    return "交易成功";
                } else if (F.equals(txnSts)) {
                    txt.setTextColor(context.getResources().getColor(R.color.red_FB5D5D));
                    return "交易失败";
                } else if (A.equals(txnSts)) {
                    txt.setTextColor(context.getResources().getColor(R.color.gray_8F8F8F));
                    return "等待授权";
                } else if (D.equals(txnSts)) {
                    txt.setTextColor(context.getResources().getColor(R.color.gray_8F8F8F));
                    return "交易撤销";
                } else {
                    txt.setTextColor(context.getResources().getColor(R.color.gray_8F8F8F));
                    return "交易未知";
                }
            }
        } else {
            if (S.equals(obj)) {
                txt.setTextColor(context.getResources().getColor(R.color.green_83C561));
                return "交易成功";
            } else if (F.equals(obj)) {
                txt.setTextColor(context.getResources().getColor(R.color.red_FB5D5D));
                return "交易失败";
            } else if (A.equals(obj)) {
                txt.setTextColor(context.getResources().getColor(R.color.gray_8F8F8F));
                return "等待授权";
            } else if (D.equals(obj)) {
                txt.setTextColor(context.getResources().getColor(R.color.gray_8F8F8F));
                return "交易撤销";
            } else {
                txt.setTextColor(context.getResources().getColor(R.color.gray_8F8F8F));
                return "交易未知";
            }
        }
        return null;
    }
}
