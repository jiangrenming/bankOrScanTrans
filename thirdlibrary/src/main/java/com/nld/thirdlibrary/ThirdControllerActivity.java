package com.nld.thirdlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jiangrenming
 * @date 2017/12/27
 *  第三方调用模块
 */

public class ThirdControllerActivity extends BaseActivity implements View.OnClickListener{


    private TextView tv_merchant,tv_order_money,tv_dicount_money,tv_actual_money,txt_pay;
    private RadioButton txt_bank,txt_alipay,txt_weichat;
    private DotViewPager viewPager;
    private List<Integer> AdViews = new ArrayList<>(); // 广告图片视图的集合
    private int transType;
    private  ThirdBean thirdBean;
    private Intent mIntent;


    @Override
    public int attachLayoutRes() {
        return R.layout.third_pay_fragment;
    }

    @Override
    public void initView() {
        AdViews.add(R.drawable.banner);
        AdViews.add(R.drawable.banner);
        AdViews.add(R.drawable.banner);
        viewPager = (DotViewPager) findViewById(R.id.dot_view_pager);
        tv_merchant = (TextView) findViewById(R.id.tv_merchant);
        tv_order_money = (TextView) findViewById(R.id.tv_order_money);
        tv_dicount_money = (TextView) findViewById(R.id.tv_dicount_money);
        tv_actual_money = (TextView) findViewById(R.id.tv_actual_money);
        txt_pay = (TextView) findViewById(R.id.txt_pay);
        txt_bank = (RadioButton) findViewById(R.id.txt_bank);
        txt_alipay = (RadioButton) findViewById(R.id.txt_alipay);
        txt_weichat = (RadioButton) findViewById(R.id.txt_weichat);
        txt_pay.setOnClickListener(this);
        ThirdAdapter menuThirdAdapter = new ThirdAdapter(getSupportFragmentManager(), this, AdViews);
        viewPager.setAdapter(menuThirdAdapter);
        thirdBean = (ThirdBean) getIntent().getSerializableExtra("scan");
        mIntent = getIntent();


        if (null != thirdBean ){
            if (thirdBean.getMerName() != null){
                tv_merchant.setText(thirdBean.getMerName());
            }
            if (thirdBean.getAmount() != null && Long.valueOf(thirdBean.getAmount()) > 0){
                tv_order_money.setText(thirdBean.getAmount());
                tv_actual_money.setText(thirdBean.getAmount());
            }
        }
        int type = thirdBean.getType();
        if (Constants.TRANS_NULL == type || Constants.TRANS_SCAN_PAY == type ){  //---->走的是扫码支付
            txt_bank.setVisibility(View.GONE);
            txt_alipay.setOnClickListener(this);
            txt_weichat.setOnClickListener(this);
        }else {   //---->走的是银行卡支付
            txt_alipay.setVisibility(View.GONE);
            txt_weichat.setVisibility(View.GONE);
            txt_bank.setOnClickListener(this);
        }
    }


    @Override
    public void onClick(View v) {
       if (v.getId() == R.id.txt_bank){
           transType = Constants.TRANS_SALE;
       }else if (v.getId() == R.id.txt_alipay){
           transType = Constants.TRANS_SCAN_ALIPAY ;
       }else if (v.getId() == R.id.txt_weichat){
           transType = Constants.TRANS_SCAN_WEIXIN;
       }
       clickView(v);
    }

    private void clickView(View v) {
        if (transType <0 ){
            Toast.makeText(this, "未选择支付方式",Toast.LENGTH_SHORT);
        }else {
            if (v.getId() == R.id.txt_pay){
                thirdBean.setTransType(transType);
                //调起扫码支付
                if ( transType == Constants.TRANS_SCAN_ALIPAY  || transType == Constants.TRANS_SCAN_WEIXIN){


                }else {   //--->调起银行支付

                }
            }
        }
    }
}
