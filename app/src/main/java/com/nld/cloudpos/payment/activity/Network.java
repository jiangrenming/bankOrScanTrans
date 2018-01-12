package com.nld.cloudpos.payment.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.controller.AsyMemberInfosThread;
import com.nld.cloudpos.payment.controller.AsyParamsActivity;
import com.nld.cloudpos.payment.controller.AsyParamsThread;
import com.nld.cloudpos.payment.controller.AsyScanBatchNoThread;
import com.nld.cloudpos.payment.controller.ComonThread;
import com.nld.cloudpos.util.MyLog;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.activity.StartTransActivity;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.wxtrade.bean.scan_pay.ScanPayBean;

import common.StringUtil;

public class Network extends BaseActivity {
    private static MyLog logger = MyLog.getLogger(Network.class);

    // 默认超时时间
    private static final int NETWORK_TIMEOUT = 60;

    private TextView count_time = null;
    private TextView nettip = null;
    private ImageView imgView = null; // 显示动画图标
    private AidlDeviceService mDeviceService;
    private ComonThread mComonThread;
    private String transCode;

    @Override
    public void goback(View v) {}

    @Override
    public void setTopTitle() {}

    @Override
    public void onDeviceConnected(AidlDeviceService deviceManager) {
        beginTrans(deviceManager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.network);
        imgView = (ImageView) super.findViewById(R.id.network_imgview);
        Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        imgView.setAnimation(operatingAnim);
        operatingAnim.start();

        count_time = (TextView) super.findViewById(R.id.net_coutdowntime_tv);
        count_time.setText(NETWORK_TIMEOUT + "");
        nettip = (TextView) super.findViewById(R.id.nettip_tv);
        transCode = Cache.getInstance().getTransCode();
        if (!TransConstans.TRANS_CODE_SIGN.equals(transCode)) {
            nettip.setText(R.string.str_dealling);
        }
        bindService(); // 绑定服务开始交易
    }

    private void beginTrans(AidlDeviceService deviceService) {
        startCountThread();
        mDeviceService = deviceService;
        if (TransConstans.TRANS_CODE_PARAMS.equals(transCode)){  //同步业务参数
            ScanPayBean scanPayBean = (ScanPayBean) getIntent().getSerializableExtra("scan");
            if (scanPayBean != null){
                mComonThread = new AsyParamsThread(Network.this,asyParamsHandler,scanPayBean);
                mComonThread.setDeviceService(deviceService);
                new Thread(mComonThread).start();
            }
        }else if (TransConstans.TRANS_CODE_MERNOINFO.equals(transCode)){  //同步商终信息
            mComonThread = new AsyMemberInfosThread(Network.this,asyMemberHandler);
            mComonThread.setDeviceService(mDeviceService);
            new Thread(mComonThread).start();
        }else if (TransConstans.TRANS_CODE_BATCHNO.equals(transCode)){ //扫码批次同步
            mComonThread = new AsyScanBatchNoThread(Network.this,asyScanHandler);
            mComonThread.setDeviceService(mDeviceService);
            new Thread(mComonThread).start();
        }
    }

    //同步业务参数
    private Handler asyParamsHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    startActivity(new Intent(Network.this,ErrorResult.class));
                    break;
                case 0x02:
                    String data = (String) msg.obj;
                    if (!StringUtil.isEmpty(data)) {
                        nettip.setText(data);
                    }
                    break;
                case 0x12:
                    gotoNext(new Intent(Network.this,AsyParamsActivity.class));
                    break;
                default:
                    startActivity(new Intent(Network.this,ErrorResult.class));
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
                    gotoNext(new Intent(Network.this, ErrorResult.class));
                    break;
                case  0x11:
                    String data = (String) msg.obj;
                    if (!StringUtil.isEmpty(data)) {
                        nettip.setText(data);
                    }
                    break;
                case  0x12:
                    Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_SIGN);
          //          mComonThread = new MainSignThread(Network.this, signHandler, TransType.SingType.YLPAY_SIGN_TYPE, TransConstans.TRANS_CODE_SIGN);
           //         mComonThread.setDeviceService(mDeviceService);
             //       new Thread(mComonThread).start();
                    startActivityForResult(new Intent(Network.this, StartTransActivity.class), Constant.BANK_SIGN);
                    break;
                default:
                    gotoNext(new Intent(Network.this, ErrorResult.class));
                    break;
            }
        }
    };
    //商户信息同步
    private Handler asyMemberHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x01:
                    gotoNext(new Intent(Network.this, ErrorResult.class));
                    break;
                case  0x11:
                    String data = (String) msg.obj;
                    if (!StringUtil.isEmpty(data)) {
                        nettip.setText(data);
                    }
                    break;
                case  0x12: //同步批次号
                    Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_BATCHNO);
                    mComonThread = new AsyScanBatchNoThread(Network.this,asyScanHandler);
                    mComonThread.setDeviceService(mDeviceService);
                    new Thread(mComonThread).start();
                    break;
                default:
                    gotoNext(new Intent(Network.this, ErrorResult.class));
                    break;
            }
        }
    };

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_HOME
                || keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onDeviceConnectFaild() {}


    //------------------------页面定时器----------------start

    private CountThread mCountThread;

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
                    //超时自动发起撤销
                    if (doReverseThread()) {
                        return;
                    }
                    //交易超时
                    Cache.getInstance().setErrCode(NldException.ERR_NET_TRANS_TIMEOUT_E101);
                    Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_TRANS_TIMEOUT_E101));
                    gotoNext(new Intent(Network.this, ErrorResult.class));
                default:
                    break;
            }
        }

    };

    /**
     * 开始计时器计时
     */
    public void startCountThread() {
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

    //----------------------------页面定时器-----------------------end

    private boolean isDoReverse = true;

    /**
     * 发起扫码撤销
     */
    private boolean doReverseThread() {
        String transCode = Cache.getInstance().getTransCode();
        if ("660000".equals(transCode) && isDoReverse) { // 扫码支付超时
            isDoReverse = false;
            mComonThread.cancel();
            startCountThread();
            nettip.setText("后台交易超时，交易撤销...");
            // 撤销类型
            // 01:用户主动撤销
            // 02:POS轮询超时
            Cache.getInstance().setExt_txn_type("02");
            // mComonThread = new ReverseThread(this, reverse2Handler);
            //   mComonThread.setDeviceService(mDeviceService);
            //   ((ReverseThread) mComonThread).setSaveRecord(false);
            //   new Thread(mComonThread).start();
            return true;
        }
        return false;
    }

    private void gotoNext(Intent intent) {
        stopCountThread();
        if (mComonThread != null) {
            mComonThread.cancel();
        }
        startActivity(intent);
        finish();
    }

    private void finishAct() {
        stopCountThread();
        if (mComonThread != null) {
            mComonThread.cancel();
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == Constant.BANK_SIGN && null != data){
                String transResultTip = data.getStringExtra("transResultTip");
                Intent intent = new Intent(Network.this,SignSuccessActivity.class);
                intent.putExtra("transResultTip",transResultTip);
                gotoNext(intent);
            }
        }else if (resultCode == RESULT_FIRST_USER){
            if (requestCode == Constant.BANK_SIGN){
                gotoNext(new Intent(Network.this,TransErrorResultActivity.class));
            }
        }
    }
}
