package com.nld.cloudpos.bankline.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lidroid.xutils.util.LogUtils;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.OnFragmentEventLisntener;

public class FragmentContainerActivity extends FragmentActivity implements View.OnClickListener {
    private View icon_back;
    private TextView page_title;
    private Button right_button;
    private OnFragmentEventLisntener mOnFragmentBackLisntener;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container_setting);
        initView();
    }

    public void initView() {
        icon_back = findViewById(R.id.icon_back);
        right_button = (Button) findViewById(R.id.right_button);
        icon_back.setOnClickListener(this);
        page_title = (TextView) findViewById(R.id.page_title);
        page_title.setText(getIntent().getStringExtra("title"));
        String className = getIntent().getStringExtra("className");
        Bundle args = getIntent().getBundleExtra("args");
        setFragment(className, args);
    }

    private void setFragment(String className, Bundle args) {
        if (!TextUtils.isEmpty(className)) {
            Fragment fragment = Fragment.instantiate(this, className, args);
            try {
                mOnFragmentBackLisntener = (OnFragmentEventLisntener) fragment;
                if (mOnFragmentBackLisntener != null) {
                    mOnFragmentBackLisntener.setRightButton(right_button);
                }
            } catch (Exception E) {
                LogUtils.d(E.toString());
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.layout_fragment, fragment)
                    .commit();
        }

    }

    public static void startFragment(Context context, String className, String title) {
        startFragment(context, className, title, null);
    }

    /**
     * 支持通过activity页面启动新的Fragment
     *
     * @param context
     * @param className
     * @param title
     * @param args
     */
    public static void startFragment(Context context, String className, String title, Bundle args) {
        Intent intent = new Intent();
        intent.setClass(context, FragmentContainerActivity.class);
        intent.putExtra("className", className);
        intent.putExtra("args", args);
        intent.putExtra("title", title);
        context.startActivity(intent);
    }

    /**
     * 支持有结果返回的Activity，Fragment启动模式
     *
     * @param activity
     * @param className
     * @param title
     * @param reqCode
     * @param args
     */
    public static void startFragmentForResult(Activity activity, String className, String title, int reqCode, Bundle args) {
        Intent intent = new Intent();
        intent.setClass(activity, FragmentContainerActivity.class);
        intent.putExtra("className", className);
        intent.putExtra("args", args);
        intent.putExtra("title", title);
        activity.startActivityForResult(intent, reqCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mOnFragmentBackLisntener != null) {
            mOnFragmentBackLisntener.onResult(requestCode, resultCode, data);
        } else {
            LogUtils.i("mOnFragmentBackLisntener is null");
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mOnFragmentBackLisntener != null) {
                if (mOnFragmentBackLisntener.onBack()) {
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.icon_back:
                if (mOnFragmentBackLisntener != null) {
                    if (mOnFragmentBackLisntener.onBack()) {
                        return;
                    } ;
                }
                finish();
                break;
            default:
                break;
        }
    }
}
