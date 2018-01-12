package com.nld.cloudpos.bankline.activity;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.bankline.view.BillItemView;
import com.nld.cloudpos.bankline.view.CarouselView;
import com.nld.cloudpos.payment.activity.ConsumeSubmitActivity;
import com.nld.cloudpos.payment.base.BaseAbstractActivity;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.wxtrade.utils.params.CommonParams;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import common.StringUtil;

/**
 * Created by L on 2017/2/7.
 *
 * @描述 收银详情页面（第三方调用）。
 */

public class BillThirdActivity extends BaseAbstractActivity implements View.OnClickListener {
    private static final int CARD = 1001;
    private static final int WECHAT = 1002;
    private static final int ALIPAY = 1003;
    private int mPayType;
    private List<ImageView> mImageViewList = new ArrayList<>();
    private TextView mPreTv;
    private BillItemView mBivMoney; // 订单金额
    private BillItemView mBivPreferential; // 已享优惠
    private BillItemView mBivDiscount; // 会员折扣
    private BillItemView mBivAccount;
    private Button mBtnPayNow;
    private String mToPayMoney; // 实际付款金额
    private String mPayMoney; // 折扣前的金额
    private String mMinusMoney; // 折扣金额
    private CarouselView mCvCarousel;
    private TextView mTvTitle;
    private ImageView mIvBack;
    private TextView mTvCard;
    private TextView mTvWeChat;
    private TextView mTvAliPay;
    private LinearLayout mLlTitle;


    @Override
    public int contentViewSourceID() {
        return R.layout.activity_bill_third;
    }

    @Override
    public void initView() {
        mLlTitle = (LinearLayout) findViewById(R.id.ll_title);
        mTvTitle = (TextView) findViewById(R.id.page_title);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mCvCarousel = (CarouselView) findViewById(R.id.cv_carousel);
        mBivMoney = (BillItemView) findViewById(R.id.biv_money);
        mBivPreferential = (BillItemView) findViewById(R.id.biv_preferential);
        mBivDiscount = (BillItemView) findViewById(R.id.biv_discount);
        mBivAccount = (BillItemView) findViewById(R.id.biv_account);
        mTvCard = (TextView) findViewById(R.id.tv_card);
        mTvWeChat = (TextView) findViewById(R.id.tv_weChat);
        mTvAliPay = (TextView) findViewById(R.id.tv_aliPay);
        mBtnPayNow = (Button) findViewById(R.id.btn_payNow);

        mIvBack.setOnClickListener(this);
        mTvCard.setOnClickListener(this);
        mTvWeChat.setOnClickListener(this);
        mTvAliPay.setOnClickListener(this);
        mBtnPayNow.setOnClickListener(this);

        mLlTitle.setBackgroundColor(getResources().getColor(R.color.white_FFFFFF));
        mTvTitle.setText(R.string.bill_third_title);
        mTvTitle.setTextColor(getResources().getColor(R.color.black_1E282C));
        mIvBack.setImageResource(R.drawable.icon_back_black);

        initData();

        setMoneyText();
        setMinusText();
        setAccountText();
        updatePayTypeBg(mTvWeChat);
    }

    private void initData() {
        initCarouselData();

        initPayMoneyData();

        mPayType = WECHAT;
    }

    /**
     * 初始轮播图数据。
     */
    private void initCarouselData() {
        mImageViewList.clear();
        // 添加测试数据
        for (int i = 0; i < 3; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.banner_third);
            mImageViewList.add(imageView);
        }
        mCvCarousel.setIconList(mImageViewList);
    }

    /**
     * 获取应该金额。
     */
    private void initPayMoneyData() {
        Intent intent = getIntent();
        if (intent != null) {
            mPayMoney = getMoneyFromIntent(intent);
            checkPayMoney();
        }
    }

    /**
     * 获取传递的金额数。
     *
     * @param intent
     * @return
     */
    private String getMoneyFromIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) {
            return null;
        }

        String scheme = uri.getScheme();

        if (scheme.equals("money")) {
            return uri.getSchemeSpecificPart();
        }

        return null;

    }

    /**
     * 校验传递的金额值。
     */
    private void checkPayMoney() {
        if (mPayMoney == null || TextUtils.isEmpty(mPayMoney)) {
            throw new RuntimeException("it is wrong payMoney.--by margintop");
        }
        double money;
        try {
            money = Double.valueOf(mPayMoney);
        } catch (Exception e) {
            throw new RuntimeException("it is wrong payMoney.--by margintop");
        }
        if (money < 0) {
            throw new RuntimeException("it is wrong payMoney.--by margintop");
        }
    }

    /**
     * 设置订单金额。
     */
    private void setMoneyText() {
        mBivMoney.setTextContent("￥"+ mPayMoney);
    }

    /**
     * 设置优惠，折扣金额。
     */
    private void setMinusText() {
        // TODO: 2017/2/8 获取折扣金额并设置到文本。
    }

    /**
     * 设置实际需要付款的金额。
     */
    private void setAccountText() {
        mToPayMoney = getAccountMoney();
        mBivAccount.setTextContent("￥" + StringUtil.addComma(mToPayMoney));
    }

    private String getAccountMoney() {
        if (mMinusMoney == null) {
            mMinusMoney ="0.00";
        }
        DecimalFormat decfmat = new DecimalFormat("#######0.00");
        return decfmat.format(Double.valueOf(mPayMoney) - Double.valueOf(mMinusMoney));
    }

    /**
     * 更改支付方式的背景图片。
     *
     * @param tv
     */
    private void updatePayTypeBg(TextView tv) {
        if (mPreTv != null) {
            mPreTv.setBackgroundResource(R.drawable.shape_msg_bg);
            mPreTv.setTextColor(getResources().getColor(R.color.green_83C561));
        }
        tv.setBackgroundResource(R.drawable.shape_bg_gradient_corner);
        tv.setTextColor(getResources().getColor(R.color.white_FFFFFF));
        mPreTv = tv;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back: // 返回键
                finish();
                break;
            case R.id.tv_card: // 支付方式：银行卡
                mPayType = CARD;
                updatePayTypeBg(mTvCard);
                break;
            case R.id.tv_weChat: // 支付方式：微信
                mPayType = WECHAT;
                updatePayTypeBg(mTvWeChat);
                break;
            case R.id.tv_aliPay: // 支付方式：支付
                mPayType = ALIPAY;
                updatePayTypeBg(mTvAliPay);
                break;
            case R.id.btn_payNow: // 立即收款
                goToNextActivity();
                break;
            default:
                break;
        }
    }

    /**
     * 根据支付方式跳转不同界面。
     */
    private void goToNextActivity() {
        DecimalFormat decfmat = new DecimalFormat("#######0.00");
        Cache.getInstance().clearAllData();
        Cache.getInstance().setTransMoney(decfmat.format(Double.valueOf(mToPayMoney)));
        if (mPayType == WECHAT || mPayType == ALIPAY) {
            Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_WX_PAY);
            if (mPayType == WECHAT) {
                Cache.getInstance().setPayChannel(CommonParams.WX_PAYCHANNEL);
            } else if (mPayType == ALIPAY) {
                Cache.getInstance().setPayChannel(CommonParams.ALI_PAYCHANNEL);
            }
          //  Intent it = new Intent(mActivity, ScanpaySubmit.class);
        //    startActivity(it);
        } else if (mPayType == CARD) {
            String signTag = ParamsUtil.getInstance().getParam("signsymbol");
            Intent intent = new Intent();
            Cache.getInstance().setTransCode(TransConstans.TRANS_CODE_CONSUME);
            intent.setClass(mActivity, ConsumeSubmitActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onServiceConnecteSuccess(AidlDeviceService service) {
    }

    @Override
    public void onServiceBindFaild() {
    }

    @Override
    public boolean saveValue() {
        return false;
    }

}