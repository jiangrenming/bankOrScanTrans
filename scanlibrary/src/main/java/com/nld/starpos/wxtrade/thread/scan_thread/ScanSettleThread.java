package com.nld.starpos.wxtrade.thread.scan_thread;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.util.LogUtils;
import com.nld.netlibrary.xutils.AsyncRequestCallBack;
import com.nld.starpos.wxtrade.R;
import com.nld.starpos.wxtrade.bean.scan_batch_no.AsyScanBatchNo;
import com.nld.starpos.wxtrade.bean.scan_common.ResultStatus;
import com.nld.starpos.wxtrade.bean.scan_common.ScanCache;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.bean.scan_query.ScanQueryDataBean;
import com.nld.starpos.wxtrade.bean.scan_query.ScanQueryRes;
import com.nld.starpos.wxtrade.bean.scan_settle.ScanSettleRes;
import com.nld.starpos.wxtrade.exception.TransException;
import com.nld.starpos.wxtrade.http.AsyncHttpUtil;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.thread.ComonThread;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.jsonUtils.DataAnalysisByJson;
import com.nld.starpos.wxtrade.utils.params.CommonParams;
import com.nld.starpos.wxtrade.utils.params.ReturnCodeParams;
import com.nld.starpos.wxtrade.utils.params.ScanTransUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;
import com.nld.starpos.wxtrade.utils.params.TransType;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import common.DateTimeUtil;
import common.StringUtil;


/**
 * Created by jiangrenming on 2017/10/10.
 * 扫码批结
 */

public class ScanSettleThread extends ComonThread {


    private ScanPayBean mScanPayBean;
    private int totalPageNo = 1;
    private ArrayList<ScanQueryDataBean> totals = new ArrayList<>();
    private ResultStatus result;

    public ScanSettleThread(Context context, Handler handler, ScanPayBean scanPayBean) {
        super(context, handler);
        this.mScanPayBean = scanPayBean;
        result = new ResultStatus();
    }


    @Override
    public void run() {
        TreeMap<String,String> scanSettle = new TreeMap<>();
        if (mScanPayBean.getTransType().equals(TransParamsValue.InterfaceType.TRANS_QUER)){  //交易查询
            Log.i("TAG","循环的次数="+mScanPayBean.getPageNo()+"一共的页数:"+totalPageNo);
            if (mScanPayBean.getPageNo() == 0 || mScanPayBean.getPageNo() > totalPageNo) {
                //直接发送信息，结束循环
                Message message = Message.obtain();
                message.what = 0x02;
                Bundle bundle = new Bundle();
                bundle.putSerializable("total", totals);
                message.setData(bundle);
                handler.sendMessage(message);
                return;
            } else {
                scanSettle.put(CommonParams.TYPE, mScanPayBean.getTransType());  //接口类型
                scanSettle.put(CommonParams.PAG_NO, String.valueOf(mScanPayBean.getPageNo()));  //页数
                scanSettle.put(CommonParams.PAG_NUM, String.valueOf(mScanPayBean.getPageNumber()));  //记录数
                scanSettle.put(CommonParams.TERMINAL_NO, mScanPayBean.getTerminalNo());  //终端号
                scanSettle.put(CommonParams.START_DATE, mScanPayBean.getDateTime());  //默认当天时间起
                scanSettle.put(CommonParams.END_DATE, mScanPayBean.getDateTime());   //默认当天时间止
                scanSettle.put(CommonParams.QUERY_BATCHNO,mScanPayBean.getBatchNo()); //批次号
                addTraceNo();
            }
        }else if (mScanPayBean.getTransType().equals(TransParamsValue.InterfaceType.SCAN_POST_BATCH_CHK)){ //批结

            scanSettle.put(CommonParams.TYPE, mScanPayBean.getTransType());
            scanSettle.put(CommonParams.TERMINAL_NO, mScanPayBean.getTerminalNo());
            scanSettle.put(CommonParams.ORDER_ID, mScanPayBean.getTransNo());
            scanSettle.put(CommonParams.BATCHNO, mScanPayBean.getBatchNo());
            scanSettle.put(CommonParams.PHAPLIYNAMT, mScanPayBean.getPhAlipayAmt());
            scanSettle.put(CommonParams.PHAPLIYNCNT, mScanPayBean.getPhAlipayCnt());
            scanSettle.put(CommonParams.POSAPLIYAMT, mScanPayBean.getPosAlipayAmt());
            scanSettle.put(CommonParams.POSAPLIYCNT, mScanPayBean.getPosAlipayCnt());
            scanSettle.put(CommonParams.PHWEIXINAMT, mScanPayBean.getPhWeiXinAmt());
            scanSettle.put(CommonParams.PHWEIXINCNT, mScanPayBean.getPhWeiXinCnt());
            scanSettle.put(CommonParams.POSWEIXINAMT, mScanPayBean.getPosWeiXinAmt());
            scanSettle.put(CommonParams.POSWEIXINCNT, mScanPayBean.getPosWeiXinCnt());
            scanSettle.put(CommonParams.PHUnionpayAMT, mScanPayBean.getPhUnionpayAmt());
            scanSettle.put(CommonParams.PHUnionpayCNT, mScanPayBean.getPhUnionpayCnt());
            scanSettle.put(CommonParams.POSUnionpayAMT, mScanPayBean.getPosUnionpayAmt());
            scanSettle.put(CommonParams.POSUnionpayCNT, mScanPayBean.getPosUnionpayCnt());
            addTraceNo();
        }else if (TransParamsValue.InterfaceType.SNYBATCHNO.equals(mScanPayBean.getTransType())) {  //同步批次号
            scanSettle.put(CommonParams.TYPE, mScanPayBean.getTransType());
            scanSettle.put(CommonParams.TERMINAL_NO, mScanPayBean.getTerminalNo());
        }

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

      //  AsyncHttpUtil.setCommonBean(mScanPayBean);
        AsyncHttpUtil.httpPostXutils(scanSettle,mScanPayBean,new AsyncRequestCallBack<String>(){
            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
                super.onFailure(httpException, errorMsg);
                result.setSucess(false);
                dealException(httpException,result);
                ScanCache.getInstance().setResultStatus(result);
                sendEmptyMessage(0x01);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (!ScanTransUtils.checkHmac(responseInfo.result)){
                    ToastUtils.showToast("没有返回hmac校验值或验证失败");
                }
                super.onSuccess(responseInfo);
                if (!StringUtil.isEmpty(responseInfo.result)){
                    LogUtils.i("批结返回数据的结果"+responseInfo.result);
                    if (mScanPayBean.getTransType().equals(TransParamsValue.InterfaceType.TRANS_QUER)){
                        try{
                            String[] split = responseInfo.result.split("&");  //处理 datas字段返回的数据类型变化为字符串的特殊处理
                            for (int i = 0; i < split.length; i++) {
                                if (split[i].startsWith("datas")){
                                    int index = split[i].indexOf("=");
                                    String value = split[i].substring(index + 1);
                                    if (value.equals("0")){
                                        //无数据时批结 需要把标志变为false
                                        ShareScanPreferenceUtils.putBoolean(context,TransParamsValue.SettleConts.SETTLE_ALL_FLAG,false);
                                        ShareScanPreferenceUtils.putBoolean(context,TransParamsValue.SettleConts.PARAMS_IS_SCAN_SETTLT_HALT,false);
                                        sendMessage(handler.obtainMessage(0x03,"无批结数据"));
                                        return;
                                    }
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            dealException(e,result);
                            sendEmptyMessage(0x01);
                        }
                        ScanQueryRes scanQueryRes =  DataAnalysisByJson.getInstance().getObjectByString2(responseInfo.result, ScanQueryRes.class);
                        if (ReturnCodeParams.SUCESS_CODE.equals(scanQueryRes.getReturnCode())){
                            if (scanQueryRes.getPagNo() == 1){
                                int totCnt = scanQueryRes.getTotCnt();
                                int pagNum = scanQueryRes.getPagNum();

                                if (totCnt !=0 && pagNum != 0){
                                    int netCount = totCnt / pagNum;
                                    totalPageNo = totCnt % pagNum != 0 ? (netCount + 1) : netCount;
                                    Log.i("TAG","共需请求网络列表数据" + totalPageNo + "次");
                                }else {
                                    //没有查询到数据信息
                                    totals.clear();
                                    sendMessage(handler.obtainMessage(0x03,"无批结数据"));
                                    return;
                                }
                            }
                            List<ScanQueryDataBean> datas = scanQueryRes.getDatas();
                            if (datas != null && !datas.isEmpty()){
                                for (ScanQueryDataBean scanQuery : datas){
                                    if (TextUtils.equals(scanQuery.getCorg_no(), "GTXYPAY")) {
                                        totals.add(scanQuery);
                                        Log.i("TAG","列表数据---流水号：" + scanQuery.getCseq_no());
                                    }
                                }
                                mScanPayBean.setPageNo(mScanPayBean.getPageNo()+1);
                                run(); //循环调用接口，相当于下拉加载更多的操作
                            }else {
                                //解析数据有误信息
                                result.setRetCode(scanQueryRes.getReturnCode());
                                result.setErrMsg("解析数据有误");
                                result.setSucess(false);
                                ScanCache.getInstance().setResultStatus(result);
                                sendMessage(handler.obtainMessage(0x01));
                            }
                        }else {
                            //返回码错误信息
                            result.setRetCode(scanQueryRes.getReturnCode());
                            result.setErrMsg(scanQueryRes.getMessage());
                            ScanCache.getInstance().setResultStatus(result);
                            result.setSucess(false);
                            sendMessage(handler.obtainMessage(0x01));
                        }
                    }else if (mScanPayBean.getTransType().equals(TransParamsValue.InterfaceType.SCAN_POST_BATCH_CHK)){
                        ScanSettleRes scanSettleRes =  DataAnalysisByJson.getInstance().getObjectByString2(responseInfo.result, ScanSettleRes.class);
                        if (ReturnCodeParams.SUCESS_CODE.equals(scanSettleRes.getReturnCode())){
                            if (TransParamsValue.AccountStatus.EQUAL.equals(scanSettleRes.getChkRspCod()) ||TransParamsValue.AccountStatus.UNEQUAL.equals(scanSettleRes.getChkRspCod()) ||
                                    TransParamsValue.AccountStatus.ERROR.equals(scanSettleRes.getChkRspCod())){
                                ShareScanPreferenceUtils.putString(context,TransParamsValue.SettleConts.PARAMS_FLAG_SCAN_ACCOUNT_CHECKING,scanSettleRes.getChkRspCod()); //对账状态
                            }
                            scanSettleRes.setTransType(String.valueOf(TransType.ScanTransType.TRANS_SCAN_SETTLE));
                            scanSettleRes.setRefundCount(mScanPayBean.getRefundCount());
                            scanSettleRes.setRefundAmount(mScanPayBean.getRefundAmount());
                            scanSettleRes.setRequestURL(mScanPayBean.getRequestUrl());
                            scanSettleRes.setProjectType(mScanPayBean.getProjectType());

                            ShareScanPreferenceUtils.putString(context,TransParamsValue.SettleConts.PARAMS_SETTLE_TIME,(DateTimeUtil.formatMillisecondAllDate(System.currentTimeMillis()))); //结算时间
                            ShareScanPreferenceUtils.putBoolean(context,TransParamsValue.SettleConts.PARAMS_IS_SCAN_SETTLT_HALT,false); //二维码结算中断
                            ShareScanPreferenceUtils.putString(context,TransParamsValue.SettleConts.PARAMS_SETTLE_DATA,(responseInfo.result+"&refundCount="+mScanPayBean.getRefundCount()+"&refundAmount="+mScanPayBean.getRefundAmount())); //批结时存储的数据
                            //打印结算单
                            Message message = Message.obtain();
                            message.what = 0x12;
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("settle",scanSettleRes);
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }else {
                            result.setRetCode(scanSettleRes.getReturnCode());
                            result.setErrMsg(scanSettleRes.getMessage());
                            ScanCache.getInstance().setResultStatus(result);
                            result.setSucess(false);
                            sendMessage(handler.obtainMessage(0x01));
                        }
                    }else if (TransParamsValue.InterfaceType.SNYBATCHNO.equals(mScanPayBean.getTransType())){ //同步批次号
                        LogUtils.i("返回批次同步的数据="+responseInfo.result);
                        AsyScanBatchNo asyBatchNo = DataAnalysisByJson.getInstance().getObjectByString(responseInfo.result, AsyScanBatchNo.class);
                        if (asyBatchNo != null){
                            if (!StringUtil.isEmpty(asyBatchNo.getReturnCode()) && ReturnCodeParams.SUCESS_CODE.equals(asyBatchNo.getReturnCode())){
                                String batNo = asyBatchNo.getBatNo();
                                if (!StringUtil.isEmpty(batNo)){  //同步本地批次号
                                    int currenNo = 1;
                                    try {
                                        currenNo = Integer.parseInt(batNo);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    int nextNo = (currenNo + 1) % 1000000;
                                    ScanParamsUtil.getInstance().update(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO,String.format("%06d", nextNo));
                                }
                                sendMessage(handler.obtainMessage(0x13));
                            }else {
                                result.setRetCode(asyBatchNo.getReturnCode());
                                result.setErrMsg(asyBatchNo.getMessage());
                                ScanCache.getInstance().setResultStatus(result);
                                result.setSucess(false);
                                sendMessage(handler.obtainMessage(0x01));
                            }
                        }else {
                            result.setRetCode(TransException.ERR_DAT_JSON_E207);
                            result.setErrMsg(TransException.getMsg(TransException.ERR_DAT_JSON_E207));
                            ScanCache.getInstance().setResultStatus(result);
                            sendMessage( handler.obtainMessage(0x01));
                        }
                    }
                }else {
                    //返回的数据为空信息
                    sendMessage(handler.obtainMessage(0x00,"返回数据有误"));
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                if (mScanPayBean.getTransType().equals(TransParamsValue.InterfaceType.TRANS_QUER)){
                    sendMessage( handler.obtainMessage(0x11,"正在查询第"+mScanPayBean.getPageNo()+"页"));
                }else if (mScanPayBean.getTransType().equals(TransParamsValue.InterfaceType.SCAN_POST_BATCH_CHK)){
                    sendMessage( handler.obtainMessage(0x11,context.getString(R.string.increase_settle_data)));
                }else if (TransParamsValue.InterfaceType.SNYBATCHNO.equals(mScanPayBean.getTransType())){
                    sendMessage( handler.obtainMessage(0x11,"正在同步扫码批次号"));
                }
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
