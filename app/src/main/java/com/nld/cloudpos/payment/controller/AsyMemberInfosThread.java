package com.nld.cloudpos.payment.controller;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.nld.cloudpos.util.CommonContants;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.xutils.AsyncRequestCallBack;
import com.nld.starpos.banktrade.bean.ResultStatus;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.banktrade.utils.TransParams;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.http.AsyncHttpUtil;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.jsonUtils.DataAnalysisByJson;
import com.nld.starpos.wxtrade.utils.params.CommonParams;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;

import java.util.TreeMap;

import common.StringUtil;

/**
 * Created by jiangrenming on 2017/10/27.
 * 商终信息同步
 */

public class AsyMemberInfosThread extends ComonThread {

    private ResultStatus result;
    public AsyMemberInfosThread(Context context, Handler handler) {
        super(context, handler);
        result = new ResultStatus();
    }

    @Override
    public void run() {
        TreeMap<String,String> asyMembers = new TreeMap<>();
        asyMembers.put(CommonParams.TYPE, TransParamsValue.AntCompanyInterfaceType.SCAN_SHOP_ID); //扫码接口类型
        String posSn = ShareScanPreferenceUtils.getString(context, CommonParams.POSSn, null);
        if (!StringUtil.isEmpty(posSn)) {
            asyMembers.put(CommonParams.SN, posSn);  //终端序列号sn
        }

        ScanPayBean bean = new ScanPayBean();
        String transNo = ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO); //流水号
        bean.setRequestId(System.currentTimeMillis() + transNo);
        bean.setTransType(TransParamsValue.AntCompanyInterfaceType.SCAN_SHOP_ID);
        bean.setRequestUrl(CommonContants.url);

 //       AsyncHttpUtil.setCommonBean(bean);

        AsyncHttpUtil.httpPostXutils(asyMembers,bean,new AsyncRequestCallBack<String>(){
            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
                super.onFailure(httpException, errorMsg);
                result.setSucess(false);
                result.setTransCode(TransConstans.TRANS_CODE_MERNOINFO);
                Cache.getInstance().setRestatus(result);
                dealException(httpException,result);
                sendMessage(handler.obtainMessage(0x01));
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                super.onSuccess(responseInfo);
                if (!StringUtil.isEmpty(responseInfo.result)){
                    LogUtils.i("终端查询获取数据2:"+ responseInfo.result);
                    BindDownInfos  bindDownInfos = DataAnalysisByJson.getInstance().getObjectByString(responseInfo.result, BindDownInfos.class);
                    if (bindDownInfos != null){
                        if (TransUtils.isExitLocalData()) {  //本地存在数据
                            if (!isSameData(bindDownInfos)) {  //数据是否相同
                                //删除本地数据库
                                TransUtils.clearWater();
                                saveBindInfos(bindDownInfos);
                            }
                        } else {
                            saveBindInfos(bindDownInfos);
                        }
                        sendMessage(handler.obtainMessage(0x12));
                    }else {
                        result.setErrMsg("解析数据有误");
                        result.setSucess(false);
                        result.setTransCode(TransConstans.TRANS_CODE_MERNOINFO);
                        Cache.getInstance().setRestatus(result);
                        sendMessage(handler.obtainMessage(0x01));
                    }
                }else {
                    result.setRetCode("302");
                    result.setErrMsg("返回数据有误");
                    result.setTransCode(TransConstans.TRANS_CODE_MERNOINFO);
                    result.setSucess(false);
                    Cache.getInstance().setRestatus(result);
                    sendMessage(handler.obtainMessage(0x01));
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                sendMessage(handler.obtainMessage(0x11,"商终信息同步中"));
            }
        });
    }


    /**
     * 判断本地的数据是否相同
     *
     * @return
     */
    public boolean isSameData(BindDownInfos bindDownInfos) {
    //    String posMeCid = bindDownInfos.getUnionMercId(); //银行商户号
   //     posMeCid = (TextUtils.isEmpty(posMeCid) ? "" : posMeCid);
        String posMeCid = ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID);
        String payMercId = bindDownInfos.getPayMercId(); //扫码商户号
        payMercId = (TextUtils.isEmpty(payMercId) ? "" : payMercId);
        String terminalNo = bindDownInfos.getTerminalNo(); //终端号
        String data1 = posMeCid + payMercId + terminalNo;
        LogUtils.i("网络数据data1=" + data1 + "/posMeCid=" + posMeCid + "/payMercId=" + payMercId + "/terminalNo=" + terminalNo);
        String bankMerchantId = ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID);
        String scanMerchantId = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID);
        scanMerchantId = (TextUtils.isEmpty(scanMerchantId) ? "" : scanMerchantId);
        String terminal = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID);
        String data2 = bankMerchantId + scanMerchantId + terminal;
        LogUtils.i("网络数据data2=" + data2 + "/bankMerchantId=" + bankMerchantId + "/scanMerchantId=" + scanMerchantId + "/terminal=" + terminal);
        return data1.equals(data2);
    }

    public void saveBindInfos(BindDownInfos bindDownInfos) {
        try {
            //更新本地数据
            ScanParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.MD5_KEY, bindDownInfos.getMd5Key());  //md5
            ScanParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID, bindDownInfos.getTerminalNo()); //终端号
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.PARAMS_KEY_CARD_ACCOUNT, bindDownInfos.getPosStlAc()); //银行卡结算账号
            ScanParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_QR_CODE_ACCOUNT, bindDownInfos.getPayStlAc()); //扫码结算账号
            ScanParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID, bindDownInfos.getPayMercId()); //扫码商户号
            ScanParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME, bindDownInfos.getMercNm()); //商户名称
            ScanParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_SHOP_ID, bindDownInfos.getStoreNo()); //门店号
            /*if (StringUtil.isEmpty(bindDownInfos.getUnionMercId()) || StringUtil.isEmpty(bindDownInfos.getUnionTrmNo() )){
                ToastUtils.showToast("银行卡商终信息无法获取,请稍后重试");
                ParamsUtil.getInstance(context).update(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID,"");  //银联商户号
                ParamsUtil.getInstance(context).update(ParamsConts.BindParamsContns.UNIONPAY_TERMID,""); //银联终端号
            }else {
                ParamsUtil.getInstance(context).update(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID, bindDownInfos.getUnionMercId());  //银联商户号
                ParamsUtil.getInstance(context).update(ParamsConts.BindParamsContns.UNIONPAY_TERMID, bindDownInfos.getUnionTrmNo()); //银联终端号
            }*/
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID, Constant.MER_NO);  //银联商户号
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.UNIONPAY_TERMID, Constant.TERM_ID); //银联终端号
            //更新配置数据库信息
            ParamsUtil.getInstance().update(ParamsConts.TransParamsContns.TYANS_BATCHNO, "000001"); //银行卡批次号
           // ParamsUtil.getInstance().update(ParamsConts.TransParamsContns.SYSTRANCE_NO, "000001"); //银行卡流水号
            ScanParamsUtil.getInstance().update(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO, "000001"); //扫码批次号
           ScanParamsUtil.getInstance().update(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO, "000001"); //扫码流水号

            //签到,参数更新状态
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.PARAMS_CAVERSION, "00"); //IC卡公钥
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.PARAMS_PARAMVERSION, "00"); //IC卡参数
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.PARAMS_UPDATASTATUS, "0"); //更新状态
            ParamsUtil.getInstance().update(ParamsConts.SIGN_SYMBOL, TransParams.SingValue.UnSingedValue); //签到更为未签到
            //参数传递
            ShareScanPreferenceUtils.putBoolean(context, TransParamsValue.PARAMS_IS_PARAM_DOWN, true);

        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("终端查询更新数据异常:"+e.getMessage());
        }
    }
    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }
}
