package com.nld.starpos.wxtrade.debug.thread.scan_thread;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.nld.netlibrary.xutils.AsyncRequestCallBack;
import com.nld.starpos.wxtrade.R;
import com.nld.starpos.wxtrade.debug.bean.scan_common.ResultStatus;
import com.nld.starpos.wxtrade.debug.bean.scan_common.ScanCache;
import com.nld.starpos.wxtrade.debug.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.debug.bean.scan_query.ScanQueryWater;
import com.nld.starpos.wxtrade.debug.exception.TransException;
import com.nld.starpos.wxtrade.debug.http.AsyncHttpUtil;
import com.nld.starpos.wxtrade.debug.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.debug.thread.ComonThread;
import com.nld.starpos.wxtrade.debug.utils.ToastUtils;
import com.nld.starpos.wxtrade.debug.utils.jsonUtils.DataAnalysisByJson;
import com.nld.starpos.wxtrade.debug.utils.params.CommonParams;
import com.nld.starpos.wxtrade.debug.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.debug.utils.params.ReturnCodeParams;
import com.nld.starpos.wxtrade.debug.utils.params.ScanTransUtils;
import com.nld.starpos.wxtrade.debug.utils.params.TransParamsValue;

import java.util.TreeMap;

import common.StringUtil;


/**
 * @author jiangrenming
 * @description 扫码查单
 */
public class QueryThread extends ComonThread {


    private ScanPayBean mScanRefund;
    private ResultStatus result;
    private ScanQueryWater scanQueryWater;

    public QueryThread(Context context, Handler handler, ScanPayBean scanRefundBean) {
        super(context, handler);
        this.mScanRefund = scanRefundBean;
        result = new ResultStatus();
    }

    @Override
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {

        final TreeMap<String, String> map = new TreeMap<String, String>();
        if (EncodingEmun.maCaoProject.getType().equals(mScanRefund.getProjectType())){  //澳门项目

            map.put(CommonParams.TYPE, mScanRefund.getTransType());
            map.put(CommonParams.QUR_TYP, mScanRefund.getQryTyp());  //查询类型
            map.put(CommonParams.OLD_ORDER_NO, mScanRefund.getLogNo()); //原交易订单号
            map.put(CommonParams.OLD_ORDER_NO, mScanRefund.getLogNo()); //原交易订单号
            map.put(CommonParams.TRM_NO, mScanRefund.getTerminalNo()); //终端编号

        }else {  // 蚂蚁企服项目
            map.put(CommonParams.TYPE, mScanRefund.getTransType());
            map.put(CommonParams.LOG_NO, mScanRefund.getLogNo());
        }

        String md5_key = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.MD5_KEY);
        if (!StringUtil.isEmpty(md5_key)){
            mScanRefund.setMd5_key(md5_key);
        }
        String transNo = ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO); //流水号
        mScanRefund.setRequestId(System.currentTimeMillis() + transNo);
        String merchantId = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID); //扫码商户号
        if (!StringUtil.isEmpty(merchantId)){
            mScanRefund.setMerchantId(merchantId);
        }
      //  AsyncHttpUtil.setCommonBean(mScanRefund);
        addTraceNo();
        AsyncHttpUtil.httpPostXutils(map,mScanRefund,new AsyncRequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (!ScanTransUtils.checkHmac(responseInfo.result)){
                    ToastUtils.showToast("没有返回hmac校验值或验证失败");
                }
                super.onSuccess(responseInfo);
                if (!StringUtil.isEmpty(responseInfo.result)){
                    Log.i("TAG","扫码查单的返回数据="+responseInfo.result);
                     scanQueryWater = DataAnalysisByJson.getInstance().getObjectByString(
                            responseInfo.result, ScanQueryWater.class);
                    if (scanQueryWater != null){
                        if (scanQueryWater.getReturnCode().equals(ReturnCodeParams.SUCESS_CODE)){
                            if (EncodingEmun.maCaoProject.getType().equals(mScanRefund.getProjectType())){
                                scanQueryWater.setLogNo(mScanRefund.getLogNo());
                                String payChannel = scanQueryWater.getPayChannel();
                                if (!TextUtils.isEmpty(payChannel) &&
                                        (TextUtils.equals(payChannel, "WXPAY") ||
                                                TextUtils.equals(payChannel, "ALIPAY"))) {
                                    mScanRefund.setMerchantId(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID);  //扫码商户号
                                }
                                String tranDtTm = scanQueryWater.getTranDtTm();  //日期
                                mScanRefund.setTranDtTm(tranDtTm); //日期
                                mScanRefund.setTransType(scanQueryWater.getType()); //接口
                                mScanRefund.setPayChannel(scanQueryWater.getPayChannel()); //支付渠道
                                // mScanRefund.setOrderId(scanQueryWater.getOrderId());  //流水号
                                mScanRefund.setTerminalNo(scanQueryWater.getTrmNo()); //终端号
                                mScanRefund.setMercNm(scanQueryWater.getMercNm()); //商户名称
                                mScanRefund.setChannelid(scanQueryWater.getOrderNo()); //订单号
                                mScanRefund.setAmount(scanQueryWater.getAmount());   //金额
                                mScanRefund.setMAX_REF_AMT(scanQueryWater.getRefAmt()); //最大退款金额
                                mScanRefund.setOrdFee(scanQueryWater.getFeeAmt()); //手续费
                                mScanRefund.setTxnSts(scanQueryWater.getTxnSts());  //交易状态
                            }else {
                                scanQueryWater.setLogNo(mScanRefund.getLogNo());
                                String payChannel = scanQueryWater.getPayChannel();
                                if (!TextUtils.isEmpty(payChannel) &&
                                        (TextUtils.equals(payChannel, "WXPAY") ||
                                                TextUtils.equals(payChannel, "ALIPAY"))) {
                                    mScanRefund.setMerchantId(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID);  //扫码商户号
                                }
                                String tranDtTm = scanQueryWater.getTranDtTm();  //日期
                                mScanRefund.setTransType(scanQueryWater.getType()); //接口
                                mScanRefund.setPayChannel(scanQueryWater.getPayChannel()); //支付渠道
                                mScanRefund.setUserNo(scanQueryWater.getPosOperId()); //操作员
                                mScanRefund.setAmount(scanQueryWater.getAmount());   //金额
                                mScanRefund.setOrderId(scanQueryWater.getOrderId());  //流水号
                                mScanRefund.setBatchNo(scanQueryWater.getBatNo());  //批次号
                                mScanRefund.setTerminalNo(scanQueryWater.getTerminalNo()); //终端号
                                mScanRefund.setChannelid(scanQueryWater.getChannelid()); //订单号
                                mScanRefund.setMAX_REF_AMT(scanQueryWater.getMAX_REF_AMT()); //最大退款金额
                                mScanRefund.setOrdFee(scanQueryWater.getOrdFee()); //手续费
                                mScanRefund.setMercNm(scanQueryWater.getMercNm()); //商户名称
                                mScanRefund.setTXN_CD(scanQueryWater.getTXN_CD()); //交易码
                                mScanRefund.setTxnTyp(scanQueryWater.getTxnTyp());  //交易类型
                                mScanRefund.setTxnSts(scanQueryWater.getTxnSts());  //交易状态
                                mScanRefund.setTranDtTm(tranDtTm); //日期
                                mScanRefund.setOprId(scanQueryWater.getPosOperId()); //操作员
                            }
                            Message message = Message.obtain();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("scan_query",mScanRefund);
                            message.setData(bundle);
                            message.what = 0x00;
                            sendMessage(message);

                        }else  if ("POS00002".equals(scanQueryWater.getReturnCode())){
                            sendMessage(handler.obtainMessage(0x02));
                        }else {
                            result.setSucess(false);
                            result.setErrMsg(scanQueryWater.getMessage());
                            result.setRetCode(scanQueryWater.getReturnCode());
                            ScanCache.getInstance().setResultStatus(result);
                            sendEmptyMessage(0x01);
                        }
                    }else {
                        result.setSucess(false);
                        result.setRetCode(TransException.ERR_DAT_JSON_E207);
                        result.setErrMsg(TransException.getMsg(TransException.ERR_DAT_JSON_E207));
                        ScanCache.getInstance().setResultStatus(result);
                        sendEmptyMessage(0x01);
                    }
                }else {
                    result.setSucess(false);
                    result.setRetCode(TransException.ERR_DAT_EOF_E208);
                    result.setErrMsg(TransException.getMsg(TransException.ERR_DAT_EOF_E208));
                    ScanCache.getInstance().setResultStatus(result);
                    sendEmptyMessage(0x01);
                }
            }

            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
                super.onFailure(httpException, errorMsg);
                result.setSucess(false);
                dealException(httpException,result);
                ScanCache.getInstance().setResultStatus(result);
                sendEmptyMessage(0x01);
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                sendMessage( handler.obtainMessage(0x11,context.getString(R.string.scan_query)));
            }
        });
    }


    /**
     * 增加流水号
     */
    private void addTraceNo() {
        String traceNo = ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO);
        Log.i("TAG","流水号:"+traceNo);
        int currenNo = 1;
        try {
            currenNo = Integer.parseInt(traceNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int nextNo = (currenNo + 1) % 1000000;
        ScanParamsUtil.getInstance().update(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO, String.format("%06d", nextNo));
    }

    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }
}
