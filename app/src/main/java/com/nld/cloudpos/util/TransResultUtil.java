package com.nld.cloudpos.util;

import com.centerm.iso8583.util.DataConverter;
import com.nld.starpos.banktrade.utils.Cache;

import org.apache.log4j.Logger;
import java.util.Map;

import common.StringUtil;

public  class TransResultUtil {
    static Logger logger= Logger.getLogger(TransResultUtil.class);
    
    /**
     * 判断有没有55域脚本结果数据
     * @return
     */
    public static  boolean haveScriptData(String F55Data){
        try {
            if(StringUtil.isEmpty(F55Data)){
                return false;
            }
            Map<String, String> icMap = TlvUtil.tlvToMap(F55Data);
            String tag71 = icMap.get("71");
            String tag72 = icMap.get("72");
            if ((null == tag71 || "".equals(tag71)) && (null == tag72 || "".equals(tag72))){
                logger.debug("no script");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("判断脚本出现异常",e);
        }
        return false;
    }
    
    /**
     *  判断圈存交易是否脚本执行成功
     * @param TVR
     * @return
     */
    public static boolean transferScriptSuc (String TVR) {
        String transCode= Cache.getInstance().getTransCode();
        if (transCode.equals("002322") || transCode.equals("002323")
                || transCode.equals("002321")) {//指定账户圈存、非指定账户圈存、现金充值
            if(scriptSucess(TVR)) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }


    /**
     *  判断终端脚本是否执行成功
     * @param TVR
     * @return
     */
    public static boolean scriptSucess(String TVR) {
        if (TVR == null || "".equals(TVR)) 
            return false;
        String byte5 = DataConverter.byteToBinaryString(DataConverter.hexStringToByte(TVR.substring(8)));
        if ("0".equals(byte5.substring(3, 4))) { 
            logger.debug("脚本tag 72 执行成功");
            return true;
        } else { // tag72 脚本执行失败
            logger.debug("脚本tag 72 执行失败");
            return false;
        }
    }
    
    
    /**
     *  判断ARPC是否执行成功（发卡行认证）
     * @param TVR
     * @return
     */
    public static boolean arpcSucess(String TVR) {
        if (TVR == null || "".equals(TVR)) 
            return false;
        String byte5 = DataConverter.byteToBinaryString(DataConverter.hexStringToByte(TVR.substring(8)));
        if ("0".equals(byte5.substring(1, 2))) {
            logger.debug("ARPC认证成功");
            return true;
        } else { // ARPC执行失败,需上送ARPC
            logger.debug("ARPC认证失败");
            return false;
        }
    }
    

    // 读取用于打印凭条所要求的内核数据
    public static String getKernelDataForPrint (String printData) {
        try {
            String arqc = Cache.getInstance().getPrintArqc();
            if (!"".equals(arqc) && arqc!=null) {
                logger.info("arqc tag替换前："+arqc);
                arqc = arqc.replaceFirst("9F26", "9F99");
                logger.info("arqc tag替换后："+arqc);
                printData = printData+arqc;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("", e);
        }
        return printData;
    }
    
}
