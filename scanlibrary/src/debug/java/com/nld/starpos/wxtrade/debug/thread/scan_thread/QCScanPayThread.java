package com.nld.starpos.wxtrade.debug.thread.scan_thread;

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
import com.nld.starpos.wxtrade.debug.bean.scan_common.ResultStatus;
import com.nld.starpos.wxtrade.debug.bean.scan_common.ScanCache;
import com.nld.starpos.wxtrade.debug.bean.scan_pay.QC_Pay_Rsp;
import com.nld.starpos.wxtrade.debug.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.debug.exception.DuplicatedTraceException;
import com.nld.starpos.wxtrade.debug.exception.TransException;
import com.nld.starpos.wxtrade.debug.http.AsyncHttpUtil;
import com.nld.starpos.wxtrade.debug.local.db.ScanTransDao;
import com.nld.starpos.wxtrade.debug.local.db.bean.ScanTransRecord;
import com.nld.starpos.wxtrade.debug.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.debug.local.db.imp.ScanTransDaoImp;
import com.nld.starpos.wxtrade.debug.thread.ComonThread;
import com.nld.starpos.wxtrade.debug.utils.ToastUtils;
import com.nld.starpos.wxtrade.debug.utils.jsonUtils.DataAnalysisByJson;
import com.nld.starpos.wxtrade.debug.utils.params.CommonParams;
import com.nld.starpos.wxtrade.debug.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.debug.utils.params.ReturnCodeParams;
import com.nld.starpos.wxtrade.debug.utils.params.ScanTransUtils;
import com.nld.starpos.wxtrade.debug.utils.params.TransParamsValue;
import com.nld.starpos.wxtrade.debug.utils.params.TransType;

import java.util.TreeMap;

import common.StringUtil;


/**
 * Created by jiangrenming on 2017/9/27.
 * 扫pos二维码
 */

public class QCScanPayThread extends ComonThread {

    private ScanPayBean mScanPayBean;
    private ResultStatus resultStatus;
    private QC_Pay_Rsp qc_pay_rsp;

    public QCScanPayThread(Context context, Handler handler, ScanPayBean scanPayBean) {
        super(context, handler);
        this.mScanPayBean = scanPayBean;
        scanPayBean.setTransName(getTransName(scanPayBean.getType()));
        resultStatus = new ResultStatus();
    }



    private String getTransName(int type) {
        switch (type){
            case TransType.ScanTransType.TRANS_QR_WEIXIN:
                return context.getString(R.string.wx_code);
            case  TransType.ScanTransType.TRANS_QR_ALIPAY:
                return  "支付宝条码";
            default:
                return  "未知条码";
        }
    }


    @Override
    public void run() {
        final TreeMap<String,String>  qc_scan_map = new TreeMap<>();
        if (mScanPayBean.getType() == TransType.ScanTransType.TRANS_QR_WEIXIN ||mScanPayBean.getType() == TransType.ScanTransType.TRANS_QR_ALIPAY ){ //两种快速支付类型
            if (EncodingEmun.maCaoProject.getType().equals(mScanPayBean.getProjectType())){

            }else {
                qc_scan_map.put(CommonParams.TYPE, mScanPayBean.getTransType());//接口类型
                qc_scan_map.put(CommonParams.SN, mScanPayBean.getSn()); //序列号sn
                qc_scan_map.put(CommonParams.TERMINAL_NO, mScanPayBean.getTerminalNo()); //终端编号
                qc_scan_map.put(CommonParams.TXN_CNL, mScanPayBean.getTxnCnl());  //交易渠道
                qc_scan_map.put(CommonParams.PAY_CHANNEL, mScanPayBean.getPayChannel()); //支付渠道
                qc_scan_map.put(CommonParams.ORDER_ID, mScanPayBean.getTransNo()); //流水号
                qc_scan_map.put(CommonParams.BATCHNO,mScanPayBean.getBatchNo()); //批次号
                qc_scan_map.put(CommonParams.AMOUNT, String.valueOf(mScanPayBean.getAmount())); //金额
                qc_scan_map.put(CommonParams.TOTAL_AMOUNT,String.valueOf(mScanPayBean.getAmount())); //订单总金额
            }

        }else if (mScanPayBean.getType() == TransType.ScanTransType.TRANS_SCAN_POS_CHECK){

            if (EncodingEmun.maCaoProject.getType().equals(mScanPayBean.getProjectType())){

            }else {
                qc_scan_map.put(CommonParams.TYPE, mScanPayBean.getTransType());//接口类型
                qc_scan_map.put(CommonParams.TERMINAL_NO, mScanPayBean.getTerminalNo()); //终端编号
                qc_scan_map.put(CommonParams.ORDER_NO, mScanPayBean.getOrderNo()); //平台订单号
                qc_scan_map.put(CommonParams.BATCHNO,mScanPayBean.getBatchNo()); //批次号
                qc_scan_map.put(CommonParams.DLD_TXN_LOGID,mScanPayBean.getTransNo()); //原始交易流水号
            }
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
    //    AsyncHttpUtil.setCommonBean(mScanPayBean);
        // 流水号增1
        addTraceNo();
        AsyncHttpUtil.httpPostXutils(qc_scan_map,mScanPayBean,new AsyncRequestCallBack<String>(){
            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
                super.onFailure(httpException, errorMsg);
                dealException(httpException,resultStatus);
                ScanCache.getInstance().setResultStatus(resultStatus);
                sendEmptyMessage(0x03);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (!ScanTransUtils.checkHmac(responseInfo.result)){
                    ToastUtils.showToast("没有返回hmac校验值或验证失败");
                }
                super.onSuccess(responseInfo);
                    if (!StringUtil.isEmpty(responseInfo.result)){
                        LogUtils.i("扫Pos返回的数据="+responseInfo.result);
                        qc_pay_rsp = DataAnalysisByJson.getInstance().getObjectByString(responseInfo.result, QC_Pay_Rsp.class);
                        if (null != qc_pay_rsp){
                            if (!StringUtil.isEmpty(qc_pay_rsp.getReturnCode()) && qc_pay_rsp.getReturnCode().equals(ReturnCodeParams.SUCESS_CODE)){
                                mScanPayBean.setProcessID(qc_pay_rsp.getReturnCode());
                                if (qc_pay_rsp.getResult().equals("S")){
                                    if (TransParamsValue.InterfaceType.SCAN_QC_POS.equals(qc_pay_rsp.getType())){
                                        String qc_code = qc_pay_rsp.getQc_Code();
                                        if (StringUtil.isEmpty(qc_code)){
                                            resultStatus.setRetCode(qc_pay_rsp.getReturnCode());
                                            resultStatus.setErrMsg("二维码生成为空,交易失败");
                                            ScanCache.getInstance().setResultStatus(resultStatus);
                                            sendMessage( handler.obtainMessage(0x03));
                                            return;
                                        }
                                        mScanPayBean.setScanResult(qc_code); //设置二维码地址
                                        mScanPayBean.setOrderNo(qc_pay_rsp.getOrderNo());
                                        mScanPayBean.setTotalAmount(qc_pay_rsp.getTotal_amount());
                                        mScanPayBean.setTransType(qc_pay_rsp.getType());
                                        mScanPayBean.setOldType(mScanPayBean.getType());
                                        Message msg = Message.obtain();
                                        msg.what= 0x06;
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("scan_pos",mScanPayBean);
                                        msg.setData(bundle);
                                        handler.sendMessage(msg);
                                    }else if (TransParamsValue.InterfaceType.SCAN_POS_QUERY.equals(qc_pay_rsp.getType())){  //扫码付款成功

                                        mScanPayBean.setOrderNo(qc_pay_rsp.getOrderNo());
                                        mScanPayBean.setTransCode("00");  //交易结果返回码，成功00,失败，撤销
                                        if (!StringUtil.isEmpty(qc_pay_rsp.getPayTime()) && qc_pay_rsp.getPayTime().length() >= 14 ) {
                                            mScanPayBean.setYear(qc_pay_rsp.getPayTime().substring(0, 4));
                                            mScanPayBean.setDate(qc_pay_rsp.getPayTime().substring(4, 8));
                                            mScanPayBean.setTime(qc_pay_rsp.getPayTime().substring(8, 14));
                                        }
                                        if (!StringUtil.isEmpty(qc_pay_rsp.getPayType())){
                                            mScanPayBean.setPayType(qc_pay_rsp.getPayType());
                                        }
                                        if (!StringUtil.isEmpty(qc_pay_rsp.getPayChannel())) {
                                            mScanPayBean.setPayChannel(qc_pay_rsp.getPayChannel());
                                        }
                                        if (!StringUtil.isEmpty(String.valueOf(qc_pay_rsp.getAmount()))){
                                            mScanPayBean.setAmount(qc_pay_rsp.getAmount());
                                            mScanPayBean.setTotalAmount(qc_pay_rsp.getAmount());
                                        }
                                        if (!StringUtil.isEmpty(qc_pay_rsp.getLogNo())){
                                            mScanPayBean.setLogNo(qc_pay_rsp.getLogNo());
                                        }
                                        //添加流水单
                                        addWater();
                                        Message msg = Message.obtain();
                                        msg.what= 0x06;
                                        resultStatus.setSucess(true);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("result",resultStatus);
                                        msg.setData(bundle);
                                        handler.sendMessage(msg);
                                    }
                                }
                            }else {
                                mScanPayBean.setProcessID(qc_pay_rsp.getReturnCode());
                                resultStatus = new ResultStatus();
                                if ("A".equals(qc_pay_rsp.getResult())){
                                    sendMessage( handler.obtainMessage(0x04));
                                }else {
                                    resultStatus.setErrMsg(qc_pay_rsp.getMessage());
                                    resultStatus.setRetCode(qc_pay_rsp.getReturnCode());
                                    resultStatus.setSucess(false);
                                    ScanCache.getInstance().setResultStatus(resultStatus);
                                    sendMessage( handler.obtainMessage(0x03));
                                }
                            }
                        }else {
                            resultStatus.setRetCode(TransException.ERR_DAT_JSON_E207);
                            resultStatus.setErrMsg(TransException.getMsg(TransException.ERR_DAT_JSON_E207));
                            ScanCache.getInstance().setResultStatus(resultStatus);
                            sendMessage( handler.obtainMessage(0x03));
                        }
                    }else {
                        resultStatus.setRetCode(TransException.ERR_DAT_EOF_E208);
                        resultStatus.setErrMsg(TransException.getMsg(TransException.ERR_DAT_EOF_E208));
                        ScanCache.getInstance().setResultStatus(resultStatus);
                        sendMessage( handler.obtainMessage(0x03));
                    }
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                Message message = handler.obtainMessage(0x11, context.getString(R.string.increase_scan));
                sendMessage(message);
            }
        });
    }

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

    /**
     * 增加流水
     */
    private synchronized  void addWater() {
        ScanTransRecord water = getWater(mScanPayBean);
        addLocalWater(water);
    }

    /**
     * 获取交易流水
     * @param scanPayBean
     * @return
     */
    private ScanTransRecord getWater(ScanPayBean scanPayBean) {
        Log.i("TAG金额",scanPayBean.getAmount()+"/交易类型="+scanPayBean.getOldType());
        ScanTransRecord record = new ScanTransRecord();
        try {
            record.setTransType(scanPayBean.getOldType()); //交易类型
            record.setPayChannel(scanPayBean.getPayChannel()); //交易渠道
            record.setTerminalId(scanPayBean.getTerminalNo()); //终端号
            record.setSystraceno(scanPayBean.getTransNo()); //POS流水号
            record.setLogNo(scanPayBean.getLogNo());  //系统流水号<内部订单号>
            record.setPayType(scanPayBean.getPayType()); //支付类型
            record.setBatchbillno(scanPayBean.getBatchNo());  //批次号
            record.setMemberId( ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID)); //商户号
            record.setMemberName(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME)); //商户名称
            record.setOper("001"); //操作员  ，暂时是默认的
            record.setOrderNo(scanPayBean.getOrderNo()); //订单号
            if (StringUtil.isEmpty(scanPayBean.getDate()) ||StringUtil.isEmpty(scanPayBean.getTime()) ||StringUtil.isEmpty(scanPayBean.getYear())){
                record.setScanDate(null);
                record.setScanTime(null);
                record.setScanYear(null);
            }else {
                record.setScanDate((scanPayBean.getDate().substring(0,2)+"/"+scanPayBean.getDate().substring(2,4))); //日期
                record.setScanTime((scanPayBean.getTime().substring(0,2)+":"+scanPayBean.getTime().substring(2,4)+":"+scanPayBean.getTime().substring(4,6))); //时间
                record.setScanYear(scanPayBean.getYear()); //年份
            }
            record.setTransamount(String.valueOf(scanPayBean.getAmount())); //交易金额
            record.setTotalAmount(String.valueOf(scanPayBean.getTotalAmount())); //交易总金额
            record.setAdddataword(scanPayBean.getAddInfos() != null ? scanPayBean.getAddInfos() : ""); //附加信息
            record.setAuthCode(scanPayBean.getScanResult()); //授权码
            record.setSettledata(scanPayBean.getSettleDate() != null ? scanPayBean.getSettleDate() : "");  //结算日期
            record.setTranscurrcode(scanPayBean.getCurrency()); //币种
            record.setStatuscode(scanPayBean.getTransCode()); //交易结果码
            record.setTransprocode("0x00"); //消费(0x00)，消费撤销(0x01)
            record.setIsrevoke(String.valueOf(TransType.TransStatueType.NORMAL));  //是否被撤销
            record.setRespcode(scanPayBean.getProcessID()); //应答码
        }catch (Exception e){
            e.printStackTrace();
            dealException(e,resultStatus);
            sendEmptyMessage(0x03);
        }
        return record;
    }

    /**
     * 添加流水到本地数据库
     * @param water
     */
    private void addLocalWater(final ScanTransRecord water) {
        if (null != water){
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    ScanTransDao scanTransDao = new ScanTransDaoImp();
                    try {
                        scanTransDao.addWater(water);
                        resultStatus.setSucess(true);
                        resultStatus.setRetCode(water.getRespcode());
                        resultStatus.setRecord(water);
                        Log.i("TAG","water== "+water.getSystraceno());
                        //接下来是打印步骤
                        addPrinter(water);
                    } catch (DuplicatedTraceException e) {
                        dealException(e,resultStatus);
                        sendEmptyMessage(0x03);
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
    /**
     * 增添打印
     */
    private void addPrinter(ScanTransRecord water) {
        if (!(qc_pay_rsp != null &&
                !TextUtils.isEmpty(qc_pay_rsp.getResult()) &&
                (TextUtils.equals(qc_pay_rsp.getResult(), "F") ||
                        TextUtils.equals(qc_pay_rsp.getResult(), "S")))) {
            sendMessage(handler.obtainMessage(0x03,"交易未知"));
        }
        Log.i("TAG","result =="+qc_pay_rsp.getResult());
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        if (water != null){
            bundle.putSerializable("water",water);//流水类
        }
        message.setData(bundle);
        message.what = 0x16;
        handler.sendMessage(message);
    }
}
