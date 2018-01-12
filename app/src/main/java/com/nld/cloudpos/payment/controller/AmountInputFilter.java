package com.nld.cloudpos.payment.controller;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 只能输入金额
 *
 * @author jianshengd
 */
public class AmountInputFilter implements InputFilter {

    private Pattern mPattern;
    //输入的最大金额    
    private static final int MAX_VALUE = Integer.MAX_VALUE;
    //小数点后的位数    
    private static final int POINTER_LENGTH = 2;
    private static final String POINTER = ".";
    private static final String ZERO = "0";

    public AmountInputFilter() {
        mPattern = Pattern.compile("([0-9]|\\.)*");
    }

    /**
     * @param source 新输入的字符串
     * @param start  新输入的字符串起始下标，一般为0
     * @param end    新输入的字符串终点下标，一般为source长度-1
     * @param dest   输入之前文本框内容
     * @param dstart 原内容起始坐标，一般为0
     * @param dend   原内容终点坐标，一般为dest长度-1
     * @return 输入内容
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String sourceText = source.toString();
        String destText = dest.toString();

        //验证删除等按键    
        if (TextUtils.isEmpty(sourceText)){
            return "";
        }
        Matcher matcher = mPattern.matcher(source);
        if (!matcher.matches()){
            return "";
        }
        if (destText.contains(POINTER)) { // 已经输入小数点的情况下，只能输入数字
            if (POINTER.equals(source.toString())) {  //只能输入一个小数点
                return "";
            }
            //验证小数点精度，保证小数点后只能输入两位    
            int index = destText.indexOf(POINTER);
            int length = dend - index;

            if (length > POINTER_LENGTH) {
                return dest.subSequence(dstart, dend);
            }
        } else { // 没有输入小数点的情况下，只能输入小数点和数字
            if ((POINTER.equals(source.toString())) && TextUtils.isEmpty(destText)) { // 首位不能输入小数点
                return "";
            } else if (!POINTER.equals(source.toString()) && ZERO.equals(destText)) { // 如果首位输入0，接下来只能输入小数点
                return "";
            }
        }

        // 检验输入金额是否超限
        return ((Double.parseDouble(destText + sourceText) > MAX_VALUE) ?
                dest.subSequence(dstart, dend) :
                dest.subSequence(dstart, dend) + sourceText);
    }
}
