package com.nld.cloudpos.payment.controller;

import android.content.Context;
import android.os.Handler;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.xutils.AsyncRequestCallBack;
import com.nld.starpos.banktrade.bean.ResultStatus;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.banktrade.utils.TransParams;
import com.nld.starpos.wxtrade.bean.asyParams.ParamsValueSny;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.http.AsyncHttpUtil;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.jsonUtils.DataAnalysisByJson;
import com.nld.starpos.wxtrade.utils.params.CommonParams;
import com.nld.starpos.wxtrade.utils.params.ReturnCodeParams;
import com.nld.starpos.wxtrade.utils.params.ScanTransUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;

import java.util.TreeMap;

import common.DateTimeUtil;
import common.StringUtil;


/**
 * Created by jiangrenming on 2017/10/18.
 * 请求业务参数线程*/



public class AsyParamsThread extends ComonThread {

    private ResultStatus result;
    private ScanPayBean mScanPayBean;
    public AsyParamsThread(Context context, Handler handler, ScanPayBean scanPayBean) {
        super(context, handler);
        this.mScanPayBean = scanPayBean;
        result = new ResultStatus();
    }

    @Override
    public void run() {

        TreeMap<String,String > asyParams = new TreeMap<>();
        asyParams.put(CommonParams.TYPE, mScanPayBean.getTransType());
        asyParams.put(CommonParams.TERMINAL_NO, mScanPayBean.getTerminalNo()); //终端号


        String md5_key = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.MD5_KEY);
        if (!StringUtil.isEmpty(md5_key)){
            mScanPayBean.setMd5_key(md5_key);
        }
        String transNo = ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO); //流水号
        mScanPayBean.setRequestId(System.currentTimeMillis() + transNo);
        String merchantId = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID); //扫码商户号
        if (!StringUtil.isEmpty(merchantId)){
            mScanPayBean.setMerchantId(merchantId);
        }
    //    AsyncHttpUtil.setCommonBean(mScanPayBean);
        AsyncHttpUtil.httpPostXutils(asyParams,mScanPayBean,new AsyncRequestCallBack<String>(){
            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
                super.onFailure(httpException, errorMsg);
                result.setRetCode("301");
                result.setErrMsg(errorMsg);
                result.setSucess(false);
                result.setTransCode(
                        TransConstans.TRANS_CODE_PARAMS);
                Cache.getInstance().setRestatus(result);
                dealException(httpException,result);
                sendEmptyMessage(0x01);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (!ScanTransUtils.checkHmac(responseInfo.result)){
                    ToastUtils.showToast("没有返回hmac校验值或验证失败");
                }
                super.onSuccess(responseInfo);
                if (!StringUtil.isEmpty(responseInfo.result)){
                    LogUtils.i("业务参数信息的获取"+responseInfo.result);
                    ParamsValueSny paramsVaule = DataAnalysisByJson.getInstance().getObjectByString(responseInfo.result, ParamsValueSny.class);
                    if (paramsVaule != null){
                        if (ReturnCodeParams.SUCESS_CODE.equals(paramsVaule.getReturnCode())){
                            ScanParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME, paramsVaule.getMerNam()); //商户名称
                            //超时时间
                            ParamsUtil.getInstance().save(TransParams.PARAMS_KEY_COMMUNICATION_OUT_TIME, paramsVaule.getTimeOut());
                            //重试次数
                            ParamsUtil.getInstance().save(TransParams.RECONN_TIMES, paramsVaule.getRetryNum());
                            //电话
                            ParamsUtil.getInstance().save(TransParams.PARAMS_KEY_DIAL_PHONE1, paramsVaule.getPhNo1());
                            ParamsUtil.getInstance().save(TransParams.PARAMS_KEY_DIAL_PHONE2, paramsVaule.getPhNo2());
                            ParamsUtil.getInstance().save(TransParams.PARAMS_KEY_DIAL_PHONE3, paramsVaule.getPhNo3());
                            ParamsUtil.getInstance().save(TransParams.PARAMS_KEY_MANAGE_PHONE, paramsVaule.getPhNoMng());
                            ParamsUtil.getInstance().save(TransParams.PARAMS_KEY_IS_CARD_INPUT, paramsVaule.getSupInputCard());

                            ScanParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME, paramsVaule.getMerNam()); //商户名称
                           /*支持的交易类型*/
                            for (int i = 0; i < 20; i++) {
                                boolean flag = paramsVaule.getTxnList().charAt(i) - '0' != 0;
                                switch (i) {
                                    case 8:
                                        ShareScanPreferenceUtils.putBoolean(context, TransParamsValue.PARAMS_KEY_TRANS_SCAN_PHONE_WEIXIN, flag);
                                        break;
                                    case 9:
                                        ShareScanPreferenceUtils.putBoolean(context, TransParamsValue.PARAMS_KEY_TRANS_SCAN_PHONE_ALIPAY, flag);
                                        break;
                                    case 10:
                                        ShareScanPreferenceUtils.putBoolean(context, TransParamsValue.PARAMS_KEY_TRANS_SCAN_POS_WEIXIN, flag);
                                        break;
                                    case 11:
                                        ShareScanPreferenceUtils.putBoolean(context, TransParamsValue.PARAMS_KEY_TRANS_SCAN_POS_ALIPAY, flag);
                                        break;
                                    case 12:
                                        ShareScanPreferenceUtils.putBoolean(context, TransParamsValue.PARAMS_KEY_TRANS_SCAN_REFUND, flag);
                                        break;
                                    default:
                                        break;
                                }
                            }
                            //参数下载成功
                            //ShareScanPreferenceUtils.putBoolean(context,TransParamsValue.PARAMS_IS_PARAM_DOWN, false);
                            ShareScanPreferenceUtils.putString(context, TransParamsValue.PARAMS_TRANSMIT_DATE, DateTimeUtil.getCurrentDate());
                            sendMessage(handler.obtainMessage(0x12));
                        }else {
                            //返回码错误信息
                            result.setRetCode(paramsVaule.getReturnCode());
                            result.setErrMsg(paramsVaule.getMessage());
                            result.setTransCode(TransConstans.TRANS_CODE_PARAMS);
                            Cache.getInstance().setRestatus(result);
                            result.setSucess(false);
                            sendMessage(handler.obtainMessage(0x01));
                        }
                    }else {
                        result.setRetCode(paramsVaule.getReturnCode());
                        result.setErrMsg("解析数据有误");
                        result.setSucess(false);
                        result.setTransCode(TransConstans.TRANS_CODE_PARAMS);
                        Cache.getInstance().setRestatus(result);
                        sendMessage(handler.obtainMessage(0x01));
                    }
                }else {
                    result.setRetCode("302");
                    result.setErrMsg("返回数据有误");
                    result.setSucess(false);
                    result.setTransCode(TransConstans.TRANS_CODE_PARAMS);
                    Cache.getInstance().setRestatus(result);
                    sendMessage(handler.obtainMessage(0x01));
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                sendMessage(handler.obtainMessage(0x02,"业务参数同步中"));
            }
        });

    }

    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }
}
