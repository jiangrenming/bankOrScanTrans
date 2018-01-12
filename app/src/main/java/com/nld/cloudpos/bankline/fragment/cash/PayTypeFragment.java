/*
package com.nld.cloudpos.bankline.fragment.cash;

import android.content.Context;
import android.view.View;

import com.example.banklibrary.utils.Cache;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.cloudpos.util.GlobeData;

*/
/**
 * Created by L on 2017/2/8.
 *
 * @描述 选择收款方式页面。
 *//*


public class PayTypeFragment extends BaseFragment implements View.OnClickListener {

    public static void startSelfFragment(Context context) {
        startFragment(context, PayTypeFragment.class.getName(), R.string.pay_type_title+"");
    }

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_pay_type;
    }

    @Override
    public void doInitSubViews(View view) {
        initView();
    }

    private void initView() {
        queryViewById(R.id.tiv_cardExpense).setOnClickListener(this);
        queryViewById(R.id.tiv_cardpreAuthorization).setOnClickListener(this);
        queryViewById(R.id.tiv_scanWeChat).setOnClickListener(this);
        queryViewById(R.id.tiv_scanAliPay).setOnClickListener(this);
        queryViewById(R.id.tiv_cash).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tiv_cardExpense: // 银行卡消费
                doSelected(GlobeData.TYPE_CARD_EXPENSE);
                break;
            case R.id.tiv_cardpreAuthorization: // 银行卡预授权
                doSelected(GlobeData.TYPE_CARD_PREAUTHORIZATION);
                break;
            case R.id.tiv_scanWeChat: // 微信支付
                doSelected(GlobeData.TYPE_SCAN_WECHAT);
                break;
            case R.id.tiv_scanAliPay: // 支付宝
                doSelected(GlobeData.TYPE_SCAN_ALIPAY);
                break;
            case R.id.tiv_cash: // 现金
                doSelected(GlobeData.TYPE_CASH);
                break;
        }
    }

    private void doSelected(int payType) {
        Cache.getInstance().setPayType(payType);
        finish();
    }
}
*/
