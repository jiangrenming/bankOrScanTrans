package com.nld.cloudpos.payment.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.nld.cloudpos.BankApplication;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;
import com.nld.cloudpos.payment.base.BaseAbstractThread;
import com.nld.cloudpos.payment.base.DZXJQuickPayThread;
import com.nld.cloudpos.payment.socket.HttpRequestJsIfc;
import com.nld.cloudpos.payment.socket.TransHandler;
import com.nld.cloudpos.util.DialogFactory;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.pinUtils.PbocDev;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.TransConstans;

public class TransStartActivity extends BaseAbstractActivity {

    // 页面传递时，intent使用的常量
    public final static String TRANS_NEXT_ACTIVITY_TAG = "transaction_next_activity";
    public final static String HOLO_WHITE = "holo_white";

    private TextView mTipTv;
    private TextView mCountTv;
    private ImageView mLoadingIv;
    private CountThread mCountThread;

    private HttpRequestJsIfc mHttpRequestJsIfc;
    private int timeout = 60;// 交易超时
    private BaseAbstractThread mTransThread;


    /**
     * 上个activity传递需要执行交易码
     */
    private String transCode;
    /**
     * 交易成功后下一个activity的包名，开始交易上个activity传递
     */
    private String nextClassName;
    /**
     * cjw脱机消费上送成功处理
     */
    private Handler myhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            // 跳转到脱机校验
            Cache.getInstance().clearAllData();// 清除所有数据
            Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_OFF_TH);
            finish();

        }
    };
    /**
     * 交易过程handler，处理交易过程中的页面变化与流程处理。
     */
    private TransHandler transHandler = new TransHandler() {

        @Override
        public void transSuccess(String keep) {
            stopCountThread();
            //清空监听
            BankApplication.mPbocListener.clearListener();
            Intent it = null;
            LogUtils.d("交易成功，下一页：" + nextClassName);
            try {
                it = new Intent(mContext, Class.forName(nextClassName));
            } catch (ClassNotFoundException e) {
                LogUtils.e("交易成功获取结果页失败" + nextClassName);
                e.printStackTrace();
            }
            //交易代码为签到，且下一页未主菜单页，说明是交易前自动签到
            if (Cache.getInstance().getTransCode().equals(TransConstans.TRANS_CODE_SIGN)
                    && nextClassName.contains("ConsumeSubmitActivity")) {
                Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_CONSUME);
                it.putExtra("transResultTip", keep);
                startActivity(it);
            } else {
                it.putExtra("transResultTip", keep);
                startActivity(it);
                LogUtils.d("交易成功，进入成功页");
            }
            finish();
        }

        @Override
        public void transFaild(String code, String msg, boolean isNeedReverse) {
            //清空监听
            BankApplication.mPbocListener.clearListener();
            stopCountThread();
            LogUtils.d("交易失败进入失败页");
            Cache.getInstance().setErrCode(code);
            Cache.getInstance().setErrDesc(msg);
            gotoNext(TransErrorResultActivity.class);

        }

        @Override
        public void transViewTipChange(String tip) {
            mTipTv.setText(tip);
            LogUtils.d("交易提示：" + tip);
        }

        @Override
        public void transStartProgress(int timeOut) {
            startCountThread(timeOut);
        }

        @Override
        public void showDialog(String[] values, int type) {
            if (TransHandler.DIALOG_TYPE_AID_SELECT == type) {
                DialogFactory.showMessage(mActivity, "多应用选择", "请选择多应用", "确定", new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            PbocDev.getInstance(mContext, mDeviceService).getOriginalDev().importAidSelectRes(0);
                        } catch (RemoteException e) {
                            try {
                                PbocDev.getInstance(mContext, mDeviceService).abortPboc();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                            LogUtils.e("设备连接失败");
                            transHandler.messageSendProgressFaild(NldException.ERR_DEV_DEV_CONNECT_E311, false);
                        } catch (Exception e) {
                            try {
                                PbocDev.getInstance(mContext, mDeviceService).abortPboc();
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }
                            e.printStackTrace();
                            LogUtils.e("设备连接失败");
                            transHandler.messageSendProgressFaild(NldException.ERR_DEV_DEV_CONNECT_E311, false);
                        }
                    }
                }, "取消", new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        LogUtils.e("交易结束");
                        transHandler.messageSendProgressFaild(NldException.ERR_DEV_TRANS_FINISH_E312, false);
                    }
                });
            } else if (TransHandler.DIALOG_TYPE_ECASH_TIP == type) {
                try {
                    PbocDev.getInstance(mContext, mDeviceService).getOriginalDev().importECashTipConfirmRes(false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    };

    @Override
    public int contentViewSourceID() {
        return R.layout.act_trans_start;
    }

    @Override
    public void initView() {
        LogUtils.e("交易activity开始--------------------");
        //获取交易码
        transCode = Cache.getInstance().getTransCode();
        nextClassName = getIntent().getStringExtra(TransStartActivity.TRANS_NEXT_ACTIVITY_TAG);

        boolean holoWhiteBg = getIntent().getBooleanExtra(TransStartActivity.HOLO_WHITE, false);
        if (holoWhiteBg){
            findViewById(R.id.rl_container).setBackgroundColor(getResources().getColor(R.color.white));
        }
        //界面初始化
        mTipTv = (TextView) findViewById(R.id.trans_start_tip_tv);
        mCountTv = (TextView) findViewById(R.id.trans_start_coutdowntime_tv);
        mLoadingIv = (ImageView) findViewById(R.id.trans_start_imgview);
        //启动动画
        Animation operatingAnim = AnimationUtils.loadAnimation(this,
                R.anim.rotate);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        mLoadingIv.setAnimation(operatingAnim);
        operatingAnim.start();

        try {
            mHttpRequestJsIfc = new HttpRequestJsIfc(this, transHandler); //实例化HttpRequestJSIfc对象
            timeout = mHttpRequestJsIfc.getDealtimeout();
        } catch (Exception e) {
            LogUtils.e("获取网络请求对象HttpRequestJSIfc异常");
            e.printStackTrace();
        }

    }

    public void doTrans() {
        stopTransThread();
          if (TransConstans.TRANS_CODE_DZXJ_TRANS_QUICK_PAY.equals(transCode)) { // 快速支付
            Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_CONSUME);//电子现金快速支付，联机时走消费
            mTransThread = new DZXJQuickPayThread(mContext, transHandler, mDeviceService, 0);//因为消费线程只走后半段PBOC，DZXJQuickPayThread走完整PBOC流程
        } else if (TransConstans.TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER.equals(transCode)) {  // 普通消费
            Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_CONSUME);  //电子现金快速支付，联机时走消费
            mTransThread = new DZXJQuickPayThread(mContext, transHandler, mDeviceService, 1); //因为消费线程只走后半段PBOC，DZXJQuickPayThread走完整PBOC流程
        }
        transHandler.messageSendStartProgress(timeout);
        transHandler.messageSendTipChange("准备交易数据");
        mTransThread.start();
    }

    public void stopTransThread() {
        if (null != mTransThread) {
            mTransThread.cancel();
        }
        mTransThread = null;
    }

    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {
        // 在服务绑定成功后开始进行交易
        LogUtils.d("服务绑定成功，开始交易");
        startTransaction();
    }

    @Override
    public void onServiceBindFaild() {
        //服务绑定失败结束交易
    }

    /**
     * 开始交易
     */
    private void startTransaction() {
        transCode = Cache.getInstance().getTransCode();
        LogUtils.d("交易开始：transCode ： " + transCode);
        doTrans();     //交易请求处理
    }


    //------------------------页面定时器----------------start
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
                    mCountTv.setText(msg.arg1 + "");
                    break;
                case 60:
                    LogUtils.d("倒计时60s已完成");
                    //交易超时
                    Cache.getInstance().setErrCode(NldException.ERR_NET_TRANS_TIMEOUT_E101);
                    Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_TRANS_TIMEOUT_E101));
                    gotoNext(TransErrorResultActivity.class);
                default:
                    break;
            }
        }

    };

    /**
     * 开始计时器计时
     */
    public void startCountThread(int timeOut) {
        if (mCountThread == null) {
            LogUtils.d("计时器--初始化========" + timeOut);
            mCountThread = new CountThread(timeOut);
            mCountThread.start();
        } else {
            mCountThread.resetTimeOut(timeOut);
        }
//        LogUtils.d("计时器--开始========"+timeOut);
        mCountTv.setText(timeOut + "");

    }

    /**
     * 停止计时器计时
     */
    public void stopCountThread() {
        LogUtils.d("计时器--停止=========" + mCountTv.getText().toString());
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
            LogUtils.d("---isRunning:" + isRunning);
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

    ;

    //----------------------------页面定时器-----------------------end
    @Override
    public boolean saveValue() {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME ||
                keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mTransThread) {
            mTransThread.stopThread();
        }
    }

    /**
     * 跳到下一步
     *
     * @param nextClass
     */
    private void gotoNext(Class<?> nextClass) {
        if (mTransThread != null) {
            mTransThread.cancel();
            mTransThread.stopThread();
            mTransThread = null;
        }
        goToNextActivity(nextClass);
        finish();
    }
}
