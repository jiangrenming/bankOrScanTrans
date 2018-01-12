package com.nld.cloudpos.payment.controller;

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
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nld.cloudpos.BankApplication;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.entity.TransactionEntity;
import com.nld.starpos.banktrade.pinUtils.AidlUtils;
import com.nld.starpos.banktrade.utils.Constant;

import java.util.HashMap;

/**
 * Created by jiangrenming on 2017/9/15.
 */

public abstract  class AbstractActivity extends FragmentActivity implements View.OnTouchListener{

    private SoundPool sp;//播放声音
    private HashMap<Integer, Integer> spMap;
    //交易数据对象，用于保存每个交易节点产生的数据
    protected TransactionEntity mTransData;
    //操作所有的aidl服务的类
    protected AidlDeviceService mDeviceService;
    //防止重复点击的时间
    protected long lastClickTime = 0;

    //初始化布局
    public abstract int contentViewSourceID();

    //初始化界面
    public abstract void initView();

    //初始化数据
    public abstract void initData();

    //服务成功的绑定
    public abstract void onServiceConnecteSuccess(AidlDeviceService service);

    //服务失败的绑定
    public abstract void onServiceBindFaild();

    //保存值
    public abstract boolean saveValue();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING); // 设置输入框不上浮
        super.onCreate(savedInstanceState);
        setStatue();
        setContentView(contentViewSourceID());
        NldPaymentActivityManager.getActivityManager().addActivity(this);
        mTransData = (TransactionEntity) getIntent().getSerializableExtra("TransactionEntity");
        if (null == mTransData) { //交易开始时，未创建
            mTransData = new TransactionEntity();
        }
        //初始化界面
        initView();
        initData();
        initSound();
        if (!bindService()) {  //不管操作什么交易的时候，进入页面时都会先判断底层的服务是否连接成功
            onServiceBindFaild();
        }
    }

    public boolean bindService() {
        Log.i("TAG","开始绑定服务");
        Intent intent = new Intent();
        intent.setAction(Constant.USDK_NAME_ACTION);
        return  bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("TAG",this.getClass().getSimpleName()+"绑定服务失败");
            BankApplication.mDeviceService = null;
            mDeviceService = null;
            onServiceBindFaild();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("TAG",this.getClass().getSimpleName()+"绑定服务成功");
            BankApplication.mDeviceService = mDeviceService = AidlDeviceService.Stub.asInterface(service);
            onServiceConnecteSuccess(mDeviceService);
            AidlUtils.getInstance().setmService(mDeviceService);
        }
    };

    /**
     * 设置状态栏
     */
    private void setStatue(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
    //初始化播放声音
    public void initSound() {
        sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        spMap = new HashMap<Integer, Integer>();
        spMap.put(1, sp.load(this, R.raw.success, 1));
        spMap.put(2, sp.load(this, R.raw.pineffect, 1));
    }
    //播放声音
    public void playSound(int sound, int number) {
        AudioManager am = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volumnCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumnRatio = volumnCurrent / audioMaxVolumn;
        sp.play(spMap.get(sound), volumnRatio, volumnRatio, 1, number, 1f);
    }

    //隐藏系统键盘
    public void hideSystemKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    /**
     * 防止重复点击
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

    @Override
    protected void onResume() {
        super.onResume();
        lastClickTime = 0;
    }

    /**
     * 设置页面标题
     * @param title
     */
    public void setTopTitle(String title) {
        TextView mTitle = (TextView) findViewById(R.id.top_title);
        mTitle.setText(title);
    }

    /**
     * 启动下一个activity，会传递TransactionEntity对象到下个activity。
     * @param clazz 准备跳转activity的class
     */
    public void goToNextActivity(Class<?> clazz) {
        Intent it = new Intent(this, clazz);
        it.putExtra("TransactionEntity", mTransData);
        startActivity(it);
    }

    /**
     * 启动下一个activity
     */
    public void goToNextActivity(Intent it) {
        startActivity(it);
    }

    /**
     * 设置顶部返回按钮点击事件，返回到上一个activity
     */
    public void setTopDefaultReturn() {
        LinearLayout mLeftBtn = (LinearLayout) findViewById(R.id.top_left_layout);
        mLeftBtn.setVisibility(View.VISIBLE);
        mLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BankApplication.isCancle = true ;
                NldPaymentActivityManager.getActivityManager().removeActivity(AbstractActivity.this);
            }
        });
    }

    /**
     * 设置顶部返回按钮点击事件，返回到上一个activity
     */
    public void setTopDefaultReturnVisibility(boolean visibility) {
        LinearLayout mLeftBtn = (LinearLayout) findViewById(R.id.top_left_layout);
        if (visibility) {
            mLeftBtn.setVisibility(View.VISIBLE);
            mLeftBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NldPaymentActivityManager.getActivityManager().removeActivity(AbstractActivity.this);
                }
            });
        } else {
            mLeftBtn.setVisibility(View.GONE);
        }
    }

    /**
     * 设置顶部返回按钮点击事件
     * @param listener 点击事件
     * @return 返回按钮id
     */
    public int setTopReturnListener(View.OnClickListener listener) {
        LinearLayout mLeftBtn = (LinearLayout) findViewById(R.id.top_left_layout);
        mLeftBtn.setVisibility(View.VISIBLE);
        mLeftBtn.setOnClickListener(listener);
        return R.id.top_left_layout;
    }


    @Override
    protected void onDestroy() {
        if (null != conn) {
            unbindService(conn);
            conn = null;
        }
        NldPaymentActivityManager.getActivityManager().removeActivity(this);
        super.onDestroy();
    }

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
    /**
     * 强制隐藏输入法键盘
     */

    public void hideSystemKeyBoard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }
}
