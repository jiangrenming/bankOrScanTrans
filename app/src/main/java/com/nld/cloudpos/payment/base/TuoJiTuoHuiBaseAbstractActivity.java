package com.nld.cloudpos.payment.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.entity.TransactionEntity;

import org.apache.log4j.Logger;

/**
 * 脱机退货所用
 * @author Administrator
 *
 */
public abstract class TuoJiTuoHuiBaseAbstractActivity extends Activity {
    
    public final static String TRANSACTION_ENTITY="TransactionEntity";
    
    protected Logger logger= Logger.getLogger(this.getClass());
    /**
     * 交易数据对象，用于保存每个交易节点产生的数据
     */
    protected TransactionEntity mTransData;
    
    protected Context mContext;
    protected Activity mActivity;
    
    //布局顶部标题栏
    protected LinearLayout mLeftBtn;
    protected ImageView mLeftImg;
    protected TextView mTitle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NldPaymentActivityManager.getActivityManager().addActivity(this);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING); // 设置输入框不上浮
        super.onCreate(savedInstanceState);
        //设置页面布局
        setContentView(contentViewSourceID());
        mTransData=(TransactionEntity) getIntent().getSerializableExtra(TRANSACTION_ENTITY);
        if(null==mTransData){//交易开始时，未创建
            mTransData=new TransactionEntity();
        }
        mContext=this;
        mActivity=this;
        //初始化界面
        initView();
    }


 
    

    @Override
    protected void onDestroy() {
       
        super.onDestroy();
    }

   
    /**
     * 设置顶部返回按钮点击事件，返回到上一个activity
     */
    public void setTopDefaultReturn(){
        if(null==mLeftBtn){
            mLeftBtn=(LinearLayout) findViewById(R.id.top_left_layout);
        }
        mLeftBtn.setVisibility(View.VISIBLE);
        mLeftBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                NldPaymentActivityManager.getActivityManager().removeActivity(mActivity);
                finish();
            }
        });
    }
    
    /**
     * 设置顶部返回按钮点击事件
     * @param listener 点击事件
     * @return 返回按钮id
     */
    public int setTopReturnListener(OnClickListener listener){
        if(null==mLeftBtn){
            mLeftBtn=(LinearLayout) findViewById(R.id.top_left_layout);
        }
        mLeftBtn.setVisibility(View.VISIBLE);
        mLeftBtn.setOnClickListener(listener);
        return R.id.top_left_layout;
    }
    
    /**
     * 设置页面标题
     * @param title
     */
    public void setTopTitle(String title){
        if(null==mTitle){
            mTitle=(TextView) findViewById(R.id.top_title);
        }
        mTitle.setText(title);
    }
    /**
     * 启动下一个activity，会传递TransactionEntity对象到下个activity。
     * @param nextClass 下个activity的class
     */
    public void goToNextActivity(Class<?> nextClass){
        Intent it=new Intent(mContext,nextClass);
        it.putExtra(TRANSACTION_ENTITY, mTransData);
        startActivity(it);
    }

    /**
     * 启动下一个activity，会传递TransactionEntity对象到下个activity。
     * @param it 下个activity的class
     */
    public void goToNextActivity(Intent it){
        startActivity(it);
    }
    
    /**
     * 隐藏系统键盘
     * @param v
     */
    public void hideSystemKeyboard(View v){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
    
    /**
     * 显示Toast的tip
     * @param msg
     */
    public void showTip(String msg){
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();;
    }

    /**
     * 设置activity的布局，实现时只需返回布局的ID。
     * @return 
     */
    public abstract int contentViewSourceID();
    /**
     * 创建activity时初始化页面
     */
    public abstract void initView();
    
    /**
     * 服务绑定成功的回调
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
}
