package com.nld.starpos.banktrade.utils;

import android.content.Context;
import android.text.TextUtils;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.pinpad.AidlPinpad;
import com.nld.cloudpos.aidl.pinpad.TusnData;
import com.nld.cloudpos.data.PinpadConstant;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.BuildConfig;
import com.nld.starpos.banktrade.db.SettleDataDao;
import com.nld.starpos.banktrade.db.bean.Reverse;
import com.nld.starpos.banktrade.db.bean.ScriptNotity;
import com.nld.starpos.banktrade.db.bean.SettleData;
import com.nld.starpos.banktrade.db.bean.TransRecord;
import com.nld.starpos.banktrade.db.local.SettleDataDaoImpl;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.HexUtil;
import common.StringUtil;
import common.Utility;

/**
 * 此处进行交易组包，每个方法返回Map<String,String>，该Map直接作为8583组包参数进行组包。
 *
 * @author Administrator
 */
public class TransactionPackageUtil {
    public static String mesauthcode = "0000000100000010000000110000010000000101000001100000011100001000"; // 临时使用12345678的二进制字符串表示，报文计算时会替换
    public static String head = "603103000000";
    //组包所需的所有的key
    private static  String[] filedKeys = {"headerdata","msg_tp","priaccount","transprocode","transamount","systraceno","expireddate",
            "entrymode","seqnumber", "conditionmode","updatecode","track2data","track3data","refernumber","idrespcode","respcode","terminalid",
            "acceptoridcode", "acceptoridname","adddataword","transcurrcode","pindata","secctrlinfo","icdata","loadparams","adddatapri","field59",
            "predealinfo","batchbillno","settledata", "mesauthcode","pincapturecode","udf_fld","opearcode","respcode","transtype","translocaltime",
            "translocaldate","receivemark"};

    /**
     * 签到组包
     * @param type 07:表示银联
     * @return
     */
    public static Map<String, String> getSignParam(String type) {
        Map<String, String> dataMap = new HashMap<String, String>();
        String billNo = TransParamsUtil.getBillNo(); //获取流水号
        if (null != type && type.equals("07")) {
            dataMap.put(filedKeys[0], ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP) + head);
            dataMap.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
            dataMap.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
            String batchno = ParamsUtil.getInstance().getParam(ParamsConts.TransParamsContns.TYANS_BATCHNO);
            dataMap.put(filedKeys[24],getLoadParams(Cache.getInstance().getTransCode(),batchno));  //60域的拼接<交易类型码+批次号+加密方式>
            dataMap.put(filedKeys[29], "000");
            dataMap.put(filedKeys[1], getMessageType(Cache.getInstance().getTransCode()));
            if (!StringUtil.isEmpty(billNo)){
                dataMap.put(filedKeys[5], Utility.addZeroForNum(billNo, 6));
            }
        }
        return dataMap;
    }
    /**
     * 参数下载
     * @param context
     * @return
     */
    // TODO: 2017/11/24
    public static Map<String, String> getParamDown(Context context) {
        Map<String, String> paramdownMap = new HashMap<String, String>();
        paramdownMap.put(filedKeys[0], ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP) + head);
        String batchNo = TransParamsUtil.getCurrentBatchNo();// 批次号
        paramdownMap.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
        paramdownMap.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
        paramdownMap.put(filedKeys[24],getLoadParams(Cache.getInstance().getTransCode(),batchNo));// 60域交易类型码批次号信息码
        paramdownMap.put(filedKeys[32], ParamsConts.NO_MAC_PARAMS);
        paramdownMap.put(filedKeys[1],getMessageType(Cache.getInstance().getTransCode()));
        return paramdownMap;
    }


    /**
     *（IC卡公钥查询，公钥下载，参数查询，参数下载）组包
     * @return
     */
    public static Map<String, String> getICCaDownInfo(Context context, String opearcode,String transCode) {
        Map<String, String> paramdownMap = new HashMap<String, String>();
        paramdownMap.put(filedKeys[32], ParamsConts.NO_MAC_PARAMS);
        paramdownMap.put(filedKeys[0], ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP) + head);
        String batchNo = TransParamsUtil.getCurrentBatchNo();// 批次号
        paramdownMap.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
        paramdownMap.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
        if (!StringUtil.isEmpty(transCode)){
            paramdownMap.put(filedKeys[24], getLoadParams(transCode,batchNo));// 60域交易类型码批次号信息码
            if (StringUtil.isEmpty(opearcode)){
                paramdownMap.put(filedKeys[33], getOperCode(transCode)); //62域
            }else {
                LogUtils.i("IC卡参数下载：" + opearcode);
                paramdownMap.put(filedKeys[33], opearcode); //62域
            }
            paramdownMap.put(filedKeys[1],getMessageType(transCode));
        }
        return paramdownMap;
    }


    /**
     * 获取(消费,余额查询,消费撤销 )参数包
     * @return
     */
    public static Map<String, String> getConsumeParam(Context context,AidlDeviceService aidlService) {

        Map<String, String> dataMap = new HashMap<String, String>();
        String billNo = TransParamsUtil.getBillNo();
        String tpdu = ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP);
        if (!StringUtil.isEmpty(tpdu)){
            dataMap.put(filedKeys[0], tpdu + head);
        }
        String inputCode = Cache.getInstance().getSerInputCode();
        if (!StringUtil.isEmpty(inputCode)){
            dataMap.put(filedKeys[7], inputCode); // 22域 服务点输入方式码
        }
        String cardNo = Cache.getInstance().getCardNo();
        if (!StringUtil.isEmpty(cardNo)){
            dataMap.put(filedKeys[2], cardNo); // 主账号
        }
        String date = Cache.getInstance().getInvalidDate();       //卡有效期
        if (!StringUtil.isEmpty(date)) {
            dataMap.put(filedKeys[6], date);
        }
        String batchStr = TransParamsUtil.getCurrentBatchNo();
        String transCode = Cache.getInstance().getTransCode();
        if (!StringUtil.isEmpty(transCode)){
            dataMap.put(filedKeys[1], getMessageType(transCode));        //交易类型
            dataMap.put(filedKeys[3], transProcodeType(transCode)); // 交易处理码
            dataMap.put(filedKeys[24],getLoadParams(transCode,batchStr)); // 60域
            dataMap.put(filedKeys[28], getBillNo(transCode,billNo,context)); // 当前交易信息(批次号+ 流水号+此次退款的原始交易信息)
            if (TransConstans.TRANS_CODE_CONSUME.equals(transCode)){
                dataMap.put(filedKeys[4],Utility.formatMount(Cache.getInstance().getTransMoney())); // 交易金额
                String field59 = getField59(aidlService); //59域值
                if (!StringUtil.isEmpty(field59)) {
                    dataMap.put(filedKeys[26], field59);
                }
            }else if (TransConstans.TRANS_CODE_CONSUME_CX.equals(transCode)){  //消费撤销
                TransRecord mTransRecord = Cache.getInstance().getTransRecord();
                dataMap.put(filedKeys[4], mTransRecord.getTransamount()); // 原交易金额
                dataMap.put(filedKeys[13], mTransRecord.getRefernumber()); //内部交易流水号《检索号》
                dataMap.put(filedKeys[27], mTransRecord.getBatchbillno());       // 61域 原始交易信息<原交易批次号+原交易流水号>
                if (!StringUtil.isEmpty(mTransRecord.getIdrespcode())) {  //原交易授权码
                    dataMap.put(filedKeys[14], mTransRecord.getIdrespcode());
                }
            }
        }
        if (Cache.getInstance().getHasPin()) {  //当带有pin的时候才需要安全控制信息,即22域存在情况下
            dataMap.put(filedKeys[21], Cache.getInstance().getPinBlock());  //个人标识码
            dataMap.put(filedKeys[22], getMessureSecurity(transCode)); // 安全控制信息<当磁道加密时 35域和36域至少存在一个> 53域安全控制中心<由带主账号的pin加密方式(2)+加密算法标志(6)+磁道加密标志(1)+13位0组合而成,定长16字节>
        }
        dataMap.put(filedKeys[5], Utility.addZeroForNum(billNo, 6)); // Pos流水号
        dataMap.put(filedKeys[9], "00");  //服务条件码 25域
        dataMap.put(filedKeys[31], "12");  //服务点pin 获取码 26域 <只有当22域存在且带有pin的时候才存在,即是密码的最长长度占用2字节>
        dataMap.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
        dataMap.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
        dataMap.put(filedKeys[20], "156"); // 货币代码
        dataMap.put(filedKeys[30], mesauthcode); // 64域校验码
        dataMap.put(filedKeys[32],  ParamsConts.MAC_PARAMS); // 判断是否需要计算mac,非“00”开头需要计算
       // 二磁道信息
        String trace2 = Cache.getInstance().getTrack_2_data();
        if (!StringUtil.isEmpty(trace2)) {
            trace2 = trace2.replace("=", "D");
            if (!TransConstans.TRANS_CODE_CONSUME_CX.equals(transCode)){  //消费撤销
                String encTrace2data = encField35(trace2,aidlService);
                dataMap.put(filedKeys[11], encTrace2data);
            }else {
                dataMap.put(filedKeys[11], trace2);
            }
        }
        // 三磁道信息
        String trace3 = Cache.getInstance().getTrack_3_data();
        if (!StringUtil.isEmpty(trace3)) {
            trace3 = trace3.replace("=", "D");
            dataMap.put(filedKeys[12], trace3);
        }
        // IC卡55域
        if (Cache.getInstance().getSerInputCode().startsWith(ParamsConts.SWING_CARD)
                || Cache.getInstance().getSerInputCode().startsWith(ParamsConts.SWIP_CARD)
                || Cache.getInstance().getSerInputCode().startsWith(ParamsConts.SWING_ALL_CARD)) { // 表示IC卡交易
            if (!TransConstans.TRANS_CODE_CONSUME_CX.equals(transCode)){
                String tag55 = Cache.getInstance().getTlvTag55();
                dataMap.put(filedKeys[23], tag55);   //IC卡数据域
            }
            // IC卡号时，卡片序列号
            dataMap.put(filedKeys[8], Cache.getInstance().getCardSeqNo());
        }
        LogUtils.d("(消费/余额查询/消费撤销)Map报文：" + dataMap.toString());
        return dataMap;
    }

    /**
     * 获取(预授权,预授权完成，预授权撤销，预授权完成撤销)集合组包
     * @param context
     * @param deviceService
     * @return
     */
    public  static Map<String,String> getPreAuthCommonParams(Context context, AidlDeviceService deviceService){

        Map<String, String> dataMap = new HashMap<String, String>();
        String tpdu = ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP) + head;
        dataMap.put(filedKeys[0], tpdu);  //报文头
        dataMap.put(filedKeys[30], mesauthcode); //mac检验码                                   ------>64域
        dataMap.put(filedKeys[32], ParamsConts.MAC_PARAMS); //计算mac
        String billNo = TransParamsUtil.getBillNo();
        String transCode = Cache.getInstance().getTransCode();
        String currentBatchNo = TransParamsUtil.getCurrentBatchNo();
        if (!StringUtil.isEmpty(transCode)){
            dataMap.put(filedKeys[3], transProcodeType(transCode));//交易处理码             ---------> 域3
            dataMap.put(filedKeys[1], getMessageType(transCode));  //消息类型
            dataMap.put(filedKeys[4], getPreAmount(transCode)); // 交易金额                  -------->域4
            dataMap.put(filedKeys[16],getTermid(transCode,context)); // 终端号                 --------->域41
            dataMap.put(filedKeys[17],getMerId(transCode,context)); // 商户编号            --------->域42
            dataMap.put(filedKeys[28], getBillNo(transCode,billNo,context)); //凭证号
            dataMap.put(filedKeys[24], getLoadParams(transCode,currentBatchNo)); //           ---------->60域组合数据
        }
        String inputCode = Cache.getInstance().getSerInputCode();
        dataMap.put(filedKeys[7], inputCode); // pos输入方式                                   --------->22域
        if (!StringUtil.isEmpty(inputCode) && !inputCode.startsWith(ParamsConts.INSERT_CARD)){
            // 主账号
            if (!StringUtil.isEmpty(Cache.getInstance().getCardNo())){
                dataMap.put(filedKeys[2], Cache.getInstance().getCardNo());  //主账号                -------->域2
            }
        }
        dataMap.put(filedKeys[5], Utility.addZeroForNum(billNo, 6)); // Pos流水号               ------>域11
        dataMap.put(filedKeys[20], "156"); // 货币代码                                       ------->域49
        // 卡有效期(贷记卡手输卡号时输入)                                                          ------>域14
        if (!StringUtil.isEmpty(Cache.getInstance().getInvalidDate())) {
            dataMap.put(filedKeys[6], Cache.getInstance().getInvalidDate());
        }
        dataMap.put(filedKeys[9], "06"); //  服务条件码                                      -------->25域
        if (Cache.getInstance().getHasPin()) {
            dataMap.put(filedKeys[31], "12");  //pin码                                         ------>域26
            dataMap.put(filedKeys[21], Cache.getInstance().getPinBlock()); //个人密码                 ---------->域52
            dataMap.put(filedKeys[22], getMessureSecurity(transCode)); // 安全控制信息 <需要磁道加密>       ---------->域53
        }
        // 二磁道信息                                                                               ------>域35
        String trace2 = Cache.getInstance().getTrack_2_data();
        if (!StringUtil.isEmpty(trace2)) {
            trace2 = trace2.replace("=", "D");
            String encTrace2data = encField35(trace2,deviceService);
            dataMap.put(filedKeys[11], encTrace2data);
        }
        // 三磁道信息                                                                               ----->域36
        String trace3 = Cache.getInstance().getTrack_3_data();
        if (!StringUtil.isEmpty(trace3)) {
            trace3 = trace3.replace("=", "D");
            dataMap.put(filedKeys[12], trace3);
        }
        // IC卡
        if (Cache.getInstance().getSerInputCode().startsWith(ParamsConts.SWING_CARD)
                || Cache.getInstance().getSerInputCode().startsWith(ParamsConts.SWIP_CARD)
                || Cache.getInstance().getSerInputCode().startsWith(ParamsConts.SWING_ALL_CARD)) {// 表示IC卡交易
            if (!StringUtil.isEmpty(transCode)){
                if (transCode.equals(TransConstans.TRANS_CODE_PRE)){  //预授权
                    String tag55 = Cache.getInstance().getTlvTag55();
                    dataMap.put(filedKeys[23], tag55);  //                                                ------>域55
                }
            }
            // IC卡号时，卡片序列号                                                                    ------>域23
            dataMap.put(filedKeys[8], Cache.getInstance().getCardSeqNo());
        }

        if (!StringUtil.isEmpty(transCode)){
            if (transCode.equals(TransConstans.TRANS_CODE_PRE_COMPLET)){  //预授权完成
                dataMap.put(filedKeys[14], Cache.getInstance().getAuthCode());  //授权码              --------->域38
                String transDate = Cache.getInstance().getTransDate();
                String oldTrans = "000000" + "000000";
                if (StringUtil.isEmpty(transDate)){
                    oldTrans += "0000";
                }else {
                    oldTrans += transDate;
                }
                dataMap.put(filedKeys[27], oldTrans);   //原始交易           ------------> 域62组合数据
            }else if (transCode.equals(TransConstans.TRANS_CODE_PRE_CX)){  //预授权撤销
                TransRecord mTransRecord = Cache.getInstance().getTransRecord();
                if (null == mTransRecord){
                    LogUtils.e("组包失败");
                    return null;
                }
                String oldBatchNo = mTransRecord.getBatchbillno();
                String oldTranslocaldate = mTransRecord.getTranslocaldate();
                String oldTrans = "";
                if (StringUtil.isEmpty(oldBatchNo)){
                    oldTrans = "000000"+"000000";
                }else {
                    oldTrans = oldBatchNo;
                }
                if (!StringUtil.isEmpty(oldTranslocaldate)){
                    oldTrans += oldTranslocaldate;
                }else {
                    oldTrans += "0000";
                }
                dataMap.put(filedKeys[27], oldTrans);
                dataMap.put(filedKeys[14], mTransRecord.getIdrespcode());
            }else if (transCode.equals(TransConstans.TRANS_CODE_PRE_COMPLET_CX)){  //预授权完成撤销
                TransRecord mTransRecord = Cache.getInstance().getTransRecord();
                if (null == mTransRecord){
                    LogUtils.e("组包失败");
                    return null;
                }
                dataMap.put(filedKeys[13], mTransRecord.getRefernumber()); //检索参考号                      ------->37域
                dataMap.put(filedKeys[14], mTransRecord.getIdrespcode());   //授权码
                dataMap.put(filedKeys[27], mTransRecord.getBatchbillno() + mTransRecord.getTranslocaldate());
            }
        }

        /*****TO  DO*******/
   /*     String info = SystemInfoDev.getInstance(context, deviceService).getManufacture()
                + SystemInfoDev.getInstance(context, deviceService).getModel()
                + CommonUtil.getAppVersion();
        info = Utility.addSpaceForStr(info, 18);
        info = info + Utility.getGSMCellLocationInfo(context);*/
        //      dataMap.put("adddatapri",info ); // 其他终端信息                                              ------->域57
        LogUtils.d("预授权"+transCode+"报文Map报文：" + dataMap.toString());
        return dataMap;
    }

    /**
     * 冲正Map
     * @param context
     * @param mReverse
     * @return
     */
    public static Map<String, String> getReverseInfo(Context context, Reverse mReverse) {
        Map<String, String> infoMap = new HashMap<String, String>();
        infoMap.put(filedKeys[1], getMessageType(TransConstans.TRANS_CODE_REVERSE)); // 交易类型
        infoMap.put(filedKeys[0], ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP) + head);
        infoMap.put(filedKeys[2], mReverse.priaccount);
        infoMap.put(filedKeys[3], mReverse.transprocode);
        infoMap.put(filedKeys[4], mReverse.transamount);
        infoMap.put(filedKeys[5], mReverse.systraceno);
        infoMap.put(filedKeys[6], mReverse.expireddate);
        infoMap.put(filedKeys[7], mReverse.entrymode);
        infoMap.put(filedKeys[8], mReverse.seqnumber);
        infoMap.put(filedKeys[9], mReverse.conditionmode);
        if (Cache.getInstance().getReserverCode() != null ){
            infoMap.put(filedKeys[34], Cache.getInstance().getReserverCode());
        }else {
            infoMap.put(filedKeys[34], "06");
        }
        infoMap.put(filedKeys[16], mReverse.terminalid);
        infoMap.put(filedKeys[17], mReverse.acceptoridcode);
        infoMap.put(filedKeys[20], mReverse.transcurrcode);
        infoMap.put(filedKeys[23], mReverse.icdata); // ic卡数据域
        infoMap.put(filedKeys[24], mReverse.loadparams);
        if ("200000".equals(mReverse.transprocode) || "000000".equals(mReverse.transprocode)) {
            infoMap.put(filedKeys[27], mReverse.batchbillno.substring(0, 6) + mReverse.systraceno);
            LogUtils.d("qqsd 61域内容:" + mReverse.batchbillno.substring(0, 6) + mReverse.systraceno);
            if (!StringUtil.isEmpty(mReverse.idrespcode)) { //预授权撤销 38域内容
                infoMap.put(filedKeys[14], mReverse.idrespcode);
                LogUtils.d("qqsd 38域内容:" + mReverse.idrespcode);
            }
        }
        infoMap.put(filedKeys[30], mesauthcode); // 64域校验码
        infoMap.put(filedKeys[32],  ParamsConts.MAC_PARAMS);
        return infoMap;
    }

    /**
     * 获取批上送IC卡交易
     * @param context
     * @param transRecord
     * @return
     */
    public static Map<String, String> getBatchSendICInfo(Context context, TransRecord transRecord) {
        Map<String, String> dataMap = new HashMap<>();
        String tpdu = ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP);
        dataMap.put(filedKeys[0], tpdu + head);
        dataMap.put(filedKeys[2], transRecord.getPriaccount());        // 主账号
        dataMap.put(filedKeys[4], transRecord.getTransamount()); // 交易金额
        dataMap.put(filedKeys[5], transRecord.getSystraceno()); // Pos流水号
        dataMap.put(filedKeys[7], transRecord.getEntrymode()); // 22域
        if (transRecord.getSeqnumber() != null) {
            dataMap.put(filedKeys[8], transRecord.getSeqnumber());
        }
        dataMap.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
        dataMap.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
        dataMap.put(filedKeys[23], transRecord.getIcdata());
        String batchStr = TransParamsUtil.getCurrentBatchNo();
        dataMap.put(filedKeys[24], getLoadParams(TransConstans.TRANS_CODE_BATCH_SEND_IC,batchStr)); // TODO: 2017/11/24
        String batchbillno = "0000" + Utility.formatMount(transRecord.getTransamount()) + "156";
        dataMap.put(filedKeys[28], batchbillno);
        dataMap.put(filedKeys[32], ParamsConts.NO_MAC_PARAMS);
        dataMap.put(filedKeys[1], getMessageType(TransConstans.TRANS_CODE_BATCH_SEND_IC));
        return dataMap;
    }

    /**
     * 结算
     * @param context
     * @return
     */
    public static Map<String, String> getSettlementInfo(Context context) {
        Map<String, String> infoMap = new HashMap<String, String>();
        infoMap.put(filedKeys[1], getMessageType(TransConstans.TRANS_CODE_SIGN_JS));  //交易类型
        String billNo = TransParamsUtil.getBillNo();// 流水号
        String batchNo = TransParamsUtil.getCurrentBatchNo();// 批次号
        infoMap.put(filedKeys[0], ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP) + head);

        infoMap.put(filedKeys[5], Utility.addZeroForNum(billNo, 6)); // 11域Pos流水号
        infoMap.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
        infoMap.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
        infoMap.put(filedKeys[19], getSettledata(context)); // 48域
        infoMap.put(filedKeys[20], "156"); // 49货币代码

        infoMap.put(filedKeys[35], getLoadParams(TransConstans.TRANS_CODE_SIGN_JS,batchNo));// 60域交易类型码批次号信息码
  //      infoMap.put("opearcode", "00");// 63域操作员代码 // TODO: 2017/11/23
        infoMap.put(filedKeys[33], "001");// 63域操作员代码

        infoMap.put(filedKeys[32], ParamsConts.NO_MAC_PARAMS);
        LogUtils.i("结算Map：" + infoMap);
        return infoMap;
    }
    /**
     * 批上送金融交易（磁卡消费）报文
     * @param context
     * @param unBatchMagCard
     * @param type 1:磁条批上送   2 批上送结束
     * @return
     */
    public static Map<String, String> getBatchSendEndInfo(Context context, List<TransRecord> unBatchMagCard, int type) {
        Map<String, String> mapInfo = new HashMap<>();

        String batchNo = TransParamsUtil.getCurrentBatchNo();// 批次号
        switch (type) {
            case 1:
                if (unBatchMagCard == null || unBatchMagCard.size() == 0) {
                    LogUtils.d("没有刷卡未上送数据");
                    return mapInfo;
                }
                mapInfo.put(filedKeys[24], getLoadParams(TransConstans.TRANS_CODE_BATCH_SEND_END,batchNo));
                StringBuffer sb = new StringBuffer();
                sb.append(Utility.addZeroForNum(unBatchMagCard.size() + "", 2));
                for (TransRecord transRecord : unBatchMagCard) {  //针对磁条卡的批上送的私人信息定义 《内卡交易[00]+pos流水号+卡号+交易金额》
                    sb.append("00").append(Utility.addZeroForNum(transRecord.getSystraceno(), 6)).append(Utility.addZeroForNum(transRecord.getPriaccount(), 20)).append(Utility.addZeroForNum(transRecord.getTransamount(), 12));
                }
                sb.insert(0, Utility.addZeroForNum(sb.length() + "", 3));
                String adddataword = sb.toString();
                mapInfo.put(filedKeys[19], adddataword);

                break;
            case 2:
                mapInfo.put(filedKeys[24], "00" + batchNo + "202");
                // 批上送成功总数
                String adddataword1 = new TransRecordDaoImpl().getTransCountByKV("reserve5", "5");
                mapInfo.put(filedKeys[19], Utility.addZeroForNum(adddataword1, 4));
                break;
            default:
                break;
        }
        mapInfo.put(filedKeys[0], ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP) + head);
        String billNo = TransParamsUtil.getBillNo();// 流水号
        mapInfo.put(filedKeys[5], Utility.addZeroForNum(billNo, 6)); // Pos流水号
        mapInfo.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
        mapInfo.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
        mapInfo.put(filedKeys[32], ParamsConts.NO_MAC_PARAMS);
        mapInfo.put(filedKeys[1], "0320");
        return mapInfo;
    }
    /**
     * 获取脚本通知Map
     * @param mSN 数据库脚本记录
     * @return
     */
    public static Map<String, String> getScriptNotityInfo(Context context, ScriptNotity mSN) {
        Map<String, String> infoMap = new HashMap<String, String>();
        String billNo = TransParamsUtil.getBillNo();// 流水号
        String batchNo = TransParamsUtil.getCurrentBatchNo();// 批次号
        infoMap.put(filedKeys[0], ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP) + head);
        infoMap.put(filedKeys[2], mSN.priaccount);
        infoMap.put(filedKeys[3], mSN.transprocode);
        if (mSN.transamount != null && !mSN.transamount.equals("")) {
            infoMap.put(filedKeys[4], mSN.transamount);
        }
        infoMap.put(filedKeys[5], Utility.addZeroForNum(billNo, 6));
        infoMap.put(filedKeys[37], mSN.translocaldate);
        infoMap.put(filedKeys[7], mSN.entrymode);
        infoMap.put(filedKeys[9], mSN.conditionmode);
        infoMap.put(filedKeys[8], mSN.seqnumber);
        infoMap.put(filedKeys[38], mSN.reserve2);
        infoMap.put(filedKeys[13], mSN.refernumber);
        if (!StringUtil.isEmpty(mSN.idrespcode)) {
            infoMap.put(filedKeys[14], mSN.idrespcode); //授权码
        }
        infoMap.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
        infoMap.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
        infoMap.put(filedKeys[20], mSN.transcurrcode);
        infoMap.put(filedKeys[23], mSN.icdata); // ic卡数据域
        infoMap.put(filedKeys[24], getLoadParams(TransConstans.TRANS_CODE_UPLOAD_SCRIPT,batchNo)); // 60域

        String predealinfo = mSN.batchbillno.substring(0, 6) + mSN.systraceno + mSN.translocaldate;
        infoMap.put(filedKeys[27], predealinfo); // 批次号票据号
        infoMap.put(filedKeys[30], mesauthcode); // 64域校验码

        infoMap.put(filedKeys[32], ParamsConts.MAC_PARAMS);
        infoMap.put(filedKeys[1], getMessageType(TransConstans.TRANS_CODE_UPLOAD_SCRIPT));
        return infoMap;
    }

    /**
     *  联机退货
     */
    public static Map<String, String> getRefundInfo(Context context, AidlDeviceService deviceService) {
        Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put(filedKeys[0], ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP) + head);
        dataMap.put(filedKeys[3], transProcodeType(TransConstans.TRANS_CODE_LJTH)); // 交易处理码
        // 主账号
        dataMap.put(filedKeys[2], Cache.getInstance().getCardNo());
        dataMap.put(filedKeys[4], getMoney(Cache.getInstance().getTransMoney())); // 交易金额
        String billNo = TransParamsUtil.getBillNo();
        dataMap.put(filedKeys[5], Utility.addZeroForNum(billNo, 6)); // Pos流水号
        // 卡有效期
        if (!StringUtil.isEmpty(Cache.getInstance().getInvalidDate())) {
            dataMap.put(filedKeys[6], Cache.getInstance().getInvalidDate());
        }
//		Cache.getInstance().getSerInputCode()
        String entryMode = Cache.getInstance().getSerInputCode();
        if (entryMode.endsWith("1")) {
            entryMode = entryMode.substring(0, entryMode.length() - 1) + "2";
        }
        dataMap.put(filedKeys[7], entryMode); // 22域pos输入方式
        dataMap.put(filedKeys[9], "00");
        dataMap.put(filedKeys[31], "06");
        // 二磁道信息
        String trace2 = Cache.getInstance().getTrack_2_data();
        if (!StringUtil.isEmpty(trace2)) {
            trace2 = trace2.replace("=", "D");
            String encTrace2data = encField35(trace2,deviceService);
            dataMap.put(filedKeys[11], encTrace2data);
        }
        // 三磁道信息
        String trace3 = Cache.getInstance().getTrack_3_data();
        if (!StringUtil.isEmpty(trace3)) {
            trace3 = trace3.replace("=", "D");
            dataMap.put(filedKeys[12], trace3);
        }
        // 37域原系统参考号
        dataMap.put(filedKeys[13], Cache.getInstance().getOldBatchBillno());
        dataMap.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
        dataMap.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
        dataMap.put(filedKeys[20], "156"); // 货币代码
        // IC卡55域
        if (Cache.getInstance().getSerInputCode().startsWith(ParamsConts.SWING_CARD)
                || Cache.getInstance().getSerInputCode().startsWith(ParamsConts.SWIP_CARD)
                || Cache.getInstance().getSerInputCode().startsWith(ParamsConts.SWING_ALL_CARD)) {// 表示IC卡交易
            // IC卡号时，卡片序列号
            dataMap.put(filedKeys[8], Cache.getInstance().getCardSeqNo());
        }
        String batchStr = TransParamsUtil.getCurrentBatchNo();
        dataMap.put(filedKeys[24],getLoadParams(TransConstans.TRANS_CODE_LJTH,batchStr));
        dataMap.put(filedKeys[27], "000000" + "000000" + Cache.getInstance().getTransDate());
        String batchbillno = TransParamsUtil.getCurrentBatchNo() + billNo + "000000"
                + Cache.getInstance().getTransDate();
        dataMap.put(filedKeys[28], batchbillno);
        dataMap.put(filedKeys[29], "000");
        dataMap.put(filedKeys[30], mesauthcode);
        dataMap.put(filedKeys[32], ParamsConts.MAC_PARAMS);
        dataMap.put(filedKeys[1], getMessageType(TransConstans.TRANS_CODE_LJTH));
        LogUtils.d("退货报文map" + dataMap);
        return dataMap;
    }

    /**
     * 获取脱机退货参数包(校验)
     * @return
     */
    public static Map<String, String> getTuoJiTuiHuoParam(Context context) {

        Map<String, String> dataMap = new HashMap<String, String>();
        String billNo = TransParamsUtil.getBillNo();
        String tpdu = ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP);
        LogUtils.i(tpdu);
        dataMap.put(filedKeys[0], tpdu + head);
        // 主账号
        dataMap.put(filedKeys[2], Cache.getInstance().getCardNo());
        dataMap.put(filedKeys[3], "280000"); // 交易处理码
        dataMap.put(filedKeys[4], getMoney(Cache.getInstance().getTransMoney())); // 原交易金额
        dataMap.put(filedKeys[5], Utility.addZeroForNum(billNo, 6)); // Pos流水号
        dataMap.put(filedKeys[36], Utility.getTransLocalTime()); // 交易本地时间
        dataMap.put(filedKeys[37], Cache.getInstance().getTransDate()); // 原交易日期
        if (!StringUtil.isEmpty(Cache.getInstance().getInvalidDate())) {
            dataMap.put(filedKeys[6], Cache.getInstance().getInvalidDate());
        } // 卡有效期(贷记卡手输卡号时输入)

        dataMap.put(filedKeys[7], Cache.getInstance().getSerInputCode()); // 22域
        dataMap.put(filedKeys[9], "01");

        dataMap.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
        dataMap.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
        dataMap.put(filedKeys[20], "156"); // 货币代码
        /****************/
        dataMap.put(filedKeys[24], get_60_param(context)); // 操作员密码
        // 脱机消费需打印原凭证号（通过脱机退货（校验）+脱机退货上送两个报文，构造字段类型“批次号+凭证号+原凭证号”）
        /*String batchbillno = TuoJITuiHuoEntity.getInstance().getPrintIcData()
                + TuoJITuiHuoEntity.getInstance().getPrintArqc() + TuoJITuiHuoEntity.getInstance().getAdddataword();
        dataMap.put(filedKeys[28], batchbillno);*/ // 票据号
        // IC卡55域
        if (Cache.getInstance().getSerInputCode().startsWith(ParamsConts.SWING_CARD)
                || Cache.getInstance().getSerInputCode().startsWith(ParamsConts.SWIP_CARD)
                || Cache.getInstance().getSerInputCode().startsWith(ParamsConts.SWING_ALL_CARD)) {// 表示IC卡交易
            String tag55 = Cache.getInstance().getTlvTag55();
            LogUtils.d("55域数据：" + tag55);
            dataMap.put(filedKeys[23], tag55);
            // IC卡号时，卡片序列号
            dataMap.put(filedKeys[8], Cache.getInstance().getCardSeqNo());
        }
        dataMap.put(filedKeys[30], mesauthcode); // 64域校验码
        dataMap.put(filedKeys[32], "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
        dataMap.put(filedKeys[1], "0200");

        LogUtils.d("脱机退货报文：" + dataMap.toString());
        return dataMap;
    }

    // 指定账户圈存
    public static Map<String, String> getQuanCunZDParam(Context context, AidlDeviceService deviceService) {
        Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put(filedKeys[0], ParamsConts.UNIONPAY_TDUP + head);
        dataMap.put(filedKeys[3], "600000"); // 交易处理码
        // 主账号
        dataMap.put(filedKeys[2], Cache.getInstance().getCardNo());
        // 交易金额
        dataMap.put(filedKeys[4], getMoney(Cache.getInstance().getTransMoney())); // 交易金额
        String billNo = TransParamsUtil.getBillNo();
        dataMap.put(filedKeys[5], Utility.addZeroForNum(billNo, 6)); // Pos流水号
        dataMap.put(filedKeys[7], Cache.getInstance().getSerInputCode()); // 22域输入方式
        dataMap.put(filedKeys[9], "91"); // 服务点条件码
        dataMap.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
        dataMap.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
        dataMap.put(filedKeys[20], "156"); // 货币代码
        if (Cache.getInstance().getHasPin()) {
            dataMap.put(filedKeys[31], "06"); // F26,参考930上送固定值
            // 52域 bin pinkey
            dataMap.put(filedKeys[21], Cache.getInstance().getPinBlock());
            dataMap.put(filedKeys[22], "2600000000000000"); // 安全控制信息（参照webView）
        }

        // // 二磁道信息
        String trace2 = Cache.getInstance().getTrack_2_data();
        if (!StringUtil.isEmpty(trace2)) {
            trace2 = trace2.replace("=", "D");
            dataMap.put(filedKeys[11], trace2);
        }
        // 三磁道信息
        String trace3 = Cache.getInstance().getTrack_3_data();
        if (!StringUtil.isEmpty(trace3)) {
            trace3 = trace3.replace("=", "D");
            dataMap.put(filedKeys[12], trace3);
        }
        // IC卡55域
        String tag55 = Cache.getInstance().getTlvTag55();
        LogUtils.d("55域数据：" + tag55);
        dataMap.put(filedKeys[23], tag55);
        // IC卡号时，卡片序列号
        dataMap.put(filedKeys[8], Cache.getInstance().getCardSeqNo());

       /* String info = SystemInfoDev.getInstance(context, deviceService).getManufacture()
                + SystemInfoDev.getInstance(context, deviceService).getModel()
                + CommonUtil.getAppVersion();
        info = Utility.addSpaceForStr(info, 18);
        info = info + Utility.getGSMCellLocationInfo(context);
        dataMap.put(filedKeys[25], info);*/ // 基站信息
        dataMap.put(filedKeys[24], get_60_param(context)); // 管理员密码
        String batchbillno = TransParamsUtil.getCurrentBatchNo() + billNo;
        dataMap.put(filedKeys[28], batchbillno); // 批次号票据号
        dataMap.put(filedKeys[30], mesauthcode);

        dataMap.put(filedKeys[32], "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
        dataMap.put(filedKeys[1], "0200");
        LogUtils.d("指定账户圈存Map:" + dataMap);
        return dataMap;
    }

    // 非指定账户圈存
    public static Map<String, String> getQuanCunFZDParam(Context context, AidlDeviceService deviceService) {
        Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put(filedKeys[0], ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP) + head);
        dataMap.put(filedKeys[3], "620000"); // 交易处理码
        // 主账号
        dataMap.put(filedKeys[2], Cache.getInstance().getCardNo());
        // 交易金额
        dataMap.put(filedKeys[4], getMoney(Cache.getInstance().getTransMoney())); // 交易金额
        String billNo = TransParamsUtil.getBillNo();
        dataMap.put(filedKeys[5], Utility.addZeroForNum(billNo, 6)); // Pos流水号
        dataMap.put(filedKeys[7], Cache.getInstance().getSerInputCode()); // 22域输入方式
        dataMap.put(filedKeys[9], "91"); // 服务点条件码

        dataMap.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
        dataMap.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
        dataMap.put(filedKeys[20], "156"); // 货币代码
        if (Cache.getInstance().getHasPin()) {
            dataMap.put(filedKeys[31], "06"); // F26,参考930上送固定值
            // 52域 bin pinkey
            dataMap.put(filedKeys[21], Cache.getInstance().getPinBlock());
            dataMap.put(filedKeys[22], "2600000000000000"); // 安全控制信息（参照webView）
        }
        // // 二磁道信息
        String trace2 = Cache.getInstance().getTrack_2_data();
        if (!StringUtil.isEmpty(trace2)) {
            trace2 = trace2.replace("=", "D");
            dataMap.put(filedKeys[11], trace2);
        }
        // 三磁道信息
        String trace3 = Cache.getInstance().getTrack_3_data();
        if (!StringUtil.isEmpty(trace3)) {
            trace3 = trace3.replace("=", "D");
            dataMap.put(filedKeys[12], trace3);
        }
        //// 48域附加数据（此处为转入卡POS输入方式+卡号）
        dataMap.put(filedKeys[19], "0510" + Cache.getInstance().getAdddataword());
        // IC卡55域

        String tag55 = Cache.getInstance().getTlvTag55();
        LogUtils.d("55域数据：" + tag55);
        dataMap.put(filedKeys[23], tag55);
        // IC卡号时，卡片序列号
        dataMap.put(filedKeys[8], Cache.getInstance().getCardSeqNo());
       /* String info = SystemInfoDev.getInstance(context, deviceService).getManufacture()
                + SystemInfoDev.getInstance(context, deviceService).getModel()
                + CommonUtil.getAppVersion();
        info = Utility.addSpaceForStr(info, 18);
        info = info + Utility.getGSMCellLocationInfo(context);
        dataMap.put(filedKeys[25], info); */// 基站信息
        dataMap.put(filedKeys[24], get_60_param(context)); // 管理员密码
        String batchbillno = TransParamsUtil.getCurrentBatchNo() + billNo;
        dataMap.put(filedKeys[28], batchbillno); // 批次号票据号
        dataMap.put(filedKeys[30], mesauthcode);

        dataMap.put(filedKeys[32], "01"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
        dataMap.put(filedKeys[1], "0200");
        LogUtils.d("非指定账户圈存Map:" + dataMap);
        return dataMap;
    }

    /**
     * 脱机消费上送type:0（包括脱机退货type:1）
     *
     * @param context
     * @param dataMap
     * @param type
     * @return
     */
    public static Map<String, String> getOfflineTransInfo(Context context, Map<String, String> dataMap, int type) {
        Map<String, String> infoMap = new HashMap<String, String>();
        String billNo = TransParamsUtil.getBillNo();// 流水号
        String batchNo = TransParamsUtil.getCurrentBatchNo();// 批次号
        infoMap.put(filedKeys[0], ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP) + head);
//		infoMap.put("priaccount", dataMap.get("priaccount"));
//		infoMap.put("transprocode", "000000");
//		infoMap.put("transamount", dataMap.get("transamount"));

        infoMap.put(filedKeys[5], Utility.addZeroForNum(billNo, 6)); // F11	Pos流水号

//		infoMap.put("translocaltime", Utility.getTransLocalTime()); // 交易本地时间
//		infoMap.put("translocaldate", Utility.getTransLocalDate()); // 交易本地日期
//		infoMap.put("expireddate", "null".equals(dataMap.get("expireddate")) ? null : dataMap.get("expireddate"));
//		infoMap.put("entrymode", "021"); // F22
//		infoMap.put("seqnumber", dataMap.get("seqnumber"));
//		if (type == 0) {
//			infoMap.put("conditionmode", "00"); // F25
//			infoMap.put("adddataword", "300"); // F48
//			String transtype = "30"+batchNo;
//			infoMap.put("transtype", transtype); // F60
//		} else if (type == 1) {
//			infoMap.put("conditionmode", "01"); // F25
//			infoMap.put("adddataword", "301"); // F48
//			String operater = ParamsUtil.getInstance(context).getParam("operatorcode")
//					+ ParamsUtil.getInstance(context).getParam("operatorpwd");
//			infoMap.put("loadparams", operater); // F60
//		}
//		infoMap.put("idrespcode", "null".equals(dataMap.get("idrespcode"))?null:dataMap.get("idrespcode"));//F38

        infoMap.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
        infoMap.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
        infoMap.put(filedKeys[19], "300");//F48 附加数据

//		infoMap.put("transcurrcode", "156");//F49
//		if (Cache.getInstance().getHasPin()) {
//			dataMap.put("secctrlinfo", "2600000000000000");//F53
//		}
//		String predealinfo = dataMap.get("batchbillno")+dataMap.get("translocaldate")+"00";
//		String settledata = "CUP";
//		infoMap.put("predealinfo", predealinfo);//F61
//		infoMap.put("settledata", settledata);//F63
//		infoMap.put("icdata", dataMap.get("icdata")); // F55
//		if (type == 0) {
//			infoMap.put("batchbillno", dataMap.get("batchbillno"));
//		} else {
//			String newvoucherno = batchNo + billNo;
//			infoMap.put("batchbillno", newvoucherno); // F62
//		}
        String transtype = "00" + batchNo + "201";
        infoMap.put(filedKeys[35], transtype); // F60	自定义域
//		infoMap.put("mesauthcode", mesauthcode);
        infoMap.put(filedKeys[32], "00"); // 兼容原webView，判断是否需要计算mac。“00”不计算，非“00”开头需要计算
        infoMap.put(filedKeys[1], "0320");
        LogUtils.d("脱机上送Map：" + infoMap);
        return infoMap;
    }



    /**
     * 获取结算信息
     * TODO 计算消费和预授权完成交易一样？是不是有问题
     */
    private static String getSettledata(Context context) {
        SettleDataDao settleDataDao = new SettleDataDaoImpl();
        List<SettleData> settleDatas = settleDataDao.getSettleData();
        String str = null;
        int saleNum = 0;
        long saleAmt = 0;
        int revNum = 0;
        long revAmt = 0;
        if (settleDatas != null) {
            for (SettleData settleData : settleDatas) {
                //计算消费撤销
                if ("200000".equals(settleData.transprocode) && "0210".equals(settleData.reserve1)) {
                    revNum++;
                    revAmt += Long.valueOf(settleData.transamount);
                }
                //计算退货交易
                if ("200000".equals(settleData.transprocode) && "0230".equals(settleData.reserve1)) {
                    revNum++;
                    revAmt += Long.valueOf(settleData.transamount);
                }
                // 计算消费
                if ("000000".equals(settleData.transprocode)
                        && "0210".equals(settleData.reserve1)) {
                    saleNum++;
                    saleAmt += Long.valueOf(settleData.transamount);
                }
                //预授权完成交易 TODO 有预授权商户时再调试
//                if ("000000".equals(settleData.transprocode)
//                        && "0210".equals(settleData.reserve1)) {
//                    saleNum++;
//                    saleAmt += Long.valueOf(settleData.transamount);
//                }
            }
        }

        str = fillzero(String.valueOf(saleAmt), 12, true) + // 借记总金额
                fillzero(String.valueOf(saleNum), 3, true) + // 借记总笔数
                fillzero(String.valueOf(revAmt), 12, true) + // 贷记总金额(包括消费撤销)
                fillzero(String.valueOf(revNum), 3, true) + // 贷记总笔数(包括消费撤销)
                fillzero("0", 32, true); // 后面全补0
        LogUtils.d("批结统计数据 saleAmt ： " + saleAmt + "，saleNum ：" + saleNum + " ，revAmt ： " + revAmt + ",revNum ： " + revNum);
        return str;
    }

    public static String fillzero(String str, int num, boolean isLeft) {
        StringBuffer zeroStr = new StringBuffer();
        if (str.length() < num) {
            int length = num - str.length();
            while (length > 0) {
                zeroStr.append("0");
                length--;
            }
        }
        return isLeft ? zeroStr.toString() + str : str + zeroStr.toString();
    }

    /**
     * TC上送
     * @param context
     * @param tr
     * @return
     */
    public static Map<String, String> getTCInfo(Context context, TransRecord tr) {
        LogUtils.d("TC上送交易记录对象：" + tr.toString());
        Map<String, String> infoMap = new HashMap<String, String>();
        String billNo = TransParamsUtil.getBillNo();// 流水号
        String batchNo = TransParamsUtil.getCurrentBatchNo();// 批次号
        infoMap.put(filedKeys[0], ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP) + head);
        infoMap.put(filedKeys[2], tr.getPriaccount());
        infoMap.put(filedKeys[4], tr.getTransamount());
        infoMap.put(filedKeys[5], Utility.addZeroForNum(billNo, 6)); // F11Pos流水号
//		infoMap.put("translocaltime", tr.translocaltime); // 原交易时间
//		infoMap.put("translocaldate", tr.translocaldate); // 原交易日期
        infoMap.put(filedKeys[7], tr.getEntrymode());
        if (!TextUtils.isEmpty(tr.getSeqnumber())) {
            infoMap.put(filedKeys[8], tr.getSeqnumber());
        }
//		infoMap.put("refernumber", tr.refernumber);
        infoMap.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
        infoMap.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
        infoMap.put(filedKeys[23], tr.getReserve3());
//		infoMap.put("loadparams", tr.loadparams);
//		infoMap.put("mesauthcode", mesauthcode); // 64域校验码
        infoMap.put(filedKeys[24], "00" + batchNo + "203" + "6" + "0");
        infoMap.put(filedKeys[28], "0000" + Utility.formatMount(tr.getTransamount()) + "156");
        infoMap.put(filedKeys[32], "01");
        return infoMap;
    }

    public static Map<String, String> getAACorARPCInfo(Context context, TransRecord tr) {
        LogUtils.d("AAC上送交易记录对象：" + tr.toString());
        Map<String, String> infoMap = new HashMap<String, String>();
        String billNo = TransParamsUtil.getBillNo();// 流水号
        String batchNo = TransParamsUtil.getCurrentBatchNo();// 批次号
        infoMap.put(filedKeys[0], ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TDUP) + head);
        infoMap.put(filedKeys[2], tr.getPriaccount());
        infoMap.put(filedKeys[4], tr.getTransamount());
        infoMap.put(filedKeys[5], Utility.addZeroForNum(billNo, 6)); // F11Pos流水号
//		infoMap.put("translocaltime", tr.translocaltime); // 原交易时间
//		infoMap.put("translocaldate", tr.translocaldate); // 原交易日期
        infoMap.put(filedKeys[7], tr.getEntrymode());
        if (!TextUtils.isEmpty(tr.getSeqnumber())) {
            infoMap.put(filedKeys[8], tr.getSeqnumber());
        }
//		infoMap.put("refernumber", tr.refernumber);
        infoMap.put(filedKeys[16], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID));
        infoMap.put(filedKeys[17], ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID));
        infoMap.put(filedKeys[23], tr.getReserve3());
//		infoMap.put("loadparams", tr.loadparams);
//		infoMap.put("mesauthcode", mesauthcode); // 64域校验码
        infoMap.put(filedKeys[24], "00" + batchNo + "203" + "6" + "0");
        infoMap.put(filedKeys[28], "0000" + Utility.formatMount(tr.getTransamount()) + "156");
        infoMap.put(filedKeys[32], "01");
        return infoMap;
    }

    private static AidlPinpad aidlPinpad;
    /**
     * 21号文
     * 获取59域
     * @return
     */
    private static String getField59(AidlDeviceService service) {
        try {
            String cardNo = Cache.getInstance().getCardNo();
            String encryptionFactor = cardNo.substring(cardNo.length() - 6, cardNo.length());
            if (aidlPinpad == null) {
                aidlPinpad = AidlPinpad.Stub.asInterface(service.getPinPad(PinpadConstant.PinpadId.BUILTIN));
            }
            TusnData tusnData = aidlPinpad.getTusnData(encryptionFactor);
            StringBuffer field59 = new StringBuffer();
            String encryptedData = tusnData.getEncryptedData() == null ? "" : tusnData.getEncryptedData();
            if (encryptedData == null) {
                LogUtils.d("硬件序列号密文数据为null");
                return null;
            }
            field59.append("01").append("002").append(tusnData.getDeviceType()) // 设备类型
                    .append("02").append(Utility.addZeroForNum(tusnData.getSn().length() + "", 3)).append(tusnData.getSn()) //终端硬件序列号
                    .append("03").append("006").append(encryptionFactor) // 加密随机因子
                    .append("04").append("008").append(Utility.addSpaceForStr(encryptedData, 8)) //硬件序列号密文数据
                    .append("05").append("008").append(Utility.addSpaceForStr(BuildConfig.VERSION_NAME, 8));//应用程序版本号
            field59.insert(0, Utility.addZeroForNum(field59.length() + "", 3)).insert(0, "A2");
            LogUtils.d("field59值 : " + field59.toString());
            return field59.toString();
        } catch (Exception e) {
            LogUtils.d("SystemService初始化失败" + e.getMessage());
        }
        LogUtils.d("59域值获取失败");
        return null;


    }

    /**
     * 获取60域数据
     */
    private static String get_60_param(Context context) {
        String loadparams = ParamsUtil.getInstance().getParam("operatorcode")
                + ParamsUtil.getInstance().getParam("operatorpwd");
        String posInputType = Cache.getInstance().getSerInputCode();
        if ("051".equals(posInputType)) {
            loadparams = loadparams + "50"; // 60域用法二
        } else if ("801".equals(posInputType)) {
            loadparams = loadparams + "52"; // 60域用法二
        } else if ("071".equals(posInputType) || "981".equals(posInputType)) { // QPBOC联机
            loadparams = loadparams + "60";
        }
        return loadparams;
    }

    /**
     * 以元为单位的
     *
     * @param yuanMoney
     * @return
     */
    public static String getMoney(String yuanMoney) {
        // 交易金额
        String money = yuanMoney;
        String fen = "00";
        if (money.contains(".")) {
            String[] moneys = money.split("\\.");
            if (moneys.length >= 2) {
                fen = moneys[1] + fen;
                fen = fen.substring(0, 2);
                money = moneys[0] + fen;
            } else {
                money = moneys[0] + fen;
            }
        } else {
            money = money + fen;
        }
        return money;
    }

    /**
     * 获取消息类型
     * @param transCode
     * @return
     */
    private static String getMessageType(String transCode){
        String msg_tp="";
        switch (transCode){
            case TransConstans.TRANS_CODE_REVERSE : //冲正
                msg_tp= "0400";
                break;
            case TransConstans.TRANS_CODE_PRE: //预授权
            case TransConstans.TRANS_CODE_PRE_CX: //预授权撤销
                msg_tp = "0100";
                break;
            case TransConstans.TRANS_CODE_CONSUME: //消费
            case TransConstans.TRANS_CODE_QUERY_BALANCE: //余额查询
            case TransConstans.TRANS_CODE_CONSUME_CX:   //消费撤销
            case TransConstans.TRANS_CODE_PRE_COMPLET: //预授权完成
            case TransConstans.TRANS_CODE_PRE_COMPLET_CX: //已授权完成撤销
                msg_tp = "0200";
                break;
            case  TransConstans.TRANS_CODE_IC_KEY_QUERY : //IC卡公钥查询
            case  TransConstans.TRANS_CODE_IC_PARAM_QUERY : //IC参数查询
                msg_tp= "0820";
                break;
            case  TransConstans.TRANS_CODE_IC_PARAM_DOWN :  //IC 参数下载
            case  TransConstans.TRANS_CODE_IC_KEY_DOWN : //IC卡公钥下载
            case  TransConstans.TRANS_CODE_PARAM_DOWN:  //参数下载
            case  TransConstans.TRANS_CODE_SIGN : //签到
                msg_tp= "0800";
                break;
            case TransConstans.TRANS_CODE_SIGN_JS: // 结算
                    msg_tp=  "0500";
                break;
            case TransConstans.TRANS_CODE_BATCH_SEND_IC: //IC卡批上送
            case TransConstans.TRANS_CODE_BATCH_SEND_END:  //刷卡
                msg_tp= "0320";
                break;
            case TransConstans.TRANS_CODE_LJTH: //联机退货
                msg_tp= "0220";
            case TransConstans.TRANS_CODE_UPLOAD_SCRIPT: //脚本
                msg_tp = "0620";
            default:
                break;
        }
        return msg_tp;
    }


    /**
     * 交易处理码
     * @return
     */
    private static String transProcodeType(String transCode) {
        String transProcode = "";
        switch (transCode){
            case  TransConstans.TRANS_CODE_CONSUME: //消费
                transProcode = "000000";
                break;
            case  TransConstans.TRANS_CODE_QUERY_BALANCE: //余额查询
                transProcode = "310000";
                break;
            case  TransConstans.TRANS_CODE_CONSUME_CX: //消费撤销
            case TransConstans.TRANS_CODE_LJTH://连接退货
                transProcode = "200000";
                break;
            case TransConstans.TRANS_CODE_PRE: //预授权
                transProcode = "030000";
                break;
            case TransConstans.TRANS_CODE_PRE_COMPLET: //预授权完成
                transProcode = "010000";
                break;
            case TransConstans.TRANS_CODE_PRE_CX: //预授权撤销
                transProcode = "220000";
                break;
            case TransConstans.TRANS_CODE_PRE_COMPLET_CX: //已授权完成撤销
                transProcode = "210000";
                break;
            default:
                break;
        }
        return transProcode;
    }
    /**
     * 获取不同类型下的金额
     * @param transCode
     * @return
     */
    private static String getPreAmount(String transCode){
        String amount="";
        switch (transCode){
            case TransConstans.TRANS_CODE_PRE: //预授权
            case TransConstans.TRANS_CODE_PRE_COMPLET: //预授权完成
            case TransConstans.TRANS_CODE_PRE_CX: //预授权撤销
                amount =Utility.formatMount(Cache.getInstance().getTransMoney());
                break;
            case TransConstans.TRANS_CODE_PRE_COMPLET_CX: //已授权完成撤销
                TransRecord mTransRecord = Cache.getInstance().getTransRecord();
                amount = mTransRecord.getTransamount();
                break;
            default:
                break;
        }
        return amount;
    }

    /**
     * 终端号
     * @param transCode
     * @param context
     * @return
     */
    private  static  String getTermid(String transCode,Context context){
        String unionpayTermid ="";
        switch (transCode){
            case TransConstans.TRANS_CODE_PRE: //预授权
            case TransConstans.TRANS_CODE_PRE_COMPLET: //预授权完成
            case TransConstans.TRANS_CODE_PRE_COMPLET_CX: //已授权完成撤销
                unionpayTermid = ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.UNIONPAY_TERMID); // 终端号
                break;
            case TransConstans.TRANS_CODE_PRE_CX: //预授权撤销
                TransRecord mTransRecord = Cache.getInstance().getTransRecord();
                unionpayTermid= mTransRecord.getTerminalid();
                break;
            default:
                break;
        }
        return unionpayTermid;
    }

    /**
     * 商户号
     * @param transCode
     * @param context
     * @return
     */
    private  static  String getMerId(String transCode,Context context){
        String unionpayTermid ="";
        switch (transCode){
            case TransConstans.TRANS_CODE_PRE: //预授权
            case TransConstans.TRANS_CODE_PRE_COMPLET: //预授权完成
            case TransConstans.TRANS_CODE_PRE_COMPLET_CX: //已授权完成撤销
                unionpayTermid = ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID); // 商户号
                break;
            case TransConstans.TRANS_CODE_PRE_CX: //预授权撤销
                TransRecord mTransRecord = Cache.getInstance().getTransRecord();
                unionpayTermid= mTransRecord.getAcceptoridcode();
                break;
            default:
                break;
        }
        return unionpayTermid;
    }

    /**
     * 获取凭证号
     * @param transCode
     * @param oldBillNo
     * @param context
     * @return
     */
    private static String getBillNo(String transCode,String oldBillNo,Context context){
        String billNo ="";
        switch (transCode){
            case TransConstans.TRANS_CODE_PRE: //预授权
            case TransConstans.TRANS_CODE_PRE_COMPLET: //预授权完成
            case TransConstans.TRANS_CODE_PRE_CX: //预授权撤销
            case TransConstans.TRANS_CODE_CONSUME:  //消费
            case TransConstans.TRANS_CODE_QUERY_BALANCE:  //余额查询
                    billNo = TransParamsUtil.getCurrentBatchNo() + oldBillNo;
                break;
            case TransConstans.TRANS_CODE_PRE_COMPLET_CX: //已授权完成撤销
            case TransConstans.TRANS_CODE_CONSUME_CX:  //消费撤销
                TransRecord mTransRecord = Cache.getInstance().getTransRecord();
                String oldBatchBillNo = mTransRecord.getBatchbillno();
                if (!StringUtil.isEmpty(oldBatchBillNo) && oldBatchBillNo.length() >= 12) {
                    oldBatchBillNo = oldBatchBillNo.substring(6, 12);
                } else {
                    oldBatchBillNo = "";
                }
                billNo = TransParamsUtil.getCurrentBatchNo() + oldBillNo + oldBatchBillNo;
                break;
            default:
                break;
        }
        return  billNo;
    }

    /**
     * 获取60域组合数据
     * @param transCode
     * @param curBillNo
     * @return
     */
    private static String getLoadParams(String transCode,String curBillNo){
        String loadParams = "";
        switch (transCode){
            case TransConstans.TRANS_CODE_SIGN://签到
                loadParams = "00" + curBillNo + "004";
                break;
            case  TransConstans.TRANS_CODE_IC_KEY_QUERY : //IC卡公钥查询
                loadParams = "00" + curBillNo + "372";
                break;
            case  TransConstans.TRANS_CODE_IC_KEY_DOWN ://IC卡公钥下载
                loadParams = "00" + curBillNo + "370";
                break;
            case  TransConstans.TRANS_CODE_IC_PARAM_QUERY : //IC参数查询
                loadParams = "00" + curBillNo + "382";
                break;
            case  TransConstans.TRANS_CODE_IC_PARAM_DOWN :  //IC 参数下载
                loadParams =  "00" + curBillNo + "380";
                break;
            case  TransConstans.TRANS_CODE_PARAM_DOWN:  //参数下载
                loadParams = "00" + curBillNo + "364";
                break;
            case TransConstans.TRANS_CODE_CONSUME: //消费
                loadParams = "22" + curBillNo + "000" + "6" + "01";
                break;
            case TransConstans.TRANS_CODE_QUERY_BALANCE: //余额查询
                loadParams = "01" + curBillNo + "000" + "6" + "01";
                break;
            case TransConstans.TRANS_CODE_CONSUME_CX: //消费撤销
                loadParams =  "23" + curBillNo + "000" + "6" + "0";
                break;
            case TransConstans.TRANS_CODE_PRE: //预授权
                loadParams = "10"+curBillNo+"000"+"6"+"0";
                break;
            case TransConstans.TRANS_CODE_PRE_COMPLET: //预授权完成
                loadParams = "20"+curBillNo+"000"+"6"+"0";
                break;
            case TransConstans.TRANS_CODE_PRE_COMPLET_CX: //已授权完成撤销
                loadParams = "21"+curBillNo+"000"+"6"+"0";
                break;
            case TransConstans.TRANS_CODE_PRE_CX: //预授权撤销
                loadParams= "11"+curBillNo+"000"+"6"+"0";
                break;
            case TransConstans.TRANS_CODE_SIGN_JS:
                 loadParams=  "00" + curBillNo + "201";
                break;
            case TransConstans.TRANS_CODE_BATCH_SEND_IC: //批上送IC卡交易
                loadParams= "00" + curBillNo + "205" + "6" + "1";
                break;
            case TransConstans.TRANS_CODE_BATCH_SEND_END://刷卡
                loadParams="00" + curBillNo + "201";
                break;
            case TransConstans.TRANS_CODE_LJTH:  //联机退货
                loadParams="25" + curBillNo + "000" + "6" + "0";
                break;
            case  TransConstans.TRANS_CODE_UPLOAD_SCRIPT: //脚本
                loadParams="00" + curBillNo + "95160";
                break;
            default:
                break;
        }
        return loadParams;
    }
    private  static  String getOperCode (String transCode){
        String operCode ="";
        switch (transCode){
            case  TransConstans.TRANS_CODE_IC_KEY_QUERY : //IC卡公钥查询
            case  TransConstans.TRANS_CODE_IC_PARAM_QUERY ://IC参数查询
                operCode = "100";
                break;
            default:
                break;
        }
        return  operCode;
    }

    /**
     * 配置不同的安全控制信息
     * @param transCode
     * @return
     */
    private  static  String  getMessureSecurity(String transCode){
        String messureCode = "";
        switch (transCode){
            case TransConstans.TRANS_CODE_QUERY_BALANCE: //余额查询
            case TransConstans.TRANS_CODE_CONSUME: //消费
            case TransConstans.TRANS_CODE_PRE: //预授权
            case TransConstans.TRANS_CODE_PRE_CX: //预授权撤销
            case TransConstans.TRANS_CODE_PRE_COMPLET: //预授权完成
            case TransConstans.TRANS_CODE_PRE_COMPLET_CX: //预授权完成撤销
                messureCode = "2610000000000000";
                break;
            case TransConstans.TRANS_CODE_CONSUME_CX: //消费撤销
                messureCode = "2600000000000000";
                break;
            default:
                break;
        }
        return  messureCode;
    }
    /**
     * 35域加密
     * @param mSecondTrack
     * @param mDeviceService
     * @return
     */
    private static  String encField35(String mSecondTrack,AidlDeviceService mDeviceService) {
        try {
            LogUtils.d("encryptTrackData [1]:" + mSecondTrack);
            String mkeyId = ParamsUtil.getInstance().getParam(Constant.FIELD_NEW_TDK_ID);
            AidlPinpad mDev = AidlPinpad.Stub.asInterface(mDeviceService.getPinPad(0));
            int len = (mSecondTrack.length() + 1) / 2;
            int end = (len - 1) * 2;
            int start = (len - 1 - 8) * 2;
            LogUtils.d("444 len:" + len + ",start:" + start + ", end:" + end);
            if (start <= 0) {
                LogUtils.d("encryptTrackData length error:" + mSecondTrack);

            } else {
                String TDB = mSecondTrack.substring(start, end);
                LogUtils.d("encryptTrackData [TDB]:" + TDB);
                byte temp = 0x00;
                byte[] enc_TDB = new byte[TDB.length()];
                byte[] a = HexUtil.hexStringToByte(TDB);
                mDev.encryptByTdk(Integer.parseInt(mkeyId), temp, null, a, enc_TDB, Constant.IS_SM);
                String ENC_TDB = HexUtil.bcd2str(enc_TDB).substring(0, 16);
                LogUtils.d("encryptTrackData [ENC_TDB]:" + ENC_TDB);
                LogUtils.d("encryptTrackData [0, start]:" + mSecondTrack.substring(0, start));
                LogUtils.d("encryptTrackData [end]:" + mSecondTrack.substring(end));
                String encTrack = mSecondTrack.substring(0, start) + ENC_TDB + mSecondTrack.substring(end);
                Cache.getInstance().setTrack_2_data(encTrack);
                LogUtils.d("encryptTrackData [encTrack]:" + encTrack);
                return encTrack;
            }
        } catch (Exception e) {
            LogUtils.d("35域加密异常");
            LogUtils.d(e.getMessage());
            e.printStackTrace();
        }
        return "";
    }
}
