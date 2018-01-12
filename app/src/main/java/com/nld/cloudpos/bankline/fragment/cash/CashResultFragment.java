package com.nld.cloudpos.bankline.fragment.cash;

import android.view.View;
import android.view.View.OnClickListener;

import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.cloudpos.payment.view.LinePathView;

import java.io.IOException;

/**
 * Created by branker on 17/2/8.
 * 收款结果提交签名
 */

public class CashResultFragment extends BaseFragment implements OnClickListener {
    private LinePathView linePathView;
    private String path = "/mnt/sdcard/" + System.currentTimeMillis() + ".jpg";

    @Override
    public int doGetContentViewId() {
        return R.layout.activity_cash_result;
    }

    @Override
    public void doInitSubViews(View view) {
        initView();

    }

    private void initView() {
        queryViewById(R.id.submit).setOnClickListener(this);
        queryViewById(R.id.wipe).setOnClickListener(this);

        linePathView = queryViewById(R.id.sign);
    }

    private void doSubmit() {
        try {
            linePathView.save(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                doSubmit();
                break;
            case R.id.wipe:
                linePathView.clear();
                break;
            default:
                break;
        }
    }
}
