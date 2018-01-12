package com.nld.cloudpos.payment.dev;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.nld.cloudpos.aidl.printer.PrintItemObj;
import com.nld.cloudpos.data.PrinterConstant;
import com.nld.cloudpos.payment.controller.FormatUtils;
import com.nld.cloudpos.util.CommonContants;
import com.nld.cloudpos.util.TlvUtil;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.SettleDataDao;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.bean.SettleData;
import com.nld.starpos.banktrade.db.bean.TransRecord;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;
import com.nld.starpos.banktrade.db.local.SettleDataDaoImpl;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.CommonUtil;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.banktrade.utils.TransParamsUtil;
import com.nld.starpos.wxtrade.bean.scan_settle.ScanSettleRes;
import com.nld.starpos.wxtrade.local.db.bean.ScanTransRecord;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.params.ReturnCodeParams;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.DateTimeUtil;
import common.HexUtil;
import common.StringUtil;
import common.Utility;

public class PrintDev {
    private static Logger logger = Logger.getLogger(PrintDev.class);

    public final static int FONT_SIZE_MIN = 4;
    public final static int FONT_SIZE_NORM = 8;
    public final static int FONT_SIZE_MAX = 16;
    //    private final static int
    private final static String total = "总计     ";
    private final static String LINE = "------------------------------------------------";

    private Context mContext;

    public static List<PrintItemObj> getPrintData(boolean isSecond,
                                                  Context context, Map<String, String> resultMap, String transName) {
        if (null == resultMap) {
            return null;
        }

        final int SALE = 1;
        final int VOID = 2;
        final int REFUND = 3;
        final int AUTH = 4;
        final int AUTH_COMP = 5;
        final int AUTH_VOID = 6;
        final int AUTH_COMP_VOID = 7;
        final int SETTLE = 8;
        final int CASH_UP_MODE = 9; // 现金充值
        final int TRANSFER_MODE = 10; // 指定账户圈存
        final int NON_TRANSFER_MODE = 11; // 非指定账户圈存
        final int CASH_UP_VOID_MODE = 12; // 现金充值撤销
        final int OFFLINE_REFUND_MODE = 13;
        final int OFFLINE_SALE_MODE = 14; // 电子现金脱机消费
        final int WX_VOID = 15;
        final int WX_REFUND = 16;
        int CUR_MODE = 0;

        String transprocode = resultMap.get("transprocode");
        String msg_tp = resultMap.get("msg_tp"); // 获取交易下发的消息类型
        String conditionmode = resultMap.get("conditionmode");

        if ("900000".equals(transprocode)) {
            CUR_MODE = SETTLE;
        } else if (ReturnCodeParams.SUCESS_CODE.equals(transprocode) && "00".equals(conditionmode)
                && "0210".equals(msg_tp)) {
            CUR_MODE = SALE;
        } else if (ReturnCodeParams.SUCESS_CODE.equals(transprocode) && "06".equals(conditionmode)) {
            CUR_MODE = AUTH_COMP;
        } else if ("030000".equals(transprocode)) {
            CUR_MODE = AUTH;
        } else if ("200000".equals(transprocode) && "00".equals(conditionmode)
                && "0230".equals(msg_tp)) {
            CUR_MODE = REFUND;
        } else if ("200000".equals(transprocode) && "00".equals(conditionmode)
                && "0210".equals(msg_tp)) {
            CUR_MODE = VOID;
        } else if ("200000".equals(transprocode) && "06".equals(conditionmode)
                && "0210".equals(msg_tp)) {
            CUR_MODE = AUTH_COMP_VOID;
        } else if ("200000".equals(transprocode) && "06".equals(conditionmode)
                && "0110".equals(msg_tp)) {
            CUR_MODE = AUTH_VOID;
        } else if ("600000".equals(transprocode) && "0210".equals(msg_tp)) {
            CUR_MODE = TRANSFER_MODE;
        } else if ("620000".equals(transprocode) && "0210".equals(msg_tp)) {
            CUR_MODE = NON_TRANSFER_MODE;
        } else if ("630000".equals(transprocode) && "0210".equals(msg_tp)) {
            CUR_MODE = CASH_UP_MODE;
        } else if ("170000".equals(transprocode) && "0210".equals(msg_tp)) {
            CUR_MODE = CASH_UP_VOID_MODE;
        } else if (ReturnCodeParams.SUCESS_CODE.equals(transprocode) && "01".equals(conditionmode)
                && "0330".equals(msg_tp)) {
            CUR_MODE = OFFLINE_REFUND_MODE;
        } else if (ReturnCodeParams.SUCESS_CODE.equals(transprocode) && "00".equals(conditionmode)
                && "0330".equals(msg_tp)) {
            CUR_MODE = OFFLINE_SALE_MODE;
        } else if ("680000".equals(transprocode) && "99".equals(conditionmode)
                && "0210".equals(msg_tp)) {
            CUR_MODE = WX_VOID;
        } else if ("690000".equals(transprocode) && "99".equals(conditionmode)
                && "0230".equals(msg_tp)) {
            CUR_MODE = WX_REFUND;
        }

        ParamsUtil params = ParamsUtil.getInstance();
        List<PrintItemObj> list = new ArrayList<PrintItemObj>();
        list.add(getPrintItemObj("POS签购单", 20, true, PrintItemObj.ALIGN.CENTER));
        list.add(getPrintItemObj("商户名称(MERCHANT NAME)"));
        list.add(getPrintItemObj(params.getParam("mchntname"), 16, true));
        list.add(getPrintItemObj("商户编号(MERCHANTNO)"));
        list.add(getPrintItemObj(params.getParam("merid_unionpay")));
        list.add(getPrintItemObj("终端号(TERMINAL)"));
        list.add(getPrintItemObj(params.getParam("termid_unionpay")));

        String addresp = resultMap.get("addrespkey");
        String issuerBank = "";
        String acquBank = "";
        try {
            if (addresp != null && !"".equals(addresp) && addresp.length() > 11) {
                issuerBank = addresp.substring(0, 11).trim();
                acquBank = addresp.substring(11).trim();
            }
        } catch (Exception e) {
        }

        list.add(getPrintItemObj("发卡行:" +
                CommonUtil.revBankName(issuerBank)));
        list.add(getPrintItemObj("收单行:" + CommonUtil.revBankName(acquBank)));


        list.add(getPrintItemObj("卡号(CARD NO):"));
        // 输入方式
        String inType = Cache.getInstance().getSerInputCode();
        if (inType.startsWith("01"))
            inType = "M";
        else if (inType.startsWith("02"))
            inType = "S";
        else if (inType.startsWith("05"))
            inType = "I";
        else if (inType.startsWith("07") || inType.startsWith("9"))
            inType = "C";
        else if (inType.startsWith("80"))
            inType = "F";
        else
            inType = "";
        list.add(getPrintItemObj("  "
                + CommonUtil.getMarkCarno(Cache.getInstance().getCardNo())
                + "(" + inType + ")"));
        String icSerial = Cache.getInstance().getCardSeqNo();
        if (!StringUtil.isEmpty(icSerial)) {
            list.add(getPrintItemObj("卡序列号(CARD SN):" + icSerial));
        }
        String cardType = resultMap.get("settledata");
        if (!StringUtil.isEmpty(cardType) && cardType.length() > 3) {
            list.add(getPrintItemObj("卡类别(CARD TYPE): "
                    + CommonUtil.revCardType(cardType.substring(0, 3)) + "  " + cardType.substring(3).trim()));
        }
        list.add(getPrintItemObj("有效期(EXP DATE)："
                + Cache.getInstance().getInvalidDate()));
        list.add(getPrintItemObj("交易类别(TRANS TYPE):"));
        list.add(getPrintItemObj(transName, 16, true));

        list.add(getPrintItemObj("批次号(BATCH NO):"
                + Cache.getInstance().getBatchNo()));
        if (!Cache.getInstance().getTransCode().equals("002309")) { // 只在pos签购时打印

            list.add(getPrintItemObj("凭证号(VOUCHER NO)"
                    + Cache.getInstance().getSerialNo()));
            if (!StringUtil.isEmpty(resultMap.get("idrespcode"))) {
                list.add(getPrintItemObj("授权号(AUTH NO)"
                        + resultMap.get("idrespcode")));
            }
            String refernumber = StringUtil.isEmpty(resultMap.get("refernumber")) ? "" : resultMap.get("refernumber");
            list.add(getPrintItemObj("参考号(REFER NO)"
                    + refernumber));
            // list.add(getPrintItemObj("外卡参考号(FRERER NO)" + ""));

        }
        list.add(getPrintItemObj("日期/时间(DATE/TIME)"));
        String datetime = resultMap.get("translocaldate")
                + resultMap.get("translocaltime");
        datetime = Utility.printFormatDateTime(datetime);
        list.add(getPrintItemObj(datetime));
        list.add(getPrintItemObj("交易金额(AMOUNT):"));

        if (CUR_MODE == VOID || CUR_MODE == AUTH_VOID
                || CUR_MODE == AUTH_COMP_VOID || CUR_MODE == REFUND
                || CUR_MODE == CASH_UP_VOID_MODE
                || CUR_MODE == OFFLINE_REFUND_MODE || CUR_MODE == WX_VOID
                || CUR_MODE == WX_REFUND) {
            list.add(getPrintItemObj("RMB" + " -"
                    + Cache.getInstance().getTransMoney() + "元", 16, true));
        } else {
            list.add(getPrintItemObj("RMB"
                    + Cache.getInstance().getTransMoney() + "元", 16, true));
        }

        // list.add(getPrintItemObj("RMB" + Cache.getInstance().getTransMoney()
        // + "元", 16, true));
        // list.add(getPrintItemObj("小费(TIP):"));
        // list.add(getPrintItemObj("总计(SUM TOTAL):"));
        // list.add(getPrintItemObj("RMB" + Cache.getInstance().getTransMoney()
        // + "元", 16, true));
        list.add(getPrintItemObj("备注(REFERENCE):"));
        String tip = resultMap.get("adddataword");
        list.add(getPrintItemObj(StringUtil.isEmpty(tip) ? "" : tip));
        // if(!StringUtil.isEmpty(icSerial)){//ic卡交易
        // list.add(getPrintItemObj("AID:"));
        // list.add(getPrintItemObj("ARQC:"));
        // list.add(getPrintItemObj("TVR:"));
        // list.add(getPrintItemObj("TSI:"));
        // list.add(getPrintItemObj("ATC:"));
        // list.add(getPrintItemObj("Appl Label:"));
        // list.add(getPrintItemObj("Appl Name:"));
        // }
        String transCode = Cache.getInstance().getTransCode();
        String reserve4 = Cache.getInstance().getPrintIcData();
        Log.d("printDev", "print data reserve4 = " + reserve4);
        logger.debug("print data reserve4 = " + reserve4);
        String exVersion = Utility.getVersion();
        if (StringUtil.isEmpty((exVersion))) exVersion = "";
        if (reserve4 != null && !"null".equals(reserve4)) {
            Map<String, String> map = TlvUtil.tlvToMap(reserve4);
//            if(StringUtil.isEmpty(map.get("9F06"))) map.put("9F06", "");
            if (StringUtil.isEmpty(map.get("4F"))) map.put("4F", "");
            if (StringUtil.isEmpty(map.get("9F26"))) map.put("9F26", "");
            if (StringUtil.isEmpty(map.get("9F99"))) map.put("9F99", "");
            if (StringUtil.isEmpty(map.get("95"))) map.put("95", "");
            if (StringUtil.isEmpty(map.get("9B"))) map.put("9B", "");
            if (StringUtil.isEmpty(map.get("5F34"))) map.put("5F34", "");
            if (StringUtil.isEmpty(map.get("9F36"))) map.put("9F36", "");
            if (StringUtil.isEmpty(map.get("9F37"))) map.put("9F37", "");
            if (StringUtil.isEmpty(map.get("82"))) map.put("82", "");
            if (StringUtil.isEmpty(map.get("9F33"))) map.put("9F33", "");
            if (StringUtil.isEmpty(map.get("9F10"))) map.put("9F10", "");
            if (StringUtil.isEmpty(map.get("50"))) map.put("50", "");
            if (StringUtil.isEmpty(map.get("9F12"))) map.put("9F12", "");

            list.add(getPrintItemObj("AID：" + map.get("9F06")));
            list.add(getPrintItemObj("AID：" + map.get("4F")));
            if (transCode.equals(TransConstans.TRANS_CODE_DZXJ_OFFLINE)) {
                list.add(getPrintItemObj("TC：" + map.get("9F26")));
            } else {
                list.add(getPrintItemObj("ARQC：" + map.get("9F99"))); // 构造tag
                // 9F99，将arqc存放于reserve4字段中
            }
            list.add(getPrintItemObj("TVR：" + map.get("95")));
//			list.add(getPrintItemObj("TSI：" + map.get("9B")));
            if (transCode.equals(TransConstans.TRANS_CODE_DZXJ_OFFLINE)) {
                list.add(getPrintItemObj("CSN：" + map.get("5F34")));
            }
            list.add(getPrintItemObj("ATC：" + map.get("9F36")));
            if (transCode.equals(TransConstans.TRANS_CODE_DZXJ_OFFLINE)) {
                list.add(getPrintItemObj("UNPR NUM：" + map.get("9F37")));
                list.add(getPrintItemObj("AIP：" + map.get("82")));
                list.add(getPrintItemObj("TEMP CAP：" + map.get("9F33")));
                list.add(getPrintItemObj("IAD：" + map.get("9F10")));
            }
//			list.add(getPrintItemObj("Appl Label："));
//			try {
//				String tag_50 = map.get("50");
//				if (tag_50 != null && !"".equals(tag_50)) {
//					list.add(getPrintItemObj(" "
//							+ new String(
//									HexUtil.hexStringToByte(map.get("50")),
//									"gbk")));
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				logger.error(e);
//			}
//			list.add(getPrintItemObj("Appl Name:"));
//			try {
//				String tag_9f12 = map.get("9F12");
//				if (tag_9f12 != null && !"".equals(tag_9f12)) {
//					list.add(getPrintItemObj(" "
//							+ new String(HexUtil.hexStringToByte(tag_9f12),
//									"gbk")));
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				logger.error(e);
//			}
            // if (CUR_MODE == OFFLINE_SALE_MODE || CUR_MODE == CASH_UP_MODE) {
            // mList.add(getPrintItemObj("卡片余额："+
            // Utility.unformatMount(map.get("9F79")==null?map.get("9F5D"):map.get("9F79"))));
            // }
        }
        if (!isSecond) {
            list.add(getPrintItemObj("持卡人签名(CARDHOLDER SIGNATURE)\n\n"));
            list.add(getPrintItemObj("本人确认以上交易,同意将其计入本卡账户"));
            list.add(getPrintItemObj(
                    "I ACKNOWLEDGE SATISFACTORY RECEIPT OF RELATIVE GOOGS/SERVICES"));
            list.add(getPrintItemObj(LINE));

            list.add(getPrintItemObj(exVersion));
            list.add(getPrintItemObj("客服务热线:400-766-6666"));
            list.add(getPrintItemObj("-------------------商户存根---------------------"));
        } else {
            list.add(getPrintItemObj(LINE));
            list.add(getPrintItemObj("客服热线:400-766-6666"));
            list.add(getPrintItemObj("-------------------持卡人存根-------------------"));
        }
        list.add(getPrintItemObj(" \n\n", FONT_SIZE_MAX));
        return list;
    }

    public static Map<String, String> getPrintMap(Map<String, String> dataMap,
                                                  Map<String, String> resultMap) {
        Map<String, String> desMap = new HashMap<String, String>();
        desMap.put("reserve1", resultMap.get("msg_tp"));
        desMap.put(
                "priaccount",
                resultMap.get("priaccount") != null ? resultMap
                        .get("priaccount") : dataMap.get("priaccount"));
        desMap.put(
                "transprocode",
                resultMap.get("transprocode") != null ? resultMap
                        .get("transprocode") : dataMap.get("transprocode"));
        desMap.put(
                "conditionmode",
                resultMap.get("conditionmode") != null ? resultMap
                        .get("conditionmode") : dataMap.get("conditionmode"));
        desMap.put(
                "acceptoridcode",
                resultMap.get("acceptoridcode") != null ? resultMap
                        .get("acceptoridcode") : dataMap.get("acceptoridcode"));
        desMap.put("respcode", resultMap.get("respcode"));

        String terminalid = resultMap.get("terminalid") != null ? resultMap
                .get("terminalid") : dataMap.get("terminalid");
        desMap.put(
                "terminalid",
                terminalid == null ? null : new String(HexUtil
                        .hexStringToByte(terminalid)));

        String loadparams = resultMap.get("loadparams") != null ? resultMap
                .get("loadparams") : dataMap.get("loadparams");
        desMap.put(
                "loadparams",
                loadparams == null ? null : new String(HexUtil
                        .hexStringToByte(loadparams)));
        desMap.put(
                "expireddate",
                resultMap.get("expireddate") != null ? resultMap
                        .get("expireddate") : dataMap.get("expireddate"));

        String batchbillno = resultMap.get("batchbillno") != null ? resultMap
                .get("batchbillno") : dataMap.get("batchbillno");
        desMap.put("batchbillno", batchbillno == null ? null : new String(
                HexUtil.hexStringToByte(batchbillno)));

        String idrespcode = resultMap.get("idrespcode") != null ? resultMap
                .get("idrespcode") : dataMap.get("idrespcode");
        desMap.put(
                "idrespcode",
                idrespcode == null ? null : new String(HexUtil
                        .hexStringToByte(idrespcode)));

        String refernumber = resultMap.get("refernumber") != null ? resultMap
                .get("refernumber") : dataMap.get("refernumber");
        desMap.put("refernumber", refernumber == null ? null : new String(
                HexUtil.hexStringToByte(refernumber)));
        desMap.put(
                "translocaldate",
                resultMap.get("translocaldate") != null ? resultMap
                        .get("translocaldate") : dataMap.get("translocaldate"));
        desMap.put(
                "translocaltime",
                resultMap.get("translocaltime") != null ? resultMap
                        .get("translocaltime") : dataMap.get("translocaltime"));
        desMap.put(
                "transamount",
                resultMap.get("transamount") != null ? resultMap
                        .get("transamount") : dataMap.get("transamount"));
        String adddataword = resultMap.get("adddataword") != null ? resultMap
                .get("adddataword") : dataMap.get("adddataword");

        try {
            desMap.put("adddataword", adddataword == null ? null : new String(
                    HexUtil.hexStringToByte(adddataword), "gbk"));
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
        }
        desMap.put("requestSettleData", resultMap.get("requestSettleData")); // 本地结算信息
        String respcode = resultMap.get("respcode") != null ? resultMap
                .get("respcode") : dataMap.get("respcode");
        desMap.put(
                "respcode",
                respcode == null ? null : new String(HexUtil
                        .hexStringToByte(respcode)));
        String settledata = resultMap.get("settledata");
        try {
            desMap.put("settledata", settledata == null ? null : new String(
                    HexUtil.hexStringToByte(settledata), "gbk"));
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
            e.printStackTrace();
        } // 前置返回结算信息

        desMap.put("entrymode", dataMap.get("entrymode"));
        desMap.put("reserve4", dataMap.get("reserve4"));
        return desMap;
    }

    /**
     * 结算打印
     * TODO 预授权有些条目还没有添加
     *
     * @param isPrintDetails
     * @return
     */
    public static List<PrintItemObj> getSettlePrintMap(Context context,
                                                       boolean isPrintDetails, Map<String, String> dataMap,
                                                       boolean isWechat) {
        List<PrintItemObj> mList = new ArrayList<PrintItemObj>();
        if (!isPrintDetails) { // 打印明细的时候不打印
            mList.add(getPrintItemObj("银行卡结算总计单", FONT_SIZE_MAX, true));
            // 43域，商户名称
            ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl();
            mList.add(getPrintItemObj("商户名称:"
                    + mParamConfigDao.get("mchntname")));

            // 42域，商户编号
            mList.add(getPrintItemObj("商户号:" + mParamConfigDao.get("unionpay_merid")));
            mList.add(getPrintItemObj("终端号:" + mParamConfigDao.get("unionpay_termid")));
            String data;
            if (!isWechat) {
                data = TransParamsUtil.getCurrentBatchNo();
            } else {
                data = TransParamsUtil.getWxCurrentBatchNo();
            }
            mList.add(getPrintItemObj("批次号:" + data));

            String strdate = dataMap.get("translocaldate");
            String strtime = dataMap.get("translocaltime");
            if (StringUtil.isEmpty(strdate)) {
                strdate = "0000";
            }
            if (StringUtil.isEmpty(strtime)) {
                strtime = "000000";

            }
            String datetime = strdate
                    + strtime;
            datetime = Utility.printFormatDateTime(datetime);
            mList.add(getPrintItemObj("日期/时间:" + datetime));

            // mList.add(getPrintItemObj(datetime));
            if ("95".equals(dataMap.get("respcode"))) {
                mList.add(getPrintItemObj("内卡对账不平\n"));
            } else if ("00".equals(dataMap.get("respcode"))) {
                mList.add(getPrintItemObj("内卡对账平\n"));
            }
            mList.add(getPrintItemObj("====终端结算总计（SUM TOTAL）==="));
            mList.add(getPrintItemObj("--------------内卡--------------"));
            mList.add(getPrintItemObj("交易类型      总笔数      总金额"));
            String requestSettleData = Cache.getInstance().getSettleData();
            if (requestSettleData != null && requestSettleData != "") {
                //消费金额
                String xfStr = Utility
                        .unformatMount(requestSettleData.substring(3,
                                15));
                String xfCount = Utility.printInteger(requestSettleData
                        .substring(0, 3));
                //退货金额
                String thStr = Utility
                        .unformatMount(requestSettleData.substring(18,
                                30));
                String thCount = Utility
                        .printInteger(requestSettleData.substring(15,
                                18));
                //消费撤销金额
                String xfCxStr = Utility
                        .unformatMount(requestSettleData.substring(33,
                                45));
                String xfCxCount = Utility
                        .printInteger(requestSettleData.substring(30,
                                33));


                //预授权完成金额
                String ysqStr = Utility
                        .unformatMount(requestSettleData.substring(108,
                                120));
                String ysqCount = Utility
                        .printInteger(requestSettleData.substring(105,
                                108));
                mList.add(getPrintItemObj("消费      "
                        + Utility.printFillSpace(xfCount
                        , 4)
                        + "   "
                        + Utility.printFillSpace(xfStr, 12)));
                mList.add(getPrintItemObj("消费撤销  "
                        + Utility.printFillSpace(xfCxCount
                        , 4)
                        + "   "
                        + Utility.printFillSpace(xfCxStr, 12)));

                mList.add(getPrintItemObj("退货      "
                        + Utility.printFillSpace(thCount, 4)
                        + "   "
                        + Utility.printFillSpace(thStr, 12)));

                mList.add(getPrintItemObj("预授权完成"
                        + Utility.printFillSpace(ysqCount, 4)
                        + "   "
                        + Utility.printFillSpace(ysqStr, 12)));
                String totalAmount = Utility.formatMountTow(Double.parseDouble(xfStr) - Double.parseDouble(xfCxStr) - Double.parseDouble(thStr) + Double.parseDouble(ysqStr));
                int totalCount = Integer.parseInt(xfCount) + Integer.parseInt(xfCxCount) + Integer.parseInt(ysqCount) + Integer.parseInt(thCount);
                mList.add(getPrintItemObj(total
                        + Utility.printFillSpace(totalCount + "", 5)
                        + "   "
                        + Utility.printFillSpace(totalAmount, 12)));
            }
            // 对账不平  这部分不需要打印
            if ("95".equals(dataMap.get("respcode"))
                    && !"true".equals(dataMap.get("isReprints"))) {
//                mList.add(getPrintItemObj(" "));
//                mList.add(getPrintItemObj("====主机结算总计（SUM TOTAL）==="));
//                mList.add(getPrintItemObj("交易类型      总笔数      总金额"));
//                String settleData = dataMap.get("field63");//F63
//                if (settleData != null && settleData != "") {
//                    //消费金额
//                    String xfStr = Utility
//                            .unformatMount(settleData.substring(3,
//                                    15));
//                    String xfCount = Utility.printInteger(settleData
//                            .substring(0, 3));
//                    //退货金额
//                    String thStr = Utility
//                            .unformatMount(settleData.substring(18,
//                                    30));
//                    String thCount = Utility
//                            .printInteger(settleData.substring(15,
//                                    18));
//
//                    //消费撤销金额
//                    String xfcxStr = Utility
//                            .unformatMount(settleData.substring(18,
//                                    30));
//                    String xfcxCount = Utility
//                            .printInteger(settleData.substring(15,
//                                    18));
//                    //预授权完成金额
//                    String ysqStr = Utility
//                            .unformatMount(settleData.substring(108,
//                                    120));
//                    String ysqCount = Utility
//                            .printInteger(settleData.substring(105,
//                                    108));
//                    mList.add(getPrintItemObj("消费      "
//                            + Utility.printFillSpace(xfCount
//                            , 4)
//                            + "   "
//                            + Utility.printFillSpace(xfStr, 12)));
//
////                    mList.add(getPrintItemObj("消费撤销   "
////                            + Utility.printFillSpace(xfcxCount
////                            , 4)
////                            + "   "
////                            + Utility.printFillSpace(xfcxStr, 12)));
//
//                    mList.add(getPrintItemObj("退货      "
//                            + Utility.printFillSpace(thCount, 4)
//                            + "   "
//                            + Utility.printFillSpace(thStr, 12)));
//
//                    mList.add(getPrintItemObj("预授权完成"
//                            + Utility.printFillSpace(ysqCount, 4)
//                            + "   "
//                            + Utility.printFillSpace(ysqStr, 12)));
//
//
//                    String totalAmount = Utility.formatMountTow(Double.parseDouble(xfStr) - Double.parseDouble(xfcxStr) - Double.parseDouble(thStr) + Double.parseDouble(ysqStr));
//                    int totalCount = Integer.parseInt(xfCount) + Integer.parseInt(xfcxCount) + Integer.parseInt(ysqCount) + Integer.parseInt(thCount);
//                    mList.add(getPrintItemObj(total
//                            + Utility.printFillSpace(totalCount + "", 5)
//                            + "   "
//                            + Utility.printFillSpace(totalAmount, 12)));
//                }
            }
        } else {
//            mList.add(getPrintItemObj("银行卡结算明细单", 16));
            mList.add(getPrintItemObj("交易明细单/TXN LIST", 16, PrintItemObj.ALIGN.CENTER));
            mList.add(getPrintItemObj("凭证号   类型     卡号            金额    授权号", 4));
            mList.add(getPrintItemObj("VOUCHER  TYPE     CARD              AMT    AUTH ", 4));
            mList.add(getPrintItemObj("------------------------------"));

            // 获取交易记录表数据
            SettleDataDao mSettleDataDao = new SettleDataDaoImpl();
            List<SettleData> mSd = null;
            mSd = mSettleDataDao.getSettleData();
            if (mSd == null) {
//				mList.add(getPrintItemObj(" \n\n",FONT_SIZE_MAX));
            } else {
                int i = 0;
                for (SettleData mRecord : mSd) {
                    i++;
                    String transCode = mRecord.transprocode;
                    String reserve1 = mRecord.reserve1;
                    String serviceCode = mRecord.conditionmode;

                    LogUtils.d("批结打印明细： transCode ：" + transCode + " ,reserve1 : " + reserve1 + " ,serviceCode : " + serviceCode);
                    String transtype = getTransTypeChar(transCode, serviceCode, reserve1);


                    String cardno = Utility.formatCardno(mRecord
                            .getPriaccount());
                    String mount = Utility.unformatMount(mRecord
                            .getTransamount());
                    String billno = mRecord.getBatchbillno().substring(6, 12);
                    String idrespcode = mRecord.getIdrespcode();
                    if ("".equals(idrespcode) || idrespcode == null
                            || "null".equals(idrespcode)) {
                        idrespcode = "";
                    }

                    mList.add(getPrintItemObj("" + billno + " " + transtype
                            + " " + Utility.printFillSpace(cardno, 19) + " "
                            + Utility.printFillSpace(mount, 10) + " "
                            + idrespcode, 4));

                }
            }

            mSd = mSettleDataDao.getUnSuccesssFulData(); // 获取未成功上送的交易明细
            if (mSd == null) {
//				mList.add(getPrintItemObj(" "));
            } else { // 打印结算明细数据
                try {
                    mList.add(getPrintItemObj(" \n", FONT_SIZE_MAX));
                    mList.add(getPrintItemObj("脱机未成功上送/UNSUCCESSFUL LIST", 4));
                    mList.add(getPrintItemObj("凭证号   类型     卡号            金额    授权号", 4));
                    mList.add(getPrintItemObj("VOUCHER  TYPE     CARD              AMT    AUTH ", 4));
                    mList.add(getPrintItemObj("------------------------------------------------"));
                    mList = printDetails(mList, mSd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mSd != null) {
                mSd.clear();
                mSd = null;
            }

            mSd = mSettleDataDao.getDeniedData(); // 获取上送被拒绝的交易明细
            if (mSd == null) {
//				mList.add(getPrintItemObj(" \n"));
            } else { // 打印结算明细数据
                try {
                    mList.add(getPrintItemObj(" \n", FONT_SIZE_MAX));
                    mList.add(getPrintItemObj("脱机上送后被平台拒绝/DENIED LIST", 4));
                    mList.add(getPrintItemObj("凭证号   类型     卡号            金额    授权号", 4));
                    mList.add(getPrintItemObj("VOUCHER  TYPE     CARD              AMT    AUTH ", 4));
                    mList.add(getPrintItemObj("------------------------------------------------"));
                    mList = printDetails(mList, mSd);
//                    mList.add(getPrintItemObj(" \n\n",FONT_SIZE_MAX));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mList.add(getPrintItemObj("交易类型:S-消费 R-退货 P-预授权完成(请求) C-预授权完成(通知) " +
                            "L-离线结算 E-电子现金(钱包)消费 Q-圈存类、充值类交易  " +
                            "B-积分消费  V-消费撤销  A-完成撤销 W-微信类消费 Y-银联二维码类消费 Z-支付宝类交易 T-扫码退货",
                    PrintDev.FONT_SIZE_MIN));
        }
        return mList;
    }


    private static String getTransTypeChar(String transCode, String serviceCode, String reserve1) {
        String transtype = "";
        if ("900000".equals(transCode)) {
            transtype = "结算";
        } else if (ReturnCodeParams.SUCESS_CODE.equals(transCode) && "0330".equals(reserve1)) {
            transtype = " E";
        } else if (ReturnCodeParams.SUCESS_CODE.equals(transCode) && "00".equals(serviceCode)) {
            transtype = " S";
        } else if (ReturnCodeParams.SUCESS_CODE.equals(transCode) && "06".equals(serviceCode)) {
            transtype = " P";
        } else if ("200000".equals(transCode) && "00".equals(serviceCode)
                && "0230".equals(reserve1)) {
            transtype = " R";
        } else if (("600000".equals(transCode) && "0210".equals(reserve1))
                || ("630000".equals(transCode) && "0210".equals(reserve1))
                || ("620000".equals(transCode) && "0210".equals(reserve1))) {
            transtype = " Q";
        } else if ("660000".equals(transCode) && "0210".equals(reserve1)) {
            transtype = " S";
        } else if ("680000".equals(transCode) && "99".equals(serviceCode)
                && "0210".equals(reserve1)) {
            transtype = " R";
        } else if ("690000".equals(transCode) && "99".equals(serviceCode)
                && "0230".equals(reserve1)) {
            transtype = " R";
        } else if ("200000".equals(transCode) && "00".equals(serviceCode)
                && "0210".equals(reserve1)) { // 消费撤销
            transtype = " V";
        }
        return transtype;
    }


    public static List<PrintItemObj> getPrintDataALLTransObjects(Context context) {
        List<PrintItemObj> mList = new ArrayList<PrintItemObj>();
        int cardCNum = 0; // 内卡借记笔数
        int cardDNum = 0; // 内卡贷记笔数
        long cardCAcount = 0; // 内卡借记金额
        long cardDAcount = 0; // 内卡贷记金额

        int exCardCNum = 0; // 外卡借记笔数
        int exCardDNum = 0; // 外卡贷记笔数
        long exCardCAcount = 0; // 外卡借记金额
        long exCardDAcount = 0; // 外卡贷记金额

        mList.add(getPrintItemObj("银行卡当批次统计单", 16, true));

        ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl();
        mList.add(getPrintItemObj("商户名称:" + mParamConfigDao.get("mchntname")));
        // mList.add(getPrintItemObj(mParamConfigDao.get("mchntname"), 20));

        // 42域，商户编号
        mList.add(getPrintItemObj("商户编号:" + mParamConfigDao.get("unionpay_merid")
                + " " + "终端号:" + mParamConfigDao.get("unionpay_termid")));
        // mList.add(getPrintItemObj(mParamConfigDao.get("merid")));

        // 41域，终端号
//		mList.add(getPrintItemObj());
        // mList.add(getPrintItemObj(mParamConfigDao.get("termid")));
        mList.add(getPrintItemObj("类型/TYPE  笔数/SUM   总金额/AMT"));
        mList.add(getPrintItemObj(LINE));
        TransRecordDao mTransRecordDao = new TransRecordDaoImpl();
        // 查询消费类型
        List<TransRecord> salelists = mTransRecordDao.getCountTransRecord(
                "000000", "00", "0210");
        if (salelists == null) {
            salelists = new ArrayList<TransRecord>();
        }
        long saleAcount = 0;
        if (salelists != null) {
            if (salelists.size() > 0) {
                cardCNum += salelists.size();
                for (TransRecord saleRecord : salelists) {
                    long transamount = Long.valueOf(saleRecord.getTransamount());
                    cardCAcount += transamount;
                    saleAcount += transamount;
                }
            }
        }
        if ((salelists != null && salelists.size() > 0)) {
            mList.add(getPrintItemObj("消费/SALE"));
            mList.add(getPrintItemObj(Utility.printFillSpace(
                    String.valueOf(salelists.size()), 17)
                    + Utility.printFillSpace(
                    Utility.unformatMount(String.valueOf(saleAcount)),
                    14)));
        }

        // 查询电子现金类型
        List<TransRecord> salelistsQ = mTransRecordDao.getCountTransRecord(
                "000000", "00", "0330");
        long saleAcountQ = 0;
        if (salelistsQ == null) {
            salelistsQ = new ArrayList<TransRecord>();
        }
        if (salelistsQ.size() > 0) {
            cardCNum += salelistsQ.size();
            for (TransRecord saleRecord : salelistsQ) {
                long transamount = Long.valueOf(saleRecord.getTransamount());
                cardCAcount += transamount;
                saleAcountQ += transamount;
            }
        }

        if ((salelistsQ != null && salelistsQ.size() > 0)) {
            mList.add(getPrintItemObj("电子现金消费/EC LOAD"));
            mList.add(getPrintItemObj(Utility.printFillSpace(
                    String.valueOf(salelistsQ.size()), 17)
                    + Utility.printFillSpace(
                    Utility.unformatMount(String.valueOf(saleAcountQ)),
                    14)));
        }


        // 查询消费撤销类型
        List<TransRecord> voidlists = mTransRecordDao.getCountTransRecord(
                "200000", "00", "0210");
        if (voidlists == null) {
            voidlists = new ArrayList<TransRecord>();
        }
        long voidAcount = 0;
        if (voidlists != null) {
            if (voidlists.size() > 0) {
                cardDNum += voidlists.size();
                for (TransRecord voidRecord : voidlists) {
                    long transamount = Long.valueOf(voidRecord.getTransamount());
                    cardDAcount += transamount;
                    voidAcount += transamount;
                }
            }

        }

        if (voidlists != null && voidlists.size() > 0) {
            mList.add(getPrintItemObj("消费撤销/VOID"));
            mList.add(getPrintItemObj(Utility.printFillSpace(
                    String.valueOf(voidlists.size()), 17)
                    + Utility.printFillSpace(
                    Utility.unformatMount(String.valueOf(voidAcount)),
                    14)));
        }

        // 查询预授权完成类型
        List<TransRecord> complists = mTransRecordDao.getCountTransRecord(
                "000000", "06", "0210");
        if (complists != null) {
            if (complists.size() > 0) {
                long compAcount = 0;
                cardCNum += complists.size();
                for (TransRecord comRecord : complists) {
                    long transamount = Long.valueOf(comRecord.getTransamount());
                    cardCAcount += transamount;
                    compAcount += transamount;
                }
                mList.add(getPrintItemObj("预授权完成/AUTH COMPLETE"));
                mList.add(getPrintItemObj(Utility.printFillSpace(
                        String.valueOf(complists.size()), 17)
                        + Utility.printFillSpace(Utility.unformatMount(String
                        .valueOf(compAcount)), 14)));
            }

        }
        // 查询预授权完成撤销类型
        List<TransRecord> compvoidlists = mTransRecordDao.getCountTransRecord(
                "200000", "06", "0210");
        if (compvoidlists != null) {
            if (compvoidlists.size() > 0) {
                long compvoidAcount = 0;
                cardDNum += compvoidlists.size();
                for (TransRecord compvoidRecord : compvoidlists) {
                    long transamount = Long.valueOf(compvoidRecord.getTransamount());
                    cardDAcount += transamount;
                    compvoidAcount += transamount;
                }
                mList.add(getPrintItemObj("预授权完成撤销/COMPLETE VOID"));
                mList.add(getPrintItemObj(Utility.printFillSpace(
                        String.valueOf(compvoidlists.size()), 17)
                        + Utility.printFillSpace(Utility.unformatMount(String
                        .valueOf(compvoidAcount)), 14)));
            }

        }
        // 查询退货类型
        List<TransRecord> refundlists = mTransRecordDao.getCountTransRecord(
                "200000", "00", "0230");
        long refundAcount = 0;
        if (refundlists != null) {
            if (refundlists.size() > 0) {
                cardDNum += refundlists.size();
                for (TransRecord compvoidRecord : refundlists) {
                    long transamount = Long.valueOf(compvoidRecord.getTransamount());
                    cardDAcount += transamount;
                    refundAcount += transamount;
                }
            }
        }

        if (refundlists != null && refundlists.size() > 0) {
            mList.add(getPrintItemObj("退货/REFUND"));
            mList.add(getPrintItemObj(
                    Utility.printFillSpace(String.valueOf(refundlists.size()),
                            17)
                            + Utility.printFillSpace(
                            Utility.unformatMount(String
                                    .valueOf(refundAcount)), 14)));
        }

        mList.add(getPrintItemObj("内卡借记笔数"
                + Utility.printFillSpace(String.valueOf(cardCNum), 4)));
        mList.add(getPrintItemObj("内卡借记金额"
                + Utility.printFillSpace(
                Utility.unformatMount(String.valueOf(cardCAcount)), 18)));
        mList.add(getPrintItemObj("内卡贷记笔数"
                + Utility.printFillSpace(String.valueOf(cardDNum), 4)));
        mList.add(getPrintItemObj("内卡贷记金额"
                + Utility.printFillSpace(
                Utility.unformatMount(String.valueOf(cardDAcount)), 18)));
        mList.add(getPrintItemObj("外卡借记笔数"
                + Utility.printFillSpace(String.valueOf(exCardCNum), 4)));
        mList.add(getPrintItemObj("外卡借记金额"
                + Utility.printFillSpace(
                Utility.unformatMount(String.valueOf(exCardCAcount)),
                18)));
        mList.add(getPrintItemObj("外卡贷记笔数"
                + Utility.printFillSpace(String.valueOf(exCardDNum), 4)));
        mList.add(getPrintItemObj("外卡贷记金额"
                + Utility.printFillSpace(
                Utility.unformatMount(String.valueOf(exCardDAcount)),
                18)));
        mList.add(getPrintItemObj(" \n\n", FONT_SIZE_MAX));

//		mList.add(getPrintItemObj(" \n"));
//
//		cardCNum = 0; // 内卡借记笔数
//		cardDNum = 0; // 内卡贷记笔数
//		cardCAcount = 0; // 内卡借记金额
//		cardDAcount = 0; // 内卡贷记金额
//
//		exCardCNum = 0; // 外卡借记笔数
//		exCardDNum = 0; // 外卡贷记笔数
//		exCardCAcount = 0; // 外卡借记金额
//		exCardDAcount = 0; // 外卡贷记金额
//		mList.add(getPrintItemObj("微信当批次统计单", FONT_SIZE_MAX, true));
//
//		mList.add(getPrintItemObj("商户名称:" + mParamConfigDao.get("wxmchntname")));
//		// mList.add(getPrintItemObj(mParamConfigDao.get("wxmchntname"), 20));
//
//		// 42域，商户编号
//		mList.add(getPrintItemObj("商户编号:" + mParamConfigDao.get("wxmerid")));
//		// mList.add(getPrintItemObj(mParamConfigDao.get("wxmerid")));
//
//		// 41域，终端号
//		mList.add(getPrintItemObj("终端号:" + mParamConfigDao.get("wxtermid")));
//		// mList.add(getPrintItemObj(mParamConfigDao.get("wxtermid")));
//		mList.add(getPrintItemObj("类型/TYPE  笔数/SUM   总金额/AMT"));
//		mList.add(getPrintItemObj(LINE));
//		WxTransRecordDao wxTransRecordDao = new WxTransRecordDaoImpl(context);
//		// 查询消费类型
//		List<WxTransRecord> wxSalelists = wxTransRecordDao.getCountTransRecord(
//				"660000", "99", "0210");
//		if (salelists == null) {
//			salelists = new ArrayList<TransRecord>();
//		}
//		if (wxSalelists == null) {
//			wxSalelists = new ArrayList<WxTransRecord>();
//		}
//		saleAcount = 0;
//		if (wxSalelists != null) {
//			if (wxSalelists.size() > 0) {
//				cardCNum += wxSalelists.size();
//				for (WxTransRecord saleRecord : wxSalelists) {
//					long transamount = Long.valueOf(saleRecord.transamount);
//					cardCAcount += transamount;
//					saleAcount += transamount;
//				}
//
//			}
//
//		}
//		if ((wxSalelists != null && wxSalelists.size() > 0)) {
//			mList.add(getPrintItemObj("微信消费/SALE"));
//			mList.add(getPrintItemObj(Utility.printFillSpace(
//					String.valueOf(wxSalelists.size()), 17)
//					+ Utility.printFillSpace(
//							Utility.unformatMount(String.valueOf(saleAcount)),
//							14)));
//		}
//
//		// 查询消费撤销类型
//		List<WxTransRecord> wxVoidlists = wxTransRecordDao.getCountTransRecord(
//				"680000", "99", "0210");
//		if (wxVoidlists == null) {
//			wxVoidlists = new ArrayList<WxTransRecord>();
//		}
//		voidAcount = 0;
//		if (wxVoidlists != null) {
//			if (wxVoidlists.size() > 0) {
//				cardDNum += wxVoidlists.size();
//				for (WxTransRecord voidRecord : wxVoidlists) {
//					long transamount = Long.valueOf(voidRecord.transamount);
//					cardDAcount += transamount;
//					voidAcount += transamount;
//				}
//
//			}
//
//		}
//
//		if ((wxVoidlists != null && wxVoidlists.size() > 0)) {
//			mList.add(getPrintItemObj("消费撤销/VOID"));
//			mList.add(getPrintItemObj(Utility.printFillSpace(
//					String.valueOf(wxVoidlists.size()), 17)
//					+ Utility.printFillSpace(
//							Utility.unformatMount(String.valueOf(voidAcount)),
//							14)));
//		}
//		List<WxTransRecord> wxRefundlists = wxTransRecordDao
//				.getCountTransRecord("690000", "99", "0230");
//		refundAcount = 0;
//
//		if (wxRefundlists != null) {
//			if (wxRefundlists.size() > 0) {
//				cardDNum += wxRefundlists.size();
//				for (WxTransRecord compvoidRecord : wxRefundlists) {
//					long transamount = Long.valueOf(compvoidRecord.transamount);
//					cardDAcount += transamount;
//					refundAcount += transamount;
//				}
//
//			}
//
//		}
//
//		if ((wxRefundlists != null && wxRefundlists.size() > 0)) {
//			mList.add(getPrintItemObj("退货/REFUND"));
//			mList.add(getPrintItemObj(
//					Utility.printFillSpace(
//							String.valueOf(wxRefundlists.size()), 17)
//							+ Utility.printFillSpace(
//									Utility.unformatMount(String
//											.valueOf(refundAcount)), 14)));
//		}
//
//		mList.add(getPrintItemObj("借记笔数"
//				+ Utility.printFillSpace(String.valueOf(cardCNum), 4)));
//		mList.add(getPrintItemObj("借记金额"
//				+ Utility.printFillSpace(
//						Utility.unformatMount(String.valueOf(cardCAcount)), 18)));
//		mList.add(getPrintItemObj("贷记笔数"
//				+ Utility.printFillSpace(String.valueOf(cardDNum), 4)));
//		mList.add(getPrintItemObj("贷记金额"
//				+ Utility.printFillSpace(
//						Utility.unformatMount(String.valueOf(cardDAcount)), 18)));
//		mList.add(getPrintItemObj(" \n\n",FONT_SIZE_MAX));
        return mList;
    }


    // 分段打印明细数据（每20条开关一次）
    private static List<PrintItemObj> printDetails(List<PrintItemObj> mList,
                                                   List<SettleData> mSd) throws Exception {
        int i = 0;
        for (SettleData mRecord : mSd) {
            i++;
            String transCode = mRecord.transprocode;
            String reserve1 = mRecord.reserve1;
            String serviceCode = mRecord.conditionmode;

            String transtype = null;
            if (ReturnCodeParams.SUCESS_CODE.equals(transCode) && "0330".equals(reserve1)) {
                transtype = " E";
            } else if (ReturnCodeParams.SUCESS_CODE.equals(transCode) && "00".equals(serviceCode)) {
                transtype = " S";
            } else if (ReturnCodeParams.SUCESS_CODE.equals(transCode) && "06".equals(serviceCode)) {
                transtype = " P";
            } else if ("200000".equals(transCode) && "00".equals(serviceCode)
                    && "0230".equals(reserve1)) {
                transtype = " R";
            } else if (("600000".equals(transCode) && "0210".equals(reserve1))
                    || ("630000".equals(transCode) && "0210".equals(reserve1))
                    || ("620000".equals(transCode) && "0210".equals(reserve1))) {
                transtype = " Q";
            } else if ("660000".equals(transCode) && "0210".equals(reserve1)) {
                transtype = " S";
            } else if ("680000".equals(transCode) && "99".equals(serviceCode)
                    && "0210".equals(reserve1)) {
                transtype = " R";
            } else if ("690000".equals(transCode) && "99".equals(serviceCode)
                    && "0230".equals(reserve1)) {
                transtype = " R";
            }

            String cardno = Utility.formatCardno(mRecord.getPriaccount());
            String mount = Utility.unformatMount(mRecord.getTransamount());
            String billno = mRecord.getBatchbillno().substring(6, 12);
            String idrespcode = mRecord.getIdrespcode();
            if ("".equals(idrespcode) || idrespcode == null
                    || "null".equals(idrespcode)) {
                idrespcode = "";
            }
            logger.debug("打印明细：" + "" + billno + " " + transtype + " "
                    + Utility.printFillSpace(cardno, 19) + "");
            mList.add(getPrintItemObj(billno + " " + transtype + " "
                    + Utility.printFillSpace(cardno, 19)
                    + " " + Utility.printFillSpace(mount, 10)
                    + "  " + idrespcode));
            logger.debug("打印明细：" + "  " + Utility.printFillSpace(mount, 10)
                    + "   " + idrespcode + " ");
//			mList.add(getPrintItemObj("  " + Utility.printFillSpace(mount, 10)
//					+ "   " + idrespcode + " "));
            if (i == mSd.size()) {
                mList.add(getPrintItemObj(" \n\n", FONT_SIZE_MAX));
            }
        }
        return mList;
    }


    /**
     * 银行卡交易打印
     *
     * @param dataMap
     * @param context
     * @return
     */
    /*
     * HashMap数据转化打印数据对象
     *
     * 打印数据前，根据情况需配置的项： HashMap中 key -- “isReprints”，值“true”时，为重打印，否则默认非重打印（重打印）
     * key -- "isSecond", 值“true”时，打印第二联，否则默认打印第一联（pos签购） key --
     * "requestSettleData"，对应Value为结算时终端结算信息（结算信息） key -- "printDetails",
     * 值“true”时，结算时，加打细明，值“false”时，加打3行空行
     */
    public static List<PrintItemObj> getPrintItemObjs(
            Map<String, String> dataMap, Context context) {
        List<PrintItemObj> mList = new ArrayList<PrintItemObj>();
        String data = null;
        final int SALE = 1;
        final int VOID = 2;
        final int REFUND = 3;
        final int AUTH = 4;
        final int AUTH_COMP = 5;
        final int AUTH_VOID = 6;
        final int AUTH_COMP_VOID = 7;
        final int SETTLE = 8;
        final int CASH_UP_MODE = 9; // 现金充值
        final int TRANSFER_MODE = 10; // 指定账户圈存
        final int NON_TRANSFER_MODE = 11; // 非指定账户圈存
        final int CASH_UP_VOID_MODE = 12; // 现金充值撤销
        final int OFFLINE_REFUND_MODE = 13;
        final int OFFLINE_SALE_MODE = 14; // 电子现金脱机消费
        final int WX_SALE = 15;// 微信
        final int WX_VOID = 16;
        final int WX_REFUND = 17;
        int CUR_MODE = 0;

        boolean isPosSalesSlip = false; // 判断是签购单还是结算单
        boolean isSale = false; // 标记是消费还是消费撤销
        String isPrintDetails = null; // 标记是否打印细明
        // if(!"true".equals(dataMap.get("printDetails")))
        isPrintDetails = dataMap.get("printDetails");

        String entrymode = dataMap.get("entrymode") == null ? "02" : dataMap
                .get("entrymode"); // pos输入方式(默认02)
        String transprocode = dataMap.get("transprocode");
        String msg_tp = dataMap.get("reserve1"); // 获取交易下发的消息类型
        String conditionmode = dataMap.get("conditionmode");
        if ("900000".equals(transprocode)) {
            CUR_MODE = SETTLE;
        } else if (ReturnCodeParams.SUCESS_CODE.equals(transprocode) && "00".equals(conditionmode)
                && "0210".equals(msg_tp)) {
            CUR_MODE = SALE;
        } else if (ReturnCodeParams.SUCESS_CODE.equals(transprocode) && "06".equals(conditionmode)) {
            CUR_MODE = AUTH_COMP;
        } else if ("030000".equals(transprocode)) {
            CUR_MODE = AUTH;
        } else if ("200000".equals(transprocode) && "00".equals(conditionmode)
                && "0230".equals(msg_tp)) {
            CUR_MODE = REFUND;
        } else if ("200000".equals(transprocode) && "00".equals(conditionmode)
                && "0210".equals(msg_tp)) {
            CUR_MODE = VOID;
        } else if ("200000".equals(transprocode) && "06".equals(conditionmode)
                && "0210".equals(msg_tp)) {
            CUR_MODE = AUTH_COMP_VOID;
        } else if ("200000".equals(transprocode) && "06".equals(conditionmode)
                && "0110".equals(msg_tp)) {
            CUR_MODE = AUTH_VOID;
        } else if ("600000".equals(transprocode) && "0210".equals(msg_tp)) {
            CUR_MODE = TRANSFER_MODE;
        } else if ("620000".equals(transprocode) && "0210".equals(msg_tp)) {
            CUR_MODE = NON_TRANSFER_MODE;
        } else if ("630000".equals(transprocode) && "0210".equals(msg_tp)) {
            CUR_MODE = CASH_UP_MODE;
        } else if ("170000".equals(transprocode) && "0210".equals(msg_tp)) {
            CUR_MODE = CASH_UP_VOID_MODE;
        } else if (ReturnCodeParams.SUCESS_CODE.equals(transprocode) && "01".equals(conditionmode)
                && "0330".equals(msg_tp)) {
            CUR_MODE = OFFLINE_REFUND_MODE;
        } else if (ReturnCodeParams.SUCESS_CODE.equals(transprocode) && "00".equals(conditionmode)
                && "0330".equals(msg_tp)) {
            CUR_MODE = OFFLINE_SALE_MODE;
        } else if ("660000".equals(transprocode) && "99".equals(conditionmode)
                && "0210".equals(msg_tp)) {
            CUR_MODE = WX_SALE;
        } else if ("680000".equals(transprocode) && "99".equals(conditionmode)
                && "0210".equals(msg_tp)) {
            CUR_MODE = WX_VOID;
        } else if ("690000".equals(transprocode) && "99".equals(conditionmode)
                && "0230".equals(msg_tp)) {
            CUR_MODE = WX_REFUND;
        }

        String exVersion = Utility.getVersion();

        if (CUR_MODE != 0 && CUR_MODE != 8) { // 只在pos签购时打印
//            mList.add(getPrintItemObj("POS签购单 ", FONT_SIZE_MAX, PrintItemObj.ALIGN.CENTER));
            mList.add(getPrintItemObj("------------------------------------------------", FONT_SIZE_MIN));
        } else if (CUR_MODE == SETTLE) {
            if (isPrintDetails == null) // 打印明细的时候不打印
                mList.add(getPrintItemObj("银行卡结算总计单", FONT_SIZE_MAX, true, PrintItemObj.ALIGN.CENTER));
        }
        ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl();
        if (isPrintDetails == null) { // 打印明细的时候不打印
            // 43域，商户名称
            mList.add(getPrintItemObj("商户名称:"
                    + mParamConfigDao.get("mchntname")));
            // mList.add(getPrintItemObj("  " +
            // mParamConfigDao.get("mchntname"),
            // 20));

            // 42域，商户编号
            // mList.add(getPrintItemObj("  " + mParamConfigDao.get("merid")));

            // 41域，终端号
            // mList.add(getPrintItemObj("终端号(TERMIANL)"));
        }

        if (CUR_MODE != 0 && CUR_MODE != 8) { // 只在pos签购时打印
            mList.add(getPrintItemObj("商户编号:" + mParamConfigDao.get("unionpay_merid")));
            mList.add(getPrintItemObj("终端编号:" + mParamConfigDao.get("unionpay_termid")));
            String addresp = dataMap.get("addrespkey");
            String issuerBank = "";
            String acquBank = "新大陆";
            try {
                if (addresp != null && !"".equals(addresp) && addresp.length() > 11) {
                    issuerBank = addresp.substring(0, 11).trim();
                    acquBank = addresp.substring(11).trim();
                }
            } catch (Exception e) {
            }

            mList.add(getPrintItemObj("操作员:"
                    + CommonContants.OPERATOR + " 发卡行:" + CommonUtil.revBankName(issuerBank) + " 收单行:" + CommonUtil.revBankName(acquBank)));
//			mList.add(getPrintItemObj("收单机构:" + "拉卡拉"));
//			 mList.add(getPrintItemObj("操作员号:"
//	                    + dataMap.get("loadparams").substring(0, 2)));

            String posInputMode = dataMap.get("posInputMode");
            if (posInputMode == null || "".equals(posInputMode)) {
                if (entrymode.startsWith("01")) { // 手输
                    posInputMode = " M";
                } else if (entrymode.startsWith("02")
                        || entrymode.startsWith("03")) { // 刷卡
                    posInputMode = " S";
                } else if (entrymode.startsWith("80")) { // Fallback
                    posInputMode = " F";
                } else if (entrymode.startsWith("05")) { // 芯片卡
                    posInputMode = " I";
                } else if (entrymode.startsWith("07")
                        || entrymode.startsWith("9")) { // 非接触式
                    posInputMode = " C";
                }
            }

            if (CUR_MODE == AUTH || CUR_MODE == OFFLINE_SALE_MODE) {
                mList.add(getPrintItemObj("卡号:"));
                mList.add(getPrintItemObj(dataMap.get("priaccount")
                        + " " + posInputMode, FONT_SIZE_MAX));
            } else {
                mList.add(getPrintItemObj("卡号:"));
                mList.add(getPrintItemObj(Utility.formatCardno(dataMap.get("priaccount"))
                        + " " + posInputMode, FONT_SIZE_MAX));
            }
            String worktime = dataMap.get("expireddate");
            if ("null".equals(worktime)) {
                worktime = "";
            }
            if (!"".equals(worktime) && worktime != null) {
                if (worktime.length() >= 4) {
                    worktime = worktime.substring(0, 2) + "/"
                            + worktime.substring(2, 4);
                }
            }
            if (CUR_MODE != OFFLINE_SALE_MODE
                    && CUR_MODE != OFFLINE_REFUND_MODE) {
                String cardType = dataMap.get("settledata");
                if (!StringUtil.isEmpty(cardType) && cardType.length() > 3) {
                    cardType = CommonUtil.revCardType(cardType.substring(0, 3)) + "  ";
//                    + cardType.substring(3).trim()
                }
                mList.add(getPrintItemObj("卡类别:"
                        + cardType + "  有效期:" + worktime));
            }
//			mList.add(getPrintItemObj("有效期:" + worktime));
            // mList.add(getPrintItemObj("交易类别(TRANS TYPE)"));
            if (CUR_MODE == SALE) {
                //消费非接Q 类型都显示消费
                if ((entrymode.startsWith("07") && !(CUR_MODE == SALE))
                        || entrymode.startsWith("9")) {
                    mList.add(getPrintItemObj("交易类别:" + "电子现金消费", FONT_SIZE_NORM, true));
                } else {
                    mList.add(getPrintItemObj("交易类别:" + "消费", FONT_SIZE_NORM, true));
                }
            } else if (CUR_MODE == VOID) {
                mList.add(getPrintItemObj("交易类别:" + "消费撤销", FONT_SIZE_NORM, true));
            } else if (CUR_MODE == REFUND) {
                mList.add(getPrintItemObj("交易类别:" + "退货", FONT_SIZE_NORM, true));
            } else if (CUR_MODE == AUTH) {
                mList.add(getPrintItemObj("交易类别:" + "预授权", FONT_SIZE_NORM, true));
            } else if (CUR_MODE == AUTH_COMP) {
                mList.add(getPrintItemObj("交易类别:" + "预授权完成", FONT_SIZE_NORM, true));
            } else if (CUR_MODE == AUTH_VOID) {
                mList.add(getPrintItemObj("交易类别:" + "预授权撤销", FONT_SIZE_NORM, true));
            } else if (CUR_MODE == AUTH_COMP_VOID) {
                mList.add(getPrintItemObj("交易类别:" + "预授权完成撤销",
                        FONT_SIZE_MAX, true));
            } else if (CUR_MODE == TRANSFER_MODE) {
                mList.add(getPrintItemObj("交易类别:" + "电子现金指定账户圈存", FONT_SIZE_NORM, true));
            } else if (CUR_MODE == NON_TRANSFER_MODE) {
                mList.add(getPrintItemObj("交易类别:" + "电子现金非指定账户圈存", FONT_SIZE_NORM, true));
            } else if (CUR_MODE == CASH_UP_MODE) {
                mList.add(getPrintItemObj("交易类别:" + "电子现金现金充值", FONT_SIZE_NORM, true));
            } else if (CUR_MODE == CASH_UP_VOID_MODE) {
                mList.add(getPrintItemObj("交易类别:" + "电子现金充值撤销",
                        FONT_SIZE_MAX, true));
            } else if (CUR_MODE == OFFLINE_REFUND_MODE) {
                mList.add(getPrintItemObj("交易类别:" + "电子现金退货", FONT_SIZE_NORM, true));
            } else if (CUR_MODE == OFFLINE_SALE_MODE) {
                if (entrymode.startsWith("05")) { // 电子现金消费（读卡）
                    mList.add(getPrintItemObj("交易类别:" + "电子现金消费", FONT_SIZE_NORM, true));
                } else { // 非接电子现金消费
                    mList.add(getPrintItemObj("交易类别:" + "电子现金消费", FONT_SIZE_NORM, true));
                }
            }
        } else {
            mList.add(getPrintItemObj("商户编号:" + mParamConfigDao.get("unionpay_merid")));
            mList.add(getPrintItemObj("终端号:" + mParamConfigDao.get("unionpay_termid")));
        }

        if (isPrintDetails == null) { // 打印明细的时候不打印
            data = dataMap.get("batchbillno");
            mList.add(getPrintItemObj("批次号:" + data.substring(0, 6)));
        }

        if (CUR_MODE != 0 && CUR_MODE != 8) { // 只在pos签购时打印
            data = dataMap.get("batchbillno");
            if (data.length() < 12) {
                data = Utility.addZeroForNum(data, 12);
            }
            if (!StringUtil.isEmpty(dataMap.get("idrespcode"))) {
                mList.add(getPrintItemObj("凭证号:" + data.substring(6, 12) + " 授权号:" + dataMap.get("idrespcode")));
            } else {
                mList.add(getPrintItemObj("凭证号:" + data.substring(6, 12)));
            }
            if (CUR_MODE != WX_REFUND) {
                if (!StringUtil.isEmpty(dataMap.get("refernumber"))) {
                    mList.add(getPrintItemObj("参考号:" + dataMap.get("refernumber")));
                }
            }

        }
        if (isPrintDetails == null) { // 打印明细的时候不打印
            // mList.add(getPrintItemObj("日期/时间(DATE/TIME)"));
            String strdate = dataMap.get("translocaldate");
            String strtime = dataMap.get("translocaltime");
            if (StringUtil.isEmpty(strdate)) {
                strdate = "0000";

            }
            if (StringUtil.isEmpty(strtime)) {
                strtime = "000000";

            }
            String datetime = strdate
                    + strtime;
            datetime = Utility.printFormatDateTime(datetime);
            mList.add(getPrintItemObj("日期/时间:" + datetime));
            if (CUR_MODE == SETTLE) {
                if ("95".equals(dataMap.get("respcode"))) {
                    mList.add(getPrintItemObj("内卡对账不平\n"));
                } else if ("00".equals(dataMap.get("respcode"))) {
                    mList.add(getPrintItemObj("内卡对账平\n"));
                }
            }
        }
        if (CUR_MODE != 0 && CUR_MODE != 8) {
//			mList.add(getPrintItemObj("金额:"));
            // mList.add(getPrintItemObj("RMB",48,true));
            data = Utility.unformatMount(dataMap.get("transamount"));
            if (CUR_MODE == SALE || CUR_MODE == AUTH || CUR_MODE == AUTH_COMP
                    || CUR_MODE == TRANSFER_MODE
                    || CUR_MODE == NON_TRANSFER_MODE
                    || CUR_MODE == CASH_UP_MODE
                    || CUR_MODE == OFFLINE_SALE_MODE) {
                mList.add(getPrintItemObj("金额:", FONT_SIZE_NORM));
                mList.add(getPrintItemObj("RMB" + " " + data + "元", 20, true));
            } else if (CUR_MODE == VOID || CUR_MODE == AUTH_VOID
                    || CUR_MODE == AUTH_COMP_VOID || CUR_MODE == REFUND
                    || CUR_MODE == CASH_UP_VOID_MODE
                    || CUR_MODE == OFFLINE_REFUND_MODE) {
                mList.add(getPrintItemObj("金额:", FONT_SIZE_NORM));
                mList.add(getPrintItemObj("RMB" + " -" + data + "元", 20, true));
            }
            // mList.add(getPrintItemObj("元", 20, true));
            mList.add(getPrintItemObj("备注:", FONT_SIZE_MIN, true));
            String idAndPz = "";
            if (CUR_MODE == AUTH_VOID || CUR_MODE == AUTH_COMP_VOID
                    || CUR_MODE == AUTH_COMP) {
                idAndPz = idAndPz + "原授权号:" + dataMap.get("idrespcode");
            }
            if (CUR_MODE == VOID || CUR_MODE == AUTH_COMP_VOID
                    || CUR_MODE == OFFLINE_REFUND_MODE) {
                if (!StringUtil.isEmpty(idAndPz)) {
                    idAndPz = idAndPz + " ";
                }
                idAndPz = idAndPz + "原凭证号:"
                        + dataMap.get("batchbillno").substring(12, 18);
            }
            if (!StringUtil.isEmpty(idAndPz)) {
                mList.add(getPrintItemObj(idAndPz, FONT_SIZE_MIN));
            }
            if (CUR_MODE == NON_TRANSFER_MODE) {
                mList.add(getPrintItemObj("转入卡卡号:"
                        + dataMap.get("adddataword").substring(4), FONT_SIZE_MIN, true));
            }

            if (CUR_MODE == WX_REFUND || CUR_MODE == REFUND) {
                String batchbill = dataMap.get("batchbillno");
                String printtime = "";
                if (!StringUtil.isEmpty(batchbill) && batchbill.length() >= 22) {
                    String time = batchbill.substring(18, 22);
                    time = time.substring(0, 2) + "/"
                            + time.substring(2, 4);
                    printtime = " 原交易日期:" + time;
                }
                mList.add(getPrintItemObj("原参考号:" + dataMap.get("refernumber") + printtime, FONT_SIZE_MIN, true));
            }
            String reserve4 = dataMap.get("reserve4");
            // Log.d("printDev", "print data reserve4 = "+reserve4);
            // logger.debug("print data reserve4 = "+reserve4);

            if (StringUtil.isEmpty((exVersion))) exVersion = "";
            if (reserve4 != null && !"null".equals(reserve4)) {
                Map<String, String> map = TlvUtil.tlvToMap(reserve4);
//                if(StringUtil.isEmpty(map.get("9F06"))) map.put("9F06", "");
                if (StringUtil.isEmpty(map.get("4F"))) map.put("4F", "");
                if (StringUtil.isEmpty(map.get("9F26"))) map.put("9F26", "");
                if (StringUtil.isEmpty(map.get("9F99"))) map.put("9F99", "");
                if (StringUtil.isEmpty(map.get("95"))) map.put("95", "");
                if (StringUtil.isEmpty(map.get("9B"))) map.put("9B", "");
                if (StringUtil.isEmpty(map.get("5F34"))) map.put("5F34", "");
                if (StringUtil.isEmpty(map.get("9F36"))) map.put("9F36", "");
                if (StringUtil.isEmpty(map.get("9F37"))) map.put("9F37", "");
                if (StringUtil.isEmpty(map.get("82"))) map.put("82", "");
                if (StringUtil.isEmpty(map.get("9F33"))) map.put("9F33", "");
                if (StringUtil.isEmpty(map.get("9F10"))) map.put("9F10", "");
                if (StringUtil.isEmpty(map.get("50"))) map.put("50", "");
                if (StringUtil.isEmpty(map.get("9F12"))) map.put("9F12", "");

//				mList.add(getPrintItemObj("AID:" + (StringUtil.isEmpty(map.get("9F06"))?"":map.get("9F06"))));
                mList.add(getPrintItemObj("AID:" + (StringUtil.isEmpty(map.get("4F")) ? "" : map.get("4F"))
                                + " TVR:" + map.get("95")
                        , FONT_SIZE_MIN));
                if (CUR_MODE == OFFLINE_SALE_MODE) {
                    mList.add(getPrintItemObj("TC:" + map.get("9F26")
                                    + "  ATC:" + map.get("9F36")
                            , FONT_SIZE_MIN));
                } else {
                    mList.add(getPrintItemObj("ARQC:" + map.get("9F99")
                                    + "  ATC:" + map.get("9F36")
                            , FONT_SIZE_MIN)); // 构造tag
                    // 9F99，将arqc存放于reserve4字段中
                }
//				mList.add(getPrintItemObj("TVR:" + map.get("95")));
//				mList.add(getPrintItemObj("  ATC:" + map.get("9F36")));
                if (CUR_MODE == OFFLINE_SALE_MODE) {
                    mList.add(getPrintItemObj("CSN:" + map.get("5F34")
                                    + " UNPR NUM:" + map.get("9F37")
                            , FONT_SIZE_MIN));
                    mList.add(getPrintItemObj("AIP:" + map.get("82")
                                    + "IAD:" + map.get("9F10")
                            , FONT_SIZE_MIN));
                    mList.add(getPrintItemObj("TEMP CAP:" + map.get("9F33")
                            , FONT_SIZE_MIN));
                }
                if (CUR_MODE == OFFLINE_SALE_MODE || CUR_MODE == CASH_UP_MODE) {
                    mList.add(getPrintItemObj(
                            "卡片余额:"
                                    + Utility
                                    .unformatMount(map.get("9F79") == null ? map
                                            .get("9F5D") : map
                                            .get("9F79"))
                            , FONT_SIZE_MIN));
                }
            }
            if (CUR_MODE != OFFLINE_SALE_MODE && CUR_MODE != NON_TRANSFER_MODE) {
                String tip = dataMap.get("adddataword");
                if (!StringUtil.isEmpty(tip)) {
                    mList.add(getPrintItemObj(tip
                            , FONT_SIZE_MIN));
                }
            }
            if ("true".equals(dataMap.get("isReprints"))) {
                mList.add(getPrintItemObj("***重打印票据***", FONT_SIZE_MAX, true, PrintItemObj.ALIGN.CENTER));
            }

            if (dataMap.get("isSecond") == null) {
                String flag = ParamsUtil.getInstance().getParam("neednopin");
                String money = ParamsUtil.getInstance().getParam("nopinamount");
                try {
                    if ((entrymode.equals(TransConstans.INPUT_TYPE_QPBOC_NO_PIN) && (CUR_MODE == SALE || CUR_MODE == AUTH) && Double.valueOf(data) < Double.valueOf(money) && "1".equals(flag))) {
                        mList.add(getPrintItemObj("交易金额不足" + money + "元，无需签名", FONT_SIZE_MIN, true));
                        mList.add(getPrintItemObj("本人确认以上交易,同意将其记入本卡账户"
                                , FONT_SIZE_MIN, true));
                    } else {
                        mList.add(getPrintItemObj("持卡人签名:", FONT_SIZE_NORM));
                        mList.add(getPrintItemObj("\n\n", FONT_SIZE_MAX));
                        mList.add(getPrintItemObj("本人确认以上交易,同意将其记入本卡账户"
                                , FONT_SIZE_MIN));
                    }
                } catch (NullPointerException e) {
                    logger.debug("打印处理非接出错，需要持卡人签名" + e);
                    e.printStackTrace();
                    mList.add(getPrintItemObj("持卡人签名:", FONT_SIZE_MIN, true));
//                    mList.add(getPrintItemObj("\n\n本人确认以上交易,同意将其记入本卡账户"
//                            , FONT_SIZE_MIN, true));
                }
//                mList.add(getPrintItemObj(
//                        "I ACKNOWLEDGE SATISFACTORY RECEIPT OF RELATIVE GOODS/SERVICES"
//                        , FONT_SIZE_MIN, true));
                mList.add(getPrintItemObj("------------------------------------------------", FONT_SIZE_MIN));
//                exVersion +
                mList.add(getPrintItemObj("星POS客服热线 4006555666                商户存根", FONT_SIZE_MIN));
                mList.add(getPrintItemObj("- - - - - - - X - - - - - - - X - - - - - - -", FONT_SIZE_MIN));
            } else if ("true".equals(dataMap.get("isSecond"))) {
                mList.add(getPrintItemObj("------------------------------------------------", FONT_SIZE_MIN));
//                exVersion +
                mList.add(getPrintItemObj("星POS客服热线 4006555666               持卡人存根", FONT_SIZE_MIN));
                mList.add(getPrintItemObj("- - - - - - - X - - - - - - - X - - - - - - -", FONT_SIZE_MIN));
            }
        } else {
            if (isPrintDetails == null) {
                mList.add(getPrintItemObj("============终端结算总计（SUM TOTAL）==========="));
                mList.add(getPrintItemObj("----------------------内卡----------------------"));
                mList.add(getPrintItemObj("交易类型             总笔数            总金额"));
                String requestSettleData = dataMap.get("requestSettleData");
                if (requestSettleData != null && requestSettleData != "") {
                    //消费金额
                    String xfStr = Utility
                            .unformatMount(requestSettleData.substring(3,
                                    15));
                    String xfCount = Utility.printInteger(requestSettleData
                            .substring(0, 3));
                    //退货金额
                    String thStr = Utility
                            .unformatMount(requestSettleData.substring(18,
                                    30));
                    String thCount = Utility
                            .printInteger(requestSettleData.substring(15,
                                    18));
                    //预授权完成金额
                    String ysqStr = Utility
                            .unformatMount(requestSettleData.substring(108,
                                    120));
                    String ysqCount = Utility
                            .printInteger(requestSettleData.substring(105,
                                    108));
                    mList.add(getPrintItemObj(" 消费         "
                            + Utility.printFillSpace(xfCount
                            , 10)
                            + "   "
                            + Utility.printFillSpace(xfStr, 16)));

                    mList.add(getPrintItemObj(" 退货         "
                            + Utility.printFillSpace(thCount, 10)
                            + "   "
                            + Utility.printFillSpace(thStr, 16)));

//	                mList.add(getPrintItemObj(" 转账         "
//	                        + Utility.printFillSpace(Utility
//	                                .printInteger(requestSettleData.substring(75,
//	                                        78)), 10)
//	                        + "   "
//	                        + Utility.printFillSpace(Utility
//	                                .unformatMount(requestSettleData.substring(78,
//	                                        90)), 16)));

                    mList.add(getPrintItemObj(" 预授权完成         "
                            + Utility.printFillSpace(ysqCount, 4)
                            + "   "
                            + Utility.printFillSpace(ysqStr, 16)));
                    mList.add(getPrintItemObj(total
                            + Utility.printFillSpace((Integer.parseInt(ysqCount) - Integer.parseInt(ysqCount) + Integer.parseInt(ysqCount)) + "", 5)
                            + "   "
                            + Utility.printFillSpace(Utility.formatMountTow(Double.parseDouble(xfStr) - Double.parseDouble(thStr) + Double.parseDouble(ysqStr)) + "", 16)));
                }
                // 对账不平
                if ("95".equals(dataMap.get("respcode"))
                        && !"true".equals(dataMap.get("isReprints"))) {
                    mList.add(getPrintItemObj("============主机结算总计（SUM TOTAL）==========="));
                    mList.add(getPrintItemObj("交易类型              总笔数            总金额"));
                    String settleData = dataMap.get("settledata");
                    if (settleData != null && settleData != "") {
                        //消费金额
                        String xfStr = Utility
                                .unformatMount(settleData.substring(3,
                                        15));
                        String xfCount = Utility.printInteger(settleData
                                .substring(0, 3));
                        //退货金额
                        String thStr = Utility
                                .unformatMount(settleData.substring(18,
                                        30));
                        String thCount = Utility
                                .printInteger(settleData.substring(15,
                                        18));
                        //预授权完成金额
                        String ysqStr = Utility
                                .unformatMount(settleData.substring(108,
                                        120));
                        String ysqCount = Utility
                                .printInteger(settleData.substring(105,
                                        108));
                        mList.add(getPrintItemObj(" 消费         "
                                + Utility.printFillSpace(xfCount
                                , 10)
                                + "   "
                                + Utility.printFillSpace(xfStr, 16)));

                        mList.add(getPrintItemObj(" 退货         "
                                + Utility.printFillSpace(thCount, 10)
                                + "   "
                                + Utility.printFillSpace(thStr, 16)));

//		                mList.add(getPrintItemObj(" 转账         "
//		                        + Utility.printFillSpace(Utility
//		                                .printInteger(settleData.substring(75,
//		                                        78)), 10)
//		                        + "   "
//		                        + Utility.printFillSpace(Utility
//		                                .unformatMount(settleData.substring(78,
//		                                        90)), 16)));

                        mList.add(getPrintItemObj(" 预授权完成         "
                                + Utility.printFillSpace(ysqCount, 4)
                                + "   "
                                + Utility.printFillSpace(ysqStr, 16)));
                        mList.add(getPrintItemObj(total
                                + Utility.printFillSpace((Integer.parseInt(ysqCount) - Integer.parseInt(ysqCount) + Integer.parseInt(ysqCount)) + "", 5)
                                + "   "
                                + Utility.printFillSpace(Utility.formatMountTow(Double.parseDouble(xfStr) - Double.parseDouble(thStr) + Double.parseDouble(ysqStr)) + "", 16)));
                    }
                }
            }
            if ("true".equals(dataMap.get("isReprints"))
                    && ("".equals(isPrintDetails) || isPrintDetails == null)) {
                mList.add(getPrintItemObj("***重打印票据***", FONT_SIZE_MAX, true, PrintItemObj.ALIGN.CENTER));
            }
            // 判断是否打印细明
            if ("true".equals(isPrintDetails)
                    && !"true".equals(dataMap.get("isReprints"))) {
                // mList.add(getPrintItemObj(""));
                mList.add(getPrintItemObj("银行卡结算明细单", 20, true));
                mList.add(getPrintItemObj("交易明细单/TXN LIST"));
                mList.add(getPrintItemObj("凭证号   类型     卡号            金额    授权号"));
                mList.add(getPrintItemObj("VOUCHER  TYPE     CARD              AMT    AUTH "));
                mList.add(getPrintItemObj("------------------------------------------------"));

                // 获取交易记录表数据
                SettleDataDao mSettleDataDao = new SettleDataDaoImpl();
                List<SettleData> mSd = null;
                mSd = mSettleDataDao.getSettleData();
                if (mSd == null) {
                } else {
                    int i = 0;
                    for (SettleData mRecord : mSd) {
                        i++;
                        String transCode = mRecord.transprocode;
                        String reserve1 = mRecord.reserve1;
                        String serviceCode = mRecord.conditionmode;

                        String transtype = getTransTypeChar(transCode, serviceCode, reserve1);

                        String cardno = Utility.formatCardno(mRecord
                                .getPriaccount());
                        String mount = Utility.unformatMount(mRecord
                                .getTransamount());
                        String billno = mRecord.getBatchbillno().substring(6,
                                12);
                        String idrespcode = mRecord.getIdrespcode();
                        if ("".equals(idrespcode) || idrespcode == null
                                || "null".equals(idrespcode)) {
                            idrespcode = "";
                        }

                        mList.add(getPrintItemObj("" + billno + " "
                                + transtype + " "
                                + Utility.printFillSpace(cardno, 19) + ""));
                        mList.add(getPrintItemObj("  "
                                + Utility.printFillSpace(mount, 10) + "   "
                                + idrespcode + " "));

                    }
                }


                mSd = mSettleDataDao.getUnSuccesssFulData(); // 获取未成功上送的交易明细
                if (mSd == null) {
                    mList.add(getPrintItemObj(" "));
                } else { // 打印结算明细数据
                    mList.add(getPrintItemObj(" \n", FONT_SIZE_MAX));
                    mList.add(getPrintItemObj("脱机未成功上送/UNSUCCESSFUL LIST"));
                    mList.add(getPrintItemObj("凭证号   类型     卡号            金额    授权号"));
                    mList.add(getPrintItemObj("VOUCHER  TYPE     CARD              AMT    AUTH "));
                    mList.add(getPrintItemObj("------------------------------------------------"));
                    try {
                        mList = printDetails(mList, mSd);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (mSd != null) {
                    mSd.clear();
                    mSd = null;
                }

                mSd = mSettleDataDao.getDeniedData(); // 获取上送被拒绝的交易明细
                if (mSd == null) {
//					mList.add(getPrintItemObj(" \n"));
                } else { // 打印结算明细数据
                    try {
                        mList.add(getPrintItemObj(" \n", FONT_SIZE_MAX));
                        mList.add(getPrintItemObj("脱机上送后被平台拒绝/DENIED LIST"));
                        mList.add(getPrintItemObj("凭证号   类型     卡号            金额    授权号"));
                        mList.add(getPrintItemObj("VOUCHER  TYPE     CARD              AMT    AUTH "));
                        mList.add(getPrintItemObj("------------------------------------------------"));
                        mList = printDetails(mList, mSd);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } /*
             * else if("false".equals(isPrintDetails) &&
			 * !"true".equals(dataMap.get("isReprints"))){ //打印交易明细点击“取消”
			 * mList.add(getPrintItemObj(" \n\n")); //原本为打印3行空格，现改需求，改为不打印
			 * //modify by chenkehui @20130814 }
			 */
        }
//        mList.add(getPrintItemObj("\n\n", FONT_SIZE_MIN));
        return mList;
    }


    //重定义打印以适合各种情况
    public static PrintItemObj getPrintItemObj(String text, int fontSize, boolean isBold,
                                               PrintItemObj.ALIGN align) {
        int fontTypeDefault = PrinterConstant.FontType.FONTTYPE_S;
        int fontType;
        if (isBold) {
            fontType = PrinterConstant.FontType.FONTTYPE_S;
        } else {
            fontType = PrinterConstant.FontType.FONTTYPE_N;
        }
        switch (fontSize) {
//            case 20:
//                return new PrintItemObj(text, FONT_SIZE_NORM, fontType);
//            case 16:
//                return new PrintItemObj(text, FONT_SIZE_NORM, fontTypeDefault, align);
            default:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_W_H, PrinterConstant.FontType.FONTTYPE_N);
        }
    }

    public static PrintItemObj getPrintItemObj(String text, int fontSize, boolean isBold) {
        int fontTypeDefault = PrinterConstant.FontType.FONTTYPE_S;
        int fontType;
        if (isBold) {
            fontType = PrinterConstant.FontType.FONTTYPE_S;
        } else {
            fontType = PrinterConstant.FontType.FONTTYPE_N;
        }
        switch (fontSize) {
            case 20:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_DW_DH, PrinterConstant.FontType.FONTTYPE_N);
            case 16:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_DW_DH, PrinterConstant.FontType.FONTTYPE_S);
            case 8:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_W_H, PrinterConstant.FontType.FONTTYPE_N);
            case 4:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_W_H, PrinterConstant.FontType.FONTTYPE_S);
            default:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_W_H, PrinterConstant.FontType.FONTTYPE_N);
        }
    }

    public static PrintItemObj getPrintItemObj(String text, int fontSize) {

        switch (fontSize) {
            case 20:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_DW_DH, PrinterConstant.FontType.FONTTYPE_N);
            case 16:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_DW_DH, PrinterConstant.FontType.FONTTYPE_S);
            case 8:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_W_H, PrinterConstant.FontType.FONTTYPE_N);
            case 4:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_W_H, PrinterConstant.FontType.FONTTYPE_S);
            default:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_W_H, PrinterConstant.FontType.FONTTYPE_N);
        }
    }

    public static PrintItemObj getPrintItemObj(String text) {
        int fontTypeDefault = PrinterConstant.FontType.FONTTYPE_N;
        return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_W_H, PrinterConstant.FontType.FONTTYPE_N);
    }

    public static PrintItemObj getPrintItemObj(String text, int fontSize, PrintItemObj.ALIGN align) {
        switch (fontSize) {
            case 20:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_DW_DH, PrinterConstant.FontType.FONTTYPE_N, align);
            case 16:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_DW_DH, PrinterConstant.FontType.FONTTYPE_S, align);
            case 8:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_W_H, PrinterConstant.FontType.FONTTYPE_N, align);
            case 4:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_W_H, PrinterConstant.FontType.FONTTYPE_S, align);
            default:
                return new PrintItemObj(text, PrinterConstant.FontScale.FONTSCALE_W_H, PrinterConstant.FontType.FONTTYPE_N, align);
        }
    }

    /**
     * 扫码交易
     *
     * @param transRecord
     * @param context
     * @return
     */
    public static List<PrintItemObj> getWxScanPrintItemObj(ScanTransRecord transRecord, Context context) {
        if (transRecord == null) {
            return new ArrayList<PrintItemObj>();
        }
        List<PrintItemObj> mList = new ArrayList<PrintItemObj>();
//        for (Map.Entry<String, String> entry : map.entrySet()) {
////            System.out.println("key= " + entry.getKey() + " and value= "
////                    + entry.getValue());
//            Log.d("打印集合数据", "key= " + entry.getKey() + " and value= "
//                    + entry.getValue());
//        }
//        mList.add(getPrintItemObj("POS收款凭证", FONT_SIZE_MAX, PrintItemObj.ALIGN.CENTER));
        mList.add(getPrintItemObj("------------------------------------------------", FONT_SIZE_MIN));
        mList.add(getPrintItemObj("商户名称：" + transRecord.getMemberName(), FONT_SIZE_NORM));
//        mList.add(getPrintItemObj("商户编号：" + transRecord.getMemberId(), FONT_SIZE_NORM));
        mList.add(getPrintItemObj("终端编号：" + transRecord.getTerminalId() + "  操作员号:" + transRecord.getOper(), FONT_SIZE_NORM));
//        mList.add(getPrintItemObj("交易类型: " + transRecord.getTransType(), FONT_SIZE_NORM));
        mList.add(getPrintItemObj("交易类型: " + getPayChannelStr(String.valueOf(transRecord.getTransType())), FONT_SIZE_NORM));
        mList.add(getPrintItemObj("批次号: " + transRecord.getBatchbillno() + "  凭证号: " + transRecord.getSystraceno(), FONT_SIZE_NORM));
        mList.add(getPrintItemObj("订单号  " + transRecord.getOrderNo(), FONT_SIZE_NORM));
//        mList.add(getPrintItemObj("支付类型: " + getPayTypeStr(transRecord.getPayType()), FONT_SIZE_NORM));
//        mList.add(getPrintItemObj("交易结果状态码  " + transRecord.getStatuscode(), FONT_SIZE_NORM));
//        mList.add(getPrintItemObj("POS流水号: " + transRecord.getSystraceno(), FONT_SIZE_NORM));
//        mList.add(getPrintItemObj("系统流水号: " + transRecord.getLogNo(), FONT_SIZE_NORM));
        String scanDate = transRecord.getScanDate();
        String scanTime = transRecord.getScanTime();
        Log.d("交易Date", "日期" + scanDate + "  时间" + scanTime);
        mList.add(getPrintItemObj("日期/时间  " + DateTimeUtil.getCurrentDate(DateTimeUtil.YYYY) + "/" + scanDate + "  " + scanTime, FONT_SIZE_NORM));
        mList.add(getPrintItemObj("交易金额  RMB " + (TextUtils.isEmpty(transRecord.getTransamount()) ? FormatUtils.formatMount(transRecord.getTotalAmount()) : FormatUtils.formatMount(transRecord.getTransamount())), FONT_SIZE_NORM));
//        mList.add(getPrintItemObj("应答码  " + transRecord.getRespcode()));
//        mList.add(getPrintItemObj("结算信息  " + transRecord.get("settledata")));
//        mList.add(getPrintItemObj("操作员  " + transRecord.get("oper")));
//        mList.add(getPrintItemObj("交易货币代码  " + transRecord.get("transcurrcode")));
//        mList.add(getPrintItemObj("是否被撤销  " + transRecord.get("isrevoke")));
        mList.add(getPrintItemObj("附加信息  " + transRecord.getAdddataword() != null ? transRecord.getAdddataword() : "", FONT_SIZE_NORM));
//        mList.add(getPrintItemObj("交易处理码  " + transRecord.get("transprocode")));
//        mList.add(getPrintItemObj("授权码  " + transRecord.get("authCode")));
        mList.add(getPrintItemObj("------------------------------------------------", FONT_SIZE_MIN));
        mList.add(getPrintItemObj("星POS客服热线 4006555666                商户存根", FONT_SIZE_MIN));
        mList.add(getPrintItemObj("- - - - - - - X - - - - - - - X - - - - - - -", FONT_SIZE_MIN));
        mList.add(getPrintItemObj("\n\n\n"));
        return mList;
    }

    /**
     * 扫码结算打印
     *
     * @param ssr
     * @param context
     * @return
     */
    public static List<PrintItemObj> getScanJsPrintItemObj(ScanSettleRes ssr, Context context) {
        if (ssr == null) {
            return new ArrayList<PrintItemObj>();
        }
        Log.d("ScanSettleRes金额", "PhWeiXinCnt==>" + ssr.getPhWeiXinCnt() + "  PosWeiXinCnt" + ssr.getPosWeiXinCnt() + "  PhWeiXinAmt" + ssr.getPhWeiXinAmt() + "  PosWeiXinAmt" + ssr.getPosWeiXinAmt());
        String MercNm = "";
        String PayMercId = "";
        String TerminalNo = "";
        String operatorcode = "";
        String scan_batchno = "";
        String params_settle_time = "";
        try {
            MercNm = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME);//商户名称
            PayMercId = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID);//商户编号
            TerminalNo = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID);//终端号
            operatorcode = ScanParamsUtil.getInstance().getParam("operatorcode"); //操作员
            scan_batchno = ScanParamsUtil.getInstance().getParam(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO);//批次号
            scan_batchno = String.format("%06d", (((Integer.valueOf(scan_batchno) - 1)) % 1000000));
            params_settle_time = ShareScanPreferenceUtils.getString(context, TransParamsValue.SettleConts.PARAMS_SETTLE_TIME, ""); //日期时间
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<PrintItemObj> mList = new ArrayList<PrintItemObj>();

        mList.add(getPrintItemObj("POS结算总计单", FONT_SIZE_MAX, PrintItemObj.ALIGN.CENTER));
        mList.add(getPrintItemObj("商户名称：" + MercNm, FONT_SIZE_NORM));
        mList.add(getPrintItemObj("商户编号：" + PayMercId, FONT_SIZE_NORM));
        mList.add(getPrintItemObj("终端编号：" + TerminalNo + " 操作员号：" + operatorcode, FONT_SIZE_NORM));
        mList.add(getPrintItemObj("批次号：" + scan_batchno, FONT_SIZE_NORM));
        mList.add(getPrintItemObj("日期时间：" + params_settle_time, FONT_SIZE_NORM));
        mList.add(getPrintItemObj("类型/TYPE  笔数/SUM  金额/AMOUNT"));
        mList.add(getPrintItemObj(getChkRspCodStr(ssr.getChkRspCod())));
        mList.add(getPrintItemObj("扫码入账总计"));
        int weixinCnt = Integer.valueOf(ssr.getPhWeiXinCnt()) + Integer.valueOf(ssr.getPosWeiXinCnt());
        double weixinAmtDou = (Double.valueOf(ssr.getPhWeiXinAmt()) + Double.valueOf(ssr.getPosWeiXinAmt()));
        mList.add(getPrintItemObj("微信" + Utility.strFillSpace(String.valueOf(weixinCnt), 15 - Utility.length("微信"), true) +
                "       " + Utility.formatMountTow(weixinAmtDou)));
        int AlipayCnt = Integer.valueOf(ssr.getPhAlipayCnt()) + Integer.valueOf(ssr.getPosAlipayCnt());
        double AlipayAmtDou = (Double.valueOf(ssr.getPhAlipayAmt()) + Double.valueOf(ssr.getPosAlipayAmt()));
        mList.add(getPrintItemObj("支付宝" + Utility.strFillSpace(String.valueOf(AlipayCnt), 15 - Utility.length("支付宝"), true) +
                "       " + Utility.formatMountTow(AlipayAmtDou)));
        int tuihuoCnt = Integer.valueOf(ssr.getRefundCount());
        double tuihuoAmt = Double.valueOf(ssr.getRefundAmount());
        double totalAmt = (weixinAmtDou + AlipayAmtDou) - tuihuoAmt;
        mList.add(getPrintItemObj("扫码退货" + Utility.strFillSpace(ssr.getRefundCount(), 15 - Utility.length("扫码退货"), true) +
                "       " + Utility.formatMountTow(tuihuoAmt)));
        mList.add(getPrintItemObj("总计" + Utility.strFillSpace(String.valueOf((weixinCnt + AlipayCnt + tuihuoCnt)), 15 - Utility.length("总计"), true) +
                "       " + Utility.formatMountTow(totalAmt)));
        mList.add(getPrintItemObj("\n\n"));
        mList.add(getPrintItemObj("- - - - - - - X - - - - - - - X - - - - - - - ", FONT_SIZE_MIN));
        return mList;
    }

    public static String getPayChannelStr(String payChannel) {
        //payChannel 1：支付宝，2：微信；9：银联
        switch (payChannel) {
            case "1":
            case "ALIPAY":
            case "68":
            case "69":
                return "支付宝支付/ALIPAY";
            case "2":
            case "WXPAY":
            case "66":
            case "67":
                return "微信支付/WEIXIN PAY";
            case "9":
                return "银联支付";
            case "REFUND":
            case "73":
                return "退款";
            default:
                return "未知";
        }
    }

    private static String getPayTypeStr(String payType) {
        // payType 1.条码支付,2.声波支付,3.二维码支付, 4.线上支付
        switch (payType) {
            case "1":
                return "条码支付";
            case "2":
                return "声波支付";
            case "3":
                return "二维码支付";
            case "4":
                return "线上支付";
            default:
                return "未知";
        }
    }

    private static String getChkRspCodStr(String type) {
        switch (type) {
            case "1":
                return "对账平";
            case "2":
                return "对账不平";
            case "3":
                return "出错";
            default:
                return "";
        }
    }
}
