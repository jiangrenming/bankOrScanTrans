package com.nld.cloudpos.bankline.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.CashFragment;
import com.nld.cloudpos.bankline.fragment.FeatureListFragment;
import com.nld.cloudpos.bankline.fragment.OnFragmentEventLisntener;
import com.nld.cloudpos.bankline.fragment.SettingFragment;
import com.nld.cloudpos.payment.activity.preauth.SuperPasswordActivity;
import com.nld.cloudpos.payment.controller.AbstractActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by branker on 17/2/7.
 */

public class LauncherActivity extends AbstractActivity implements OnClickListener, OnTouchListener {
    public static final int CASH_PAGE = 0; //收银
    public static final int FUNCTION_PAGE = 1;//功能
    private List<Fragment> fragmentList;
    private BroadcastReceiver mBroadcastReceiver;
    private ViewPager viewPager;
    private int currentTabIndex = CASH_PAGE;
    private TextView tab_one,tab_two;
    private AidlDeviceService aidlDeviceService;

    @Override
    public int contentViewSourceID() {
        return R.layout.launcher_activity;
    }

    @Override
    public void initView() {
        viewPager = (ViewPager) findViewById(R.id.nsvp_main);
        findViewById(R.id.operator).setOnClickListener(this);
        findViewById(R.id.operator).setOnTouchListener(this);
        findViewById(R.id.setting).setOnClickListener(this);
        findViewById(R.id.setting).setOnTouchListener(this);
        tab_one = (TextView) findViewById(R.id.tab_one);
        tab_two = (TextView) findViewById(R.id.tab_two);
    }

    @Override
    public void initData() {
        createFragment();
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {}
            @Override
            public void onPageSelected(int index) {
                currentTabIndex = index;
                upDateTab(currentTabIndex);
            }
            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        setCurrentTab(currentTabIndex);
        registerBroadcastReceiver();
    }

    private void upDateTab(int index) {
        switch (index){
            case CASH_PAGE:
                tab_one.setTextColor(getResources().getColor(R.color.white_F8F8F8));
                tab_one.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
                tab_two.setTextColor(getResources().getColor(R.color.gray_8F8F8F));
                tab_two.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
                break;
            case FUNCTION_PAGE:
                tab_two.setTextColor(getResources().getColor(R.color.white_F8F8F8));
                tab_two.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
                tab_one.setTextColor(getResources().getColor(R.color.gray_8F8F8F));
                tab_one.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
                break;
            default:
                break;
        }
    }

    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {
        if (service != null){
            this.aidlDeviceService = service;
        }
    }

    @Override
    public void onServiceBindFaild() {}

    @Override
    public boolean saveValue() {
        return false;
    }


  @Override
  protected void onDestroy() {
      super.onDestroy();
      unregisterBroadcastReceiver();
  }

    private void registerBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (Constant.MESSAGE_GOTO_CASH_PAGE.equals(action)) {
                    clearTabStyle();
                    setCurrentTab(CASH_PAGE);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.MESSAGE_GOTO_CASH_PAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }
    }

    private void createFragment() {
        if (fragmentList != null) {
            fragmentList.clear();
        }
        fragmentList = new ArrayList<Fragment>();
        /* add tab fragment at here */
        fragmentList.add(new CashFragment());
        fragmentList.add(new FeatureListFragment());
        viewPager.setOffscreenPageLimit(fragmentList.size());
    }

    private PagerAdapter pagerAdapter;
    private void setCurrentTab(int index) {
        upDateTab(index);
        viewPager.setCurrentItem(index, false);
    }

    /**
     * clear all tab style
     */
    private void clearTabStyle() {
        tab_one.setTextColor(getResources().getColor(R.color.gray_8F8F8F));
        tab_one.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        tab_two.setTextColor(getResources().getColor(R.color.gray_8F8F8F));
        tab_two.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
    }

    /* onTabClicked */
    public void onTabClicked(View view) {
        switch (view.getId()) {
            case R.id.tab_one:
                setCurrentTab(CASH_PAGE);
                break;
            case R.id.tab_two:
                setCurrentTab(FUNCTION_PAGE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.operator:
//                Intent intent = new Intent();
//                intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
//                startActivity(intent);
                break;
            case R.id.setting:
                Cache.getInstance().setTransCode(TransConstans.SETTING_PASSWORD);
                Intent intent = new Intent(LauncherActivity.this, SuperPasswordActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (pagerAdapter.getItem(currentTabIndex) instanceof SettingFragment) {
            ((OnFragmentEventLisntener) pagerAdapter.getItem(currentTabIndex)).onResult(requestCode, resultCode, data);
        }
    }

    private long exitTime = 0;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                ToastUtils.showToast("再按一次退出应用");
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

}