package com.nld.starpos.banktrade.utils;

import com.nld.starpos.banktrade.db.bean.Reverse;
import com.nld.starpos.banktrade.db.bean.ScriptNotity;
import com.nld.starpos.banktrade.db.bean.SettleData;
import com.nld.starpos.banktrade.db.bean.TransRecord;

import java.util.Map;

/**
 * Created by jiangrenming on 2017/12/7.
 */

public class ParseDataUtils {

    /**
     * 将Map数据转化为冲正对象
     * @param dataMap
     * @return
     */
    public static Reverse mapObjToReverse(Map<String, String> dataMap) {
        Reverse mReverse = new Reverse();
        mReverse.setPriaccount(dataMap.get("priaccount"));
        mReverse.setTransprocode(dataMap.get("transprocode"));
        mReverse.setTransamount(dataMap.get("transamount"));
        mReverse.setSystraceno(dataMap.get("systraceno"));
        mReverse.setTranslocaltime(dataMap.get("translocaltime"));
        mReverse.setTranslocaldate(dataMap.get("translocaldate"));
        mReverse.setExpireddate(dataMap.get("expireddate"));
        mReverse.setEntrymode(dataMap.get("entrymode"));
        mReverse.setSeqnumber(dataMap.get("seqnumber"));
        mReverse.setConditionmode(dataMap.get("conditionmode"));
        mReverse.setTrack2data(dataMap.get("track2data"));
        mReverse.setTrack3data(dataMap.get("track3data"));
        if (dataMap.get("idrespcode") != null) {
            mReverse.setIdrespcode(dataMap.get("idrespcode"));
        }
        mReverse.setTerminalid(dataMap.get("terminalid"));
        mReverse.setAcceptoridcode(dataMap.get("acceptoridcode"));
        mReverse.setTranscurrcode(dataMap.get("transcurrcode"));
        mReverse.setIcdata(dataMap.get("reversalF55")); // 冲正上送报文F55数据
        mReverse.setLoadparams(dataMap.get("loadparams"));
        mReverse.setBatchbillno(dataMap.get("batchbillno"));
        mReverse.setReversetimes("0");  //冲正次数
        return mReverse;
    }

    /**
     * 将Map数据转化为交易对象
     * @param dataobj
     * @param resobj
     * @return
     */
    public static TransRecord mapObjToTransBean(Map<String, String> dataobj, Map<String, String> resobj) {

        TransRecord record = null;
        try {
            record = new TransRecord();
            String id = resobj.get("id");
            if (id != null && !"".equals(id)) {
                record.setId(Integer.parseInt(resobj.get("id")));
            } else {
                id = dataobj.get("id");
                if (id != null && !"".equals(id)) {
                    record.setId(Integer.parseInt(dataobj.get("id")));
                }
            }
            record.setPriaccount(resobj.get("priaccount") != null ? resobj
                    .get("priaccount") : dataobj.get("priaccount")); // 主账号
            record.setTransprocode(resobj.get("transprocode") != null ? resobj
                    .get("transprocode") : dataobj.get("transprocode")); // 交易处理码

            record.setTransamount(resobj.get("transamount") != null ? resobj
                    .get("transamount") : dataobj.get("transamount")); // 交易金额
            record.setSystraceno(resobj.get("systraceno") != null ? resobj
                    .get("systraceno") : dataobj.get("systraceno")); // pos流水号
            record.setTranslocaltime(resobj.get("translocaltime") != null ? resobj
                    .get("translocaltime") : dataobj.get("translocaltime")); // 时间
            record.setTranslocaldate(resobj.get("translocaldate") != null ? resobj
                    .get("translocaldate") : dataobj.get("translocaldate")); // 日期
            record.setExpireddate(resobj.get("expireddate") != null ? resobj
                    .get("expireddate") : dataobj.get("expireddate")); // 卡有效期（14域）
            record.setEntrymode(resobj.get("entrymode") != null ? resobj
                    .get("entrymode") : dataobj.get("entrymode")); // POS输入方式(22域)
            record.setSeqnumber(resobj.get("seqnumber") != null ? resobj
                    .get("seqnumber") : dataobj.get("seqnumber"));
            record.setConditionmode(resobj.get("conditionmode") != null ? resobj
                    .get("conditionmode") : dataobj.get("conditionmode"));
            record.setUpdatecode(resobj.get("updatecode") != null ? resobj
                    .get("updatecode") : dataobj.get("updatecode"));
            record.setTrack2data(resobj.get("track2data") != null ? resobj
                    .get("track2data") : dataobj.get("track2data"));
            record.setTrack3data(resobj.get("track3data") != null ? resobj
                    .get("track3data") : dataobj.get("track3data"));
            record.setRefernumber(resobj.get("refernumber") != null ?
                    resobj.get("refernumber") : dataobj.get("refernumber"));
            record.setIdrespcode(resobj.get("idrespcode") != null ?
                    resobj.get("idrespcode") : dataobj.get("idrespcode"));
            record.setRespcode(resobj.get("respcode") != null ?
                    resobj.get("respcode") : dataobj.get("respcode"));
            record.setTerminalid(resobj.get("terminalid") != null ?
                    resobj.get("terminalid") : dataobj.get("terminalid"));
            record.setAcceptoridcode(resobj.get("acceptoridcode") != null ?
                    resobj.get("acceptoridcode") : dataobj.get("acceptoridcode"));
            record.setAcceptoridname(resobj.get("acceptoridname") != null ?
                    resobj.get("acceptoridname") : dataobj.get("acceptoridname"));
            record.setAddrespkey(resobj.get("addrespkey") != null ? resobj
                    .get("addrespkey") : dataobj.get("addrespkey"));
            record.setAdddataword(resobj.get("adddataword") != null ?
                    resobj.get("adddataword") : dataobj.get("adddataword"));
            record.setTranscurrcode(resobj.get("transcurrcode") != null ?
                    resobj.get("transcurrcode") : dataobj.get("transcurrcode"));
            record.setPindata(resobj.get("pindata") != null ? resobj
                    .get("pindata") : dataobj.get("pindata"));
            record.setSecctrlinfo(resobj.get("secctrlinfo") != null ? resobj
                    .get("secctrlinfo") : dataobj.get("secctrlinfo"));
            record.setBalanceamount(resobj.get("balanceamount") != null ?
                    resobj.get("balanceamount") : dataobj.get("balanceamount"));
            record.setIcdata(resobj.get("icdata") != null ? resobj
                    .get("icdata") : dataobj.get("icdata"));
            record.setAdddatapri(resobj.get("adddatapri") != null ? resobj
                    .get("adddatapri") : dataobj.get("adddatapri"));
            record.setPbocdata(resobj.get("pbocdata") != null ? resobj
                    .get("pbocdata") : dataobj.get("pbocdata"));
            record.setLoadparams(dataobj.get("loadparams"));
            record.setCardholderid(resobj.get("cardholderid") != null ? resobj
                    .get("cardholderid") : dataobj.get("cardholderid"));
            record.setBatchbillno(resobj.get("batchbillno") != null ?
                    resobj.get("batchbillno") :
                    dataobj.get("batchbillno"));
            record.setSettledata(resobj.get("settledata") != null ?
                    resobj.get("settledata") : dataobj.get("settledata"));
            record.setMesauthcode(resobj.get("mesauthcode") != null ?
                    resobj.get("mesauthcode") : null); // 接收拉卡拉前置的结算数据
            record.setStatuscode(resobj.get("statuscode") != null ? resobj
                    .get("statuscode") : dataobj.get("statuscode"));
            record.setReversetimes("0");
            record.setReserve1(resobj.get("msg_tp")); // 保留字段1，保存交易应答的消息类型
            record.setReserve2(resobj.get("reserve2") != null ? resobj
                    .get("reserve2") : dataobj.get("reserve2"));    // 受理方标识码
            record.setReserve3(dataobj.get("reserve3"));    // AAC、ARPC、TC上送报文中F55
            record.setReserve4(resobj.get("reserve4") != null ? resobj
                    .get("reserve4") : dataobj.get("reserve4"));
            record.setReserve5(resobj.get("reserve5") != null ? resobj
                    .get("reserve5") : dataobj.get("reserve5"));
            String transCode = Cache.getInstance().getTransCode();
            switch (transCode) {
                case TransConstans.TRANS_CODE_CONSUME:
                    record.setTransType(Constant.CONSUME);//消费
                    break;
                case TransConstans.TRANS_CODE_CONSUME_CX:
                    record.setTransType(Constant.CONSUME_REVOCATION);//消费撤销
                    break;
                case TransConstans.TRANS_CODE_PRE:  //预授权
                    record.setTransType(Constant.PRE_CONSUME);
                    break;
                case TransConstans.TRANS_CODE_PRE_COMPLET: //预授权完成
                    record.setTransType(Constant.PRE_CONSUME_COMPLETE);
                    break;
                case TransConstans.TRANS_CODE_PRE_CX:  //预授权撤销
                    record.setTransType(Constant.PRE_CONSUME_RESERVE);
                    break;
                case TransConstans.TRANS_CODE_PRE_COMPLET_CX:  //预授权完成撤销
                    record.setTransType(Constant.PRE_CONSUME_COMPLETE_RESERVE);
                    break;
                default:
                    break;
            }
            record.setTransState("1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return record;
    }

    //将Map数据转化为结算明细表记录对象
    public static SettleData mapObjToSettleBean(Map<String, String> dataobj, Map<String, String> resobj) {
        SettleData record = new SettleData();
        record.priaccount = resobj.get("priaccount") != null ? resobj.get("priaccount") : dataobj.get("priaccount");//卡号
        record.batchbillno = resobj.get("batchbillno") != null ?
                resobj.get("batchbillno") : dataobj.get("batchbillno");//批次号、凭证号
        record.transamount = resobj.get("transamount") != null ? resobj.get("transamount") : dataobj.get("transamount");//金额
        record.idrespcode = resobj.get("idrespcode") != null ?
                resobj.get("idrespcode") : dataobj.get("idrespcode");//授权号
        record.conditionmode = resobj.get("conditionmode") != null ? resobj.get("conditionmode") : dataobj.get("conditionmode");//服务条件码
        record.transprocode = resobj.get("transprocode") != null ? resobj.get("transprocode") : dataobj.get("transprocode");//交易处理码
        record.reserve1 = resobj.get("msg_tp"); // 保留字段1，保存交易下发的消息类型
        return record;
    }

    /**
     * 将Map数据转化为脚本通知对象
     *
     * @return
     */
    public static ScriptNotity mapObjToScriptNotity(Map<String, String> dataMap, Map<String, String> resMap, String scriptNotityF55) {
        ScriptNotity scriptNotity = new ScriptNotity();
        scriptNotity.setPriaccount(dataMap.get("priaccount"));
        scriptNotity.setTransprocode(dataMap.get("transprocode"));
        scriptNotity.setTransamount(dataMap.get("transamount"));
        scriptNotity.setSystraceno(dataMap.get("systraceno"));
        scriptNotity.setTranslocaltime(dataMap.get("translocaltime"));
        scriptNotity.setTranslocaldate(resMap.get("translocaldate"));
        scriptNotity.setExpireddate(dataMap.get("expireddate"));
        scriptNotity.setEntrymode(dataMap.get("entrymode"));    //F22
        scriptNotity.setSeqnumber(dataMap.get("seqnumber"));    //F23
        scriptNotity.setConditionmode(dataMap.get("conditionmode"));
        scriptNotity.setReserve2(resMap.get("receivemark"));    // F32
        scriptNotity.setRefernumber(resMap.get("refernumber")); // F37
        scriptNotity.setIdrespcode(resMap.get("idrespcode"));
        scriptNotity.setTerminalid(dataMap.get("terminalid"));
        scriptNotity.setAcceptoridcode(dataMap.get("acceptoridcode"));
        scriptNotity.setTranscurrcode(dataMap.get("transcurrcode"));
        scriptNotity.setIcdata(scriptNotityF55);
        scriptNotity.setLoadparams(dataMap.get("loadparams")); // 脚本通知中60域需要 60.3、60.4子域
        scriptNotity.setBatchbillno(dataMap.get("batchbillno"));
        scriptNotity.setReversetimes("0"); // 重发次数
        return scriptNotity;
    }
}
