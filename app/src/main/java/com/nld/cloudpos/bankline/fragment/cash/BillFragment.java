package com.nld.cloudpos.bankline.fragment.cash;/*
package com.nld.cloudpos.bankline.fragment.cash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.banklibrary.utils.Cache;
import com.example.banklibrary.utils.ParamsUtil;
import com.example.banklibrary.utils.StringUtil;
import com.example.banklibrary.utils.TransConstans;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.fragment.BaseFragment;
import com.nld.cloudpos.bankline.view.BillItemView;
import com.nld.cloudpos.payment.activity.ConsumeSubmitActivity;
import com.nld.cloudpos.payment.constant.GlobeData;
import com.nld.starpos.wxtrade.utils.ToastUtils;
import com.nld.starpos.wxtrade.utils.params.CommonParams;

import java.text.DecimalFormat;

*/
/**
 * Created by L on 2017/2/7.
 *
 * @描述 订单详情页面。
 *//*


public class BillFragment extends BaseFragment implements View.OnClickListener {

    private BillItemView mBivMoney; // 订单金额
    private BillItemView mBivPreferential; // 已享优惠
    private BillItemView mBivDiscount; // 会员折扣
    private BillItemView mBivVolume; // 代金卷
    private BillItemView mBivAccount;
    private Button mTvPayType;
    private Button mBtnPayNow;
    private String mToPayMoney; // 实际付款金额
    private String mPayMoney; // 折扣前的金额
    private String mMinusMoney; // 折扣金额
    private Button mTvRecommend1;
    private Button mTvRecommend2;
    private TextView mTvMore;

    public static void startSelfFragment(Context context, String payMoney) {
        Bundle args = new Bundle();
        args.putString(GlobeData.PAY_MONEY, payMoney);
        startFragment(context, BillFragment.class.getName(), R.string.bill_title+"", args);
    }

    @Override
    public int doGetContentViewId() {
        return R.layout.fragment_bill;
    }

    @Override
    public void doInitSubViews(View view) {
        initSubView();
        setMoneyText();
        setMinusText();
        setAccountText();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    private void initSubView() {
        mBivMoney = queryViewById(R.id.biv_money);
        mBivPreferential = queryViewById(R.id.biv_preferential);
        mBivDiscount = queryViewById(R.id.biv_discount);
        mBivVolume = queryViewById(R.id.biv_volume);
        mBivAccount = queryViewById(R.id.biv_account);
        mTvPayType = queryViewById(R.id.btn_payType);
        mBtnPayNow = queryViewById(R.id.btn_payNow);
        mTvRecommend1 = queryViewById(R.id.btn_recommend1);
        mTvRecommend2 = queryViewById(R.id.btn_recommend2);
        mTvMore = queryViewById(R.id.tv_more);

        updateView();

        mTvPayType.setOnClickListener(this);
        mBtnPayNow.setOnClickListener(this);
        mTvRecommend1.setOnClickListener(this);
        mTvRecommend2.setOnClickListener(this);
        mTvMore.setOnClickListener(this);
    }

    */
/**
     * 设置订单金额。
     *//*

    private void setMoneyText() {
        String payMoney = mArgs.getString(GlobeData.PAY_MONEY);
        if (payMoney == null || TextUtils.isEmpty(payMoney)) {
            payMoney = "0.00";
        }
        mBivMoney.setTextContent(R.string.cash_symbol+ StringUtil.addComma(payMoney));
        mPayMoney = payMoney;
    }

    */
/**
     * 设置优惠，折扣，代金卷金额。
     *//*

    private void setMinusText() {
    }

    */
/**
     * 设置实际需要付款的金额。
     *//*

    private void setAccountText() {
        mToPayMoney = getAccountMoney();
        mBivAccount.setTextContent(R.string.cash_symbol + " " + StringUtil.addComma(mToPayMoney));
    }

    private String getAccountMoney() {
        if (mMinusMoney == null) {
            mMinusMoney = "0.00";
        }
        DecimalFormat decfmat = new DecimalFormat("#######0.00");
        return decfmat.format(Double.valueOf(mPayMoney) - Double.valueOf(mMinusMoney));
    }

    */
/**
     * 更新界面。
     *//*

    private void updateView() {
        // 默认扫码付-微信。
        int payType = Cache.getInstance().getPayType();
        setPayType(payType);
        setRecommend(payType);
    }

    */
/**
     * 设置付款方式。
     *
     * @param payType
     *//*

    private void setPayType(int payType) {
        if (payType == GlobeData.TYPE_SCAN_WECHAT) {
            mTvPayType.setText(R.string.bill_we_chat_pay);
            Cache.getInstance().setPayChannel(CommonParams.WX_PAYCHANNEL);
        } else if (payType == GlobeData.TYPE_SCAN_ALIPAY) {
            mTvPayType.setText(R.string.bill_ali_pay);
            Cache.getInstance().setPayChannel(CommonParams.ALI_PAYCHANNEL);
        } else if (payType == GlobeData.TYPE_CASH) {
            mTvPayType.setText(R.string.bill_cash);
        } else if (payType == GlobeData.TYPE_CARD_EXPENSE) {
            mTvPayType.setText(R.string.bill_card_expense);
        } else if (payType == GlobeData.TYPE_CARD_PREAUTHORIZATION) {
            mTvPayType.setText(R.string.bill_card_pre_authorization);
        }
    }

    */
/**
     * 设置优惠推荐。
     *
     * @param payType
     *//*

    private void setRecommend(int payType) {
        if (payType == GlobeData.TYPE_SCAN_WECHAT || payType == GlobeData.TYPE_SCAN_ALIPAY) {
            mTvRecommend1.setText(R.string.bill_we_chat_pay_day);
            mTvRecommend2.setText(R.string.bill_ali_pay_day);
        } else if (payType == GlobeData.TYPE_CARD_EXPENSE || payType == GlobeData.TYPE_CARD_PREAUTHORIZATION) {
            mTvRecommend1.setText(R.string.bill_card_js_day);
            mTvRecommend2.setText(R.string.bill_card_zx_day);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_payType: // 收款方式
                PayTypeFragment.startSelfFragment(mActivity);
                break;
            case R.id.btn_payNow: // 立即收款
                goToNextActivity();
                break;
            case R.id.btn_recommend1: // 优惠推荐1
                ToastUtils.showToast("功能暂未实现");
                break;
            case R.id.btn_recommend2: // 优惠推荐2
                ToastUtils.showToast("功能暂未实现");
                break;
            default:
                break;
        }
    }

    private void goToNextActivity() {
        int payType = Cache.getInstance().getPayType();
        DecimalFormat decfmat = new DecimalFormat("#######0.00");
        Cache.getInstance().clearAllData();
        Cache.getInstance().setTransMoney(decfmat.format(Double.valueOf(mToPayMoney)));
        if (payType == GlobeData.TYPE_SCAN_WECHAT || payType == GlobeData.TYPE_SCAN_ALIPAY) {
            Cache.getInstance().setTransCode("660000");
    //        Intent it = new Intent(mActivity, ScanpaySubmit.class);
   //         startActivity(it);
        } else if (payType == GlobeData.TYPE_CARD_EXPENSE) {
            String signTag = ParamsUtil.getInstance().getParam("signsymbol");
            Intent intent = new Intent();
            Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_CONSUME);
            intent.setClass(mActivity, ConsumeSubmitActivity.class);
            startActivity(intent);
        } else if (payType == GlobeData.TYPE_CASH) {
            PrintReceiptFragment.startSelfFragment(mActivity);
        } else {
            ToastUtils.showToast("功能暂未实现");
        }
    }
}*/
