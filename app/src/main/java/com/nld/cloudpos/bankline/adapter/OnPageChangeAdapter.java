package com.nld.cloudpos.bankline.adapter;

import android.support.v4.view.ViewPager;

/**
 * Created by L on 2017/2/21.
 *
 * @描述
 */

public abstract class OnPageChangeAdapter implements ViewPager.OnPageChangeListener {

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {}

    @Override
    public void onPageScrollStateChanged(int state) {}

}
