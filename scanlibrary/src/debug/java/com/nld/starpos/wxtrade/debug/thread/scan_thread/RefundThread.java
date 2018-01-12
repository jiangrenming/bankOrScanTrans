package com.nld.starpos.wxtrade.debug.thread.scan_thread;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.nld.netlibrary.xutils.AsyncRequestCallBack;
import com.nld.starpos.wxtrade.R;
import com.nld.starpos.wxtrade.debug.bean.scan_common.ResultStatus;
import com.nld.starpos.wxtrade.debug.bean.scan_common.ScanCache;
import com.nld.starpos.wxtrade.debug.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.debug.bean.scan_pay.ScanPayRsp;
import com.nld.starpos.wxtrade.debug.bean.scan_refund.ReverseRsp;
import com.nld.starpos.wxtrade.debug.exception.DuplicatedTraceException;
import com.nld.starpos.wxtrade.debug.exception.TransException;
import com.nld.starpos.wxtrade.debug.http.AsyncHttpUtil;
import com.nld.starpos.wxtrade.debug.local.db.ScanTransDao;
import com.nld.starpos.wxtrade.debug.local.db.bean.ScanTransRecord;
import com.nld.starpos.wxtrade.debug.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.debug.local.db.imp.ScanTransDaoImp;
import com.nld.starpos.wxtrade.debug.thread.ComonThread;
import com.nld.starpos.wxtrade.debug.utils.FormatUtils;
import com.nld.starpos.wxtrade.debug.utils.ToastUtils;
import com.nld.starpos.wxtrade.debug.utils.jsonUtils.DataAnalysisByJson;
import com.nld.starpos.wxtrade.debug.utils.params.CommonParams;
import com.nld.starpos.wxtrade.debug.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.debug.utils.params.ReturnCodeParams;
import com.nld.starpos.wxtrade.debug.utils.params.ScanTransUtils;
import com.nld.starpos.wxtrade.debug.utils.params.TransParamsValue;
import com.nld.starpos.wxtrade.debug.utils.params.TransType;

import java.util.List;
import java.util.TreeMap;

import common.StringUtil;


/**
 * @author Xrh
 * @description 微信被扫支付退货
 */
public class RefundThread extends ComonThread {

    private ScanPayBean mRefundBean ;
    private ResultStatus result;
    private ReverseRsp reverseRsp;
    private ScanPayRsp scanPayResponse;

    public RefundThread(Context context, Handler handler, ScanPayBean scanRefundBean) {
        super(context, handler);
        this.mRefundBean = scanRefundBean;
        result = new ResultStatus();
    }

    @Override
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {

        final TreeMap<String, String> map = new TreeMap<String, String>();
        if (mRefundBean.getTransType().equals(TransParamsValue.InterfaceType.SCAN_REFUND)){

            if (EncodingEmun.maCaoProject.getType().equals(mRefundBean.getProjectType())){

                map.put(CommonParams.TYPE,mRefundBean.getTransType()); //接口类型
                map.put(CommonParams.SN,mRefundBean.getSn()); //序列号
                map.put(CommonParams.TRM_NO,mRefundBean.getTerminalNo()) ; //终端号
                map.put(CommonParams.TXN_CNL, mRefundBean.getTxnCnl()); //支付渠道
                map.put(CommonParams.OLD_ORDER_NO,mRefundBean.getOrderNo()) ; //退货订单号
                if (mRefundBean.getAmount() != null){
                    map.put(CommonParams.REF_AMT,String.valueOf(mRefundBean.getAmount())); //退款金额
                }
                map.put(CommonParams.LOG_TYPE, EncodingEmun.ORDERNO_TYPE.getType()); //退款类型

            }else {
                map.put(CommonParams.TYPE,mRefundBean.getTransType()); //接口类型
                map.put(CommonParams.TERMINAL_NO,mRefundBean.getTerminalNo()) ; //终端号
                map.put(CommonParams.TXN_CNL, mRefundBean.getTxnCnl()); //支付渠道
                map.put(CommonParams.ORDER_ID, mRefundBean.getOrderId()); //流水号
                map.put(CommonParams.ORDER_NO,mRefundBean.getOrderNo()) ; //退货订单号
                map.put(CommonParams.SN,mRefundBean.getSn()); //序列号
                map.put(CommonParams.BATCHNO,mRefundBean.getBatchNo()); //批次号
            }
        }else {
            //扫码支付查询的接口参数
            map.put(CommonParams.TYPE, mRefundBean.getTransType());//接口类型
            map.put(CommonParams.OLD_REQUEST_ID, mRefundBean.getOldRequestId()); //原商户请求号
            map.put(CommonParams.QUR_TYP, mRefundBean.getQryTyp());  //查询类型
            map.put(CommonParams.TRM_NO, mRefundBean.getTerminalNo()); //终端编号
        }

        String md5_key = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.MD5_KEY);
        if (!StringUtil.isEmpty(md5_key)){
            mRefundBean.setMd5_key(md5_key);
        }
        String transNo = ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO); //流水号
        mRefundBean.setRequestId(System.currentTimeMillis() + transNo);
        String merchantId = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID); //扫码商户号
        if (!StringUtil.isEmpty(merchantId)){
            mRefundBean.setMerchantId(merchantId);
        }

      //  AsyncHttpUtil.setCommonBean(mRefundBean);
        addTraceNo();

        AsyncHttpUtil.httpPostXutils(map,mRefundBean, new AsyncRequestCallBack<String>() {
            @Override
            public void onFailure(HttpException httpException, String errorMsg) {
                super.onFailure(httpException, errorMsg);
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
                    Log.i("TAG","扫码退货的返回数据="+responseInfo.result);
                    if (mRefundBean.getTransType().equals(TransParamsValue.InterfaceType.SCAN_REFUND)){
                        reverseRsp = DataAnalysisByJson.getInstance().getObjectByString(responseInfo.result, ReverseRsp.class);
                        if (reverseRsp != null){
                            if (EncodingEmun.antCompany.getType().equals(mRefundBean.getProjectType())){
                                mRefundBean.setProcessId(reverseRsp.getReturnCode());
                                if (ReturnCodeParams.SUCESS_CODE.equals(reverseRsp.getReturnCode())) {
                                    String code = reverseRsp.getResult();
                                    if ("S".equals(code)) { //撤销成功
                                        mRefundBean.setTransCode("00");
                                        if (!StringUtil.isEmpty(reverseRsp.getAmount())){
                                            mRefundBean.setAmount(FormatUtils.parseLong(reverseRsp.getAmount()));
                                            mRefundBean.setTotal_amount(FormatUtils.parseLong(reverseRsp.getAmount()));
                                        }
                                        addWater();
                                    }else {
                                        result.setRetCode(reverseRsp.getReturnCode());
                                        result.setErrMsg(reverseRsp.getMessage());
                                        result.setSucess(false);
                                        ScanCache.getInstance().setResultStatus(result);
                                        sendEmptyMessage(0x01);
                                    }
                                } else if ("POS20301".equals(reverseRsp.getReturnCode())){
                                    sendEmptyMessage(0x02);
                                }else {
                                    result.setRetCode(reverseRsp.getReturnCode());
                                    result.setErrMsg(reverseRsp.getMessage());
                                    result.setSucess(false);
                                    ScanCache.getInstance().setResultStatus(result);
                                    sendEmptyMessage(0x01);
                                }
                            }else {
                                mRefundBean.setProcessId(reverseRsp.getReturnCode());
                                if (ReturnCodeParams.SUCESS_CODE.equals(reverseRsp.getReturnCode())) {
                                    String code = reverseRsp.getResult();
                                    if ("S".equals(code)) { //撤销成功
                                        mRefundBean.setTransCode("00");
                                        if (!StringUtil.isEmpty(reverseRsp.getRefAmt())){
                                            mRefundBean.setAmount(FormatUtils.parseLong(reverseRsp.getRefAmt()));
                                            mRefundBean.setTotal_amount(FormatUtils.parseLong(reverseRsp.getRefAmt()));
                                        }
                                        if (!StringUtil.isEmpty(reverseRsp.getChlDate()) && reverseRsp.getChlDate().length() >= 14){
                                            mRefundBean.setYear(reverseRsp.getChlDate().substring(0,4));
                                            mRefundBean.setDate(reverseRsp.getChlDate().substring(4,8));
                                            mRefundBean.setTime(reverseRsp.getChlDate().substring(8,14));
                                        }
                                        if (!StringUtil.isEmpty(reverseRsp.getOrderNo())){
                                            mRefundBean.setOrderNo(reverseRsp.getOrderNo());
                                        }
                                        addWater();
                                    }else {
                                        result.setRetCode(reverseRsp.getReturnCode());
                                        result.setErrMsg(reverseRsp.getMessage());
                                        result.setSucess(false);
                                        ScanCache.getInstance().setResultStatus(result);
                                        sendEmptyMessage(0x01);
                                    }
                                } else if ("POS20301".equals(reverseRsp.getReturnCode())){
                                    sendEmptyMessage(0x02);
                                }else if ("POS00035".equals(reverseRsp.getReturnCode())){
                                    String code = reverseRsp.getResult();
                                    if ("T".equals(code)){ //交易未知状态轮询
                                        mRefundBean.setTransType(TransParamsValue.InterfaceType.SCAN_QUER); //查询接口
                                        mRefundBean.setQryTyp(EncodingEmun.REQID_TYEP.getType()); //查询类别
                                        mRefundBean.setOldRequestId(reverseRsp.getRequestId());  //商户请求号
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("refund",mRefundBean);
                                        Message message = Message.obtain();
                                        message.setData(bundle);
                                        message.what = 0x03;
                                        handler.sendMessage(message);
                                    }
                                }else {
                                    result.setRetCode(reverseRsp.getReturnCode());
                                    result.setErrMsg(reverseRsp.getMessage());
                                    result.setSucess(false);
                                    ScanCache.getInstance().setResultStatus(result);
                                    sendEmptyMessage(0x01);
                                }
                            }
                        }else {
                            result.setRetCode(TransException.ERR_DAT_JSON_E207);
                            result.setErrMsg(TransException.getMsg(TransException.ERR_DAT_JSON_E207));
                            result.setSucess(false);
                            ScanCache.getInstance().setResultStatus(result);
                            sendEmptyMessage(0x01);
                        }
                    }else {
                          scanPayResponse = DataAnalysisByJson.getInstance().getObjectByString(responseInfo.result, ScanPayRsp.class);
                        if (scanPayResponse != null){
                            if (ReturnCodeParams.SUCESS_CODE.equals(scanPayResponse.getReturnCode())){
                                if ("S".equals(scanPayResponse.getTxnSts())){
                                    mRefundBean.setTransCode("00");
                                    if (!StringUtil.isEmpty(String.valueOf(scanPayResponse.getAmount()))){
                                        mRefundBean.setAmount(scanPayResponse.getAmount());
                                        mRefundBean.setTotal_amount(scanPayResponse.getAmount());
                                    }
                                    if (!StringUtil.isEmpty(scanPayResponse.getLogNo())){
                                        mRefundBean.setLogNo(scanPayResponse.getLogNo());
                                    }
                                    if (!StringUtil.isEmpty(scanPayResponse.getTranDtTm())&& scanPayResponse.getTranDtTm().length() >= 14){
                                        mRefundBean.setYear(scanPayResponse.getTranDtTm().substring(0, 4));
                                        mRefundBean.setDate(scanPayResponse.getTranDtTm().substring(4, 8));
                                        mRefundBean.setTime(scanPayResponse.getTranDtTm().substring(8, 14));
                                    }
                                    if (!StringUtil.isEmpty(scanPayResponse.getOrderNo())){
                                        mRefundBean.setOrderNo(scanPayResponse.getOrderNo());
                                    }
                                    addWater();
                                }else {
                                    result.setRetCode(reverseRsp.getReturnCode());
                                    result.setErrMsg(reverseRsp.getMessage());
                                    result.setSucess(false);
                                    ScanCache.getInstance().setResultStatus(result);
                                    sendEmptyMessage(0x01);
                                }
                            }else {
                                result.setRetCode(reverseRsp.getReturnCode());
                                result.setErrMsg(reverseRsp.getMessage());
                                result.setSucess(false);
                                ScanCache.getInstance().setResultStatus(result);
                                sendEmptyMessage(0x01);
                            }
                        }else {
                            result.setRetCode(TransException.ERR_DAT_JSON_E207);
                            result.setErrMsg(TransException.getMsg(TransException.ERR_DAT_JSON_E207));
                            result.setSucess(false);
                            ScanCache.getInstance().setResultStatus(result);
                            sendEmptyMessage(0x01);
                        }
                    }
                }else {
                    result.setRetCode(TransException.ERR_DAT_EOF_E208);
                    result.setErrMsg(TransException.getMsg(TransException.ERR_DAT_EOF_E208));
                    result.setSucess(false);
                    ScanCache.getInstance().setResultStatus(result);
                    sendEmptyMessage(0x01);
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                super.onLoading(total, current, isUploading);
                sendMessage( handler.obtainMessage(0x11,context.getString(R.string.scan_refund_view)));
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


    /**
     * 增加流水
     */
    private synchronized  void addWater() {
        ScanTransRecord water = getWater(mRefundBean);
        addLocalWater(water);
    }

    private void addLocalWater(final ScanTransRecord water) {
        if (null != water){
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    ScanTransDao scanTransDao = new ScanTransDaoImp();
                    try {
                        scanTransDao.addWater(water);
                        result.setSucess(true);
                        result.setRetCode(water.getRespcode());
                        result.setRecord(water);
                        Log.i("TAG","water== "+water.getSystraceno()+"/原交易订单号="+mRefundBean.getOrderNo());
                        //需要更新数据库
                       /* List<ScanTransRecord> oldOrderNo = scanTransDao.findByOrderNo(mRefundBean.getOrderNo());
                        if (oldOrderNo != null && oldOrderNo.size() > 0){
                            ScanTransRecord oldWater = oldOrderNo.get(0);
                            switch (oldWater.getTransType()) {
                                case TransType.ScanTransType.TRANS_SCAN_WEIXIN:
                                case TransType.ScanTransType.TRANS_SCAN_ALIPAY:
                                case TransType.ScanTransType.TRANS_QR_WEIXIN:
                                case TransType.ScanTransType.TRANS_QR_ALIPAY:
                                case TransType.ScanTransType.TRANS_SCAN_POS_CHECK:
                                    // 更新原交易流水
                                    Long oldAmount = Long.valueOf(oldWater.getAmount());
                                    Long amount = mRefundBean.getAmount();
                                    if (amount < oldAmount) { // 部分退款
                                        oldWater.setIsrevoke(String.valueOf(TransType.TransStatueType.REBATE));
                                    } else if (amount.equals(oldAmount)) { // 已全额退款
                                        oldWater.setIsrevoke(String.valueOf(TransType.TransStatueType.RETURN));
                                    }
                                    scanTransDao.updateWater(oldWater);
                                    break;
                            }
                        }*/
                        //接下来是打印步骤
                        addPrinter(water);

                    } catch (DuplicatedTraceException e) {
                        dealException(e,result);
                        sendEmptyMessage(0x01);
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    private void addPrinter(ScanTransRecord water) {
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        if (water != null){
            bundle.putSerializable("water",water);//流水类
        }
        message.setData(bundle);
        message.what = 0x00;
        handler.sendMessage(message);
    }

    /**
     * 获取交易流水
     * @param mRefundBean
     * @return
     */
    private ScanTransRecord getWater(ScanPayBean mRefundBean) {
        Log.i("TAG金额","/流水号="+mRefundBean.getOrderId());
        ScanTransRecord record = new ScanTransRecord();
        ScanTransDao scanTransDao = new ScanTransDaoImp();
        try {
            record.setTransType(mRefundBean.getType()); //交易类型
            record.setPayChannel(mRefundBean.getPayChannel()); //交易渠道
            record.setTerminalId(mRefundBean.getTerminalNo()); //终端号
            record.setSystraceno(mRefundBean.getOrderId()); //POS流水号
            record.setLogNo(mRefundBean.getLogNo());  //系统流水号<内部订单号>
            record.setPayType(mRefundBean.getPayType()); //支付类型
            record.setBatchbillno(mRefundBean.getBatchNo());  //批次号
            record.setMemberId( ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID)); //商户号
            record.setMemberName(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME)); //商户名称
            record.setOper("001"); //操作员  ，暂时是默认的
            record.setOrderNo(mRefundBean.getOrderNo()); //订单号
            if (StringUtil.isEmpty(mRefundBean.getDate()) || StringUtil.isEmpty(mRefundBean.getTime()) || StringUtil.isEmpty(mRefundBean.getYear()) ){
                record.setScanYear(null);
                record.setScanDate(null);
                record.setScanTime(null);
            }else {
                record.setScanYear(mRefundBean.getYear());
                record.setScanDate((mRefundBean.getDate().substring(0,2)+"/"+mRefundBean.getDate().substring(2,4))); //日期
                record.setScanTime((mRefundBean.getTime().substring(0,2)+":"+mRefundBean.getTime().substring(2,4)+":"+mRefundBean.getTime().substring(4,6))); //时间
            }
            record.setTransamount(String.valueOf(mRefundBean.getAmount())); //交易金额
             record.setTotalAmount(String.valueOf(mRefundBean.getTotal_amount())); //交易总金额
            record.setAdddataword(mRefundBean.getAddtionInfo()!= null ? mRefundBean.getAddtionInfo() : ""); //附加信息
            record.setAuthCode(mRefundBean.getScanResult()); //授权码
            record.setSettledata(mRefundBean.getSettleData() != null ? mRefundBean.getSettleData() : "");  //结算日期
            record.setTranscurrcode(mRefundBean.getCurrency()); //币种
            record.setStatuscode(mRefundBean.getTransCode()); //交易结果码
            record.setTransprocode("0x00"); //消费(0x00)，消费撤销(0x01)
            record.setRespcode(mRefundBean.getProcessId()); //应答码
            List<ScanTransRecord> list = scanTransDao.findByOrderNo(mRefundBean.getOrderNo());
            if (list != null && list.size() > 0){
                ScanTransRecord oldWater = list.get(0);
                String oldAmount ="";
                String transamount = oldWater.getTransamount();
                String totalAmount = oldWater.getTotalAmount();
                if(!StringUtil.isEmpty(transamount)){
                    oldAmount = transamount ;
                }else {
                    if (!StringUtil.isEmpty(totalAmount)){
                        oldAmount =totalAmount;
                    }
                }
                Long oldTransAmount = Long.valueOf(oldAmount)*100;
                Long amount = mRefundBean.getAmount();
                if (amount < oldTransAmount) { // 部分退款
                    record.setIsrevoke(String.valueOf(TransType.TransStatueType.REBATE));
                } else if (amount.equals(oldAmount)) { // 已全额退款
                    record.setIsrevoke(String.valueOf(TransType.TransStatueType.RETURN));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            dealException(e,result);
            sendEmptyMessage(0x01);
        }
        return record;
    }


    @Override
    public byte[] getIsopack() {
        return  new byte[0];
    }
}