package com.nld.cloudpos.bankline.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.activity.FragmentContainerActivity;

/**
 * Created by jidongdong on 2017/2/7.øø
 */

public class BaseFragment extends Fragment implements OnTouchListener, OnFragmentEventLisntener {
    protected View containerView;
    public Activity mActivity;
    protected Bundle mArgs;
    private SparseArray<View> mSaView = new SparseArray<View>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mArgs = getArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (containerView == null) {
            if (doGetContentView() != null) {
                containerView = doGetContentView();
            } else {
                containerView = inflater.inflate(doGetContentViewId(), container, false);
            }
            doInitSubViews(containerView);
            doInitData();
        }
        ViewGroup parent = (ViewGroup) containerView.getParent();
        if (parent != null) {
            parent.removeView(containerView);
        }
        return containerView;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            view.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.touch_down));
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP
                || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
            view.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.touch_up));
        }
        return false;
    }

    /**
     * 返回布局layout id
     *
     * @return
     */
    public int doGetContentViewId() {
        return 0;
    }


    public View doGetContentView() {
        return null;
    }

    /**
     * 初始化子view
     *
     * @return
     */
    public void doInitSubViews(View view) {
    }

    /**
     * 初始化界面数据
     */
    public void doInitData() {
    }

    public static void startFragment(Context context, String className, String title) {
        startFragment(context, className, title, null);
    }

    public static void startFragment(Context context, String className, String title, Bundle args) {
        Intent intent = new Intent();
        intent.setClass(context, FragmentContainerActivity.class);
        intent.putExtra("className", className);
        intent.putExtra("args", args);
        intent.putExtra("title", title);
        context.startActivity(intent);
    }

    public void startFragmentForResult(Activity activity, String className, String title, int reqCode, Bundle args) {
        Intent intent = new Intent();
        intent.setClass(activity, FragmentContainerActivity.class);
        intent.putExtra("className", className);
        intent.putExtra("args", args);
        intent.putExtra("title", title);
        activity.startActivityForResult(intent, reqCode);
    }

    /**
     * 获取子View对象
     *
     * @param viewId
     * @param <T>
     * @return
     */
    protected <T extends View> T queryViewById(int viewId) {
        if (viewId > 0) {
            View view = mSaView.get(viewId);
            if (view == null) {
                view = containerView.findViewById(viewId);
                mSaView.put(viewId, view);
            }
            return (T) view;
        }
        return null;
    }

    public void finish() {
        getActivity().finish();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSaView.clear();
    }

    @Override
    public boolean onBack() {
        return false;
    }

    @Override
    public boolean setRightButton(Button view) {
        return false;
    }

    @Override
    public void setResult(int respCode, Intent data) {
        getActivity().setResult(respCode, data);
    }


    @Override
    public void onResult(int reqCode, int respCode, Intent intent) {

    }
    /**
     * 隐藏系统键盘
     *
     * @param v
     */
    public void hideSystemKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
    public void toast(String msg){
        if (TextUtils.isEmpty(msg)){
            return;
        }
        Toast.makeText(mActivity,msg, Toast.LENGTH_SHORT).show();
    }
}
