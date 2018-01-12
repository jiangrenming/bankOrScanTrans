package com.nld.cloudpos.bankline.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.nld.cloudpos.bankline.R;

import common.DensityUtils;

/**
 * Created by L on 2017/2/7.
 *
 * @描述 订单详情里的条目View。
 */

public class BillItemView extends FrameLayout {
    private int mTextSize; // 字体大小
    private int mLeftColor; // 左边字体颜色
    private int mRightColor; // 右边字体颜色
    private String mLeftText; // 左边字体内容
    private String mRightText; // 右边字体内容
    private TextView mTvLeft;
    private TextView mTvRight;
    private int mLeftSize;
    private int mRightSize;

    public BillItemView(Context context) {
        this(context, null);
    }

    public BillItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BillItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.item_bill, this, true);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BillItemView);
        mTextSize = a.getDimensionPixelSize(R.styleable.BillItemView_biv_contentSize, -1);
        mLeftSize = a.getDimensionPixelSize(R.styleable.BillItemView_biv_leftSize, DensityUtils.sp2px(context, 20));
        mRightSize = a.getDimensionPixelSize(R.styleable.BillItemView_biv_rightSize, DensityUtils.sp2px(context, 20));
        mLeftColor = a.getColor(R.styleable.BillItemView_biv_leftColor, getResources().getColor(R.color.black_1E282C));
        mRightColor = a.getColor(R.styleable.BillItemView_biv_rightColor, getResources().getColor(R.color.black_1E282C));
        mLeftText = a.getString(R.styleable.BillItemView_biv_leftText);
        mRightText = a.getString(R.styleable.BillItemView_biv_rightText);
        a.recycle();
        initView();
    }

    private void initView() {
        mTvLeft = (TextView) findViewById(R.id.tv_left);
        mTvRight = (TextView) findViewById(R.id.tv_right);
        setTextContent(mTvLeft, getText(mLeftText));
        setTextContent(mTvRight, getText(mRightText));
        setTextColor(mTvLeft, mLeftColor);
        setTextColor(mTvRight, mRightColor);
        setTextSize();
    }

    /**
     * 设置文本内容。
     *
     * @param tv
     * @param content
     */
    private void setTextContent(TextView tv, String content) {
        tv.setText(content);
    }

    /**
     * 动态修改右边文本内容。
     *
     * @param content
     */
    public void setTextContent(String content) {
        mTvRight.setText(content);
    }

    /**
     * 修改左边文字
     * @param content
     */
    public void setLeftText(String content){
        mTvLeft.setText(content);
    }

    /**
     * 设置字体大小。
     */
    private void setTextSize() {
        if (mTextSize > 0) {
            mTvLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
            mTvRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        } else {
            mTvLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX, mLeftSize);
            mTvRight.setTextSize(TypedValue.COMPLEX_UNIT_PX, mRightSize);
        }
    }

    /**
     * 设置字体颜色。
     *
     * @param tv
     * @param color
     */
    private void setTextColor(TextView tv, int color) {
        tv.setTextColor(color);
    }

    private String getText(String content) {
        if (content == null || TextUtils.isEmpty(content)) {
            return "margintop";
        }
        return content;
    }

    public void setContentColor(int color){
        mTvRight.setTextColor(color);
    }
}
