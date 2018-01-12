package com.nld.thirdlibrary;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by jiangrenming on 2017/12/27.
 */

public abstract  class BaseActivity extends FragmentActivity{


    public abstract int attachLayoutRes();
    public  abstract  void initView();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(attachLayoutRes());
        initView();
    }
}
