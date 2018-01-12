package com.nld.starpos.banktrade.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.centerm.iso8583.util.DataConverter;
import com.nld.cloudpos.aidl.emv.AidlPboc;
import com.nld.cloudpos.aidl.pinpad.AidlPinpad;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.R;
import com.nld.starpos.banktrade.db.ReverseDao;
import com.nld.starpos.banktrade.db.ScriptNotityDao;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.UpDateDBUtils;
import com.nld.starpos.banktrade.db.bean.Reverse;
import com.nld.starpos.banktrade.db.bean.ScriptNotity;
import com.nld.starpos.banktrade.db.local.ReverseDaoImpl;
import com.nld.starpos.banktrade.db.local.ScriptNotityDaoImpl;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.pinUtils.AidlUtils;
import com.nld.starpos.banktrade.pinUtils.PbocDev;
import com.nld.starpos.banktrade.pinUtils.SystemInfoDev;
import com.nld.starpos.banktrade.thread.AACorARPCUpload;
import com.nld.starpos.banktrade.thread.BankConsumeThread;
import com.nld.starpos.banktrade.thread.BankSettleThread;
import com.nld.starpos.banktrade.thread.BatchEMVSendThread;
import com.nld.starpos.banktrade.thread.BatchSendEndThread;
import com.nld.starpos.banktrade.thread.ComonThread;
import com.nld.starpos.banktrade.thread.ElectriCashThread;
import com.nld.starpos.banktrade.thread.ICCardBatchSendThread;
import com.nld.starpos.banktrade.thread.ICKeyDownloadThread;
import com.nld.starpos.banktrade.thread.ICKeyQueryThread;
import com.nld.starpos.banktrade.thread.MainSignThread;
import com.nld.starpos.banktrade.thread.OffLineRefundThread;
import com.nld.starpos.banktrade.thread.OfflineUploadThread;
import com.nld.starpos.banktrade.thread.OnLineRefundThread;
import com.nld.starpos.banktrade.thread.ParamsDownloadThread;
import com.nld.starpos.banktrade.thread.ParamsQueryThread;
import com.nld.starpos.banktrade.thread.PreConsumeThread;
import com.nld.starpos.banktrade.thread.QuanCunThread;
import com.nld.starpos.banktrade.thread.ReversalThread;
import com.nld.starpos.banktrade.thread.ScriptUploadThread;
import com.nld.starpos.banktrade.thread.TCUploadThread;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.ShareBankPreferenceUtils;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.banktrade.utils.TransParams;
import com.nld.starpos.banktrade.utils.TransactionPackageUtil;

import java.util.List;
import java.util.Map;

import common.StringUtil;

/**
 * Created by jiangrenming on 2017/12/6.
 * 银行卡交易的类
 */

public class StartTransActivity extends BaseActivity {

    private ImageView imgView;
    private TextView count_time;
    private TextView nettip;
    private String transCode;
    private CountThread mCountThread;
    private ComonThread mComonThread;
    // 默认超时时间
    private static final int NETWORK_TIMEOUT = 60;
    private String uri;
    private Map<String, String> transMap;
    private boolean is_card;


    @Override
    public String getTransCode() {
        transCode = Cache.getInstance().getTransCode();
        return StringUtil.isEmpty(transCode) ? "" : transCode;
    }

    @Override
    public Map<String, String> getTransMap() {
        if (!StringUtil.isEmpty(transCode)){
            if (transCode.equals(TransConstans.TRANS_CODE_CONSUME)
                    || transCode .equals(TransConstans.TRANS_CODE_QUERY_BALANCE)
                    || transCode.equals(TransConstans.TRANS_CODE_CONSUME_CX)
                    || TransConstans.TRANS_CODE_CONSUME_CX.equals(transCode)){  //消费,余额查询 ，消费撤销
                transMap = TransactionPackageUtil.getConsumeParam(StartTransActivity.this, AidlUtils.getInstance().getmService());
            }else if (transCode.equals(TransConstans.TRANS_CODE_PRE) || transCode.equals(TransConstans.TRANS_CODE_PRE_COMPLET_CX)
                       || transCode.equals(TransConstans.TRANS_CODE_PRE_COMPLET) || transCode.equals(TransConstans.TRANS_CODE_PRE_CX)){  //预授权，预授权撤销，预授权完成,预授权完成撤销
                transMap = TransactionPackageUtil.getPreAuthCommonParams(StartTransActivity.this, AidlUtils.getInstance().getmService());
            }else if (transCode.equals(TransConstans.TRANS_CODE_SIGN_JS)) {  //批结算
                transMap = TransactionPackageUtil.getSettlementInfo(StartTransActivity.this);
            }else if (transCode.equals(TransConstans.TRANS_CODE_SIGN)){  //签到
                transMap = TransactionPackageUtil.getSignParam("07");
            }else if (transCode.equals(TransConstans.TRANS_CODE_LJTH)){  //联机退货
                transMap = TransactionPackageUtil.getRefundInfo(StartTransActivity.this, AidlUtils.getInstance().getmService());
            }else if (transCode.equals(TransConstans.TRANS_CODE_OFF_TH)){  //脱机退货
                transMap = TransactionPackageUtil.getTuoJiTuiHuoParam(StartTransActivity.this);
            }else if (transCode.equals(TransConstans.TRANS_CODE_QC_FZD)){  //非指定用户圈存
                transMap = TransactionPackageUtil.getQuanCunFZDParam(StartTransActivity.this, AidlUtils.getInstance().getmService());
            }else if (transCode.equals(TransConstans.TRANS_CODE_QC_ZD)){  //指定用户圈存
                transMap = TransactionPackageUtil.getQuanCunZDParam(StartTransActivity.this, AidlUtils.getInstance().getmService());
            }
            return transMap;
        }
        return null;
    }
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
        nettip.setText("正在交易中，请稍后");
        uri = ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TRANS_URL);
        doTransStart();
    }

    private void doTransStart() {
         startCountThread();
        if (hasReversal() &&
                !TransConstans.TRANS_CODE_UPLOAD_OFF_CONSUME.equals(transCode)
                && !TransConstans.TRANS_CODE_SIGN.equals(transCode)){     //冲正
                mComonThread = new ReversalThread(StartTransActivity.this,reverseHandler, AidlUtils.getInstance().getmService(),uri);
                new Thread(mComonThread).start();
        }else if (hasScriptNotity() &&
                !TransConstans.TRANS_CODE_UPLOAD_OFF_CONSUME.equals(transCode)
                && !TransConstans.TRANS_CODE_SIGN.equals(transCode)){    //脚本上送
            mComonThread = new ScriptUploadThread(StartTransActivity.this,scriptUploadHandler, AidlUtils.getInstance().getmService(),uri);
            new Thread(mComonThread).start();
        }else if (transCode.equals(TransConstans.TRANS_CODE_SIGN_JS)
                && !transCode.equals(TransConstans.TRANS_CODE_SIGN)) {    //批结算
            doBeforeSettleUpLoad();
        } else {
            if (TransConstans.TRANS_CODE_CONSUME.equals(transCode)
                    || transCode .equals(TransConstans.TRANS_CODE_QUERY_BALANCE)
                     || TransConstans.TRANS_CODE_CONSUME_CX.equals(transCode)){  //消费,余额查询,消费撤销
                LogUtils.e("消费/余额查询"+transCode);
                UpDateDBUtils.getInstance().saveReverseRecord(transMap, AidlUtils.getInstance().getmService());
                mComonThread = new BankConsumeThread(this,bankConsumeHandler,transMap, AidlUtils.getInstance().getmService(),transCode,uri);
                new Thread(mComonThread).start();
            }else if (transCode.equals(TransConstans.TRANS_CODE_PRE_COMPLET_CX)){   // 预授权，预授权撤销，预授权完成，预授权完成撤销
                UpDateDBUtils.getInstance().saveReverseRecord(transMap, AidlUtils.getInstance().getmService());
                mComonThread = new PreConsumeThread(this,bankPreHandler,transMap,transCode,uri);
                new Thread(mComonThread).start();
            }else if (transCode.equals(TransConstans.TRANS_CODE_BATCH_SEND_START)){  //批上送<批结中断的时候会走>
                mComonThread = new BatchEMVSendThread(StartTransActivity.this,emvHandler, AidlUtils.getInstance().getmService(),uri);
                new Thread(mComonThread).start();
            }else if (transCode.equals(TransConstans.TRANS_CODE_SIGN)){  //签到
                mComonThread = new MainSignThread(this, signHandler, transCode,transMap,uri, AidlUtils.getInstance().getmService());
                new Thread(mComonThread).start();
            }else if (transCode.equals(TransConstans.TRANS_CODE_IC_PARAM_QUERY)){  //IC卡参数查询
                is_card = getIntent().getBooleanExtra("ic_card",true);
                mComonThread = new ParamsQueryThread(StartTransActivity.this, ICParamsHandler, transCode,uri, AidlUtils.getInstance().getmService());
                new Thread(mComonThread).start();
            }else if (transCode.equals(TransConstans.TRANS_CODE_LJTH)){  //联机退货
                mComonThread = new OnLineRefundThread(StartTransActivity.this, onOrOffLineRefundHandler, transCode,uri, AidlUtils.getInstance().getmService(),transMap);
                new Thread(mComonThread).start();
            }else if (transCode.equals(TransConstans.TRANS_CODE_OFF_TH)){  //脱机退货
                mComonThread = new OffLineRefundThread(StartTransActivity.this, onOrOffLineRefundHandler, transCode,uri, AidlUtils.getInstance().getmService(),transMap);
                new Thread(mComonThread).start();
            }else if (transCode.equals(TransConstans.TRANS_CODE_QC_FZD) ||transCode.equals(TransConstans.TRANS_CODE_QC_ZD) ){  //(非)指定账户圈存
                UpDateDBUtils.getInstance().saveReverseRecord(transMap, AidlUtils.getInstance().getmService());
                mComonThread = new QuanCunThread(StartTransActivity.this, quanCunThread, transCode,uri, AidlUtils.getInstance().getmService(),transMap);
                new Thread(mComonThread).start();
            }else if (transCode.equals(TransConstans.TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER) ||transCode.equals(TransConstans.TRANS_CODE_DZXJ_TRANS_QUICK_PAY) ){
                //电子现金普通消费与快速消费
                mComonThread = new ElectriCashThread(StartTransActivity.this, electriCashHandler, transCode,uri, AidlUtils.getInstance().getmService(),transMap);
                new Thread(mComonThread).start();
            }
        }
    }


    /**
     * 电子现金（普通消费与快速支付）
     */

    private  Handler electriCashHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case 0x02:
                    LogUtils.e("交易失败");
                    gotoNextErrorCallBack(new Intent());
                    break;
                case 0x03:
                    LogUtils.e("交易成功");
                    gotoNextCallBack(new Intent());
                    break;
                case 0x05:
                    startCountThread();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * (非)指定账户圈存
     */
    private Handler quanCunThread = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case 0x02:
                    LogUtils.e("交易失败");
                    gotoNextErrorCallBack(new Intent());
                    break;
                case 0x03:
                    LogUtils.e("交易成功");
                    gotoNextCallBack(new Intent());
                    break;
                case 0x05:
                    startCountThread();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 联机退货,脱机退货
     */
    private Handler  onOrOffLineRefundHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case 0x02:
                    LogUtils.e("交易失败");
                    gotoNextErrorCallBack(new Intent());
                    break;
                case 0x03:
                    LogUtils.e("交易成功");
                    gotoNextCallBack(new Intent());
                    break;
                case 0x05:
                    startCountThread();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 预授权.预授权完成.预授权撤销.预授权完成撤销
     */
    private Handler bankPreHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case 0x02:
                    LogUtils.e("交易失败");
                    gotoNextErrorCallBack(new Intent());
                    break;
                case 0x03:
                    LogUtils.e("交易成功");
                    gotoNextCallBack(new Intent());
                    break;
                case  0x05:  //更新倒计时
                    startCountThread();
                    break;
                default:
                    break;

            }
        }
    };

    private  String updatecode;
    private  String checkUpdateCode = "0";
    private String localUpdatecode;
    /**
     * 签到
     */
    private String sucessful;
    private Handler signHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:  //界面变化
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case 0x02:  //失败
                    gotoNextErrorCallBack(new Intent());
                    break;
                case 0x03:  //签到成功
                    try {
                        sucessful = (String) msg.obj;
                        String key = Cache.getInstance().getResultMap().get(ParamsConts.BATHC_BILNO);
                        updatecode = Cache.getInstance().getResultMap().get(ParamsConts.UPDATA_CODE);
                        LogUtils.d("更新code=" + updatecode);
                        if (StringUtil.isEmpty(updatecode)) {
                            updatecode = "0";
                        }
                        if (!TextUtils.isEmpty(key)) {
                            String pinkeybin = "";
                            String pikCheckvalue = "";
                            String mackeybin = "";
                            String makCheckvalue = "";
                            String tdkbin = "";
                            String tdkCheckvalue = "";
                            LogUtils.i("获取的key="+key);
                            //有checkvalue
                            if (key.length() == 80) {
                                pinkeybin = key.substring(0, 32);
                                pikCheckvalue = key.substring(32, 40);
                                mackeybin = key.substring(40, 72);
                                makCheckvalue = key.substring(72, 80);
                            } else if (key.length() == 120) {
                                pinkeybin = key.substring(0, 32);
                                pikCheckvalue = key.substring(32, 40);
                                mackeybin = key.substring(40, 56);
                                makCheckvalue = key.substring(72, 80);
                                tdkbin = key.substring(80, 112);
                                tdkCheckvalue = key.substring(112, 120);
                            } else {
                                pinkeybin = key.substring(0, 32);
                                mackeybin = key.substring(32, 64);
                            }
                            String pinpadType = ParamsUtil.getInstance().getParam(ParamsConts.PINPAD_TYPE);
                            LogUtils.e("pik: " + pinkeybin + " checkvalue：" + pikCheckvalue);
                            LogUtils.e("mak: " + mackeybin + " checkvalue：" + makCheckvalue);
                            LogUtils.e("tdk: " + tdkbin + " checkvalue：" + tdkCheckvalue);
                            LogUtils.e("密码键盘类型：" + pinpadType);
                            AidlPinpad dev = AidlPinpad.Stub.asInterface(AidlUtils.getInstance().getmService()
                                    .getPinPad(pinpadType.equals("0") ? 0 : 1));
                            String mkeyId = ParamsUtil.getInstance().getParam(
                                    Constant.FIELD_NEW_MKEY_ID);
                            String pikId = ParamsUtil.getInstance().getParam(
                                    Constant.FIELD_NEW_PIK_ID);
                            String makId = ParamsUtil.getInstance().getParam(
                                    Constant.FIELD_NEW_MAK_ID);
                            String tdkId = ParamsUtil.getInstance().getParam(
                                    Constant.FIELD_NEW_TDK_ID);
                            LogUtils.e("密钥索引：mkeyId:" + mkeyId + "  pikId:" + pikId
                                    + "  makId:" + makId + "  tdkId:" + tdkId);
                            if (StringUtil.isEmpty(mkeyId)) {
                                mkeyId = "0";
                            }
                            if (StringUtil.isEmpty(pikId)) {
                                pikId = "0";
                            }
                            if (StringUtil.isEmpty(makId)) {
                                makId = "0";
                            }
                            if (StringUtil.isEmpty(tdkId)) {
                                makId = "0";
                            }
                            int iPik, iMak, iMKey, iTdk;
                            iPik = Integer.parseInt(pikId);
                            iMak = Integer.parseInt(makId);
                            iMKey = Integer.parseInt(mkeyId);
                            iTdk = Integer.parseInt(tdkId);
                            // PIK
                            boolean loadPik = dev.loadWorkKey(0x01, iMKey, iPik,
                                    DataConverter.hexStringToByte(pinkeybin),
                                    DataConverter.hexStringToByte(pikCheckvalue), Constant.IS_SM);
                            // MAK
                            boolean loadMak = dev.loadWorkKey(0x03, iMKey, iMak,
                                    DataConverter.hexStringToByte(mackeybin + mackeybin),
                                    DataConverter.hexStringToByte(makCheckvalue), Constant.IS_SM);
                            // TDK
                            boolean loadTdk = dev.loadWorkKey(0x02, iMKey, iTdk,
                                    DataConverter.hexStringToByte(tdkbin),
                                    DataConverter.hexStringToByte(tdkCheckvalue), Constant.IS_SM);
                            if (loadPik) {
                                LogUtils.e("PIK注入成功");
                            } else {
                                LogUtils.e("PIK注入失败");
                                Cache.getInstance().setErrCode(NldException.ERR_DEV_PIK_E320);
                                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_PIK_E320));
                                gotoNextErrorCallBack(new Intent());
                                return;
                            }
                            if (loadMak) {
                                LogUtils.e("MAK注入成功");
                            } else {
                                LogUtils.e("MAK注入失败");
                                Cache.getInstance().setErrCode(NldException.ERR_DEV_MAK_E321);
                                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_MAK_E321));
                                gotoNextErrorCallBack(new Intent());
                                return;
                            }
                            if (loadTdk) {
                                LogUtils.e("TDK注入成功");
                            } else {
                                LogUtils.e("TDK注入失败");
                                Cache.getInstance().setErrCode(NldException.ERR_DEV_MAK_E321);
                                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_MAK_E321));
                                gotoNextErrorCallBack(new Intent());
                                return;
                            }
                            // 修改签到标志为已签到
                            ParamsUtil.getInstance().save(ParamsConts.SIGN_SYMBOL, TransParams.SingValue.SingedValue);
                            LogUtils.i("签到后更新系统时：" + Cache.getInstance().getResultMap()
                                    .get("translocaldate") + Cache.getInstance().getResultMap()
                                    .get("translocaltime"));
                            SystemInfoDev sysDev = SystemInfoDev.getInstance(StartTransActivity.this, AidlUtils.getInstance().getmService());
                            boolean result = sysDev.setDatetime(Cache.getInstance().getResultMap()
                                    .get("translocaldate") + Cache.getInstance().getResultMap()
                                    .get("translocaltime"));
                            LogUtils.i("更新系统时间" + result);
                            //更新时间
                            ParamsUtil.getInstance().update(ParamsConts.PARAMS_RUN_LOGIN_DATE, Cache.getInstance().getResultMap().get(ParamsConts.PARAMS_RUN_LOGIN_DATE));
                            // 签到后更新pos流水号
                            LogUtils.i("返回的集合数据= " + Cache.getInstance().getResultMap().toString());
                            String systraceno = Cache.getInstance().getResultMap().get(ParamsConts.TransParamsContns.SYSTRANCE_NO);
                            if (systraceno != null) {
                                ParamsUtil.getInstance().update(ParamsConts.TransParamsContns.SYSTRANCE_NO, systraceno);
                            }
                            LogUtils.i("更新流水号：" + systraceno);
                            // 签到后更新批次号
                            String batchno = Cache.getInstance().getResultMap().get(ParamsConts.TYANS_TYPE).substring(2, 8);
                            ParamsUtil.getInstance().update(ParamsConts.TransParamsContns.TYANS_BATCHNO, batchno);
                            LogUtils.i("更新批次号：" + batchno);
                            try {
                                // 更新标志0x01 公钥需要更新;0x02 IC卡参数需要更新;0x04 公钥参数都需要更新
                                 localUpdatecode = ParamsUtil.getInstance().getParam(ParamsConts.UPDATA_STATUS);
                                AidlPboc pbocDev = AidlPboc.Stub.asInterface(AidlUtils.getInstance().getmService().getEMVL2());
                                int resultKey = pbocDev.isExistAidPublicKey();
                          //      String checkUpdateCode = "0";
                                LogUtils.e("几个code=" + localUpdatecode + ",result = " + result);
                                switch (resultKey) {
                                    case 3:
                                        checkUpdateCode = "0";
                                        break;
                                    case 2://公钥为空
                                        checkUpdateCode = "1";
                                        break;
                                    case 1://AID为空
                                        checkUpdateCode = "2";
                                        break;
                                    case 0://两个都空
                                        checkUpdateCode = "4";
                                        break;
                                    default:
                                        break;
                                }
                                if (updatecode.equals("4")
                                        || localUpdatecode.equals("4")
                                        || checkUpdateCode.equals("4")) {
                                    LogUtils.i("都参数查询");
                                    mComonThread = new ParamsQueryThread(StartTransActivity.this, ICParamsHandler,
                                            TransConstans.TRANS_CODE_IC_PARAM_QUERY,uri, AidlUtils.getInstance().getmService());
                                    new Thread(mComonThread).start();
                                } else if (updatecode.equals("2")
                                        || localUpdatecode.equals("2")
                                        || checkUpdateCode.equals("2")) {
                                    LogUtils.i("参数查询");
                                    mComonThread = new ParamsQueryThread(StartTransActivity.this, ICParamsHandler,
                                            TransConstans.TRANS_CODE_IC_PARAM_QUERY,uri, AidlUtils.getInstance().getmService());
                                    new Thread(mComonThread).start();
                                } else if (updatecode.equals("1")
                                        || localUpdatecode.equals("1")
                                        || checkUpdateCode.equals("1")) {
                                    LogUtils.i("公钥查询");
                                    mComonThread = new ICKeyQueryThread(StartTransActivity.this, ICParamsHandler,
                                            TransConstans.TRANS_CODE_IC_KEY_QUERY ,uri, AidlUtils.getInstance().getmService());
                                    new Thread(mComonThread).start();
                                } else if (updatecode.equals("0")
                                        || localUpdatecode.equals("0")
                                        || checkUpdateCode.equals("0")) {
                                    // 修改签到标志为已签到，IC卡参数、公钥下载成功后修改标志
                                    ParamsUtil.getInstance().save(ParamsConts.SIGN_SYMBOL, TransParams.SingValue.SingedValue);
                                    ParamsUtil.getInstance().update(ParamsConts.UPDATA_STATUS, TransParams.SingValue.Singed_UPDATA_CODE);
                                    Intent intent = new Intent();
                                    intent.putExtra("transResultTip", sucessful);
                                    gotoNextCallBack(intent);
                                } else {  //表示更新中断，签到后 两者都更新
                                    mComonThread = new ParamsQueryThread(StartTransActivity.this, ICParamsHandler, TransConstans.TRANS_CODE_IC_PARAM_QUERY
                                                                               ,uri, AidlUtils.getInstance().getmService());
                                    new Thread(mComonThread).start();

                                    mComonThread = new ICKeyQueryThread(StartTransActivity.this, ICParamsHandler,
                                            TransConstans.TRANS_CODE_IC_KEY_QUERY ,uri, AidlUtils.getInstance().getmService());
                                    new Thread(mComonThread).start();
                                }
                            } catch (RemoteException e) {
                                e.printStackTrace();
                                Cache.getInstance().setErrDesc(NldException.ERR_WX_WKEY_E502);
                                Cache.getInstance().setErrCode(NldException.getMsg(NldException.ERR_WX_WKEY_E502));
                                gotoNextErrorCallBack(new Intent());
                            } catch (NumberFormatException e) {
                                Cache.getInstance().setErrDesc(NldException.ERR_WX_WKEY_E502);
                                Cache.getInstance().setErrCode(NldException.getMsg(NldException.ERR_WX_WKEY_E502));
                                gotoNextErrorCallBack(new Intent());
                            }
                        } else {
                            LogUtils.i("银行卡商户工作密钥注入失败，密钥为空");
                            Cache.getInstance().setErrDesc(NldException.ERR_DEV_WKEY_E319);
                            Cache.getInstance().setErrCode(NldException.getMsg(NldException.ERR_DEV_WKEY_E319));
                            gotoNextErrorCallBack(new Intent());
                            return;
                        }
                    } catch (RemoteException e) {
                        LogUtils.i("银行卡商户工作密钥注入失败", e);
                        Cache.getInstance().setErrDesc(NldException.ERR_DEV_WKEY_E319);
                        Cache.getInstance().setErrCode(NldException.getMsg(NldException.ERR_DEV_WKEY_E319));
                        gotoNextErrorCallBack(new Intent());
                        e.printStackTrace();
                        return;
                    } catch (NumberFormatException e) {
                        LogUtils.i("类型强制转换错误", e);
                        Cache.getInstance().setErrDesc(NldException.ERR_DAT_UNPACK_E206);
                        Cache.getInstance().setErrCode(NldException.getMsg(NldException.ERR_DAT_UNPACK_E206));
                        gotoNextErrorCallBack(new Intent());
                        return;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * IC卡参数查询
     */
    Handler ICParamsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)) {
                        nettip.setText(tips);
                    }
                    break;
                case 0x02:  //错误界面
                    gotoNextErrorCallBack(new Intent());
                    break;
                case 0x03:  //成功,下载参数或公钥
                    String sucess = (String) msg.obj;
                    Log.i("sucess==", sucess);
                    switch (sucess) {
                        case TransConstans.TRANS_CODE_IC_PARAM_QUERY: //参数查询
                            LogUtils.i("参数查询成功，开始下载");
                            downParams("", TransConstans.TRANS_CODE_IC_PARAM_QUERY);
                            break;
                        case TransConstans.TRANS_CODE_IC_KEY_QUERY: //公钥查询
                            LogUtils.i("公钥查询成功，开始下载");
                            downParams("", TransConstans.TRANS_CODE_IC_KEY_QUERY);
                            break;
                        case TransConstans.TRANS_CODE_IC_KEY_DOWN:  //公钥下载
                            Map<String, String> resultMap = Cache.getInstance().getResultMap();
                            downCASuccess(resultMap);
                            if (catime < caList.length) {
                                catime = catime + 1;
                                opearcode = "0012" + caList[catime - 1];
                                downParams(opearcode, TransConstans.TRANS_CODE_IC_KEY_DOWN);
                            }else {
                                // IC卡参数、公钥下载成功后修改标志
                                if (is_card){
                                    LogUtils.i("设置中的参数下载");
                                    gotoNextCallBack(new Intent());
                                }else {
                                    ParamsUtil.getInstance().save(ParamsConts.SIGN_SYMBOL, TransParams.SingValue.SingedValue);
                                    ParamsUtil.getInstance().update(ParamsConts.UPDATA_STATUS, TransParams.SingValue.Singed_UPDATA_CODE);
                                    Intent intent = new Intent();
                                    intent.putExtra("transResultTip", "签到成功");
                                    gotoNextCallBack(intent);
                                }
                            }
                            break;
                        case TransConstans.TRANS_CODE_IC_PARAM_DOWN:  //IC卡参数
                            Map<String, String> result = Cache.getInstance().getResultMap();
                            downAIDSuccess(result);
                            if (icparamtime < icParamList.length) {
                                LogUtils.i("IC卡参数下载循环多少次");
                                icparamtime = icparamtime + 1;
                                opearcode = "0011" + icParamList[icparamtime - 1];
                                downParams(opearcode, TransConstans.TRANS_CODE_IC_PARAM_DOWN);
                            }else {
                                if (("4".equals(updatecode)
                                        || "4".equals(localUpdatecode)
                                        || "4".equals(checkUpdateCode)) || is_card){
                                    LogUtils.e("开始公钥查询");
                                    mComonThread = new ICKeyQueryThread(StartTransActivity.this, ICParamsHandler,
                                            TransConstans.TRANS_CODE_IC_KEY_QUERY,uri, AidlUtils.getInstance().getmService());
                                    new Thread(mComonThread).start();
                                }else {
                                    // IC卡参数、公钥下载成功后修改标志
                                    ParamsUtil.getInstance().save(ParamsConts.SIGN_SYMBOL, TransParams.SingValue.SingedValue);
                                    ParamsUtil.getInstance().update(ParamsConts.UPDATA_STATUS, TransParams.SingValue.Singed_UPDATA_CODE);
                                    Intent i = new Intent();
                                    i.putExtra("transResultTip", "签到成功");
                                    gotoNextCallBack(i);
                                }
                            }
                            break;
                        default:
                            break;

                    }
                    break;
                default:
                    gotoNextErrorCallBack(new Intent());
                    break;
            }
        }
    };

    /**
     * OF,TC,AAC,ARPC 批结交易前上送
     */
    private void doBeforeSettleUpLoad(){
        TransRecordDao dao = new TransRecordDaoImpl();
        if (!dao.getTransRecordsByStatuscode("OF").isEmpty()) {
            mComonThread = new OfflineUploadThread(StartTransActivity.this,offLineHandler, AidlUtils.getInstance().getmService(),uri);
            new Thread(mComonThread).start();
        } else if (!dao.getTransRecordsByStatuscode("TC").isEmpty()) {
            startCountThread();
            mComonThread = new TCUploadThread(StartTransActivity.this,tcUpLoadHandler, AidlUtils.getInstance().getmService(),uri);
            new Thread(mComonThread).start();
        } else if (!dao.getTransRecordsByStatuscode("AACorARPC").isEmpty()) {
            startCountThread();
            mComonThread = new AACorARPCUpload(StartTransActivity.this,accOrArpcHandler, AidlUtils.getInstance().getmService(),uri);
            new Thread(mComonThread).start();
        } else {
            startCountThread();
            mComonThread = new BankSettleThread(StartTransActivity.this,bankSettleHandler, AidlUtils.getInstance().getmService(),uri,transCode,transMap);
            new Thread(mComonThread).start();
        }
    }

    /**
     * 批结算
     */
    private Handler bankSettleHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case  0x02:
                    LogUtils.i("批结算异常");
                    gotoNextErrorCallBack(new Intent());
                    break;
                case  0x03:
                    LogUtils.d( "批结算对账平成功");
                    gotoNextCallBack(new Intent());
                    break;
                case  0x04:
                    LogUtils.d( "批结算对账不平");
                    startCountThread();
                    ShareBankPreferenceUtils.putString(Constant.BATCH_SETTLE_INTERRUPT_FLAG, Constant.BATCH_SETTLE_INTERRUPT_STEP1);
                    mComonThread = new BatchEMVSendThread(StartTransActivity.this,emvHandler, AidlUtils.getInstance().getmService(),uri);
                    new Thread(mComonThread).start();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 刷卡批上送
     */
    private Handler emvHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case  0x01:
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case  0x02:
                case 0x03:  //不管刷卡批上送是失败还是成功也要继续下面的批上送
                    startCountThread();
                    mComonThread = new ICCardBatchSendThread(StartTransActivity.this,icUpLoadThread, AidlUtils.getInstance().getmService(),uri);
                    new Thread(mComonThread).start();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * IC卡交易批上送
     */
    private Handler icUpLoadThread = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case 0x02:
                case 0x03:  //批上送结束
                    startCountThread();
                    mComonThread = new BatchSendEndThread(StartTransActivity.this,sendEndThread,uri, AidlUtils.getInstance().getmService());
                    new Thread(mComonThread).start();
                 break;
                default:
                    break;
            }
        }
    };

    /**
     * 批上送结束
     */
    private Handler sendEndThread = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case  0x02:
                case  0x03: //批结算结束跳转账界面
                    gotoNextCallBack(new Intent());
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * acc/aprc上送
     */
    private  Handler accOrArpcHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:  //更改界面变化文字
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case  0x02:  //异常处理
                    LogUtils.i("ACC/APRC上送异常");
                    gotoNextErrorCallBack(new Intent());
                    break;

                case  0x03: //脱机消费上送成功
                    LogUtils.i("ACC/APRC上送成功");
                    doBeforeSettleUpLoad();
                    break;
                default:
                    LogUtils.i("ACC/APRC上送异常");
                    gotoNextErrorCallBack(new Intent());
                    break;
            }
        }
    };

    /**
     * TC上送
     */
    private Handler tcUpLoadHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:  //更改界面变化文字
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case  0x02:  //异常处理
                    LogUtils.i("TC上送异常");
                    gotoNextErrorCallBack(new Intent());
                    break;

                case  0x03: //脱机消费上送成功
                    LogUtils.i("TC上送成功");
                    doBeforeSettleUpLoad();
                    break;
                default:
                    LogUtils.i("TC上送异常");
                    gotoNextErrorCallBack(new Intent());
                    break;
            }
        }
    };


    /**
     * 脱机消费上送
     */
    private Handler offLineHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:  //更改界面变化文字
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case  0x02:  //异常处理
                    LogUtils.i("脱机消费异常");
                    gotoNextErrorCallBack(new Intent());
                    break;

                case  0x03: //脱机消费上送成功
                    LogUtils.i("脱机消费成功");
                    doBeforeSettleUpLoad();
                    break;
                default:
                    LogUtils.i("脱机消费异常");
                    gotoNextErrorCallBack(new Intent());
                    break;
            }
        }
    };

    /**
     * 冲正
     */
    private  Handler reverseHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:  //更改界面变化文字
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case  0x02:  //异常处理
                    LogUtils.i("冲正异常");
                    gotoNextErrorCallBack(new Intent());
                    break;

                case  0x03: //冲正结束
                    LogUtils.i("冲正成功");
                    doTransStart();
                    break;
                default:
                    LogUtils.i("冲正异常");
                    gotoNextErrorCallBack(new Intent());
                    break;
            }
        }
    };

    /**
     * 脚本上送
     */
    private  Handler scriptUploadHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:  //更改界面变化文字
                    String tips = (String) msg.obj;
                    if (!StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case  0x02:  //异常处理
                    LogUtils.i("脚本上送异常异常");
                    gotoNextErrorCallBack(new Intent());
                    break;

                case  0x03: //冲正结束
                    LogUtils.i("脚本上送成功");
                    doTransStart();
                    break;
                default:
                    LogUtils.i("脚本上送异常");
                    gotoNextErrorCallBack(new Intent());
                    break;
            }
        }
    };

    /**
     * 消费，消费撤销，余额查询
     */
    private Handler bankConsumeHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:  //界面显示变化
                    String  tips = (String) msg.obj;
                    if (StringUtil.isEmpty(tips)){
                        nettip.setText(tips);
                    }
                    break;
                case 0x02 : //交易失败
                    LogUtils.d("交易失败");
                    //删除交易前保存的冲正记录
                    try{
                        gotoNextErrorCallBack(new Intent());
                    }catch (Exception e){
                        e.printStackTrace();
                        LogUtils.i("删除交易数据失败");
                    }
                    break;
                case 0x03: //交易成功
                    LogUtils.d( "交易结束，成功返回");
                    gotoNextCallBack(new Intent());
                    break;
                case  0x05: //更新界面倒计时
                    if (mComonThread != null){
                        mComonThread.cancel();
                    }
                    startCountThread();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 判断是否冲正
     * @return
     */
    private boolean hasReversal() {
        List<Reverse> reverseList = getReverseList();
        if (reverseList.isEmpty() && reverseList.size() == 0) {
            return false;
        }
        return true;
    }
    /**
     * 判断是否含有脚本结果通知
     * @return
     */
    private boolean hasScriptNotity() {
        List<ScriptNotity> scriptNotities = geScriptNotityList();
        if (scriptNotities.isEmpty() && scriptNotities.size() == 0) {
            return false;
        }
        return true;
    }

    /**
     * 获取本地数据库的冲正数据
     * @return
     */
    private  List<Reverse> getReverseList(){
        ReverseDao dao = new ReverseDaoImpl();
        List<Reverse> mReverses = dao.getEntities();
        return  mReverses;
    }

    /**
     * 获取本地数据库脚本数据
     * @return
     */
    private  List<ScriptNotity> geScriptNotityList(){
        ScriptNotityDao dao = new ScriptNotityDaoImpl();
        List<ScriptNotity> mScriptNotities = dao.getEntities();
        return  mScriptNotities;
    }


    private  void goToNext(Intent intent){
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
     * 倒计时的界面显示
     */
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
                    Cache.getInstance().setErrCode(NldException.ERR_NET_TRANS_TIMEOUT_E101);
                    Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_TRANS_TIMEOUT_E101));
                    Intent intent = new Intent();
                    gotoNextErrorCallBack(intent);
                default:
                    break;
            }
        }
    };

    /**
     * IC卡参数注入
     * @param resultMap
     */
    private boolean downAIDSuccess(Map<String, String> resultMap) {
        String ret62 = resultMap.get("batchbillno");
        String retNum = new String(DataConverter.hexStringToByte(ret62.substring(0, 2)));
        if ("0".equals(retNum)) {
            LogUtils.i("aid--" + icParamList[icparamtime - 1] + "在平台不存在");
            return false;
        } else if ("1".equals(retNum)) {
            String aidParamString = ret62.substring(2);
            LogUtils.i("下载aid参数成功----" + icParamList[icparamtime - 1] + ":" + aidParamString);
            boolean result = false;
            try {
                result = PbocDev.getInstance(StartTransActivity.this, AidlUtils.getInstance().getmService())
                        .getOriginalDev().updateAID((byte) 0x01, aidParamString);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            LogUtils.i("result" + result);
            return result;
        }
        LogUtils.i("无IC参数下载了");
        return false;
    }
    /**
     * 公钥下载成功
     * @param resultMap
     */
    private boolean downCASuccess(Map<String, String> resultMap) {
        String cadata = resultMap.get("batchbillno");
        if (cadata.startsWith("31")) {
            cadata = cadata.substring(2);
            boolean result = false;
            LogUtils.i("下载ca参数成功--" + caList[catime - 1] + ":" + cadata);
            try {
                result = PbocDev.getInstance(StartTransActivity.this, AidlUtils.getInstance().getmService())
                        .getOriginalDev().updateCAPK((byte) 0x01, cadata);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            LogUtils.i("result--" + result);
            return result;
        }
        return false;
    }

    /**
     * 下载公钥与参数
     * @param opearcode
     */
    private String[] caList = null;
    private String[] icParamList = null;
    private int catime = 0;
    private int icparamtime = 0;
    private String opearcode;
    private  void downParams(String opearcode,String transCode) {
        Map<String, String> resultMap = Cache.getInstance().getResultMap();
        String param = resultMap.get("batchbillno");
        if (transCode.equals(TransConstans.TRANS_CODE_IC_PARAM_QUERY)) {  //参数查询
            LogUtils.e("IC卡参数查询参数：" + param);
            try {
                icParamList = tlvformat_aid(param);
            } catch (Exception e) {
                e.printStackTrace();
                //本地公钥更新失败
                Cache.getInstance().setErrCode(NldException.ERR_DEV_AID_UPDATE_E314);
                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_AID_UPDATE_E314));
                gotoNextErrorCallBack(new Intent());
                return;
            }
            if (icParamList != null && icParamList.length != 0) {
                opearcode = "0011" + icParamList[0];
                LogUtils.e("icParamList[0]:" + icParamList[0]);
                transCode = TransConstans.TRANS_CODE_IC_PARAM_DOWN;
                icparamtime = icparamtime + 1;
                try {//清空参数
                    PbocDev.getInstance(StartTransActivity.this, AidlUtils.getInstance().getmService()).getOriginalDev()
                            .updateAID((byte) 0x03, null);
                } catch (RemoteException e) {
                    LogUtils.e("清空参数时获取设备失败：", e.toString());
                    e.printStackTrace();
                } catch (Exception e) {
                    LogUtils.e("清空参数失败：", e.toString());
                    e.printStackTrace();
                } // 第一次更新AID参数，需清除原先的参数
                downParams(opearcode,transCode);
                return;
            }
            Intent i = new Intent();
            i.putExtra("transResultTip", "签到成功");
            gotoNextCallBack(i);
        } else if (transCode.equals(TransConstans.TRANS_CODE_IC_KEY_QUERY)) { //公钥查询
            LogUtils.i("IC卡公钥查询参数：" + param);
            try {
                caList = tlvformat_ca(param);
            } catch (Exception e) {
                e.printStackTrace();
                //本地公钥更新失败
                Cache.getInstance().setErrCode(NldException.ERR_DEV_PK_UPDATE_E313);
                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_PK_UPDATE_E313));
                gotoNextErrorCallBack(new Intent());
                return;
            }
            if (caList != null && caList.length != 0) {
                opearcode = "0012" + caList[0];
                LogUtils.e("caList[0]:" + caList[0]);
                transCode = TransConstans.TRANS_CODE_IC_KEY_DOWN;// 公钥下载
                catime = catime + 1;
                try {
                    PbocDev.getInstance(StartTransActivity.this, AidlUtils.getInstance().getmService()).getOriginalDev()
                            .updateCAPK((byte) 0x03, null);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } // 第一次更新公钥需清除当前
                downParams(opearcode,transCode);
                return;
            }
            Intent i = new Intent();
            i.putExtra("transResultTip",  "签到成功");
            gotoNextCallBack(i);
        } else if (transCode.equals(TransConstans.TRANS_CODE_IC_KEY_DOWN)) {  //公钥下载
            LogUtils.i("IC卡公钥查询下载：" + opearcode);
            mComonThread = new ICKeyDownloadThread(StartTransActivity.this, ICParamsHandler, transCode
                                             ,uri, AidlUtils.getInstance().getmService());
            mComonThread.setOperCode(opearcode);
            mComonThread.setCatTime(catime);
            new Thread(mComonThread).start();

        } else if (transCode.equals(TransConstans.TRANS_CODE_IC_PARAM_DOWN)) {  //参数下载
            LogUtils.i("IC卡参数下载：" + opearcode);
            mComonThread = new ParamsDownloadThread(StartTransActivity.this, ICParamsHandler, transCode,uri, AidlUtils.getInstance().getmService());
            mComonThread.setOperCode(opearcode);
            mComonThread.setIpParamtime(icparamtime);
            new Thread(mComonThread).start();
        }
    }

    /**
     * IC卡公钥ca解析
     * @param tlvStr
     * @return
     */
    private String[] tlvformat_ca(String tlvStr) {
        String[] list = null;
        if (tlvStr == null) {
            return null;
        } else {
            String str = tlvStr.substring(2);
            int count = str.length() / 46;
            list = new String[count];

            if (str.startsWith("9F06")) {
                for (int i = 0; i < count; i++) {
                    String[] listStr = str.substring(i * 46, (i + 1) * 46).split("DF05");
                    list[i] = listStr[0];
                }
            }
            return list;
        }
    }

    /**
     * IC卡参数aid解析
     *
     * @param tlvStr
     * @return
     */
    private String[] tlvformat_aid(String tlvStr) {
        String[] list = null;
        if (tlvStr == null) {
            return null;
        } else {
            String str = tlvStr.substring(2);
            String b[] = str.split("9F06");
            int count = b.length - 1;
            list = new String[count];
            for (int i = 1; i < b.length; i++) {
                list[i - 1] = "9F06" + b[i];
            }
            return list;
        }
    }


}
