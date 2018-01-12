package com.nld.starpos.wxtrade.thread.scan_thread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.util.LogUtils;
import com.nld.netlibrary.xutils.AsyncRequestCallBack;
import com.nld.starpos.wxtrade.R;
import com.nld.starpos.wxtrade.bean.scan_common.ResultStatus;
import com.nld.starpos.wxtrade.bean.scan_common.ScanCache;
import com.nld.starpos.wxtrade.bean.scan_pay.MessageTipBean;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayRsp;
import com.nld.starpos.wxtrade.exception.DuplicatedTraceException;
import com.nld.starpos.wxtrade.exception.TransException;
import com.nld.starpos.wxtrade.http.AsyncHttpUtil;
import com.nld.starpos.wxtrade.local.db.ScanTransDao;
import com.nld.starpos.wxtrade.local.db.bean.ScanTransRecord;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.local.db.imp.ScanTransDaoImp;
import com.nld.starpos.wxtrade.thread.ComonThread;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.jsonUtils.DataAnalysisByJson;
import com.nld.starpos.wxtrade.utils.params.CommonParams;
import com.nld.starpos.wxtrade.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.utils.params.ReturnCodeParams;
import com.nld.starpos.wxtrade.utils.params.ScanTransUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;
import com.nld.starpos.wxtrade.utils.params.TransType;
import java.util.TreeMap;

import common.StringUtil;

/*
 * @author Xrh
 * @description 微信被扫支付
 * @date 2015-8-28 11:31:22
*/





public class ScanpayThread extends ComonThread {


    private ScanPayBean scanPayBean;
    private ResultStatus resultStatus;
    private ScanPayRsp scanPayResponse;

    public ScanpayThread(Context context, Handler handler, ScanPayBean scanPayBean) {
        super(context, handler);
        this.scanPayBean = scanPayBean;
        scanPayBean.setTransName(getTransName(scanPayBean.getType()));
        resultStatus = new ResultStatus();
    }


    public ScanpayThread(Context context, Handler handler) {
        super(context, handler);
    }

    @Override
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        TreeMap<String, String> map = new TreeMap<String, String>();
        if (scanPayBean.getType() == TransType.ScanTransType.TRANS_SCAN_WEIXIN ||scanPayBean.getType() == TransType.ScanTransType.TRANS_SCAN_ALIPAY ){  //微信扫码和支付宝扫码
            if (EncodingEmun.maCaoProject.getType().equals(scanPayBean.getProjectType())){  //澳门项目

                map.put(CommonParams.TYPE, scanPayBean.getTransType());//接口类型
                map.put(CommonParams.AUTH_CODE, scanPayBean.getScanResult());    //二维码中存储的商户信息
                map.put(CommonParams.SN, scanPayBean.getSn()); //序列号sn
                map.put(CommonParams.TRM_NO, scanPayBean.getTerminalNo()); //终端编号
                map.put(CommonParams.TXN_CNL, scanPayBean.getTxnCnl());  //交易渠道
                map.put(CommonParams.PAY_CHANNEL, scanPayBean.getPayChannel()); //支付渠道
                map.put(CommonParams.AMOUNT, String.valueOf(scanPayBean.getAmount())); //金额
                map.put(CommonParams.TOTAL_AMOUNT,String.valueOf(scanPayBean.getAmount())); //订单总金额

            }else {  //蚂蚁企服项目

                map.put(CommonParams.TYPE, scanPayBean.getTransType());//接口类型
                map.put(CommonParams.AUTH_CODE, scanPayBean.getScanResult());    //二维码中存储的商户信息
                map.put(CommonParams.SN, scanPayBean.getSn()); //序列号sn
                map.put(CommonParams.TERMINAL_NO, scanPayBean.getTerminalNo()); //终端编号
                map.put(CommonParams.TXN_CNL, scanPayBean.getTxnCnl());  //交易渠道
                map.put(CommonParams.PAY_CHANNEL, scanPayBean.getPayChannel()); //支付渠道
                map.put(CommonParams.AMOUNT, String.valueOf(scanPayBean.getAmount())); //金额
                map.put(CommonParams.ORDER_ID, scanPayBean.getTransNo()); //流水号
                map.put(CommonParams.TOTAL_AMOUNT,String.valueOf(scanPayBean.getAmount())); //订单总金额
                map.put(CommonParams.BATCHNO,scanPayBean.getBatchNo()); //批次号
            }
        }else if (scanPayBean.getType() == TransType.ScanTransType.TRANS_SCAN_POS_CHECK){  //等待授权<微信扫码或支付宝扫码>

            if (scanPayBean.getOldType() == TransType.ScanTransType.TRANS_SCAN_WEIXIN){
                scanPayBean.setType(TransType.ScanTransType.TRANS_SCAN_WEIXIN);
            }else if (scanPayBean.getOldType() == TransType.ScanTransType.TRANS_SCAN_ALIPAY){
                scanPayBean.setType(TransType.ScanTransType.TRANS_SCAN_ALIPAY);
            }
            //扫码支付查询的接口参数
            if (EncodingEmun.maCaoProject.getType().equals(scanPayBean.getProjectType())){

                map.put(CommonParams.TYPE, scanPayBean.getTransType());//接口类型
                map.put(CommonParams.OLD_REQUEST_ID, scanPayBean.getOldRequestId()); //商户请求号
                map.put(CommonParams.QUR_TYP, scanPayBean.getQryTyp());  //查询类型
                map.put(CommonParams.TRM_NO, scanPayBean.getTerminalNo()); //终端编号

            }else {

                //扫码支付查询的接口参数
                map.put(CommonParams.TYPE, scanPayBean.getTransType());//接口类型
                map.put(CommonParams.TERMINAL_NO, scanPayBean.getTerminalNo()); //终端编号
                map.put(CommonParams.ORDER_NO, scanPayBean.getOrderNo()); //平台订单号
                map.put(CommonParams.BATCHNO,scanPayBean.getBatchNo()); //批次号
                map.put(CommonParams.DLD_TXN_LOGID,scanPayBean.getTransNo()); //原始交易流水号
            }
        }

        String md5_key = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.MD5_KEY);
        if (!StringUtil.isEmpty(md5_key)){
            scanPayBean.setMd5_key(md5_key);
        }
        String transNo = ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO); //流水号
        scanPayBean.setRequestId(System.currentTimeMillis()+transNo);
        String merchantId = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID); //扫码商户号
        if (!StringUtil.isEmpty(merchantId)){
            scanPayBean.setMerchantId(merchantId);
        }
        //初始化公共请求参数值
      //  AsyncHttpUtil.setCommonBean(scanPayBean);
        // 流水号增1
        addTraceNo();
        AsyncHttpUtil.httpPostXutils(map,scanPayBean,new AsyncRequestCallBack<String>() {
            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
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
                     scanPayResponse = DataAnalysisByJson.getInstance().getObjectByString(responseInfo.result, ScanPayRsp.class);
                    if (scanPayResponse != null){
                        LogUtils.i("扫手机支付="+responseInfo.result);
                        if (EncodingEmun.antCompany.getType().equals(scanPayBean.getProjectType())){
                            scanPayBean.setProcessID(scanPayResponse.getReturnCode());
                            if (!StringUtil.isEmpty(scanPayResponse.getReturnCode()) && scanPayResponse.getReturnCode().equals(ReturnCodeParams.SUCESS_CODE)){  //交易成功 <后面细分 ：A,F,S,Z 集中情况>
                                if ("S".equals(scanPayResponse.getResult())){  //交易成功
                                    scanPayBean.setOrderNo(scanPayResponse.getOrderNo());
                                    scanPayBean.setTransCode("00");  //交易结果返回码，成功00,失败，撤销
                                    if (!StringUtil.isEmpty(scanPayResponse.getPayTime()) && scanPayResponse.getPayTime().length() >= 14 ) {
                                        scanPayBean.setYear(scanPayResponse.getPayTime().substring(0, 4));
                                        scanPayBean.setDate(scanPayResponse.getPayTime().substring(4, 8));
                                        scanPayBean.setTime(scanPayResponse.getPayTime().substring(8, 14));
                                    }
                                    if (!StringUtil.isEmpty(scanPayResponse.getPayType())){
                                        scanPayBean.setPayType(scanPayResponse.getPayType());
                                    }
                                    if (!StringUtil.isEmpty(scanPayResponse.getPayChannel())) {
                                        scanPayBean.setPayChannel(scanPayResponse.getPayChannel());
                                    }
                                    if (TransParamsValue.InterfaceType.SCAN_POS_QUERY.equals(scanPayResponse.getType())){ //扫码查询接口
                                        if (!StringUtil.isEmpty(String.valueOf(scanPayResponse.getAmount()))){
                                            scanPayBean.setAmount(scanPayResponse.getAmount());
                                            scanPayBean.setTotalAmount(scanPayResponse.getAmount());
                                        }
                                        if (!StringUtil.isEmpty(scanPayResponse.getLogNo())){
                                            scanPayBean.setLogNo(scanPayResponse.getLogNo());
                                        }
                                    }
                                    //增加流水打印明细
                                    addWater();
                                }
                            }else if ("POS00034".equals(scanPayResponse.getReturnCode())){
                                resultStatus.setRetCode(scanPayResponse.getReturnCode());
                                resultStatus.setErrMsg(scanPayResponse.getMessage());
                                ScanCache.getInstance().setResultStatus(resultStatus);
                                sendEmptyMessage(0x03);
                            }else {  //POS00035 为等待授权支付
                                if ("A".equals(scanPayResponse.getResult())){  //等待授权
                                    if (null == scanPayResponse.getOrderNo()){
                                        scanPayBean.setOrderNo(scanPayBean.getOrderNo());
                                        ToastUtils.showToast("请在手机上输入支付密码");
                                    }else {
                                        scanPayBean.setOrderNo(scanPayResponse.getOrderNo()); //订单号
                                    }
                                    //等待执行用户的输入密码交易的操作
                                    pwdCheckStep();
                                }else {  //失败操作
                                    resultStatus.setRetCode(scanPayResponse.getReturnCode());
                                    resultStatus.setErrMsg(scanPayResponse.getMessage());
                                    ScanCache.getInstance().setResultStatus(resultStatus);
                                    sendEmptyMessage(0x03);
                                }
                            }
                        }else {
                            scanPayBean.setProcessID(scanPayResponse.getReturnCode());
                            if (TransParamsValue.InterfaceType.SCAN_QUER.equals(scanPayResponse.getType())){
                                if (!StringUtil.isEmpty(scanPayResponse.getReturnCode()) && scanPayResponse.getReturnCode().equals(ReturnCodeParams.SUCESS_CODE)){
                                    if ("S".equals(scanPayResponse.getTxnSts())){
                                        if (!StringUtil.isEmpty(String.valueOf(scanPayResponse.getAmount()))){
                                            scanPayBean.setAmount(scanPayResponse.getAmount());
                                            scanPayBean.setTotalAmount(scanPayResponse.getAmount());
                                        }
                                        if (!StringUtil.isEmpty(scanPayResponse.getLogNo())){
                                            scanPayBean.setLogNo(scanPayResponse.getLogNo());
                                        }
                                        if (!StringUtil.isEmpty(scanPayResponse.getTranDtTm())&& scanPayResponse.getTranDtTm().length() >= 14){
                                            scanPayBean.setYear(scanPayResponse.getTranDtTm().substring(0, 4));
                                            scanPayBean.setDate(scanPayResponse.getTranDtTm().substring(4, 8));
                                            scanPayBean.setTime(scanPayResponse.getTranDtTm().substring(8, 14));
                                        }
                                    }else {
                                        resultStatus.setRetCode(scanPayResponse.getReturnCode());
                                        resultStatus.setErrMsg(scanPayResponse.getMessage());
                                        ScanCache.getInstance().setResultStatus(resultStatus);
                                        sendEmptyMessage(0x03);
                                    }
                                    //增加等待授权所有的流水交易(包括失败都要添加到数据库)
                                    addWater();
                                }else {
                                    resultStatus.setRetCode(scanPayResponse.getReturnCode());
                                    resultStatus.setErrMsg(scanPayResponse.getMessage());
                                    ScanCache.getInstance().setResultStatus(resultStatus);
                                    sendEmptyMessage(0x03);
                                }
                            }else {
                                if (!StringUtil.isEmpty(scanPayResponse.getReturnCode()) && scanPayResponse.getReturnCode().equals(ReturnCodeParams.SUCESS_CODE)){
                                    if ("S".equals(scanPayResponse.getResult())){  //交易成功
                                        if (!StringUtil.isEmpty(scanPayResponse.getPayTime()) && scanPayResponse.getPayTime().length() >= 14 ) {
                                            scanPayBean.setYear(scanPayResponse.getPayTime().substring(0, 4));
                                            scanPayBean.setDate(scanPayResponse.getPayTime().substring(4, 8));
                                            scanPayBean.setTime(scanPayResponse.getPayTime().substring(8, 14));
                                        }
                                        if (!StringUtil.isEmpty(scanPayResponse.getPayType())){
                                            scanPayBean.setPayType(scanPayResponse.getPayType());
                                        }
                                        if (!StringUtil.isEmpty(scanPayResponse.getPayChannel())) {
                                            scanPayBean.setPayChannel(scanPayResponse.getPayChannel());
                                        }
                                        if (!StringUtil.isEmpty(String.valueOf(scanPayResponse.getTotal_amount()))){
                                            scanPayBean.setAmount(scanPayResponse.getTotal_amount());
                                            scanPayBean.setTotalAmount(scanPayResponse.getTotal_amount());
                                        }
                                        if (!StringUtil.isEmpty(scanPayResponse.getOrderNo())){
                                            scanPayBean.setOrderNo(scanPayResponse.getOrderNo());
                                        }
                                        //增加流水打印明细
                                        addWater();
                                    }else {
                                        resultStatus.setRetCode(scanPayResponse.getReturnCode());
                                        resultStatus.setErrMsg(scanPayResponse.getMessage());
                                        ScanCache.getInstance().setResultStatus(resultStatus);
                                        sendEmptyMessage(0x03);
                                    }
                                }else if ("POS00034".equals(scanPayResponse.getReturnCode())){
                                    resultStatus.setErrMsg(scanPayResponse.getMessage());
                                    resultStatus.setRetCode(scanPayResponse.getReturnCode());
                                    resultStatus.setSucess(false);
                                    ScanCache.getInstance().setResultStatus(resultStatus);
                                    sendMessage( handler.obtainMessage(0x03));
                                }else if ("POS00035".equals(scanPayResponse.getReturnCode())){
                                    if ("A".equals(scanPayResponse.getResult())){  //等待授权
                                        if (null == scanPayResponse.getOrderNo()){
                                            scanPayBean.setOrderNo(scanPayBean.getOrderNo());
                                            ToastUtils.showToast(context.getString(R.string.str_input_deal_pw));
                                        }else {
                                            scanPayBean.setOrderNo(scanPayResponse.getOrderNo()); //订单号
                                        }
                                        scanPayBean.setRequestId(scanPayResponse.getRequestId()); //商户请求号
                                        //等待执行用户的输入密码交易的操作
                                        pwdCheckStep();
                                    }else {  //失败操作
                                        resultStatus.setRetCode(scanPayResponse.getReturnCode());
                                        resultStatus.setErrMsg(scanPayResponse.getMessage());
                                        ScanCache.getInstance().setResultStatus(resultStatus);
                                        sendEmptyMessage(0x03);
                                    }
                                }else {
                                    resultStatus.setRetCode(scanPayResponse.getReturnCode());
                                    resultStatus.setErrMsg(scanPayResponse.getMessage());
                                    ScanCache.getInstance().setResultStatus(resultStatus);
                                    sendEmptyMessage(0x03);
                                }
                            }
                        }
                    }else {
                        resultStatus.setRetCode(TransException.ERR_DAT_JSON_E207);
                        resultStatus.setErrMsg(TransException.getMsg(TransException.ERR_DAT_JSON_E207));
                        ScanCache.getInstance().setResultStatus(resultStatus);
                        sendEmptyMessage(0x03);
                    }
                }else {
                    resultStatus.setRetCode(TransException.ERR_DAT_EOF_E208);
                    resultStatus.setErrMsg(TransException.getMsg(TransException.ERR_DAT_EOF_E208));
                    ScanCache.getInstance().setResultStatus(resultStatus);
                    sendEmptyMessage(0x03);
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                Message message = null;
                if (scanPayBean.getType() == TransType.ScanTransType.TRANS_SCAN_WEIXIN){
                    message = handler.obtainMessage(0x11, context.getString(R.string.scan_wx_view));
                }else if (scanPayBean.getType() == TransType.ScanTransType.TRANS_SCAN_ALIPAY){
                    message = handler.obtainMessage(0x11, context.getString(R.string.scan_pay_alipy));
                }else {
                    message = handler.obtainMessage(0x11, context.getString(R.string.scan_pay_ing));
                }
                sendMessage(message);
            }
        });
    }


    /**
     * 授权等待的操作步骤
     * @return
     * */
    public void  pwdCheckStep(){
        final int oldType = scanPayBean.getType(); //原交易类型
        final String oldTransNo = scanPayBean.getTransNo();//原流水号
        String oldChlDate = scanPayBean.getChlDate(); //原交易日期
        String requestId = scanPayBean.getRequestId(); //原商户请求号

        MessageTipBean messageTip = new MessageTipBean();
        messageTip.setTitle(scanPayBean.getTransName());
        messageTip.setContent(context.getString(R.string.pay_sucess_fail));
        messageTip.setCancelable(false);
        messageTip.setShowIcon(true);
        messageTip.setOldBabtchNo(oldTransNo);
        messageTip.setOldTransType(oldType);
        messageTip.setTerminalNo(scanPayBean.getTerminalNo());
        messageTip.setOldOrderNo(scanPayBean.getOrderNo());
        messageTip.setCurrency(scanPayBean.getCurrency());
        messageTip.setAuthCode(scanPayBean.getScanResult());
        messageTip.setOldChlDate(oldChlDate);
        messageTip.setRequestId(requestId);
        messageTip.setReQuestURL(scanPayBean.getRequestUrl());
        messageTip.setProjectType(scanPayBean.getProjectType());

        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putSerializable("message",messageTip);
        message.setData(bundle);
        message.what = 0x15;
        handler.sendMessage(message);

    }


    /*
    *
    * 增加流水
     *
    */
    private synchronized  void addWater() {
        ScanTransRecord water = getWater(scanPayBean);
        addLocalWater(water);
    }

    /*
     *
     * 添加流水到本地数据库
     * @param water
     *
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
                        LogUtils.i("water== "+water.getSystraceno());
                        //接下来是打印步骤
                        addPrinter(water);
                    } catch (DuplicatedTraceException e) {
                        resultStatus.setSucess(false);
                        resultStatus.setRetCode(water.getRespcode());
                        resultStatus.setErrMsg("重复插入流水，交易失败");
                        ScanCache.getInstance().setResultStatus(resultStatus);
                        dealException(e,resultStatus);
                        sendEmptyMessage(0x03);
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    /*
     *
     * 增添打印
     *
    */

    private void addPrinter(ScanTransRecord water) {
        if (EncodingEmun.maCaoProject.getType().equals(scanPayBean.getPayType())){  //-->澳门项目
            if (TransParamsValue.InterfaceType.SCAN_QUER.equals(scanPayResponse.getType())){
                if (!(scanPayResponse != null &&
                        !TextUtils.isEmpty(scanPayResponse.getTxnSts()) &&
                        (TextUtils.equals(scanPayResponse.getTxnSts(), "F") ||
                                TextUtils.equals(scanPayResponse.getTxnSts(), "S")))) {
                    sendMessage(handler.obtainMessage(0x03,"交易未知"));
                }
            }else {
                if (!(scanPayResponse != null &&
                        !TextUtils.isEmpty(scanPayResponse.getResult()) &&
                        (TextUtils.equals(scanPayResponse.getResult(), "F") ||
                                TextUtils.equals(scanPayResponse.getResult(), "S")))) {
                    sendMessage(handler.obtainMessage(0x03,"交易未知"));
                }
            }
        }else {  //---》蚂蚁企服
            if (!(scanPayResponse != null &&
                    !TextUtils.isEmpty(scanPayResponse.getResult()) &&
                    (TextUtils.equals(scanPayResponse.getResult(), "F") ||
                            TextUtils.equals(scanPayResponse.getResult(), "S")))) {
                sendMessage(handler.obtainMessage(0x03,"交易未知"));
            }
        }
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        if (water != null){
            bundle.putSerializable("water",water);//流水类
        }
        message.setData(bundle);
        message.what = 0x16;
        handler.sendMessage(message);
    }

    /*
     *
     * 获取交易流水
     * @param scanPayBean
     * @return
    */


    private ScanTransRecord getWater(ScanPayBean scanPayBean) {
        ScanTransRecord record = new ScanTransRecord();
        try {
            record.setTransType(scanPayBean.getType()); //交易类型
            record.setPayChannel(scanPayBean.getPayChannel() != null ? scanPayBean.getPayChannel() : ""); //交易渠道
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
            record.setIsrevoke(String.valueOf(TransType.TransStatueType.NORMAL));  //是否被撤销 0 表示初始化， 1 已撤销
            record.setRespcode(scanPayBean.getProcessID()); //应答码
        }catch (Exception e){
            e.printStackTrace();
            dealException(e,resultStatus);
            sendEmptyMessage(0x03);
        }
        return record;
    }


    private String getTransName(int transType){
        switch (transType){
            case TransType.ScanTransType.TRANS_SCAN_WEIXIN:
            case TransType.ScanTransType.TRANS_SCAN_POS_CHECK:
                return context.getString(R.string.scan_wx);
            case TransType.ScanTransType.TRANS_SCAN_ALIPAY:
                return  context.getString(R.string.scan_aplpy);
            default:
                return  context.getString(R.string.no_pay);
        }
    }

    /*
    *
    * 增加流水号
    */

    private void addTraceNo() {
        String traceNo = ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO);
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
