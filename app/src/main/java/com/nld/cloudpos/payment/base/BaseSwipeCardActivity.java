package com.nld.cloudpos.payment.base;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centerm.iso8583.util.DataConverter;
import com.nld.cloudpos.BankApplication;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.emv.AidlCheckCardListener;
import com.nld.cloudpos.aidl.emv.AidlPboc;
import com.nld.cloudpos.aidl.emv.CardInfo;
import com.nld.cloudpos.aidl.emv.PCardLoadLog;
import com.nld.cloudpos.aidl.emv.PCardTransLog;
import com.nld.cloudpos.aidl.magcard.TrackData;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.activity.InputCardValidityActivity;
import com.nld.cloudpos.payment.activity.TransErrorResultActivity;
import com.nld.cloudpos.payment.interfaces.IDialogItemClickListener;
import com.nld.cloudpos.payment.view.MEditText;
import com.nld.cloudpos.util.DialogFactory;
import com.nld.cloudpos.util.TlvUtil;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.bean.SettleData;
import com.nld.starpos.banktrade.db.bean.TransRecord;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;
import com.nld.starpos.banktrade.db.local.SettleDataDaoImpl;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.pinUtils.EMVTAGStr;
import com.nld.starpos.banktrade.pinUtils.PbocDev;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.banktrade.utils.TransParamsUtil;

import java.util.HashMap;
import java.util.Map;

import common.HexUtil;
import common.StringUtil;
import common.Utility;

public abstract class BaseSwipeCardActivity extends BaseAbstractActivity {

    private static final int MSG_TIP = 101;// 显示提示
    private static final int TOAST_TIP = 102;//Toast提示回调
    private static final int PBOC_REPEAT = 104;// 重新开始
    private static final int PBOC_MUL_AID = 105;// 多应用选择
    private static final int PBOC_USE_ECASH = 106;// 是否使用电子现金
    private static final int PBOC_START_READ = 107;// 开始加载IC卡数据
    private static final int FORCE_SWIPE_TIP = 108;//强制挥卡读卡方式错误提示
    private static final int FORCE_INSERT_TIP = 109;//强制插卡读卡方式错误提示
    private static final int FALLBACK_CARD = 110;// 降级刷卡

    /**
     * 该变量在电子现金非指定账户圈存中使用， 用来判断是否为转入卡
     */
    protected boolean isInCard = false;

    protected LinearLayout mInputLl;
    protected RelativeLayout mCarnoLl, mMoneyLl;
    protected TextView mInputTip, mMoneyTv, mCarnoTv, mCarnoTipTV;
    protected MEditText mCarnoEt;
    protected Button mNextBtn;
    protected AidlPboc mPbocDev;
    protected ImageView iv_bottom;
    protected ImageView mLeftIv, mRightIv;
    protected LinearLayout mForceNfCLl, mForceInsertLl;
    private String mCarno;
    private String mFirstTrack;
    private String mSecondTrack;
    private String mThirdTrack;
    private String mInvalidate;
    private boolean mIsChecking = false;//是否开启检卡
    private boolean mIsStartPBOC = false;//是否开始PBOC

    private String[] multAids = null;//多应用选择

    private boolean mIsECash = false;
    private boolean isForceSwipe = true;//是否强制挥卡


    protected ParamConfigDao mParamConfigDao;
    protected TransRecordDao mTransRecordDao;
    protected SettleDataDaoImpl settleDataDao;
    private String batchno; // 批次号
    private String billno; // 凭证号
    private String systraceno; // POS流水号
    private String inputMethodStr = ""; //输入条件
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (isCanceled) {
                LogUtils.d("页面已取消，不执行");
                return;
            }
            switch (msg.what) {
                case MSG_TIP:
                    String tip = (String) msg.obj;
                    if (tip.contains("请刷卡")) {
                        playSound(2, 0);
                        LogUtils.d("检卡降级，启动刷卡");
                        abordPboc();
                        DialogFactory.showConfirmMessage(mActivity, "提示", "读卡失败，请重新插卡", "确定", new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                stopCheckCard();
                                startCheckCart(3);
                            }
                        });
                    } else if (tip.contains("请插卡")) {

                        if (isForceSwipe) {
                            tip = "请挥卡";
                        }
                        playSound(2, 0);
                        LogUtils.d("检卡IC卡刷卡时，启动检卡");
                        startCheckCart(2);
                        DialogFactory.showConfirmMessage(mActivity, "提示", tip, "确定", new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                stopCheckCard();
                                startCheckCart(3);
                            }
                        });
                    }
                    super.handleMessage(msg);
                    break;
                case TOAST_TIP:
                    String toastTip = (String) msg.obj;
                    playSound(2, 0);
                    showTip(toastTip);
                    break;
                case PBOC_REPEAT:
                    startCheckCart(3);
                    break;
                case PBOC_START_READ:
                    DialogFactory.showLoadingDialog(mActivity, "检测到IC卡", "正在读取IC卡数据...");
                    break;
                case PBOC_MUL_AID:
                    String title = 2 > multAids.length ? "单应用选择" : "多应用选择";
                    DialogFactory.dismissAlert(mActivity);
                    DialogFactory.showChooseListDialog(mActivity, title, null, null, multAids, new IDialogItemClickListener() {

                        @Override
                        public void onDialogItemClick(View v, String title, int pos) {
                            try {
                                mPbocDev.importAidSelectRes(pos + 1);
                            } catch (Exception e) {
                                LogUtils.e("多应用选择结果+e.getMessage()败");
                                e.printStackTrace();
                            }
                            DialogFactory.dismissAlert(mActivity);
                            mHandler.sendEmptyMessage(PBOC_START_READ);
                        }
                    });
                    break;
                case PBOC_USE_ECASH:
                    DialogFactory.showMessage(mActivity, "提示", "是否使用电子现金？", "使用", new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            try {
                                mIsECash = true;
                                mPbocDev.importECashTipConfirmRes(true);
                                DialogFactory.dismissAlert(mActivity);
                                mHandler.sendEmptyMessage(PBOC_START_READ);
                            } catch (Exception e) {
                                LogUtils.e("请求确认是否使+e.getMessage()现金失败");
                                e.printStackTrace();
                            }
                        }
                    }, "不使用", new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            try {
                                mIsECash = false;
                                mPbocDev.importECashTipConfirmRes(false);
                                DialogFactory.dismissAlert(mActivity);
                                mHandler.sendEmptyMessage(PBOC_START_READ);
                            } catch (Exception e) {
                                LogUtils.e("请求确认是否使+e.getMessage()现金失败");
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                case FORCE_SWIPE_TIP:
                    DialogFactory.showConfirmMessage(mActivity, "温馨提示", "请挥卡", "确认", new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            startCheckCart(0);
                        }
                    });
                    break;
                case FORCE_INSERT_TIP:
                    DialogFactory.showConfirmMessage(mActivity, "读卡失败", "请插卡或切换成其他方式", "确认", new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            startCheckCart(0);
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };


    public void initPobcDev() {
        LogUtils.d("获取PBOC设备对象mPbocDev=" + mPbocDev);
        if (null == mPbocDev) {
            try {
                mPbocDev = AidlPboc.Stub.asInterface(mDeviceService.getEMVL2());
                LogUtils.d("获取PBOC设备对象成功mPbocDev=" + mPbocDev);
            } catch (Exception e) {
                LogUtils.e("获取PBOC对此异常"+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public int contentViewSourceID() {
        return R.layout.consume_submit;
    }

    @Override
    public void initView() {

        setTopTitle("输入卡号");
        //初始化输入方式，避免因旧值导致判断错误
        Cache.getInstance().setSerInputCode("");
        mParamConfigDao = new ParamConfigDaoImpl();
        settleDataDao = new SettleDataDaoImpl();
        mTransRecordDao = new TransRecordDaoImpl();
        setTopReturnListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                abordPboc();
                NldPaymentActivityManager.getActivityManager().removeActivity(mActivity);
            }
        });
        mCarnoLl = (RelativeLayout) findViewById(R.id.swipe_carno_tip_ll);
        mInputLl = (LinearLayout) findViewById(R.id.swipe_carno_input_ll);
        mMoneyLl = (RelativeLayout) findViewById(R.id.swipe_carno_show_money_ll);
        mMoneyTv = (TextView) findViewById(R.id.swipe_carno_input_money);
        mInputTip = (TextView) findViewById(R.id.swipe_carno_input_tip);
        mCarnoEt = (MEditText) findViewById(R.id.et_cardno);
        mNextBtn = (Button) findViewById(R.id.swipe_next_btn);
        mNextBtn.setOnClickListener(mNextClick);
        mCarnoTv = (TextView) findViewById(R.id.tv_tip_cardno);
        mCarnoTipTV = (TextView) findViewById(R.id.swipe_carno_tip_tip);
        iv_bottom = (ImageView) findViewById(R.id.iv_bottom);
        iv_bottom.setImageResource(R.drawable.pic_1_1_1);
        mMoneyTv.setText("￥ " + Cache.getInstance().getTransMoney());
        mLeftIv = (ImageView) findViewById(R.id.iv_left);
        mRightIv = (ImageView) findViewById(R.id.iv_right);
        mForceNfCLl = (LinearLayout) findViewById(R.id.iv_force_swipe_ll);
        mForceInsertLl = (LinearLayout) findViewById(R.id.iv_force_insert_ll);
        mLeftIv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                swipeTypeChange(true);  //挥卡或刷卡界面
            }
        });
        mRightIv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                swipeTypeChange(false);  //插卡界面
            }
        });
        showTransMoney(true);
        showInputCarno(false);
        initViewData();
        initForceNfcView(getSwipCardType());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        DialogFactory.dismissAlert(mActivity);
        stopCheckCard();
        super.onDestroy();
    }

    /**
     * 强制挥卡页面初始化
     *
     * @param type
     */
    private void initForceNfcView(int type) {
        switch (type) {
            case 1:
                mForceNfCLl.setVisibility(View.VISIBLE);
                mForceInsertLl.setVisibility(View.GONE);
                iv_bottom.setVisibility(View.GONE);
                mCarnoTv.setText("请挥卡或刷卡");
                break;
            case 2:
                mForceNfCLl.setVisibility(View.GONE);
                mForceInsertLl.setVisibility(View.VISIBLE);
                iv_bottom.setVisibility(View.GONE);
                mCarnoTv.setText("请插卡");
                break;
            case 3:
                mForceNfCLl.setVisibility(View.GONE);
                mForceInsertLl.setVisibility(View.GONE);
                iv_bottom.setVisibility(View.VISIBLE);
                mCarnoTv.setText("请挥卡、刷卡或插卡");
                break;
            default:
                break;
        }
    }

    private void swipeTypeChange(boolean isForceNfc) {
        isForceSwipe = isForceNfc;
        if (isForceNfc) { //强制挥卡
            mForceNfCLl.setVisibility(View.VISIBLE);
            mForceInsertLl.setVisibility(View.GONE);
            mCarnoTv.setText("请挥卡或刷卡");
        } else {//强制插卡
            mForceNfCLl.setVisibility(View.GONE);
            mForceInsertLl.setVisibility(View.VISIBLE);
            mCarnoTv.setText("请插卡");
        }
    }

    /**
     * 下一步监听器
     */
    public OnClickListener mNextClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            String carno = mCarnoEt.getText().toString().trim();
            if (null == carno || carno.length() < 9) {
                showTip("请刷/插卡或者手动输入卡号！");
                return;
            }
            mCarno = carno;
            mSecondTrack = null;
            mThirdTrack = null;
            mInvalidate = null;
            stopCheckCard();
            endPboc();
            nextStep(TransConstans.INPUT_TYPE_SG_PIN);
        }
    };

    public void stopCheckCard() {
        //取消检卡
        LogUtils.d("取消检卡");
        try {
            mPbocDev.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mIsChecking = false;
    }

    /**
     * 中断PBOC
     */
    private void abordPboc() {
        try {
            LogUtils.d("中断PBOC交易");
            mPbocDev.abortPBOC();
        } catch (RemoteException e) {
            LogUtils.d("中断PBOC流程失败");
        } catch (Exception e) {
            LogUtils.d("中断PBOC流程失败" + e.getMessage());
        }
        //清空回调
        BankApplication.mPbocListener.clearListener();
        mIsStartPBOC = false;
    }

    /**
     * 结束PBOC流程
     */
    public void endPboc() {
        if (!mIsStartPBOC) {
            return;
        }
        DialogFactory.dismissAlert(mActivity);
        try {// 结束PBOC流程
            mPbocDev.cancelCheckCard();
            mPbocDev.endPBOC();
        } catch (RemoteException e) {
            LogUtils.d("停止PBOC异常" + e.getMessage());
        } catch (Exception e) {
            LogUtils.d("停止PBOC异常" + e.getMessage());
        }
        //清空回调
        BankApplication.mPbocListener.clearListener();
        mIsStartPBOC = false;
    }

    /**
     * 进入到下一步
     *
     * @param inputMode 输入方式
     *                  </p>
     *                  011 手工方式，且带PIN
     *                  </p>
     *                  012 手工方式，且不带PIN
     *                  </p>
     *                  021 磁条读入，且带PIN
     *                  </p>
     *                  022 磁条读入, 且不带PIN
     *                  </p>
     *                  05x IC卡输入, 且磁条信息可靠
     *                  </p>
     *                  80x Fallback磁条卡
     *                  </p>
     *                  07x qPBOC快速支付
     *                  </p>
     *                  91x 非接触式磁条读入(CUPS MSD)
     *                  </p>
     *                  98x 标准PBOC借/贷记IC卡读入(非接触式)
     *                  </p>
     * @return
     */
    private boolean nextStep(String inputMode) {
        Cache.getInstance().setSerInputCode(inputMode);
        if (!saveValue()) {
            return false;
        }
        Intent it = null;
        if (inputMode.startsWith("01")) { //手输卡号，需要输入卡有效期
            it = new Intent(mContext, InputCardValidityActivity.class);
            it.putExtra("class", getNextStep().getComponent().getClassName());
            startActivity(it);
        } else {
            it = getNextStep();
        }
        if (null == it) {
            return false;
        }
        goToNextActivity(it);

        //非接300元以下 直接关闭这里的对话框 不结束页面 在当前页面发起交易
        if (inputMode.equals(TransConstans.INPUT_TYPE_QPBOC_NO_PIN)) {
            DialogFactory.dismissAlert(mActivity);
        } else {
            finish();
        }
        return true;
    }

    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {
        LogUtils.d("服务绑定成功service: " + service);
        initPobcDev();
        abordPboc();
        startCheckCart(3);
    }


    private int startTimes = 1;

    /**
     * 启动检卡1：支持磁条卡 2：支持IC卡 3：同时支持磁条与IC卡
     * @param checkType
     */
    private boolean startCheckCart(int checkType) {
        boolean result = false;
        try {
            boolean supportIC = true;
            boolean supportRF = true;
            boolean supportMG = true;
            String transCode = Cache.getInstance().getTransCode();
            LogUtils.d("开始检卡,启动类型" + checkType + ": 1：支持磁条卡 2：支持IC卡3：同时支持磁条与IC卡"+"交易类型:"+transCode);
            if (!StringUtil.isEmpty(transCode)){
                if (transCode.equals(TransConstans.TRANS_CODE_QC_ZD)
                        || transCode.equals(TransConstans.TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER)
                        || (transCode.equals(TransConstans.TRANS_CODE_QC_FZD) && isInCard)){   //---->1.指定账户圈存，2.非指定账户圈存转入卡,3.电子现金普通消费，只支持插卡
                    supportRF = false;
                    supportMG = false;
                }else if (transCode.equals(TransConstans.TRANS_CODE_DZXJ_TRANS_QUICK_PAY)){  //----->电子现金快速消费，只支持挥卡
                    supportIC = false;
                    supportMG = false;
                }else {
                    switch (checkType){
                        case 1:  //磁条卡
                            supportRF = false;
                            supportIC = false;
                            break;
                        case 2: //IC卡<插卡>
                            supportRF = false;
                            supportMG = false;
                            break;
                        default:  //三种类型都可
                            break;
                        }
                    }
                    mPbocDev.checkCard(supportMG, supportIC, supportRF, 60000, mCheckCardListener);
                }else {
                    LogUtils.e("交易类型为空 ,无法交易");
                }
            result = true;
            mIsChecking = true;
        } catch (Exception e) {
            LogUtils.e("服务绑定失败" + e.getMessage());
            bindService();
            e.printStackTrace();
            mIsChecking = false;
            result = false;

        }
        LogUtils.d("开启检卡是否成功：" + result);
        return result;
    }

    @Override
    public void onServiceBindFaild() {

    }

    @Override
    public boolean saveValue() {
        Cache.getInstance().setCardNo(mCarno);
        Cache.getInstance().setTrack_2_data(mSecondTrack);
        Cache.getInstance().setTrack_3_data(mThirdTrack);
        Cache.getInstance().setInvalidDate(mInvalidate);
        return true;
    }

    /**
     * 页面显示交易金额
     */
    public void showTransMoney(boolean isShow) {
        if (isShow) {
            mMoneyLl.setVisibility(View.VISIBLE);
        } else {
            mMoneyLl.setVisibility(View.GONE);
        }
    }

    /**
     * 显示卡号输入框
     */
    public void showInputCarno(boolean isShow) {
        if (isShow) {
            mInputLl.setVisibility(View.VISIBLE);
            mCarnoLl.setVisibility(View.GONE);
        } else {
            mInputLl.setVisibility(View.GONE);
            mCarnoLl.setVisibility(View.VISIBLE);
        }
    }

    //
    /**
     * pboc开始交易回调-----------------------------------------------------------
     * *************************************************************************
     * **
     */
    public PbocListener.PbocProcessListener mPbocProcessListener = new PbocListener.PbocProcessListener() {

        @Override
        public void onConfirmCardInfo(CardInfo arg0) throws RemoteException {
            String transCode = Cache.getInstance().getTransCode();
            if (mIsECash) {   //使用电子现金流程
                LogUtils.d("卡信息确认使用电子现金");
                try {
                    mPbocDev.importConfirmCardInfoRes(true);
                    if (TransConstans.TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER.equals(transCode)) {  //电子现金普通消费
                        try {
                            byte[] results = new byte[1024];
                            int count = 0;
                            count = mPbocDev.readKernelData(PbocDev.getKernalTag(Cache.getInstance().getTransCode()), results);
                            byte[] datas2 = new byte[count];
                            System.arraycopy(results, 0, datas2, 0, count);
                            String f55Data = HexUtil.bcd2str(datas2);
                            // 设置55域
                            Cache.getInstance().setTlvTag55(f55Data);
                            LogUtils.d("读取内核数据结果：" + DataConverter.bytesToHexString(results) + "长度：" + count);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            playSound(1, 0);
            //清空回调,确认卡信息后不再处理回调结果
            BankApplication.mPbocListener.clearListener();
            // 请求确认卡信息
            mCarno = arg0.getCardno();
            mCarno = mCarno.replace("F", "");
            if (StringUtil.isEmpty(mCarno) || mCarno.length() < 12 || mCarno.length() > 19) {  //---->这里处理获取卡信息失败时的处理，其中包括IC卡获取信息失败降级处理
                abordPboc();
                showToastTip("无效卡号");
                mHandler.sendEmptyMessage(PBOC_REPEAT);
                return;
            }
            LogUtils.d("PBOC确认卡信息");
            if (TransConstans.TRANS_CODE_OFF_TH.equals(transCode)) {  //--->脱机消费
                Intent it = getNextStep();
                goToNextActivity(it);
                return;
            }
            if (isInCard) {   // 非指定圈存账户为转入卡,直接开始交易，因为转出卡仅需要55域数据
                LogUtils.d("PBOC非指定账户圈存，转入卡：" + mCarno);
                mInvalidate = Cache.getInstance().getInvalidDate();
                mFirstTrack = null;
                mSecondTrack = Cache.getInstance().getTrack_2_data();
                mThirdTrack = null;
                Cache.getInstance().setAdddataword(mCarno);// 附加数据为转入卡号
                Intent it = getNextStep();
                if (null == it) {
                    LogUtils.d("下一步界面为空");
                    return;
                }
                goToNextActivity(it);
                return;
            }
            //不读取磁道2，到交易时读取
            LogUtils.i("确认卡的信息这里到底走还是不走"+inputMethodStr);
            getTrans2Data();
            nextStep(inputMethodStr);
        }

        @Override
        public void onError(int arg0) throws RemoteException {
            LogUtils.e("PBOC交易失败onError：" + arg0);
            Intent it = new Intent(mContext, TransErrorResultActivity.class);

            Cache.getInstance().setErrCode(NldException.ERR_DEV_READ_CARD_E301);
            Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_READ_CARD_E301));
            startActivity(it);
        }

        @Override
        public void onReadCardLoadLog(String arg0, String arg1, PCardLoadLog[] arg2) throws RemoteException {
            // 返回读取卡片圈存日志结果
            LogUtils.d("PBOC返回读取卡片圈存日志结果");
        }

        @Override
        public void onReadCardOffLineBalance(String arg0, String arg1, String arg2, String arg3)
                throws RemoteException {
            // 返回读取卡片多级余额结果
            LogUtils.d("PBOC返回读取卡片多级余额结果");

        }

        @Override
        public void onReadCardTransLog(PCardTransLog[] arg0) throws RemoteException {
            // 返回读取卡片交易日志结果
            LogUtils.d("PBOC返回读取卡片交易日志结果");

        }

        @Override
        public void onRequestOnline() throws RemoteException {
            // 请求联机
            LogUtils.d("请求联机");
            getTrans2Data();
            //消费非接<目前是指挥卡操作>Q联机 此处直接读取内核数据 不走thread emv流程
            if (inputMethodStr.startsWith("07")) {
                try {
                    byte[] results = new byte[1024];
                    int count = 0;
                    count = PbocDev.getInstance(mContext, mDeviceService)
                            .getOriginalDev().readKernelData(PbocDev.getKernalTag(Cache.getInstance().getTransCode())
                                    , results);
                    LogUtils.d("读取内核数据结果：" + DataConverter.bytesToHexString(results) + "长度：" + count);

                    byte[] datas = null;
                    if (count > 0) {
                        datas = new byte[count];
                        System.arraycopy(results, 0, datas, 0, count);
                        LogUtils.d("1field55 =" + HexUtil.bcd2str(datas));
                        LogUtils.d("2field55 =" + DataConverter.bytesToHexString(datas));
                        LogUtils.d("3field55 =" + HexUtil.bcd2str(new String(datas).getBytes()));
                    }
                    String tag55 = HexUtil.bcd2str(datas);
                    Cache.getInstance().setTlvTag55(tag55);
                    LogUtils.d("55域数据：" + tag55);
                    //获取磁道、卡片序列号、ARQC数据
                    getTraceAndArqc();


                } catch (RemoteException e) {
                    LogUtils.e("55域获取失败，PBOC对象获取失败", e);
                    e.printStackTrace();
                } catch (Exception e) {
                    LogUtils.e("55域获取失败", e);
                    e.printStackTrace();
                }
                nextStep(inputMethodStr);
            } else {
                LogUtils.i("插卡是否走联机操作");
                Cache.getInstance().setSerInputCode("051");
                goToNextActivity(getNextStep());
                abordPboc();
            }
        }

        @Override
        public void onTransResult(int arg0) throws RemoteException {

            if (!mIsStartPBOC) {
                //避免出现未启动PBOC时出错
                return;
            }
            // 批准: 0x01 拒绝: 0x02 终止: 0x03
            // FALLBACK: 0x04 采用其他界面: 0x05 其他： 0x06
            Intent it;
            LogUtils.d("PBOC交易结果：" + arg0);
            if (arg0 != 0x01) {
                DialogFactory.dismissAlert(mActivity);
            }
            switch (arg0) {
                case 0x02:   //交易拒绝
                    it = new Intent(mContext, TransErrorResultActivity.class);
                    Cache.getInstance().setErrCode(NldException.ERR_DEV_TRANS_REFUSE_E307);
                    Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_TRANS_REFUSE_E307));
                    startActivity(it);
                    endPboc();
                    break;
                case 0x04:   //fallback 降级处理
                    showResultTip("请刷卡");
                    endPboc();
                    break;
                case 0x05:   //交易其他界面
                    it = new Intent(mContext, TransErrorResultActivity.class);
                    Cache.getInstance().setErrCode(NldException.ERR_DEV_TRANS_OTHERPAGE_E324);
                    Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_TRANS_OTHERPAGE_E324));
                    startActivity(it);
                    endPboc();
                    break;
                case 0x06:
                case 0x03:  //交易终止
                    it = new Intent(mContext, TransErrorResultActivity.class);
                    Cache.getInstance().setErrCode(NldException.ERR_DEV_READ_CARD_E302);
                    Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_READ_CARD_E302));
                    startActivity(it);
                    endPboc();
                    break;
                case 0x01:   //交易批准
                    String transCode = Cache.getInstance().getTransCode();
                    LogUtils.i("交易批准");
                    if (mIsECash && (TransConstans.TRANS_CODE_DZXJ_TRANS_QUICK_PAY.equals(transCode)
                            || TransConstans.TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER.equals(transCode))){  //电子现金消费 <包括普通消费与快速消费,只在消费业务的情况下使用>
                        Intent intent = getNextStep();
                        intent.putExtra("cardno", mCarno);
                        intent.putExtra("paymoney", Cache.getInstance().getTransMoney());
                        getTrans2Data();
                        getICData();
                        saveDataToDB();
                        goToNextActivity(intent);
                   }else if (TransConstans.TRANS_CODE_CONSUME_CX.equals(transCode)) {  //消费撤销
                        LogUtils.i("消费撤销获取卡信息的结果");
                        getTrans2Data();
                        mCarno = Cache.getInstance().getCardNo();
                        nextStep(inputMethodStr);
                    }
                    break;
                default:
                    it = new Intent(mContext, TransErrorResultActivity.class);
                    //读卡失败，交易终止
                    Cache.getInstance().setErrCode(NldException.ERR_DEV_READ_CARD_E302);
                    Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DEV_READ_CARD_E302));
                    startActivity(it);
                    break;
            }

        }

        @Override
        public void requestAidSelect(int arg0, String[] arg1) throws RemoteException {
            // /**请求多应用选择*/
            LogUtils.d("PBOC请求多应用选择arg0=" + arg0 + "====" + arg1);
            multAids = arg1;
            mHandler.sendEmptyMessage(PBOC_MUL_AID);
        }


        @Override
        public void requestEcashTipsConfirm() throws RemoteException {
            String transCode = Cache.getInstance().getTransCode();

             /**请求确认是否使用电子现金*/
            if (TransConstans.TRANS_CODE_QUERY_BALANCE.equals(transCode)
                    || TransConstans.TRANS_CODE_CONSUME_CX.equals(transCode)
                    || TransConstans.TRANS_CODE_LJTH.equals(transCode)
                    || TransConstans.TRANS_CODE_PRE.equals(transCode)
                    || TransConstans.TRANS_CODE_PRE_COMPLET.equals(transCode)
                    || TransConstans.TRANS_CODE_PRE_COMPLET_CX.equals(transCode)
                    || TransConstans.TRANS_CODE_PRE_CX.equals(transCode)
                    || TransConstans.TRANS_CODE_QC_ZD.equals(transCode)
                    || TransConstans.TRANS_CODE_QC_FZD.equals(transCode)) { //除了消费,电子现金普通消费插卡，电子现金消费查询，电子现金交易查询，暂时不支持电子现金消费
                try {
                    mPbocDev.importECashTipConfirmRes(false);
                } catch (Exception e) {
                    LogUtils.e("请求确认是否使用电子现金失败");
                    e.printStackTrace();
                }
                return;
            }
            LogUtils.e("消费是否走电子现金消费的流程");
            mHandler.sendEmptyMessage(PBOC_USE_ECASH);
        }

        @Override
        public void requestImportAmount(int arg0) throws RemoteException {
            /** 请求输入金额 */
            LogUtils.d("PBOC请求输入金额:" + arg0);
            /*
             * 金额类别（ 1byte），取值说明： 0x01：只要授权金额； 0x02：只要返现金额； 0x03：既要授权金额，也要返现金额；
			 */
            switch (arg0) {
                case 0x01://只要授权⾦额
                    if (!StringUtil.isEmpty(Cache.getInstance().getTransMoney())) {
                        // 如果缓存中存在金额，则说明在刷卡前已输入金额，则直接传入金额
                        try {
                            mPbocDev.importAmount(Cache.getInstance().getTransMoney());
                        } catch (Exception e) {
                            LogUtils.e("金额导入失败，获取PBOC实例异常", e);
                            e.printStackTrace();
                        }
                    } else {
                        // 如果缓存中不存在金额，说明需要输入金额，所以进入下一页（此处默认下一页会是输入金额）
                        try {
                            mPbocDev.importAmount("0.01");
                        } catch (Exception e) {
                            LogUtils.e("金额导入失败，获取PBOC实例异常", e);
                            e.printStackTrace();
                        }
                    }

                    break;
                case 0x02://只要返现⾦额
                    break;
                case 0x03://既要授权⾦额，也要返现⾦额
                    if (!StringUtil.isEmpty(Cache.getInstance().getTransMoney())) {
                        LogUtils.d("请求输入金额，既要授权又要反现金额");
                        // 如果缓存中存在金额，则说明在刷卡前已输入金额，则直接传入金额
                        try {
                            mPbocDev.importAmount(Cache.getInstance().getTransMoney());
                        } catch (Exception e) {
                            LogUtils.e("金额导入失败，获取PBOC实例异常", e);
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }

        }

        @Override
        public void requestImportPin(int arg0, boolean arg1, String arg2) throws RemoteException {
            // /** 请求导入 PIN */
            LogUtils.d("PBOC请求导入 PIN arg0" + arg0 + ";arg1:" + arg1 + "arg2:" + arg2);
            try {
                mPbocDev.importPin("26888888FFFFFFFF");
            } catch (Exception e) {
                LogUtils.e("联机交易时导入PIN失败");
                e.printStackTrace();
            }
        }

        @Override
        public void requestTipsConfirm(String arg0) throws RemoteException {
            /**
             * 请求提示信息， 提示信息格式为 16 进制字符串， 格式为 显示标志+显示超时时间+显示标题长度+显
             * 示标题内容+显示内容长度+显示内容； 显示标志： 1byte，表示是否需要持卡人确 认； 0x00：不需要确认；
             * 0x01：需要确认； 显示超时时间： 1byte，单位 s； 显示标题长度： 1byte，若为0，标题内容不 存在； 标题内容：
             * ASC 码，若“显示标题长度”为 0，则该字段不存在； 显示内容长度： 1byte，若为0，若“显示内
             * 容长度”为0，则该字段不存在； 显示内容： ASC 码
             */

            LogUtils.d("PBOC请求提示信息" + arg0);
            try {
                mPbocDev.importMsgConfirmRes(true);
            } catch (Exception e) {
                LogUtils.e("请求信息确认失败", e);
                e.printStackTrace();
                abordPboc();
            }
        }

        @Override
        public void requestUserAuth(int arg0, String arg1) throws RemoteException {
            // /** 请求身份认证 */
            LogUtils.d("PBOC请求身份认证 arg0=" + arg0 + ";arg1=" + arg1);
        }
    };
    // *******************************PBOC回调结束**********************************

    // ************************开始***********PBOC检卡回调************************************
    public AidlCheckCardListener mCheckCardListener = new AidlCheckCardListener.Stub() {

        @Override
        public void onTimeout() throws RemoteException {
            // 检卡超时
            LogUtils.d("PBOC检卡超时");
            try {
                mPbocDev.cancelCheckCard();
                LogUtils.d("停止IC卡检卡");
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSwipeCardFail() throws RemoteException {
            // 刷卡失败
            LogUtils.d("PBOC检卡失败");
            if (!isChekCardSuccess(10)) {
                return;
            }
            mIsChecking = false;
            showToastTip("刷卡失败请重刷");
            //重新开始检卡
            mHandler.sendEmptyMessage(PBOC_REPEAT);
        }

        @Override
        public void onFindRFCard() throws RemoteException {
            // 检索到非接卡
            LogUtils.d("PBOC检索到挥卡");
            if (!isChekCardSuccess(12)) {
                return;
            }
            String transCode = Cache.getInstance().getTransCode();
            if (TransConstans.TRANS_CODE_QC_ZD.equals(transCode)
                    || TransConstans.TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER.equals(transCode)
                    || (TransConstans.TRANS_CODE_QC_FZD.equals(transCode) && isInCard)){
                LogUtils.i("该交易暂不支持挥卡的流程");
                return;
            }

            mIsChecking = false;
            LogUtils.d(" 检索到挥卡,交易代码" + transCode);
            mHandler.sendEmptyMessage(PBOC_START_READ);
            mIsChecking = false;
            //启动PBOC流程
            try {
                //通过300元来判断输入条件 低于300不需要pin 072 反之071
                try {
                    double inputMoney = 0.00;
                    if (!StringUtil.isEmpty(Cache.getInstance().getTransMoney())) {
                        inputMoney = Double.valueOf(Cache.getInstance().getTransMoney());
                    }
                    String no_pin = ParamsUtil.getInstance().getParam(ParamsConts.PINPAD_PIN);
                    String noneedmoney = ParamsUtil.getInstance().getParam(ParamsConts.PINPAD_AMOUNT);
                    if (inputMoney < Double.valueOf(noneedmoney) && ParamsConts.NO_PIN.equals(no_pin)) { // 限额免密操作的消费方式 <当满足最低金额且免密标志时 才免密>
                        inputMethodStr = TransConstans.INPUT_TYPE_QPBOC_NO_PIN;
                    } else {  //需要输入密码的方式
                        inputMethodStr = TransConstans.INPUT_TYPE_QPBOC_PIN;
                    }
                } catch (NullPointerException e) {
                    LogUtils.e("处理是否需要免密出现异常，非接交易开启密码", e);
                    inputMethodStr = TransConstans.INPUT_TYPE_QPBOC_PIN;
                }
                LogUtils.d("qqq 非接输入条件为:" + inputMethodStr);
                BankApplication.mPbocListener.setPbocProcessListener(mPbocProcessListener);
                mIsStartPBOC = true;
                LogUtils.d("检卡成功启动PBOC流程");
                mPbocDev.processPBOC(PbocDev.getPbocEmvTransData2(Cache.getInstance().getTransCode()),
                        BankApplication.mPbocListener);
            } catch (Exception e) {
                LogUtils.e("启动PBOC流程失败，PBOC对象获取异常", e);
                e.printStackTrace();
            }
        }

        @Override
        public void onFindMagCard(final TrackData arg0) throws RemoteException {
            LogUtils.d("检卡检测到磁条卡");
           String transCode = Cache.getInstance().getTransCode();
            // 检索到磁条卡,若是电子现金普通消费(快速支付)，脱机消费，指定账户圈存(非接快速支付)，磁条卡是不能用的
            if (!StringUtil.isEmpty(transCode) &&
                    (transCode == TransConstans.TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER
                            || transCode == TransConstans.TRANS_CODE_DZXJ_TRANS_QUICK_PAY
                    || transCode == TransConstans.TRANS_CODE_OFF_TH
                    || transCode == TransConstans.TRANS_CODE_QC_FZD)) {
                return;
            }
            if (!isChekCardSuccess(10)) {
                return;
            }
            mIsChecking = false;
            mCarno = arg0.getCardno();
            mInvalidate = arg0.getExpiryDate();
            mFirstTrack = arg0.getFirstTrackData();
            mSecondTrack = arg0.getSecondTrackData();
            mThirdTrack = arg0.getThirdTrackData();
            LogUtils.d("卡号:" + mCarno+"卡有效期：" +mInvalidate+"一磁道信息" + mFirstTrack+"二磁道信息" + mSecondTrack+ "三磁道信息" + mThirdTrack);
            if (StringUtil.isEmpty(mCarno) || mCarno.length() < 12 || mCarno.length() > 19) {
                showToastTip("无效卡号");
                mHandler.sendEmptyMessage(PBOC_REPEAT);
                return;
            }
            if (!StringUtil.isEmpty(mSecondTrack)) {
                mSecondTrack = mSecondTrack.replaceAll("[:;<>=]", "D"); // 替换二磁道信息中出现的特殊符号
            }
            if (!StringUtil.isEmpty(mThirdTrack)) {
                mThirdTrack = mThirdTrack.replaceAll("[:;<>=]", "D"); // 替换三磁道信息中出现的特殊符号
            }
            try {
                String posInputType = Cache.getInstance().getSerInputCode();
                if ("801".equals(posInputType)) {    //--->fallback降级处理,<这个现象存在于：当插卡时读卡信息失败时，存在磁条卡时，则出现降级>
                    LogUtils.d("IC卡降级处理");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCarnoEt.setText(arg0.getCardno());
                            mCarnoEt.refreshDrawableState();
                            mCarnoTv.setText(arg0.getCardno());
                            // 默认磁卡输入有pin，到输密界面做调整 <降级流程的处理>
                            nextStep(TransConstans.INPUT_TYPE_FALLBACK_PIN);
                        }
                    });
                    playSound(1, 0);
                    return;
                } else {    //--->按正常的磁条卡流程上送
                    // 判断是否为IC卡，若是IC卡，则要求插卡处理
                    if (mSecondTrack != null && mSecondTrack.contains("D")) {
                        String serviceCode = mSecondTrack.substring(mSecondTrack.indexOf("D") + 5, mSecondTrack.indexOf("D") + 6);
                        //如果是IC卡提示请插卡
                        if ("2".equals(serviceCode) || "6".equals(serviceCode)) {  // 先刷IC卡
                            // 需要提示请插卡
                            showResultTip("请插卡");
                            LogUtils.d("该卡为IC卡，请插卡");
                            return;
                        }
                    }
                    Cache.getInstance().setSerInputCode("021");
                }
            } catch (Exception e) {
                Cache.getInstance().setSerInputCode("021");
                e.printStackTrace();
            }
            playSound(1, 0);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCarnoEt.setText(arg0.getCardno());
                    mCarnoEt.refreshDrawableState();
                    mCarnoTv.setText(arg0.getCardno());
                    // 默认磁卡输入有pin，到输密界面做调整<正常磁条卡的流程>
                    nextStep(TransConstans.INPUT_TYPE_MAG_PIN);
                }
            });
        }

        @Override
        public void onFindICCard() throws RemoteException {
            // 检索到IC卡
            if (!isChekCardSuccess(11)) {
                return;
            }
            String transCode = Cache.getInstance().getTransCode();
            if (TransConstans.TRANS_CODE_DZXJ_TRANS_QUICK_PAY.equals(transCode)){  //电子现金快速消费
                LogUtils.i("暂不支持该交易");
                return;
            }
            if (!isInCard){  //-->非指定账户圈存转入卡时，输入的模式都是一样的
                inputMethodStr = TransConstans.INPUT_TYPE_IC_PIN;
            }
            LogUtils.d(" 检索到IC卡,交易代码" + transCode);
            mHandler.sendEmptyMessage(PBOC_START_READ);
            mIsChecking = false;
            //启动PBOC流程
            try {
                BankApplication.mPbocListener.setPbocProcessListener(mPbocProcessListener);
                mIsStartPBOC = true;
                LogUtils.d("检卡成功启动PBOC流程");
                mPbocDev.processPBOC(PbocDev.getPbocEmvTransData(Cache.getInstance().getTransCode()),
                        BankApplication.mPbocListener);
            } catch (Exception e) {
                LogUtils.e("启动PBOC流程失败，PBOC对象获取异常", e);
                e.printStackTrace();
            }
        }

        @Override
        public void onError(int arg0) throws RemoteException {
            // 检卡异常
            LogUtils.d("PBOC检卡异常" + arg0);
            switch (arg0) {
                case 13://IC卡刷卡会返回13
                    showResultTip("请插卡");
                    break;
                default:
                    showToastTip("刷/插卡失败请重新刷/插卡");
                    LogUtils.d("刷/插卡失败请重新刷/插卡");
                    mHandler.sendEmptyMessage(PBOC_REPEAT);
                    break;
            }

        }

        @Override
        public void onCanceled() throws RemoteException {
            // 取消检卡
            LogUtils.d("PBOC取消检卡");

        }
    };
    // ******************************PBOC检卡回调结束*******************************************

    private void showResultTip(String tip) {
        Message msg = new Message();
        msg.what = MSG_TIP;
        msg.obj = tip;
        mHandler.sendMessage(msg);
    }

    private void showToastTip(String tip) {
        Message msg = new Message();
        msg.what = TOAST_TIP;
        msg.obj = tip;
        mHandler.sendMessage(msg);
    }

    private void sendMsgToHandle(int msgWhat) {
        Message msg = new Message();
        msg.what = msgWhat;
        mHandler.sendMessage(msg);
    }

    public void getTrans2Data() {
        try {
            String[] tags = {EMVTAGStr.EMVTAG_APP_PAN,
                    EMVTAGStr.EMVTAG_TRACK2,
                    EMVTAGStr.EMVTAG_APP_PAN_SN,
                    EMVTAGStr.TAG_5F24_IC_APPEXPIREDATE};
            byte[] track_2_byte = new byte[512];
            int trac_count;
            trac_count = mPbocDev.readKernelData(tags, track_2_byte);
            LogUtils.d("PBOC再次读取内核获取磁道：读取长度" + trac_count);
            if (trac_count > 0) {
                byte[] trackResult = new byte[trac_count];
                System.arraycopy(track_2_byte, 0, trackResult, 0, trac_count);
                LogUtils.d("磁道等数据：" + DataConverter.bytesToHexString(trackResult));
                Map<String, String> resMap = TlvUtil.tlvToMap(HexUtil.bcd2str(trackResult));
                if (resMap.get("5A") == null) { //如果5A域未读取到，则从磁道数据中获取
                    String track2data = resMap.get("57");
                    String card = track2data.split("D")[0];
                    resMap.put("5A", card);
                }
                LogUtils.i("TLV格式数据="+resMap.size()+"/="+resMap.toString());
                Cache.getInstance().setCardNo(resMap.get("5A").replace("F", ""));
                LogUtils.d(resMap.get("5A").replace("F", "") + " ............磁道2数据：. " + resMap.get("57") + " .............. " + resMap.get("5F34"));
                String mSecondTrack = resMap.get("57").replace("F", "");
                mSecondTrack.replaceAll("[:;<>=]", "D");
                String invalidate = null;
                if (!StringUtil.isEmpty(mSecondTrack) && mSecondTrack.contains("D")) {
                    int pos = mSecondTrack.indexOf("D");
                    invalidate = mSecondTrack.substring(pos + 1, pos + 5);
                    if (StringUtil.isEmpty(invalidate)) {
                        invalidate = "0000";
                    }
                    LogUtils.d("IC卡有效期=" + invalidate);
                    Cache.getInstance().setInvalidDate(invalidate);
                }
                LogUtils.d("卡片序列号：" + resMap.get("5F34"));
                Cache.getInstance().setCardSeqNo(resMap.get("5F34"));

                mInvalidate = invalidate;
                mFirstTrack = null;
                this.mSecondTrack = mSecondTrack;
                Cache.getInstance().setTrack_2_data(mSecondTrack);
                mThirdTrack = null;
            }

        } catch (RemoteException e) {
            LogUtils.e("读取IC卡数据失败", e);
            e.printStackTrace();
        } catch (Exception e) {
            LogUtils.e("读取IC卡数据失败", e);
            e.printStackTrace();
        }
    }

    /**
     * 将脱机消费数据保存到数据库
     *
     * @param
     */
    public void saveDataToDB() {

        batchno = TransParamsUtil.getCurrentBatchNo(); // 批次号
        billno = TransParamsUtil.getBillNo(); // 凭证号
        systraceno = billno; // POS流水号
        TransRecord transRecord = MapObjToTransBean();   // 交易流水表记录
        mTransRecordDao.save(transRecord);
        SettleData settleData = MapObjToSettleBean();    // 结算表记录
        settleDataDao.save(settleData);
    }

    //将Map数据转化为结算明细表记录对象
    public SettleData MapObjToSettleBean() {
        SettleData record = new SettleData();
        record.priaccount = Cache.getInstance().getCardNo();//卡号
        record.batchbillno = batchno + billno;
        record.transamount = Utility.formatMount(Cache.getInstance().getTransMoney());//金额
        record.conditionmode = "00";//服务条件码
        record.transprocode = "000000";//交易处理码
        record.reserve1 = "0330"; // 保留字段1，保存交易下发的消息类型
        return record;
    }

    public void getICData() {
        byte[] resultTemp = new byte[1024];
        try {
            int count = PbocDev.getInstance(mContext, mDeviceService).getOriginalDev().readKernelData(EMVTAGStr.getLakalaF55UseModeOne(), resultTemp);
            LogUtils.d("读取内核55域长度：" + count);
            if (count <= 0) {
                return;
            }
            byte[] icData = new byte[count];
            System.arraycopy(resultTemp, 0, icData, 0, count);
            LogUtils.d("55域数据：" + HexUtil.bcd2Str(icData));
            Cache.getInstance().setTlvTag55(HexUtil.bcd2str(icData));
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 非接Q联机获取内核数据
     */
    public void getTraceAndArqc() {
        String[] tags = {
                EMVTAGStr.EMVTAG_APP_PAN,
                EMVTAGStr.EMVTAG_TRACK2,
                EMVTAGStr.EMVTAG_APP_PAN_SN
        };
        byte[] track_2_byte = new byte[512];
        int trac_count = 0;
        try {

            trac_count = PbocDev.getInstance(mContext, mDeviceService)
                    .getOriginalDev().readKernelData(tags, track_2_byte);
        } catch (RemoteException e2) {
            e2.printStackTrace();
            LogUtils.e("读取内核磁道数据失败!", e2);
            return;
        } catch (Exception e2) {
            e2.printStackTrace();
            LogUtils.e("读取内核磁道数据失败!", e2);
            return;
        }
        LogUtils.d("再次读取内核获取磁道：读取长度" + trac_count);
        //非指定账户圈存不获取转入卡磁道数据，其实此处可以去掉，因为在刷卡时已经获取了磁道数据
        if (trac_count > 0
                && !Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_QC_FZD)) {
            byte[] trackResult = new byte[trac_count];
            System.arraycopy(track_2_byte, 0, trackResult, 0, trac_count);
            String magData = DataConverter.bytesToHexString(trackResult);
            LogUtils.d("磁道等数据：" + DataConverter.bytesToHexString(trackResult));
            String data = HexUtil.bcd2str(trackResult);
            Map<String, String> resMap = TlvUtil.tlvToMap(HexUtil.bcd2str(trackResult));
            if (resMap.get("5A") == null) {//如果5A域未读取到，则从磁道数据中获取
                String track2data = resMap.get("57");
                String card = track2data.split("D")[0];
                resMap.put("5A", card);
                mCarno = card;
            } else {
                mCarno = resMap.get("5A");
            }
            LogUtils.d(resMap.get("5A").replace("F", "") + " " + resMap.get("57") + " " + resMap.get("5F34"));
            LogUtils.d("磁道2数据：" + resMap.get("57").replace("F", ""));
            String mSecondTrack = resMap.get("57").replace("F", "");
            mSecondTrack.replaceAll("[:;<>=]", "D");
            Cache.getInstance().setTrack_2_data(mSecondTrack);
            if (!StringUtil.isEmpty(mSecondTrack) && mSecondTrack.contains("D")) {
                int pos = mSecondTrack.indexOf("D");
                String invalidate = mSecondTrack.substring(pos + 1, pos + 5);
                if (StringUtil.isEmpty(invalidate)) {
                    invalidate = "0000";
                }
                LogUtils.d("IC卡有效期=" + invalidate);
                Cache.getInstance().setInvalidDate(invalidate);
            }
            LogUtils.d("卡片序列号：" + resMap.get("5F34"));
            Cache.getInstance().setCardSeqNo(resMap.get("5F34"));
        } else {
            if (Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_QC_FZD)) {
                LogUtils.d("非指定账户圈存不读取装入卡的磁道数据");
            }
        }

        AidlPboc mDev = null;
        try {
            mDev = PbocDev.getInstance(mContext, mDeviceService).getOriginalDev();
        } catch (Exception e1) {
            LogUtils.e("获取PBOC设备异常，读取ARQC失败");
            e1.printStackTrace();
            return;
        }
        // 读取ARQC值
        String arqc = null;
        try {
            byte[] arqcTemp = new byte[256];
            int num = mDev.readKernelData(new String[]{EMVTAGStr.EMVTAG_AC}, arqcTemp);
            byte[] arqcData = new byte[num];
            if (num >= 0) {
                System.arraycopy(arqcTemp, 0, arqcData, 0, num);
                arqc = HexUtil.bcd2str(arqcData);
            }
            Cache.getInstance().setPrintArqc(arqc);
            byte[] temp3 = new byte[1024];
            int count3 = mDev.readKernelData(EMVTAGStr.getkernelDataForPrint(), temp3);
            byte[] data3 = new byte[count3];
            System.arraycopy(temp3, 0, data3, 0, count3);
            String printData = HexUtil.bcd2str(data3);
            LogUtils.d("打印内核数据：" + printData);
            if (!"".equals(arqc) && arqc != null) {
                LogUtils.d("arqc tag替换前：" + arqc);
                arqc = arqc.replaceFirst("9F26", "9F99");
                LogUtils.d("arqc tag替换后：" + arqc);
                printData = printData + arqc;
            }
            Cache.getInstance().setPrintIcData(printData);
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
        }
        LogUtils.d("kernelProc读取 arqc =" + arqc);

    }

    /**
     * 电子现金普通消费获取交易记录对象
     * modify by chenkehui for 8583jar-ic
     *
     * @param
     * @return
     */
    public TransRecord MapObjToTransBean() {
        Map<String, String> dataMap = new HashMap<String, String>();
        TransRecord record = null;
        try {
            record = new TransRecord();
            record.setPriaccount(Cache.getInstance().getCardNo()); // 主账号 F2
            dataMap.put("priaccount", Cache.getInstance().getCardNo());
            record.setTransprocode("000000"); // 交易处理码 F3
            dataMap.put("transprocode", "000000");
            record.setTransamount(Utility.formatMount(Cache.getInstance().getTransMoney())); // 交易金额 F4
            dataMap.put("transamount", Cache.getInstance().getTransMoney());
            record.setSystraceno(String.valueOf(systraceno)); // pos流水号(11域)
            dataMap.put("systraceno", "000000");
            String date = Utility.getTransLocalDate();
            record.setTranslocaldate(date);
            dataMap.put("translocaldate", date);
            String time = Utility.getTransLocalTime();
            record.setTranslocaltime(time);
            dataMap.put("translocaltime", time);
            record.setExpireddate(Cache.getInstance().getInvalidDate()); // 卡有效期（14域）
            dataMap.put("expireddate", Cache.getInstance().getInvalidDate());
            Cache.getInstance().setSerInputCode("072");
            record.setEntrymode("072"); // POS输入方式(22域)
            dataMap.put("entrymode", "072");
            record.setSeqnumber(Cache.getInstance().getCardSeqNo()); // F23
            dataMap.put("seqnumber", Cache.getInstance().getCardSeqNo());
            record.setConditionmode("00");  // F25
            dataMap.put("conditionmode", "00");
            String termId = mParamConfigDao.get("unionpay_termid");
            record.setTerminalid(termId); // F41
            dataMap.put("terminalid", termId);
            String merId = mParamConfigDao.get("unionpay_merid");
            record.setAcceptoridcode(merId); // F42
            dataMap.put("acceptoridcode", merId);
            record.setAdddataword("300"); // F48
            dataMap.put("adddataword", "");
            record.setTranscurrcode("156"); // F49
            dataMap.put("transcurrcode", "156");
            record.setIcdata(Cache.getInstance().getTlvTag55()); //F55
            dataMap.put("icdata", Cache.getInstance().getTlvTag55());
            String loadPa = mParamConfigDao.get("operatorcode") + mParamConfigDao.get("operatorpwd") + "50";
            record.setLoadparams(loadPa); //F60
            dataMap.put("loadparams", loadPa);
            record.setBatchbillno(batchno + billno); // F62
            dataMap.put("batchbillno", batchno + billno);
            String printData = getKernelDataForPrint();
            record.setReserve4(printData);
            dataMap.put("reserve4", printData);
            record.setStatuscode("OF"); // offline脱机标志
            record.setReserve1("0330"); // 保留字段1，保存交易下发的消息类型
            Cache.getInstance().setResultMap(dataMap);
            LogUtils.d("电子现金结果：" + dataMap);
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
        }
        return record;
    }

    // 读取用于打印凭条所要求的内核数据
    private String getKernelDataForPrint() {
        String printData = null;
        try {
            byte[] temp3 = new byte[1024];
            int count3 = PbocDev.getInstance(mContext, mDeviceService).getOriginalDev()
                    .readKernelData(EMVTAGStr.getkernelDataForPrint(), temp3);
            byte[] data3 = new byte[count3];
            System.arraycopy(temp3, 0, data3, 0, count3);
            printData = HexUtil.bcd2str(data3);
            Log.i("ckh", "reserve4 == " + printData);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(e.getMessage());
        }
        return printData;
    }

    /**
     * 读卡方式
     * @return 1：强制挥卡；2：强制插卡；3：无限制
     */
    private int getSwipCardType() {
        String transCode = Cache.getInstance().getTransCode();
        if (!StringUtil.isEmpty(transCode) && transCode.equals(TransConstans.TRANS_CODE_CONSUME)) {
            if (isForceSwipe) {
                return 1;
            } else {
                return 2;
            }
        }
        return 3;
    }


    /**
     * 用于判断检卡方式是否正确
     * @param type 10表示刷卡;11表示插卡;12表示挥卡
     */
    private boolean isChekCardSuccess(int type) {
        int swipeCardType = getSwipCardType();  //读卡方式
        switch (swipeCardType) {
            case 1://强制挥卡
                if (10 == type || 12 == type) {
                    return true;
                } else if (11 == type) {
                    sendMsgToHandle(FORCE_SWIPE_TIP);
                    return false;
                }
                break;
            case 2://强制插卡
                if (10 == type || 12 == type) {
                    sendMsgToHandle(FORCE_INSERT_TIP);
                    return false;
                } else if (11 == type) {
                    return true;
                }
                break;
            default:  //无限制
                break;
        }
        return true;
    }

    /**
     * 初始化页面
     */
    public abstract void initViewData();


    /**
     * 获取下一个页面的activity
     *
     * @return
     */
    public abstract Intent getNextStep();
}
