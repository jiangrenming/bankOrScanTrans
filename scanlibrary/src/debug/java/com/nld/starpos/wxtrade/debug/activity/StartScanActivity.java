package com.nld.starpos.wxtrade.debug.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.util.LogUtils;
import com.nld.starpos.wxtrade.R;
import com.nld.starpos.wxtrade.debug.bean.scan_common.ScanCache;
import com.nld.starpos.wxtrade.debug.bean.scan_pay.MessageTipBean;
import com.nld.starpos.wxtrade.debug.bean.scan_pay.ScanPayBean;
import com.nld.starpos.wxtrade.debug.bean.scan_query.ScanQueryDataBean;
import com.nld.starpos.wxtrade.debug.bean.scan_settle.ScanSettleRes;
import com.nld.starpos.wxtrade.debug.exception.TransException;
import com.nld.starpos.wxtrade.debug.local.db.ScanTransDao;
import com.nld.starpos.wxtrade.debug.local.db.bean.ScanTransRecord;
import com.nld.starpos.wxtrade.debug.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.debug.local.db.imp.ScanTransDaoImp;
import com.nld.starpos.wxtrade.debug.thread.ComonThread;
import com.nld.starpos.wxtrade.debug.thread.scan_thread.AsyScanBatchNoThread;
import com.nld.starpos.wxtrade.debug.thread.scan_thread.CheckPwThread;
import com.nld.starpos.wxtrade.debug.thread.scan_thread.OperPassWordThread;
import com.nld.starpos.wxtrade.debug.thread.scan_thread.QCScanPayThread;
import com.nld.starpos.wxtrade.debug.thread.scan_thread.QueryThread;
import com.nld.starpos.wxtrade.debug.thread.scan_thread.RefundThread;
import com.nld.starpos.wxtrade.debug.thread.scan_thread.ScanSettleThread;
import com.nld.starpos.wxtrade.debug.thread.scan_thread.ScanpayThread;
import com.nld.starpos.wxtrade.debug.utils.FormatUtils;
import com.nld.starpos.wxtrade.debug.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.debug.utils.jsonUtils.DataAnalysisByJson;
import com.nld.starpos.wxtrade.debug.utils.params.EncodingEmun;
import com.nld.starpos.wxtrade.debug.utils.params.ScanTransFlagUtil;
import com.nld.starpos.wxtrade.debug.utils.params.ScanTransUtils;
import com.nld.starpos.wxtrade.debug.utils.params.TransParamsValue;
import com.nld.starpos.wxtrade.debug.utils.params.TransType;

import java.util.ArrayList;

import common.StringUtil;

/**
 * Created by jiangrenming on 2017/10/31.
 */

public class StartScanActivity extends Activity {

    private ImageView imgView;
    private TextView count_time;
    private TextView nettip;
    private String transCode;
    private CountThread mCountThread;
    private ComonThread mComonThread;
    // 默认超时时间
    private static final int NETWORK_TIMEOUT = 60;
    private ScanPayBean mScanBean;
    private ScanSettleRes scanSettleRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.network);
        imgView = (ImageView)findViewById(R.id.network_imgview);
        Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        imgView.setAnimation(operatingAnim);
        operatingAnim.start();
        count_time = (TextView) findViewById(R.id.net_coutdowntime_tv);
        count_time.setText(NETWORK_TIMEOUT + "");
        nettip = (TextView)findViewById(R.id.nettip_tv);
        transCode = ScanCache.getInstance().getTransCode();
        if (!ScanTransFlagUtil.TRANS_CODE_SIGN.equals(transCode)) {
            nettip.setText(getString(R.string.str_dealling));
        }
        startTransThread();
    }

    private void startTransThread() {
        startCountThread();
         if (ScanTransFlagUtil.TRANS_CODE_WX_PAY.equals(transCode)) { // 扫码支付
            ScanPayBean scanBean = (ScanPayBean) getIntent().getSerializableExtra("scan");
            if (scanBean != null) {
                if (TransParamsValue.InterfaceType.WXPOS.equals(scanBean.getTransType())) {  //扫手机
                   mComonThread = new ScanpayThread(this, scanpayHandler, scanBean);
                   new Thread(mComonThread).start();
                } else {  //扫pos支付
                    mComonThread = new QCScanPayThread(this, qcScanPayHandler, scanBean);
                    new Thread(mComonThread).start();
                }
            }
        }else if (ScanTransFlagUtil.TRANS_CODE_WX_TH.equals(transCode)) { // 扫码退货
            ScanPayBean scanRefundBean = (ScanPayBean) getIntent().getSerializableExtra("scan");
            if (scanRefundBean != null) {
                mComonThread = new RefundThread(this, refundHandler, scanRefundBean);
                new Thread(mComonThread).start();
            }
        } else if (ScanTransFlagUtil.TRANS_CODE_WX_QUERY.equals(transCode)){  //扫码查询
             ScanPayBean scanRefundBean = (ScanPayBean) getIntent().getSerializableExtra("scan");
            if (scanRefundBean != null) {
                mComonThread = new QueryThread(this, queryHandler, scanRefundBean);
                new Thread(mComonThread).start();
            }
        }else if (ScanTransFlagUtil.TRANS_CODE_WX_SETTLE.equals(transCode)){ //扫码批结
            try {
                ScanPayBean scanBean = (ScanPayBean) getIntent().getSerializableExtra("scan");
                mScanBean = scanBean;
                // 1.先看是否存在结算中断的情况
                if (!ShareScanPreferenceUtils.getBoolean(this, TransParamsValue.SettleConts.SETTLE_ALL_FLAG, false)) {  // 批结正常
                    if (null != scanBean) {
                        //设置批结的总标志和二维码批结的总标志《为true》,在清除结算数据时，把总标志置为false;在二维码批结成功时，将标志置为false
                        ShareScanPreferenceUtils.putBoolean(StartScanActivity.this, TransParamsValue.SettleConts.SETTLE_ALL_FLAG, true);  //批结的总标志,
                        ShareScanPreferenceUtils.putBoolean(StartScanActivity.this, TransParamsValue.SettleConts.PARAMS_IS_SCAN_SETTLT_HALT, true); //二维码结算中断标志
                        mComonThread = new ScanSettleThread(StartScanActivity.this, settleHandler, scanBean);
                        new Thread(mComonThread).start();
                    }
                } else {  //非正常的情况下
                    MessageTipBean messageTipBean = new MessageTipBean();
                    messageTipBean.setTitle(getString(R.string.prompt));
                    messageTipBean.setContent(String.format(getString(R.string.settle_exit), ScanTransUtils.getSettleHaltStep(StartScanActivity.this)));
                    messageTipBean.setCancelable(true);
                    messageTipBean.setTimeOut(60);
                    Intent intent = new Intent(StartScanActivity.this, MessageTipActivity.class);
                    intent.putExtra("message", messageTipBean);
                    goNextByResult(intent, TransType.SCAN_SETTLE_WAIT_CODE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(e.getMessage());
            }
        }else if (ScanTransFlagUtil.TRANS_CODE_BATCHNO.equals(transCode)){ //同步批次号
            ScanPayBean scanBean = (ScanPayBean) getIntent().getSerializableExtra("scan");
            if (scanBean != null){
                mComonThread = new AsyScanBatchNoThread(StartScanActivity.this,asyScanHandler,scanBean);
                new Thread(mComonThread).start();
            }
        }else if (ScanTransFlagUtil.PASSWORD_CHANGE.equals(transCode) || ScanTransFlagUtil.OPER_PASSWORD_CHANGE.equals(transCode)){  //主管密码修改,操作员密码重置
             ScanPayBean checkPwBean = (ScanPayBean) getIntent().getSerializableExtra("scan");
             if (null != checkPwBean){
                 mComonThread = new OperPassWordThread(StartScanActivity.this,passWordHandler,checkPwBean);
                 new Thread(mComonThread).start();
             }else {
                 LogUtils.i("接收的数据为:null");
             }
         }else if (ScanTransFlagUtil.OPER_PASSWORD_EXIT.equals(transCode)){  //验证密码
             ScanPayBean checkPwBean = (ScanPayBean) getIntent().getSerializableExtra("scan");
             if (null != checkPwBean ){
                 mComonThread = new CheckPwThread(StartScanActivity.this,pwdCheckHandler,checkPwBean);
                 new Thread(mComonThread).start();
             }
         }
    }

    //密码验证
    private Handler pwdCheckHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:
                    gotoNextCallBack(new Intent());
                    break;
                case 0x02:
                    gotoNextErrorCallBack(new Intent());
                    break;
                case 0x03:
                    String data = (String) msg.obj;
                    if (!StringUtil.isEmpty(data)) {
                        nettip.setText(data);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //密码修改与重置
    private Handler passWordHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:
                   gotoNextErrorCallBack(new Intent());
                    break;
                case  0x02:
                   gotoNextCallBack(new Intent());
                    break;
                case  0x03:
                    String data = (String) msg.obj;
                    if (!StringUtil.isEmpty(data)) {
                        nettip.setText(data);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //扫码批次号同步
    private Handler asyScanHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:
                    gotoNextErrorCallBack(new Intent());
                    break;
                case  0x11:
                    String data = (String) msg.obj;
                    if (!StringUtil.isEmpty(data)) {
                        nettip.setText(data);
                    }
                    break;
                case  0x12:
                   gotoNextCallBack(new Intent());
                    break;
                default:
                    break;
            }
        }
    };

    //扫码批结
    private Handler settleHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x00:
                case 0x01:
                    gotoNextErrorCallBack(new Intent());
                    break;
                case 0x02: //查询批结数据
                    try {
                        final ArrayList<ScanQueryDataBean> scanQueryDataBeen = (ArrayList<ScanQueryDataBean>) msg.getData().getSerializable("total");
                        if (scanQueryDataBeen != null && !scanQueryDataBeen.isEmpty()) {
                            //更新数据库
                            new Thread() {
                                @Override
                                public void run() {
                                    super.run();
                                    //1.更新数据库
                                    //a 删除扫码流水表中的数据
                                    ScanTransDao dao = new ScanTransDaoImp();
                                    dao.clearScanWater();
                                    //b 添加数据
                                    for (ScanQueryDataBean scanQueryDataBean : scanQueryDataBeen) {
                                        if (scanQueryDataBean != null && scanQueryDataBean.getTxn_sts().equals("S")) {
                                            if (!checkNecessaryValue(scanQueryDataBean)) {
                                                return;
                                            }
                                            String settleType = null;
                                            long amount = Long.parseLong(scanQueryDataBean.getTxn_amt());
                                            switch (scanQueryDataBean.getTxn_cd()) {
                                                case TransType.QueryNet.SCANREFUND:
                                                    settleType = TransType.ScanTransType.SCAN_REFUND;
                                                    break;
                                                case TransType.QueryNet.SCAN_PHONE:
                                                    settleType = getSettleTypeFromPaychannel(scanQueryDataBean, true);
                                                    break;
                                                case TransType.QueryNet.SCAN_POS:
                                                    settleType = getSettleTypeFromPaychannel(scanQueryDataBean, false);
                                                    break;
                                                case TransType.QueryNet.MORE_SCAN_PHONE:
                                                    break;
                                                default:
                                                    break;
                                            }
                                            if (!StringUtil.isEmpty(settleType)) {
                                                int transType = getTransTypeBySettleType(settleType, scanQueryDataBean);
                                                if (transType != -1) {
                                                    ScanTransRecord water = new ScanTransRecord();
                                                    water.setTransamount(FormatUtils.formatMount(String.valueOf(amount)));
                                                    water.setBatchbillno(ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO));
                                                    water.setTranscurrcode(EncodingEmun.CNYCURRENCY.getType());
                                                    if (scanQueryDataBean.getAc_dt().length() > 4) {
                                                        water.setScanDate(scanQueryDataBean.getAc_dt().substring(4));
                                                    } else {
                                                        water.setScanDate(scanQueryDataBean.getAc_dt());
                                                    }
                                                    water.setOldTransType(0);
                                                    water.setOper("001"); //操作员  ，暂时是默认的
                                                    water.setPayChannel(scanQueryDataBean.getPaychannel());
                                                    water.setSystraceno(scanQueryDataBean.getCseq_no());
                                                    if (transType != TransType.ScanTransType.TRANS_SCAN_REFUND) {
                                                        water.setIsrevoke(TextUtils.equals(scanQueryDataBean.getMax_ref_amt(), "0") ?
                                                                String.valueOf(TransType.TransStatueType.REV) :
                                                                String.valueOf(TransType.TransStatueType.NORMAL));
                                                    } else {
                                                        water.setIsrevoke(String.valueOf(TransType.TransStatueType.NORMAL));
                                                    }
                                                    water.setTransType(transType); //交易类型
                                                    dao.addWaterCanRepeat(water);
                                                }
                                            }
                                        }
                                    }

                                    // 2.开始批结
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ScanPayBean scanPayBean = new ScanPayBean();
                                            scanPayBean.setType(TransType.ScanTransType.TRANS_SCAN_SETTLE);  //交易类型
                                            scanPayBean.setTransType(TransParamsValue.InterfaceType.SCAN_POST_BATCH_CHK); //交易接口
                                            scanPayBean.setTerminalNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID)); //终端号
                                            scanPayBean.setBatchNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO)); //批次号
                                            scanPayBean.setTransNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO)); //流水号
                                            scanPayBean.setPhWeiXinAmt(String.valueOf(wxScanPosAmount));
                                            scanPayBean.setPhWeiXinCnt(String.valueOf(wxScanPosCount));
                                            scanPayBean.setPosWeiXinAmt(String.valueOf(posScanWXAmount));
                                            scanPayBean.setPosWeiXinCnt(String.valueOf(posScanWXCount));
                                            scanPayBean.setPhAlipayAmt(String.valueOf(ApliyScanPosAmount));
                                            scanPayBean.setPhAlipayCnt(String.valueOf(ApliyScanPosCount));
                                            scanPayBean.setPosAlipayAmt(String.valueOf(posScanApliyAmount));
                                            scanPayBean.setPosAlipayCnt(String.valueOf(PosScanApliyCount));
                                            scanPayBean.setPhUnionpayAmt("0");
                                            scanPayBean.setPhUnionpayCnt("0");
                                            scanPayBean.setPosUnionpayAmt("0");
                                            scanPayBean.setPosUnionpayCnt("0");
                                            scanPayBean.setRefundCount(String.valueOf(refundCount));
                                            scanPayBean.setRefundAmount(FormatUtils.formatMount(String.valueOf(refundAmount)));
                                            scanPayBean.setRequestUrl(mScanBean.getRequestUrl());
                                            scanPayBean.setProjectType(mScanBean.getProjectType());
                                            mComonThread = new ScanSettleThread(StartScanActivity.this, settleHandler, scanPayBean);
                                            new Thread(mComonThread).start();
                                        }
                                    });
                                }
                            }.start();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtils.i(e.getMessage());
                        ScanCache.getInstance().setErrCode(TransException.ERR_DAT_EOF_E208);
                        ScanCache.getInstance().setErrMesage(TransException.getMsg(TransException.ERR_DAT_EOF_E208));
                        gotoNextErrorCallBack(new Intent());
                    }
                    break;
                case 0x12: //同步批次号
                    try {
                        scanSettleRes = (ScanSettleRes) msg.getData().getSerializable("settle");
                        ScanPayBean scanPayBean = new ScanPayBean();
                        scanPayBean.setTransType(TransParamsValue.InterfaceType.SNYBATCHNO);
                        scanPayBean.setTerminalNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID));
                        scanPayBean.setRequestUrl(scanSettleRes.getRequestURL());
                        mComonThread = new ScanSettleThread(StartScanActivity.this, settleHandler, scanPayBean);
                        new Thread(mComonThread).start();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Intent intent = new Intent();
                        intent.putExtra("error",e.getMessage());
                        finishSettle(intent);
                    }
                    break;
                case 0x13: //跳转打印
                    try {
                        if (scanSettleRes != null) {
                            ShareScanPreferenceUtils.putBoolean(StartScanActivity.this, TransParamsValue.SettleConts.PARAMS_IS_PRINT_SETTLE_HALT, true);  //打印结算单中断标志
                            Intent intent = new Intent();
                            intent.putExtra("water", scanSettleRes);
                            gotoNextCallBack(intent);
                        } else {
                            LogUtils.i("打印的数据为空");
                            Intent intent = new Intent();
                            intent.putExtra("error","打印的数据为空");
                            finishSettle(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Intent intent = new Intent();
                        intent.putExtra("error",e.getMessage());
                        finishSettle(intent);
                    }
                    break;
                case 0x03: //无批结数据
                    String data = (String) msg.obj;
                    if (!StringUtil.isEmpty(data)) {
                        Intent intent = new Intent();
                        intent.putExtra("error",data);
                        finishSettle(intent);
                    }
                    break;
                case 0x11:
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)) {
                        nettip.setText(tips);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private long wxScanPosAmount;
    private long posScanWXAmount;
    private long ApliyScanPosAmount;
    private long posScanApliyAmount;
    private long refundAmount;
    private int wxScanPosCount = 0;
    private int posScanWXCount = 0;
    private int ApliyScanPosCount = 0;
    private int PosScanApliyCount = 0;
    private int refundCount = 0;
    private int getTransTypeBySettleType(String settleType, ScanQueryDataBean scanQueryDataBean) {

        int transType = -1;
        if (!TextUtils.isEmpty(settleType)) {
            switch (settleType) {
                case TransType.ScanTransType.ALIPAY_PAY_SCANPHONE:
                    transType = TransType.ScanTransType.TRANS_SCAN_ALIPAY;
                    ApliyScanPosAmount += Long.valueOf(scanQueryDataBean.getTxn_amt());
                    ApliyScanPosCount++;
                    break;
                case TransType.ScanTransType.ALIPAY_PAY_SCANPOS:
                    transType = TransType.ScanTransType.TRANS_QR_ALIPAY;
                    posScanApliyAmount += Long.valueOf(scanQueryDataBean.getTxn_amt());
                    PosScanApliyCount++;
                    break;
                case TransType.ScanTransType.WEIXIN_PAY_SCANPHONE:
                    transType = TransType.ScanTransType.TRANS_SCAN_WEIXIN;
                    wxScanPosAmount += Long.valueOf(scanQueryDataBean.getTxn_amt());
                    wxScanPosCount++;
                    break;
                case TransType.ScanTransType.WEIXIN_PAY_SCANPOS:
                    transType = TransType.ScanTransType.TRANS_QR_WEIXIN;
                    posScanWXAmount += Long.valueOf(scanQueryDataBean.getTxn_amt());
                    posScanWXCount++;
                    break;
                case TransType.ScanTransType.SCAN_REFUND:
                    transType = TransType.ScanTransType.TRANS_SCAN_REFUND;
                    refundCount++;
                    refundAmount += Long.valueOf(scanQueryDataBean.getTxn_amt());
                    break;
            }
        }
        return transType;
    }
    private String getSettleTypeFromPaychannel(ScanQueryDataBean scanQueryDataBean, boolean isScanPay) {
        String settleType = null;
        switch (scanQueryDataBean.getPaychannel()) {
            case TransType.QueryNet.ALIPAY_NUMBER:
                settleType = isScanPay ?
                        TransType.ScanTransType.ALIPAY_PAY_SCANPHONE :
                        TransType.ScanTransType.ALIPAY_PAY_SCANPOS;
                break;
            case TransType.QueryNet.WEICHAT_NUMBER:
                settleType = isScanPay ?
                        TransType.ScanTransType.WEIXIN_PAY_SCANPHONE :
                        TransType.ScanTransType.WEIXIN_PAY_SCANPOS;
                break;
            default:
                break;
        }
        return settleType;
    }
    /**
     * 校验必要的值。
     * @param transNetIntro
     * @return
     */
    private boolean checkNecessaryValue(ScanQueryDataBean transNetIntro) {
        boolean hasValue = true;
        if (TextUtils.isEmpty(transNetIntro.getTxn_amt())) {
            hasValue = false;
        } else if (TextUtils.isEmpty(transNetIntro.getTxn_cd())) {
            hasValue = false;
        } else if (TextUtils.isEmpty(transNetIntro.getAc_dt())) {
            hasValue = false;
        } else if (TextUtils.isEmpty(transNetIntro.getPaychannel())) {
            hasValue = false;
        }
        return hasValue;
    }

    //扫pos
    private  Handler qcScanPayHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x11:
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)) {
                        nettip.setText(tips);
                    }
                    break;
                case 0x03:
                    gotoNextErrorCallBack(new Intent());
                    break;
                case 0x06:
                    ScanPayBean scanPayBean = (ScanPayBean) msg.getData().getSerializable("scan_pos");
                    if (null != scanPayBean) {  //跳转到生成二维码的界面
                        Intent intent = new Intent();
                        intent.putExtra("scan_pos", scanPayBean);
                        gotoNextCallBack(intent);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    // 扫码退货
    Handler refundHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x00:
                    ScanTransRecord water = (ScanTransRecord) msg.getData().getSerializable("water");
                    if (water != null){
                        Intent intent = new Intent();
                        intent.putExtra("water", water);
                        gotoNextCallBack(intent);
                    }
                    break;
                case 0x01:
                    Intent errorIntent = new Intent();
                    gotoNextErrorCallBack(errorIntent);
                    break;
                case 0x02:
                    finishAct();
                    break;
                case 0x03: //查询
                    ScanPayBean refund = (ScanPayBean) msg.getData().getSerializable("refund");
                    if (refund != null){
                        mComonThread = new RefundThread(StartScanActivity.this, refundHandler, refund);
                        new Thread(mComonThread).start();
                    }
                    break;
                case 0x11: // 刷新页面提示
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)) {
                        nettip.setText(tips);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    // 扫码查询
    Handler queryHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x00:
                    ScanPayBean scanRefundBean = (ScanPayBean) msg.getData().getSerializable("scan_query");
                    if (null != scanRefundBean) {
                        Intent intent = new Intent();
                        intent.putExtra("scan_query", scanRefundBean);
                        gotoNextCallBack(intent);
                    }
                    break;
                case 0x01:
                    Intent intent = new Intent();
                    gotoNextErrorCallBack(intent);
                    break;
                case 0x02:
                    finishAct();
                    break;
                case 0x11:
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)) {
                        nettip.setText(tips);
                    }
                    break;
                default:
                    break;
            }
        }
    };


    // 扫码支付
    Handler scanpayHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x10: //刷新倒计时
                    startCountThread();
                    break;
                case 0x11: // 刷新页面提示
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)) {
                        nettip.setText(tips);
                    }
                    break;
                case 0x03:  //交易出错
                    Intent errorIntent = new Intent();
                    gotoNextErrorCallBack(errorIntent);
                    break;
                case 0x15: //弹出窗
                    MessageTipBean messageTip = (MessageTipBean) msg.getData().getSerializable("message");
                    Intent intent = new Intent(StartScanActivity.this, MessageTipActivity.class);
                    intent.putExtra("message", messageTip);
                    goNextByResult(intent, TransType.SCAN_PAY_WAIT_CODE);
                    break;
                case 0x16: //跳转打印流水
                    ScanTransRecord water = (ScanTransRecord) msg.getData().getSerializable("water");
                    Intent printerIntent = new Intent();
                    printerIntent.putExtra("water", water);
                    gotoNextCallBack(printerIntent);
                    break;
                default:
                    break;
            }
        }
    };

    private void startCountThread() {
        int timeOut = 60;
        if (mCountThread == null) {
            mCountThread = new CountThread(timeOut);
            mCountThread.start();
        } else {
            mCountThread.resetTimeOut(timeOut);
        }
        count_time.setText(timeOut + "");
    }

    /**
     * 倒计时线程
     */
    class CountThread extends Thread {
        private int leftTime;
        private boolean isRunning = true;
        public CountThread(int timeOut) {
            this.leftTime = timeOut;
        }

        @Override
        public void run() {
            while (leftTime >= 0 && isRunning) {
                Message msg = Message.obtain();
                msg.what = 1;
                msg.arg1 = leftTime;
                mCountHandler.sendMessage(msg);
                leftTime--;
                SystemClock.sleep(1000); // 休眠1s
            }
            if (isRunning) {
                mCountHandler.sendEmptyMessage(60);// 超时
            }
        }

        public void resetTimeOut(int timeOut) {
            leftTime = timeOut;
        }

        public void stopTimeOut() {
            LogUtils.e("停止超时器");
            isRunning = false;
        }

        public void startTimeOut() {
            isRunning = true;
        }
    }
    /**
     * 计时器handler
     */
    public Handler mCountHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (msg.arg1 % 10 == 0) {
                        LogUtils.d("倒计时" + msg.arg1 + "s ...");
                    }
                    count_time.setText(msg.arg1 + "");
                    break;
                case 60:
                    LogUtils.i("倒计时60s已完成");
                    //交易超时
                    ScanCache.getInstance().setErrCode(TransException.ERR_NET_TRANS_TIMEOUT_E101);
                    ScanCache.getInstance().setErrMesage(TransException.getMsg(TransException.ERR_NET_TRANS_TIMEOUT_E101));
                    Intent intent = new Intent();
                    gotoNextErrorCallBack(intent);
                default:
                    break;
            }
        }
    };

    /**
     * 跳转activity
     * @param intent
     */
    private void gotoNext(Intent intent) {
        stopCountThread();
        if (mComonThread != null) {
            mComonThread.cancel();
        }
        startActivity(intent);
        finish();
    }

    /**
     * 请求成功的回调
     * @param intent
     */
    private void gotoNextCallBack(Intent intent){
        stopCountThread();
        if (mComonThread != null) {
            mComonThread.cancel();
        }
        setResult(RESULT_OK,intent);
        finish();
    }

    /**
     * 请求失败的回调
     * @param intent
     */
    private void gotoNextErrorCallBack(Intent intent){
        stopCountThread();
        if (mComonThread != null) {
            mComonThread.cancel();
        }
        setResult(RESULT_FIRST_USER,intent);
        finish();
    }
    /**
     * 跳转带回调的activity
     * @param intent
     * @param requestCode
     */
    private void goNextByResult(Intent intent, int requestCode) {
        stopCountThread();
        if (mComonThread != null) {
            mComonThread.cancel();
        }
        startActivityForResult(intent, requestCode);
    }

    /**
     * 扫码批结失败时回调
     */
    private void finishSettle(Intent intent){
        stopCountThread();
        if (mComonThread != null) {
            mComonThread.cancel();
        }
        setResult(RESULT_CANCELED,intent);
        finish();
    }

    /**
     * 支付取消回调
     */
    private void finishAct() {
        stopCountThread();
        if (mComonThread != null) {
            mComonThread.cancel();
        }
        setResult(RESULT_CANCELED);
        finish();
    }
    /**
     * 停止计时器计时
     */
    public void stopCountThread() {
        mCountHandler.removeCallbacksAndMessages(null);
        if (null == mCountThread) {
            return;
        }
        mCountThread.stopTimeOut();
        mCountThread = null;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (null != data) {
                MessageTipBean messageTip = (MessageTipBean) data.getSerializableExtra("message");
                if (messageTip != null) {
                    switch (requestCode) {
                        case TransType.SCAN_PAY_WAIT_CODE: //微信扫码支付
                            if (messageTip.isResult()) {
                                ScanPayBean scanPayBean = new ScanPayBean();

                                scanPayBean.setTransNo(messageTip.getOldBabtchNo());  //流水号
                                scanPayBean.setType(TransType.ScanTransType.TRANS_SCAN_POS_CHECK);  //交易类型
                                scanPayBean.setOldType(messageTip.getOldTransType()); //原交易类型
                                scanPayBean.setBatchNo(ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO)); //批次号
                                if (messageTip.getProjectType().equals(EncodingEmun.antCompany.getType())){
                                    scanPayBean.setTransType(TransParamsValue.AntCompanyInterfaceType.SCAN_POS_QUERY); //扫码查询接口
                                }else {
                                    scanPayBean.setTransType(TransParamsValue.InterfaceType.SCAN_QUER); //扫码查询接口
                                }
                                scanPayBean.setTerminalNo(messageTip.getTerminalNo()); //终端号
                                scanPayBean.setOrderNo(messageTip.getOldOrderNo()); //订单号
                                scanPayBean.setScanResult(messageTip.getAuthCode()); //二维码信息 <存数据库>
                                scanPayBean.setCurrency(messageTip.getCurrency()); //币种 <存数据库>
                                scanPayBean.setQryTyp(EncodingEmun.REQID_TYEP.getType()); //查询类型 1对应商户请求号
                                scanPayBean.setChlDate(messageTip.getOldChlDate()); //原交易日期
                                scanPayBean.setOldRequestId(messageTip.getRequestId()); //原交易商户请求号
                                scanPayBean.setRequestUrl(messageTip.getReQuestURL()); //请求路径
                                scanPayBean.setProjectType(messageTip.getProjectType()); //项目类型

                                mComonThread = new ScanpayThread(this, scanpayHandler, scanPayBean);
                                new Thread(mComonThread).start();
                            } else {
                                 finishAct();
                            }
                            break;

                        case TransType.SCAN_SETTLE_WAIT_CODE: //批结中断回调
                            if (messageTip.isResult()) {
                                try {
                                    if (ShareScanPreferenceUtils.getBoolean(StartScanActivity.this, TransParamsValue.SettleConts.PARAMS_IS_SCAN_SETTLT_HALT, false)) {  //二维码批结中断

                                        ShareScanPreferenceUtils.putBoolean(StartScanActivity.this, TransParamsValue.SettleConts.SETTLE_ALL_FLAG, true);  //批结的总标志,
                                        ShareScanPreferenceUtils.putBoolean(StartScanActivity.this, TransParamsValue.SettleConts.PARAMS_IS_SCAN_SETTLT_HALT, true); //二维码结算中断标志
                                        mComonThread = new ScanSettleThread(StartScanActivity.this, settleHandler, mScanBean);
                                        new Thread(mComonThread).start();

                                    } else if (ShareScanPreferenceUtils.getBoolean(StartScanActivity.this, TransParamsValue.SettleConts.PARAMS_IS_PRINT_SETTLE_HALT, false)) {  //打印中断
                                        String result = ShareScanPreferenceUtils.getString(StartScanActivity.this, TransParamsValue.SettleConts.PARAMS_SETTLE_DATA, null);
                                        if (!StringUtil.isEmpty(result)) {
                                            ScanSettleRes scanSettleRes = DataAnalysisByJson.getInstance().getObjectByString2(result, ScanSettleRes.class);
                                            ShareScanPreferenceUtils.putBoolean(StartScanActivity.this, TransParamsValue.SettleConts.PARAMS_IS_PRINT_SETTLE_HALT, true);  //打印结算单中断标志
                                            Intent printer = new Intent();
                                            scanSettleRes.setTransType(String.valueOf(TransType.ScanTransType.TRANS_SCAN_SETTLE));
                                            printer.putExtra("water", scanSettleRes);
                                            gotoNextCallBack(printer);
                                        }
                                    } else if (ShareScanPreferenceUtils.getBoolean(StartScanActivity.this, TransParamsValue.SettleConts.PARAMS_IS_PRINT_ALLWATER_HALT, false)) {  //打印明细中断

                                        Intent printer = new Intent();
                                        ScanSettleRes scanSettleRes = new ScanSettleRes();
                                        scanSettleRes.setTransType(String.valueOf(TransType.ScanTransType.TRANS_SCAN_SETTLE));
                                        printer.putExtra("water", scanSettleRes);
                                        gotoNextCallBack(printer);

                                    } else if (ShareScanPreferenceUtils.getBoolean(StartScanActivity.this, TransParamsValue.SettleConts.PARAMS_IS_CLEAR_SETTLT_HLAT, false)) {  //清算数据中断

                                        ShareScanPreferenceUtils.putBoolean(StartScanActivity.this, TransParamsValue.SettleConts.PARAMS_IS_CLEAR_SETTLT_HLAT, true);
                                        ScanTransUtils.clearWaterForScanTrans(this);

                                        Intent intent = new Intent();
                                        intent.putExtra("error","清除数据成功");
                                        finishSettle(intent);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    LogUtils.e("结算中断异常:" + e.getMessage());
                                }
                            } else {
                                Intent intent = new Intent();
                                intent.putExtra("error","批结中断取消");
                                finishSettle(intent);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }
}
