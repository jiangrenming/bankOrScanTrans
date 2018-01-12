package com.nld.starpos.wxtrade.debug.utils;

import android.text.TextUtils;

import java.text.DecimalFormat;

import common.StringUtil;

/**
 * Created by jiangrenming on 2017/9/23.
 */

public class FormatUtils {

    /**
     * 用"*"符号格式化卡号前6后4
     * @param cardNo 卡号："6666666666666666"
     * @return 格式化 卡号："666666******6666"
     */
    public static String formatCardNoWithStar(String cardNo) {
        if (cardNo == null || "".equals(cardNo))
            return "";
        int len = cardNo.length();
        if (len <= 10) {
            return cardNo;
        } else {
            String c1 = cardNo.substring(0, 6);
            String c3 = cardNo.substring(len - 4, len);
            String c2 = "";
            for (int i = 0; i < len - 10; i++) {
                c2 += "*";
            }
            return c1 + c2 + c3;
        }
    }

    /**
     * 用"*"符号格式化手机号前3后3
     * @param phoneNo 手机号："6666666666666666"
     * @return 格式化 卡号："666******666"
     */
    public static String formatPhoneNumberWithStar(String phoneNo) {
        if (phoneNo == null || "".equals(phoneNo))
            return "";
        int len = phoneNo.length();
        if (len <= 6) {
            return phoneNo;
        } else {
            String c1 = phoneNo.substring(0, 3);
            String c3 = phoneNo.substring(len - 3, len);
            String c2 = "";
            for (int i = 0; i < len - 6; i++) {
                c2 += "*";
            }
            return c1 + c2 + c3;
        }
    }

    /**
     * 将报文中12位的字符串转化为带2位小数点金额，不带逗号的
     * 例如：0000000341250转化成3412.50
     * @param mount 12位的字符串金额
     * @return 带2位小数点金额
     */
    public static String formatMount(String mount) {
        if (TextUtils.isEmpty(mount) || !StringUtil.isDigital(mount)) {
            return "0.00";
        }
        mount = mount.trim();
        double money = Long.parseLong(mount) * 0.01;
        if (money > 0) {
            DecimalFormat df = new DecimalFormat("##0.00");
            return df.format(money);
        } else {
            return "0.00";
        }
    }
    public static long parseLong(String longStr) {
        long temp = 0;
        if (!TextUtils.isEmpty(longStr)) {
            try {
                temp = Long.parseLong(longStr);
            } catch (Exception e) {
                temp = 0;
            }
        }
        return temp;
    }
}
