package com.nld.cloudpos.payment.activity.preauth;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.cloudpos.bankline.view.CalculatorView;
import com.nld.cloudpos.payment.activity.ConsumeSubmitActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.Constant;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import java.text.DecimalFormat;

/**
 * Created by L on 2017/2/21.
 *
 * @描述 预授权页面。
 */

public class PreAuthFormFragment extends BaseFragment implements View.OnClickListener {

    private static final Double MAX_MONEY = 99999999.99;
    private static int mType;
    private CalculatorView mCvCalculator;
    private Button mBtnConfirm;
    private String mPayMoney;

    public static void startSelfFragment(Context context, int type) {
        mType = type;
        if (mType == Constant.TYPE_CARD_EXPENSE) {
            startFragment(context, PreAuthFormFragment.class.getName(), context.getString(R.string.cosume));
        } else if (mType == Constant.TYPE_CARD_PREAUTHORIZATION) {
            startFragment(context, PreAuthFormFragment.class.getName(), "预授权");
        }
    }

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_pre_auth_form;
    }

    @Override
    public void doInitSubViews(View view) {
        mCvCalculator = queryViewById(R.id.cv_calculator);
        mBtnConfirm = queryViewById(R.id.btn_confirm);

        mBtnConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                mPayMoney = mCvCalculator.getPayMoney();
                toPay();
                break;
        }
    }

    private void toPay() {
        if (mPayMoney == null || TextUtils.equals(mPayMoney, "0.00")) {
            ToastUtils.showToast(getString(R.string.cash_pay_input_money));
        } else if (Double.valueOf(mPayMoney) < 0) {
            ToastUtils.showToast(R.string.cash_pay_more_than_0);
        } else if (Double.valueOf(mPayMoney) > MAX_MONEY) {
            ToastUtils.showToast(getString(R.string.cash_more_than_max));
        } else {
            goToNextStep();
        }
    }

    /**
     * 跳转到预授权确认页面。
     */
    private void goToNextStep() {
        DecimalFormat decfmat = new DecimalFormat("#######0.00");
        Cache.getInstance().setTransMoney(decfmat.format(Double.valueOf(mPayMoney)));
        if (mType == Constant.TYPE_CARD_EXPENSE) {
            startActivity(new Intent(mActivity, ConsumeSubmitActivity.class));
        } else if (mType == Constant.TYPE_CARD_PREAUTHORIZATION) {
            startActivity(new Intent(mActivity, PreAuthSubmitActivity.class));
        }
    }
}
