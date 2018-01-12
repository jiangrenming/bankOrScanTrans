package com.nld.cloudpos.bankline.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nld.cloudpos.bankline.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/10/28.
 * 计算机键盘
 */
public class NumberKeyMainBoard extends LinearLayout implements View.OnClickListener {
    private TextView mTextView;          // 表达式的显示View
    private LayoutInflater mInflater;
    private double calResult = 0;
    private boolean canCal = true;
    private numberKeyBoardListener kBL;
    private String inputStr = "";       // 当前输入的金额
    private String calStr = "";         // 上面的表达式的金额

    /*--------------add by margintop-----------------------*/
    private boolean clickedDot;                           // 点击了小数点
    private static final int MAX_BIT = 8;
    private static final int MAX_AFTERDOT = 2;
    private int afterDot;                            // 现在的小数位个数(包含小数点)
    private int integerPart;                              // 整数部分
    private List<Integer> historyList = new ArrayList<>(); // 历史小数位数

    public NumberKeyMainBoard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public NumberKeyMainBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    protected void initView(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.view_mnumber_keyboard, this);
        initView();
    }

    public void setTextView(TextView mTextView) {
        this.mTextView = mTextView;
    }

    private void initView() {
        findViewById(R.id.keyboard_bt_0).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_1).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_2).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_3).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_4).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_5).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_6).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_7).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_8).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_9).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_dot).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_add).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_minus).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_multiply).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_backspace).setOnClickListener(this);
        findViewById(R.id.keyboard_bt_backspace).setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                cleanCal();
                return true;
            }
        });
        findViewById(R.id.keyboard_bt_equal).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.keyboard_bt_0:
            case R.id.keyboard_bt_1:
            case R.id.keyboard_bt_2:
            case R.id.keyboard_bt_3:
            case R.id.keyboard_bt_4:
            case R.id.keyboard_bt_5:
            case R.id.keyboard_bt_6:
            case R.id.keyboard_bt_7:
            case R.id.keyboard_bt_8:
            case R.id.keyboard_bt_9:
                clickNum(((TextView) view));
                break;
            case R.id.keyboard_bt_dot:
                clickDot();
                break;
            case R.id.keyboard_bt_add:
            case R.id.keyboard_bt_minus:
            case R.id.keyboard_bt_multiply:
                clickSymbol(((TextView) view));
                break;
            case R.id.keyboard_bt_backspace:
                clickBackspace();
                break;
            case R.id.keyboard_bt_equal:
                clickEqual();
                break;
        }
    }

    private void clickNum(TextView tv) {
        if (!canCal) {
            cleanCal();
        }
        if (integerPart < MAX_BIT && afterDot < (MAX_AFTERDOT + 1) || (clickedDot && integerPart == MAX_BIT && afterDot < (MAX_AFTERDOT + 1))) {
            if (!TextUtils.equals(inputStr, "0")) {
                inputStr = inputStr + tv.getText();
            }

//            if (!clickedDot && inputStr.startsWith("00")) {
//                inputStr = inputStr.substring(1);
//            }

            addAfterDot();
            saveIntegerPart();

            if (mTextView != null) {
                mTextView.setText(calStr + inputStr);
            }

            calculationResult();
        }
    }

    private void clickDot() {
        if (!canCal) {
            cleanCal();
        }
        if (!clickedDot) {
            clickedDot = true;
            inputStr = inputStr + ".";
            if (inputStr.startsWith(".")) {
                inputStr = "0" + inputStr;
            }
            addAfterDot();
            saveIntegerPart();
            if (mTextView != null) {
                mTextView.setText(calStr + inputStr);
            }
        }
    }

    private void clickSymbol(TextView tv) {
        if (!canCal) {
            historyList.clear();
            afterDot = getNumFromStr(calStr);
        }
        if (inputStr.length() > 0 || !canCal) {
            canCal = true;
            calStr = calStr + inputStr;
            inputStr = "";
            if (calStr.endsWith(".")) {
                calStr = calStr.substring(0, calStr.length() - 1);
            }
            calStr = calStr + tv.getText();
            if (mTextView != null) {
                mTextView.setText(calStr);
            }
            /************记录并还原*************/
            historyList.add(afterDot);
            clickedDot = false;
            afterDot = 0;
            integerPart = 0;
        }
    }

    private void clickBackspace() {
        if (!canCal) {
            cleanCal();
            return;
        }
        String text = mTextView.getText().toString().trim();
        if (text.length() <= 0)
            return;
        if (text.startsWith("-")) {
            text = "0" + text;
        }
        boolean haveCal = false;
        if (text.substring(text.length() - 1).equals("+") || text.substring(text.length() - 1).equals("-") || text.substring(text.length() - 1).equals("×")) {
            haveCal = true;
            text = text.substring(0, text.length() - 1);
            if (!historyList.isEmpty()) {
                afterDot = historyList.get(historyList.size() - 1);
                historyList.remove(historyList.size() - 1);
            }
        }
        clickedDot = afterDot > 0;
        String[] textStr = text.split("\\+|-|×");
        String tempStr = textStr[textStr.length - 1];
        if (!TextUtils.isEmpty(tempStr)) {
            //整数部分长度
            saveIntegerPart();
            inputStr = tempStr;
        } else {
            integerPart = 0;
            inputStr = "";
        }
        calStr = text.substring(0, text.length() - tempStr.length());
        if (!haveCal) {
            if (!clickedDot) {
                integerPart--;
                integerPart = integerPart <= 0 ? 0 : integerPart;
            }

            afterDot--;
            if (afterDot <= 0) {
                afterDot = 0;
                clickedDot = false;
            } else {
                clickedDot = true;
            }

            if (integerPart == 0 && afterDot == 0) {
                inputStr = "";
            } else {
                inputStr = inputStr.substring(0, integerPart + afterDot);
            }
        }

        saveIntegerPart();

        if (mTextView != null) {
            mTextView.setText(calStr + inputStr);
        }
        calculationResult();
    }

    private void clickEqual() {
        inputStr = "";
        canCal = false;
        DecimalFormat df;
        df = new DecimalFormat("######0.00");
        calStr = df.format(calResult);
        calStr = formatStr(calStr);
        mTextView.setText(calStr);
        kBL.getEqualNumber(df.format(calResult));
    }

    /**
     * 获取按等号后的结果的小数位数。
     *
     * @param str
     * @return
     */
    private int getNumFromStr(String str) {
        if (!str.contains("."))
            return 0;
        int num = MAX_AFTERDOT + 1;
        while (str.endsWith("0") && str.length() > 1) {
            num--;
            str = str.substring(0, str.length() - 1);
        }
        if (str.endsWith(".")) {
            num--;
        }
        num = num <= 0 ? 0 : num;
        return num;
    }

    /**
     * 把字符串格式化，当有小数位时才显示小数位后的数字。
     *
     * @param str
     * @return
     */
    private String formatStr(String str) {
        if (str.contains(".")) {
            while (str.endsWith("0") && str.length() > 1) {
                str = str.substring(0, str.length() - 1);
            }
            if (str.endsWith(".") && str.length() > 1) {
                str = str.substring(0, str.length() - 1);
            }
        }
        return str;
    }

    /**
     * 加上一位小数位。
     */
    private void addAfterDot() {
        if (clickedDot) {
            if (afterDot < (MAX_AFTERDOT + 1)) {
                afterDot++;
            }
        }
    }

    private void saveIntegerPart() {
        if (inputStr.isEmpty())
            integerPart = 0;
        if (inputStr.contains(".")) {
            integerPart = inputStr.substring(0, inputStr.indexOf(".")).length();
        } else {
            integerPart = inputStr.length();
        }
    }

    private void calculationResult() {
        if (mTextView == null)
            return;
        String text = mTextView.getText().toString().trim();
        if (text.length() > 0) {
            calResult = 0;
            if (text.contains("=")) {
                String[] arrayStr = text.split("=");
                if (arrayStr.length > 1)
                    text = arrayStr[1];
            }
            if (text.startsWith("-")) {
                text = "0" + text;
            }
            if (TextUtils.equals(text.substring(text.length() - 1), "+") || TextUtils.equals(text.substring(text.length() - 1), "-") || TextUtils.equals(text.substring(text.length() - 1), "×")) {
                text = text.substring(0, text.length() - 1);
            }
            String[] textSplit = text.split("\\+|-");
            for (int i = 0; i < textSplit.length; i++) {
                if (textSplit[i].contains("×")) {
                    double temp = 0.00;
                    String[] tempSplit = textSplit[i].split("×");
                    for (int j = 0; j < tempSplit.length; j++) {
                        if (j == 0) {
                            temp = Double.parseDouble(tempSplit[j]);
                        } else {
                            temp = temp * Double.parseDouble(tempSplit[j]);
                        }
                    }
                    String replaced = new DecimalFormat("######0.00").format(temp);
                    text = text.replace(textSplit[i], replaced);
                    textSplit[i] = replaced;
                }
            }
            for (int i = 0; i < textSplit.length; i++) {
                if (i == 0) {
                    calResult = Double.parseDouble(textSplit[i]);
                } else {
                    int start = 0;
                    for (int j = 0; j < i; j++) {
                        start = start + textSplit[j].length();
                    }
                    start = start + i - 1;
                    String symbol = text.substring(start, start + 1);
                    if (TextUtils.equals(symbol, "+")) {
                        calResult = calResult + Double.parseDouble(textSplit[i]);
                    } else if (TextUtils.equals(symbol, "-")) {
                        calResult = calResult - Double.parseDouble(textSplit[i]);
                    }
                }
            }
        } else {
            calResult = 0;
        }
        DecimalFormat df = new DecimalFormat("######0.00");
        kBL.getResult(df.format(calResult));
        if (TextUtils.isEmpty(inputStr)) {
            kBL.getInputNumber("");
        } else {
            kBL.getInputNumber(df.format(Double.parseDouble(inputStr)));
        }
    }

    public void cleanCal() {
        historyList.clear();
        clickedDot = false;
        afterDot = 0;
        integerPart = 0;
        canCal = true;
        mTextView.setText("");
        calResult = 0;
        inputStr = "";
        calStr = "";
        calculationResult();
    }

    public interface numberKeyBoardListener {
        void getResult(String result);

        void getInputNumber(String str);

        void getEqualNumber(String str);
    }

    public void setOnNKBListener(numberKeyBoardListener kBL) {
        this.kBL = kBL;
    }

}
