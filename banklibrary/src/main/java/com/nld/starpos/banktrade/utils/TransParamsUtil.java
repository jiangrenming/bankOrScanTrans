package com.nld.starpos.banktrade.utils;

import android.text.TextUtils;
import android.util.Log;

import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;

import java.util.HashMap;
import java.util.Map;

import common.StringUtil;
import common.Utility;

/**
 * 进行交易组包操作的类；
 *
 * @author Administrator
 */
public class TransParamsUtil {

    /**
     * 获取批次号
     * @return
     */
    public static String getCurrentBatchNo() {
        ParamConfigDao mParamConfigDao;
        mParamConfigDao = new ParamConfigDaoImpl();
        String batchno = mParamConfigDao.get("batchno");           //批次号
        LogUtils.e("数据库获取批次号：" + batchno);
        if (StringUtil.isEmpty(batchno)) {    //对批次号、凭证号、流水号判空处理
            batchno = "000001";
        } else if (batchno.length() < 6) {
            batchno = Utility.addZeroForNum(batchno, 6);
        }
        //保存当前批次号到缓存中，方便之后使用
        Cache.getInstance().setBatchNo(batchno);
        return batchno;
    }

    /**
     * 获取微信批次号
     * @return
     */
    public static String getWxCurrentBatchNo() {
        ParamConfigDao mParamConfigDao;
        mParamConfigDao = new ParamConfigDaoImpl();
        String batchno = mParamConfigDao.get("wxbatchno");           //批次号
        LogUtils.e("数据库获取批次号：" + batchno);
        if ("".equals(batchno) || batchno == null) {    //对批次号、凭证号、流水号判空处理
            batchno = "000001";
        } else if (batchno.length() < 6) {
            batchno = Utility.addZeroForNum(batchno, 6);
        }
        //保存当前批次号到缓存中，方便之后使用
        Cache.getInstance().setBatchNo(batchno);
        return batchno;
    }

    /**
     * 获取流水号+1
     * @return
     */
    public static String getBillNo() {
        ParamConfigDao mParamConfigDao;
        mParamConfigDao = new ParamConfigDaoImpl();
        String billno = mParamConfigDao.get("billno");
        Integer billNoi = Integer.valueOf(billno);

        if (TextUtils.isEmpty(billno) || billNoi + 1 > 999999) {
            billno = "000001";
        } else {
            billno = Utility.addZeroForNum(String.valueOf(billNoi + 1), 6);
        }

        String systracenoStr = mParamConfigDao.get("systraceno");
        if (TextUtils.isEmpty(systracenoStr)) {
            systracenoStr = "000001";
        }
        long systraceno = Long.valueOf(systracenoStr) + 1; //POS流水号
        if (systraceno > 999999) {
            systraceno = 1;
        }
        // 数据库更新凭证号和POS流水号
        Map<String, String> map = new HashMap<String, String>();
        map.put("systraceno", String.valueOf(systraceno));
        map.put("billno", billno);
        mParamConfigDao.update(map);
        //保存流水凭证号都缓存中方便之后使用
        Cache.getInstance().setSerialNo(billno);
        Log.i("BILLNO","getBatchNo : " + Cache.getInstance().getBatchNo() +"getBillNo : " + billno);
        return billno;
    }

    /**
     * 获取//票据号/凭证号/pos流水号,每次获取值加1。
     * @return
     */
    public static String getWxBillNo() {
        ParamConfigDao mParamConfigDao;
        mParamConfigDao = new ParamConfigDaoImpl();
        String billno = mParamConfigDao.get("wxbillno");
        if ("".equals(billno) || billno == null) {
            billno = "000001";
        } else if (billno.length() < 6) {
            billno = Utility.addZeroForNum(billno, 6);
        }
        if (Integer.valueOf(billno) + 1 > 999999) {
            billno = Utility.addZeroForNum(String.valueOf(1), 6);
        } else {
            billno = Utility.addZeroForNum(String.valueOf(Integer.valueOf(billno) + 1), 6);
        }

        String systracenoStr = mParamConfigDao.get("wxsystraceno");
        if ("".equals(systracenoStr) || systracenoStr == null) {
            systracenoStr = "000001";
        }
        long systraceno = Long.valueOf(systracenoStr) + 1; //POS流水号
        if (systraceno > 999999) {
            systraceno = 1;
        }
        // 数据库更新凭证号和POS流水号
        Map<String, String> map = new HashMap<String, String>();
        map.put("wxsystraceno", String.valueOf(systraceno));
        map.put("wxbillno", billno);
        mParamConfigDao.update(map);
        //保存流水凭证号都缓存中方便之后使用
//       Cache.getInstance().setSerialNo(billno);
        return billno;
    }

}
