package com.nld.cloudpos.payment.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nld.cloudpos.BankApplication;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.entity.TransactionEntity;
import com.nld.cloudpos.payment.socket.NetWorkActivity;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.utils.Constant;

import common.StringUtil;

/**
 * 所有类的基类
 *
 * @author Tianxiaobo
 */
public abstract class BaseActivity extends Activity {

    //布局顶部标题栏
    protected LinearLayout mLeftBtn;
    protected AidlDeviceService deviceManager;
    protected Context mContext;
    protected Activity mActivity;
    private static final String TRANSACTION_INFO = "transaction_info_entity";
    private TransactionEntity mTransEntity;
    private static Toast mToast;
    private long lastClickTime = 0;
    /**
     * 服务连接桥
     */
    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.d("服务断开了");
            deviceManager = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BankApplication.mDeviceService = deviceManager = AidlDeviceService.Stub.asInterface(service);
            LogUtils.d("服务绑定成功");
            onDeviceConnected(deviceManager);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NldPaymentActivityManager.getActivityManager().addActivity(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING); // 设置输入框不上浮
        //新大陆机具捕获home键与菜单键
//		getWindow().addFlags(3);
        super.onCreate(savedInstanceState);
//		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

        //add----lzc-----
        mContext = this;
        mActivity = this;
        initTransEntity();
        bindService();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {    // 透明状态栏
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setTopTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        lastClickTime = 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
//		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        NldPaymentActivityManager.getActivityManager().removeActivity(this);
        if (null != conn) {
            try {
                unbindService(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
            conn = null;
        }
        super.onDestroy();
    }

    /**
     * 绑定服务
     *
     * @createtor：Administrator
     * @date:2015-8-18 下午8:33:25
     */
    public void bindService() {
        if (null != deviceManager) {
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Constant.USDK_NAME_ACTION);
        boolean flag = bindService(intent, conn, Context.BIND_AUTO_CREATE);
        if (!flag) {
            onDeviceConnectFaild();
        }
        LogUtils.d("bindService返回" + flag);
    }

    protected void initTransEntity() {
        mTransEntity = (TransactionEntity) getIntent().getSerializableExtra(TRANSACTION_INFO);
        //交易开始时，intent中不存在mTransEntity，需要创建。
        if (null == mTransEntity) {
            mTransEntity = new TransactionEntity();
        }
    }

    /**
     * 防止重复点击
     *
     * @return
     */
    public boolean isCanClick() {
        return isCanClick(500);
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
        System.out.println("点击判断：lastClickTime=" + lastClickTime + ",time=" + time);
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
     * 返回按钮
     *
     * @param v
     * @createtor：Administrator
     * @date:2015-8-18 下午8:34:51
     */
    public abstract void goback(View v);

    /**
     * 设置标题
     *
     * @createtor：Administrator
     * @date:2015-8-18 下午8:34:58
     */
    public abstract void setTopTitle();

    /**
     * 连接设备服务
     *
     * @param deviceManager
     * @createtor：Administrator
     * @date:2015-8-18 下午8:37:13
     */
    public abstract void onDeviceConnected(AidlDeviceService deviceManager);

    /**
     * 服务绑定失败
     */
    public abstract void onDeviceConnectFaild();

    /**
     * 显示提示信息功能
     *
     * @param msg 显示内容
     * @createtor：Administrator
     * @date:2015-8-17 下午12:27:35
     */
    public void showTips(final String msg) {
        if (StringUtil.isEmpty(msg)) {
            return;
        }
        if (null == mToast) {
            mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        }
        mToast.setText(msg);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
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

    /**
     * 启动Activity
     *
     * @param className
     * @param tipMessage
     * @createtor：Administrator
     * @date:2015-8-18 下午6:45:51
     */
    public void startActivityWithAnim(String className, String tipMessage) {
        Intent intent = new Intent();
        intent.putExtra(NetWorkActivity.CLASS_NAME, className);
        intent.putExtra(NetWorkActivity.TIP_MESSAGE, tipMessage);
        intent.setClass(this, NetWorkActivity.class);
        startActivity(intent);
    }

    public void startActivityWithAnim(String className, String tipMessage, String cardno, String paymoney) {
        Intent intent = new Intent();
        intent.putExtra(NetWorkActivity.CLASS_NAME, className);
        intent.putExtra(NetWorkActivity.TIP_MESSAGE, tipMessage);
        intent.putExtra("cardno", cardno);
        intent.putExtra("paymoney", paymoney);
        intent.setClass(this, NetWorkActivity.class);
        startActivity(intent);
    }

    /**
     * 启动Activity
     *
     * @param cls
     * @createtor：Administrator
     * @date:2015-8-18 下午6:51:16
     */
    public void startActivity(Class cls, String paymoney, String cardno) {
        Intent intent = new Intent();
        intent.putExtra("cardno", cardno);
        intent.putExtra("paymoney", paymoney);
        intent.setClass(this, cls);
        startActivity(intent);
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
}
