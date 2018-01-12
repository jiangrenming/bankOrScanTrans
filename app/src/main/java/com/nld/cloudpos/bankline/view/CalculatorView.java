package com.nld.cloudpos.bankline.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.nld.cloudpos.bankline.R;

import common.StringUtil;

/**
 * Created by L on 2017/2/22.
 *
 * @描述 计算器整体View。
 */

public class CalculatorView extends FrameLayout {
    private TextView mCalTextView; // 算式TextView
    private TextView mCalResultTextView; // 结果TextView
    private NumberKeyMainBoard mNumKeyBoard;
    private String mPayMoney;

    public CalculatorView(Context context) {
        this(context, null);
    }

    public CalculatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalculatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_calculator, this, true);
        initView();
    }

    private void initView() {
        mCalTextView = (TextView) findViewById(R.id.cal_edit);
        mCalResultTextView = (TextView) findViewById(R.id.cal_edit_result);
        mNumKeyBoard = (NumberKeyMainBoard) findViewById(R.id.keyBoard_num);
        mNumKeyBoard.setTextView(mCalTextView);
        mNumKeyBoard.setOnNKBListener(new NumberKeyMainBoard.numberKeyBoardListener() {
            @Override
            public void getResult(String result) {
                if (TextUtils.isEmpty(result)) {
                    result ="0.00";
                }
                mPayMoney = result;
            }

            @Override
            public void getInputNumber(String str) {
                if (TextUtils.isEmpty(str)) {
                    mCalResultTextView.setText(R.string.cash_default);
                } else {
                    mCalResultTextView.setText("￥"+ StringUtil.addComma(str));
                }
            }

            @Override
            public void getEqualNumber(String str) {
                if (TextUtils.isEmpty(str)) {
                    mCalResultTextView.setText(R.string.cash_default);
                } else {
                    mCalResultTextView.setText("￥" + StringUtil.addComma(str));
                }
            }
        });
    }

    public String getPayMoney() {
        return mPayMoney;
    }

    public void setDefaultMoney(String money){
        mCalResultTextView.setText("￥"+ StringUtil.addComma(money));
        mCalTextView.setText("");
        mPayMoney = null;
    }

    /**
     * 初始化金额输入栏
     */
    public void initMoney(){
        mCalResultTextView.setText("￥" + StringUtil.addComma(null));
        mCalTextView.setText("");
        mPayMoney = null;
        mNumKeyBoard.cleanCal();
    }
}
