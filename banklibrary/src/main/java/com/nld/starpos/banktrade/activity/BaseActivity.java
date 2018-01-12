package com.nld.starpos.banktrade.activity;

import android.app.Activity;
import android.os.Bundle;

import java.util.Map;

/**
 * Created by jiangrenming on 2017/12/7.
 */

public abstract  class BaseActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTransCode();
        getTransMap();
    }
    /**
     * 返回交易码
     * @return
     */
    public abstract String getTransCode();

    /**
     * 获取交易参数Map对象
     * @return
     */
    public abstract Map<String, String> getTransMap();
}
