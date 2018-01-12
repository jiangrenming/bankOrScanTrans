package com.nld.cloudpos.payment.base;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nld.cloudpos.BankApplication;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.activity.BalanceResultActivity;
import com.nld.cloudpos.payment.activity.PrintResultActivity;
import com.nld.cloudpos.payment.activity.TransErrorResultActivity;
import com.nld.cloudpos.payment.entity.TransactionEntity;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.pinUtils.AidlUtils;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.TransConstans;

import org.apache.log4j.Logger;

import java.util.HashMap;

import common.StringUtil;

public abstract class BaseAbstractActivity extends FragmentActivity implements View.OnTouchListener {

    protected Logger logger = Logger.getLogger(this.getClass());

    public final static String TRANSACTION_ENTITY = "TransactionEntity";
    /**
     * 交易数据对象，用于保存每个交易节点产生的数据
     */
    protected TransactionEntity mTransData;

    /**
     * SDK的AIDL服务对象
     */
    protected AidlDeviceService mDeviceService;
    protected Context mContext;
    protected Activity mActivity;

    //布局顶部标题栏
    protected LinearLayout mLeftBtn;
    protected ImageView mLeftImg;
    protected TextView mTitle;

    protected long lastClickTime = 0;
    private SoundPool sp;//播放声音
    private HashMap<Integer, Integer> spMap;
    private static Toast mToast = null;
    protected boolean isCanceled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NldPaymentActivityManager.getActivityManager().addActivity(this);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING); // 设置输入框不上浮
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //新大陆机具捕获home键与菜单键
//        getWindow().addFlags(3);
        super.onCreate(savedInstanceState);
        // 透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
//        //设置页面布局
        setContentView(contentViewSourceID());
        mTransData = (TransactionEntity) getIntent().getSerializableExtra(TRANSACTION_ENTITY);
        if (null == mTransData) {//交易开始时，未创建
            mTransData = new TransactionEntity();
        }
        mContext = this;
        mActivity = this;

        //初始化界面
        initView();
        initSound();

        //绑定AIDL服务
        mDeviceService = BankApplication.mDeviceService;
//          if(null==mDeviceService){
        if (!bindService()) {
            onServiceBindFaild();
        }
//          }else{
//              onServiceConnecteSuccess(mDeviceService);
//          }
    }


    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.e("绑定服务失败：" + mContext.getClass().getName());
            BankApplication.mDeviceService = null;
            mDeviceService = null;
            onServiceBindFaild();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BankApplication.mDeviceService = mDeviceService = AidlDeviceService.Stub.asInterface(service);
            AidlUtils.getInstance().setmService(mDeviceService);
            onServiceConnecteSuccess(mDeviceService);
        }
    };


    @Override
    protected void onResume() {
        lastClickTime = 0;
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (null != conn) {
            unbindService(conn);
            conn = null;
        }

        NldPaymentActivityManager.getActivityManager().removeActivity(mActivity);
        isCanceled = true;
        super.onDestroy();
    }

    /**
     * 绑定服务,绑定成功后会回调onServiceConnecteSuccess(AidlDeviceService service)
     *
     * @return 发起绑定是是否成功
     * @createtor：Administrator
     * @date:2015-8-18 下午8:33:25
     */
    public boolean bindService() {
//        LogUtils.d("----------------------开始绑定服务-----------------------" + DateTimeUtil.formatMillisecondTimeSecond(System.currentTimeMillis()));
        Intent intent = new Intent();
        intent.setAction(Constant.USDK_NAME_ACTION);
        boolean flag = bindService(intent, conn, Context.BIND_AUTO_CREATE);
//        LogUtils.d("bindService返回" + flag);
        return flag;
    }

    /**
     * 防止重复点击
     *
     * @return
     */
    public boolean isCanClick() {
        long cur = System.currentTimeMillis();
        long time = cur - lastClickTime;
        time = Math.abs(time);
        if (time < 500) {
            return false;
        }
        lastClickTime = cur;
        return true;
    }

    /**
     * 防止重复点击
     *
     * @param duration 重复点击间隔时间 毫秒
     * @return
     */
    public boolean isCanClick(int duration) {
        long cur = System.currentTimeMillis();
        long time = cur - lastClickTime;
        time = Math.abs(time);
        if (time < duration) {
            return false;
        }
        lastClickTime = cur;
        return true;
    }

    /**
     * 设置顶部返回按钮点击事件，返回到上一个activity
     */
    public void setTopDefaultReturn() {
        if (null == mLeftBtn) {
            mLeftBtn = (LinearLayout) findViewById(R.id.top_left_layout);
        }
        mLeftBtn.setVisibility(View.VISIBLE);
        mLeftBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                NldPaymentActivityManager.getActivityManager().removeActivity(mActivity);
            }
        });
    }

    /**
     * 设置顶部返回按钮点击事件，返回到上一个activity
     */
    public void setTopDefaultReturnVisibility(boolean visibility) {
        if (null == mLeftBtn) {
            mLeftBtn = (LinearLayout) findViewById(R.id.top_left_layout);
        }
        if (visibility) {
            mLeftBtn.setVisibility(View.VISIBLE);
            mLeftBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    NldPaymentActivityManager.getActivityManager().removeActivity(mActivity);
                }
            });
        } else {
            mLeftBtn.setVisibility(View.GONE);
        }
    }

    /**
     * 设置顶部返回按钮点击事件
     *
     * @param listener 点击事件
     * @return 返回按钮id
     */
    public int setTopReturnListener(OnClickListener listener) {
        if (null == mLeftBtn) {
            mLeftBtn = (LinearLayout) findViewById(R.id.top_left_layout);
        }
        mLeftBtn.setVisibility(View.VISIBLE);
        mLeftBtn.setOnClickListener(listener);
        return R.id.top_left_layout;
    }

    /**
     * 设置页面标题
     *
     * @param title
     */
    public void setTopTitle(String title) {
        if (null == mTitle) {
            mTitle = (TextView) findViewById(R.id.top_title);
        }
        mTitle.setText(title);
    }

    /**
     * 启动下一个activity，会传递TransactionEntity对象到下个activity。
     * @param nextClass 下个activity的class
     */
    public void goToNextActivity(Class<?> nextClass) {
        Intent it = new Intent(mContext, nextClass);
        it.putExtra(TRANSACTION_ENTITY, mTransData);
        startActivity(it);
    }

    /**
     * 启动下一个activity，会传递TransactionEntity对象到下个activity。
     * @param it 下个activity的class
     */
    public void goToNextActivity(Intent it) {
    //    startActivity(it);
        LogUtils.e("银行卡交易跳转");
        if (!StringUtil.isEmpty(Cache.getInstance().getTransCode()) &&
                TransConstans.TRANS_CODE_CONSUME.equals(Cache.getInstance().getTransCode())){  //消费
            startActivityForResult(it, Constant.COSUME_PIN);
        }else if (TransConstans.TRANS_CODE_QUERY_BALANCE.equals(Cache.getInstance().getTransCode())){  //余额查询
            startActivityForResult(it, Constant.QUERY_BALANCE);
        }else if (TransConstans.TRANS_CODE_CONSUME_CX.equals(Cache.getInstance().getTransCode())){  //消费撤销
            startActivityForResult(it, Constant.COSUME_REVERSE);
        }else if (TransConstans.TRANS_CODE_PRE_COMPLET_CX.equals(Cache.getInstance().getTransCode())){  //预授权完成撤销
            startActivityForResult(it, Constant.PRE_COMPLETED_REVERSE);
        }else if (TransConstans.TRANS_CODE_PRE.equals(Cache.getInstance().getTransCode())){  //预授权
            startActivityForResult(it, Constant.PRE_COSUME);
        }else if (TransConstans.TRANS_CODE_PRE_COMPLET.equals(Cache.getInstance().getTransCode())){  //预授权完成
            startActivityForResult(it, Constant.PRE_COSUME_COMPLETE);
        }else if (TransConstans.TRANS_CODE_PRE_CX.equals(Cache.getInstance().getTransCode())){  //预授权撤销
            startActivityForResult(it, Constant.PRE_COSUME_REVERSE);
        }else if (TransConstans.TRANS_CODE_LJTH.equals(Cache.getInstance().getTransCode())){  //联机退货
            startActivityForResult(it, Constant.BANK_REVERSE_ONLINE);
        }else if (TransConstans.TRANS_CODE_OFF_TH.equals(Cache.getInstance().getTransCode())){   //脱机消费
            startActivityForResult(it, Constant.BANK_REVERSE_OFFLINE);
        }else if (TransConstans.TRANS_CODE_QC_FZD.equals(Cache.getInstance().getTransCode())){  //非指定账户圈存
            startActivityForResult(it, Constant.BANK_NO_QUAN_CUN);
        }else if (TransConstans.TRANS_CODE_QC_ZD.equals(Cache.getInstance().getTransCode())){  //指定账户圈存
            startActivityForResult(it, Constant.BANK_QUAN_CUN);
        }else if (TransConstans.TRANS_CODE_DZXJ_TRANS_PUTONG_CONSUMER.equals(Cache.getInstance().getTransCode())){  //电子现金普通消费
            startActivityForResult(it, Constant.ELECTRI_NOMARAL_COSUME);
        }else if (TransConstans.TRANS_CODE_DZXJ_TRANS_QUICK_PAY.equals(Cache.getInstance().getTransCode()) ){  //电子现金快速消费
            startActivityForResult(it, Constant.ELECTRI_QC_PAY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == Constant.COSUME_PIN
                    || requestCode == Constant.COSUME_REVERSE
                    || requestCode == Constant.PRE_COMPLETED_REVERSE
                    || requestCode == Constant.PRE_COSUME
                    || requestCode == Constant.PRE_COSUME_COMPLETE
                    || requestCode == Constant.PRE_COSUME_REVERSE
                    || requestCode == Constant.BANK_REVERSE_ONLINE
                    || requestCode == Constant.BANK_REVERSE_OFFLINE
                    || requestCode == Constant.BANK_QUAN_CUN
                    || requestCode == Constant.BANK_NO_QUAN_CUN
                    || requestCode == Constant.ELECTRI_NOMARAL_COSUME
                    || requestCode == Constant.ELECTRI_QC_PAY){ //消费,消费撤销,预授权相关,联机退货,脱机退货，(非)指定账户圈存,电子现金
                LogUtils.e("消费 or 消费撤销 or 预授权完成撤销:成功");
                Intent intent = new Intent(this, PrintResultActivity.class);
                startActivity(intent);
            }else if (requestCode == Constant.QUERY_BALANCE){  //余额查询
                startActivity(new Intent(this,BalanceResultActivity.class));
            }
        }else if (resultCode == RESULT_FIRST_USER){
            LogUtils.e("冲正 or 消费 or 消费撤销 or 预授权完成撤销:失败");
            startActivity(new Intent(this,TransErrorResultActivity.class));
        }
    }

    /**
     * 隐藏系统键盘
     *
     * @param v
     */
    public void hideSystemKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public void initSound() {
        sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        spMap = new HashMap<Integer, Integer>();
        spMap.put(1, sp.load(this, R.raw.success, 1));
        spMap.put(2, sp.load(this, R.raw.pineffect, 1));

    }

    public void playSound(int sound, int number) {
        AudioManager am = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volumnCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumnRatio = volumnCurrent / audioMaxVolumn;

        sp.play(spMap.get(sound), volumnRatio, volumnRatio, 1, number, 1f);
    }

    /**
     * 显示Toast的tip
     *
     * @param msg
     */
    public void showTip(String msg) {
        if (null == mToast) {
            mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
        }
        mToast.setText(msg);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();

    }

    /**
     * 设置activity的布局，实现时只需返回布局的ID。
     *
     * @return
     */
    public abstract int contentViewSourceID();

    /**
     * 创建activity时初始化页面
     */
    public abstract void initView();

    /**
     * 服务绑定成功的回调
     *
     * @param service AIDL服务对象
     */
    public abstract void onServiceConnecteSuccess(AidlDeviceService service);

    /**
     * 服务绑定失败回调
     */
    public abstract void onServiceBindFaild();

    /**
     * 用于保存值。
     */
    public abstract boolean saveValue();

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME
                || keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME
                || keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.touch_down));
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP
                || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
            view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.touch_up));
        }
        return false;
    }
}
