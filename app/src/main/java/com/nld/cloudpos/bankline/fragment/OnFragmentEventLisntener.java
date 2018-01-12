package com.nld.cloudpos.bankline.fragment;

import android.content.Intent;
import android.widget.Button;

/**
 * Created by jidongdong on 2017/2/14.
 * <p>
 * 实现在Fragment中自定义实现用户触发返回按钮时需要做的事情
 * <p>
 * 默认实现在BaseFragment
 * <p>
 * 继承于BaseFragment的子Fragment可以重写该方法实现自己的功能
 */

public interface OnFragmentEventLisntener {

    /**
     * 点击返回按钮
     *
     * @return
     */
    boolean onBack();


    /**
     * 初始化标题右按钮，设置样式，事件等
     *
     * @param view
     * @return
     */
    boolean setRightButton(Button view);

    /**
     * 对于StartActivityForResult的，处理完事件后需要将结果设置返回
     */
    void setResult(int respCode, Intent data);

    /**
     * 返回请求的结果到对应的Fragment
     *
     * @param reqCode
     * @param respCode
     * @param data
     */
    void onResult(int reqCode, int respCode, Intent data);
}
