package com.nld.cloudpos.bankline.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nld.cloudpos.bankline.R;

/**
 * Created by L on 2017/2/21.
 *
 * @描述 指示器View。
 */

public class IndicateView extends FrameLayout {

    private int mChildNum;
    private int mWidth;
    private int mHeight;
    private int mLeftMagin;
    private boolean mIsFinished;
    private LinearLayout mLlIndicate;
    private ImageView mIvMove;              // 移动的指示器

    public IndicateView(Context context) {
        this(context, null);
    }

    public IndicateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_indicate, this, true);
        initView();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mIsFinished = true;
    }

    private void initView() {
        mLlIndicate = (LinearLayout) findViewById(R.id.ll_indicate);
        mIvMove = (ImageView) findViewById(R.id.iv_move);
        mIvMove.setBackgroundResource(R.drawable.lunbo_xuanzhong);
    }

    /**
     * 初始化指示器。
     */
    private void initIndicate() {
        LayoutParams paramsMove = new LayoutParams(mWidth, mHeight);
        mIvMove.setLayoutParams(paramsMove);
        mLlIndicate.removeAllViews();
        for (int i = 0; i < mChildNum; i++) {
            ImageView ivIndicate = new ImageView(getContext());
            ivIndicate.setBackgroundResource(R.drawable.lunbo_weixuanzhong);
            mLlIndicate.addView(ivIndicate);
            LinearLayout.LayoutParams paramsIndicate = new LinearLayout.LayoutParams(mWidth, mHeight);
            if (i != 0) {
                paramsIndicate.leftMargin = mLeftMagin;
            }
            ivIndicate.setLayoutParams(paramsIndicate);
        }
    }

    /**
     * 更新childNum。
     * @param childNum
     */
    public void updateChildNum(int childNum) {
        setIntputData(childNum, mWidth, mHeight, mLeftMagin);
    }

    /**
     * 设置指示器需要的数据。
     *
     * @param childNum
     * @param width
     * @param height
     * @param leftMagin
     */
    public void setIntputData(int childNum, int width, int height, int leftMagin) {
        if (childNum < 2) {
            throw new RuntimeException("the number of indicates must be more than 2.--by margintop");
        }
        mChildNum = childNum;
        mWidth = width;
        mHeight = height;
        mLeftMagin = leftMagin;
        initIndicate();
    }

    public void setMoveX(int position, float positionOffset) {
        if (mIsFinished) {
            mIvMove.setTranslationX((position + positionOffset) * (mLlIndicate.getChildAt(1).getLeft() - mLlIndicate.getChildAt(0).getLeft()));
        }
    }

}
