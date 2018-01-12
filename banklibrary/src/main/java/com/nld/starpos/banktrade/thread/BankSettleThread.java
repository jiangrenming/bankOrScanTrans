package com.nld.starpos.banktrade.thread;

import android.content.Context;
import android.os.Handler;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.SettleDataDao;
import com.nld.starpos.banktrade.db.bean.SettleData;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;
import com.nld.starpos.banktrade.db.local.SettleDataDaoImpl;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.NldPacketHandle;
import com.nld.starpos.banktrade.utils.TransParamsUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.HexUtil;
import common.StringUtil;

/**
 * Created by jiangrenming on 2017/12/11.
 * 批结算线程
 */

public class BankSettleThread extends ComonThread {

    private AidlDeviceService mService;
    private String uri;
    private ParamConfigDao mParamConfigDao;
    private SettleDataDao settleDataDao;
    private NldPacketHandle nldPacketHandle;
    private String mTransCode;
    private  Map<String,String> settleData;

    public BankSettleThread(Context context, Handler handler, AidlDeviceService service, String uri,
                            String transCode, Map<String,String> settle) {
        super(context, handler);
        this.mService = service;
        this.uri = uri;
        this.mTransCode = transCode;
        this.settleData = settle;
        try {
            mParamConfigDao = new ParamConfigDaoImpl();
            settleDataDao = new SettleDataDaoImpl();
            nldPacketHandle = new NldPacketHandle(context);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("nldPacketHandle is not initlized");
        }
    }


    @Override
    public void run() {
        handler.sendMessage(handler.obtainMessage(0x01,"批结算正在请求中..."));

        byte[] msgData = null;
        try {
            msgData = nldPacketHandle.pack(mService, mTransCode, settleData, 1);
        } catch (Exception e1) {
            LogUtils.e("组包过程发生异常.... transCode=[" + mTransCode + "]&&dataMap=[" + settleData + "]", e1);
            e1.printStackTrace();
            Cache.getInstance().setTransCode(NldException.ERR_DAT_PACK_E201);
            Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DAT_PACK_E201));
            handler.sendMessage(handler.obtainMessage(0x02));
        }
        byte[] msg = nldPacketHandle.addMessageLen(msgData);
        LogUtils.d("发送加签的报文=[" + HexUtil.bcd2str(msg) + "]");
        try{
            String resultStr = HttpConnetionHelper.httpClient.postHttpsUrl(uri, HexUtil.bcd2str(msg),handler);
            if (null == resultStr) {  //网络访问失败
                Cache.getInstance().setErrCode(NldException.ERR_NET_DEFAULT_E102);
                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_DEFAULT_E102));
                handler.sendMessage(handler.obtainMessage(0x02));
            }
            LogUtils.d("接收前置返回的交易数据：[" + resultStr + "]");
            byte[] pack = nldPacketHandle.subMessLen(HexUtil.hexStringToByte(resultStr));
            LogUtils.d("返回的报文=[" + HexUtil.bcd2str(pack) + "]");
            Map<String, String> resmap = nldPacketHandle.unPack(pack, "002311", 1);//解包
            LogUtils.d("解包后resmap " + StringUtil.map2LineStr(resmap)+"/39域 rescode ="+ resmap.get("respcode"));
            String respcode = resmap.get("respcode");
            if (StringUtil.isEmpty(respcode)){
                respcode = "00";
            }
            if ("00".equals(respcode)) {
                LogUtils.i("签到交易返回MAP: " + resmap);
                String field63 = getField63();
                Cache.getInstance().setSettleData(field63);
                resmap.put("field63", field63);
                settleData.put("field63", field63);
                Cache.getInstance().setResultMap(resmap);
                String adddataword = resmap.get("adddataword");// 获取返回的48域
                LogUtils.i("结算返回48域 == " + adddataword);
                //内卡应答码
                String inCard = adddataword.substring(30, 31);
                // 将商户信息等结算信息存配置表中的requestSettleData字段中
                saveSettleDataToDB(settleData, resmap); // 保存结算信息到参数表，用于重打印结算
                if ("1".equals(inCard)) {
                    handler.sendMessage(handler.obtainMessage(0x03));  //批结成功
                } else {
                    handler.sendMessage(handler.obtainMessage(0x04));  //对账不平
                }
            } else {            //接到数据，但是交易处理不成功
                Cache.getInstance().setResultMap(resmap);
                String tip = NldException.getMsg(resmap.get("respcode"), NldException.MSG_RECEIVE_ERR);
                Cache.getInstance().setErrCode(resmap.get("respcode"));
                Cache.getInstance().setErrDesc(tip);
                handler.sendMessage(handler.obtainMessage(0x02));
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtils.d("交易异常：" + e.getMessage());
            //交易超时需要冲正。
            Cache.getInstance().setReserverCode("06");
            String expcode = NldException.getExpCode(e, NldException.ERR_NET_DEFAULT_E102);
            Cache.getInstance().setErrCode(expcode);
            Cache.getInstance().setErrDesc(NldException.getMsg(expcode));
            handler.sendMessage(handler.obtainMessage(0x02));
        }
    }

    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }

    /**
     * 保存结算信息
     * @param dataMap
     * @param resultMap
     */
    private void saveSettleDataToDB(Map<String, String> dataMap, Map<String, String> resultMap) {
        LogUtils.d("开始保存结算数据");
        Map<String, String> configMap = new HashMap<String, String>();
        String billNo = TransParamsUtil.getBillNo();// 流水号
        String batchNo = TransParamsUtil.getCurrentBatchNo();// 批次号
        String settlebatchno = batchNo + billNo;
        configMap.put("settlebatchno", settlebatchno);// 批次流水号
        configMap.put("translocaldate", resultMap.get("translocaldate"));// 日期
        configMap.put("translocaltime", resultMap.get("translocaltime"));// 时间
        configMap.put("requestSettleData", dataMap.get("field63"));// 本地结算信息
        configMap.put("respcode", resultMap.get("respcode"));// 返回码
        configMap.put("settledata", resultMap.get("settledata") == null ? null
                : resultMap.get("settledata")); // 平台结算信息
        ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl();
        int result = mParamConfigDao.update(configMap);
        LogUtils.d("保存结算信息修改数据个数：" + result);
    }
    /**
     * @return
     * @description 组织63域
     */
    private String getField63() {
        StringBuffer sb = new StringBuffer();
        SettleDataDaoImpl settleDataDao = new SettleDataDaoImpl();
        List<SettleData> settleDatas = settleDataDao.getSettleData();
        if (settleDatas == null || settleDatas.size() == 0) {
            LogUtils.d("无交易空结算");
            String nosettle = StringUtil.addBackZero("0", 126);
            sb.append(nosettle);
            return sb.toString();
        }
        int saleNum = 0;
        long saleAmt = 0;
        int compNum = 0;
        long compAmt = 0;
        int refNum = 0;
        long refAmt = 0;
        int transferNum = 0;
        long transferAmt = 0;
        int saleUndoNum = 0;
        long saleUndoAmt = 0;
        for (SettleData settledata : settleDatas) {
            // 计算消费（包括脱机消费）
            if ("000000".equals(settledata.transprocode)
                    && "00".equals(settledata.conditionmode)
                    && ("0210".equals(settledata.reserve1) || "0330"
                    .equals(settledata.reserve1))) {
                saleNum++;
                saleAmt += Long.valueOf(settledata.transamount);
            }
            // 计算预授权
            if ("000000".equals(settledata.transprocode)
                    && "06".equals(settledata.conditionmode)
                    && "0210".equals(settledata.reserve1)) {
                compNum++;
                compAmt += Long.valueOf(settledata.transamount);
            }
            // 计算退货 (包括电子现金脱机退货)
            if (("200000".equals(settledata.transprocode)
                    && "00".equals(settledata.conditionmode) && "0230"
                    .equals(settledata.reserve1))
                    || ("000000".equals(settledata.transprocode)
                    && "01".equals(settledata.conditionmode) && "0330"
                    .equals(settledata.reserve1))) {
                refNum++;
                refAmt += Long.valueOf(settledata.transamount);
            }
            // 计算转账（包括指定账户圈存、电子现金充值、非指定账户圈存）
            if (("600000".equals(settledata.transprocode) && "0210"
                    .equals(settledata.reserve1))
                    || ("630000".equals(settledata.transprocode) && "0210"
                    .equals(settledata.reserve1))
                    || ("620000".equals(settledata.transprocode) && "0210"
                    .equals(settledata.reserve1))) {
                transferNum++;
                transferAmt += Long.valueOf(settledata.transamount);
            }

            // 计算消费撤销
            if ("200000".equals(settledata.transprocode) && "0210".equals(settledata.reserve1)
                    && "00".equals(settledata.conditionmode)) {
                saleUndoNum++;
                saleUndoAmt += Long.valueOf(settledata.transamount);
            }
        }
        LogUtils.d("消费总笔数：" + saleNum + ";消费总金额：" + saleAmt + "消费撤销总笔数：" + saleUndoNum + "消费撤销总金额：" + saleUndoAmt
                + "退货总笔数" + refNum + "退货总金额" + refAmt);
        sb.append(StringUtil.addHeadZero(saleNum, 3)); // 消费总笔数
        sb.append(StringUtil.addHeadZero(saleAmt, 12)); // 总金额
        sb.append(StringUtil.addHeadZero(refNum, 3)); // 退货总笔数
        sb.append(StringUtil.addHeadZero(refAmt, 12)); // 总金额
        sb.append(StringUtil.addHeadZero(saleUndoNum, 3)); // 消费撤销笔数
        sb.append(StringUtil.addHeadZero(saleUndoAmt, 12)); // 总金额
        sb.append(StringUtil.addHeadZero("0", 30));
        sb.append(StringUtil.addHeadZero(transferNum, 3)); // 转帐总笔数
        sb.append(StringUtil.addHeadZero(transferAmt, 12)); // 总金额
        sb.append(StringUtil.addHeadZero("0", 15)); // 代收费总笔数、总金额
        sb.append(StringUtil.addHeadZero(compNum, 3)); // 转帐总笔数
        sb.append(StringUtil.addHeadZero(compAmt, 12)); // 总金额
        sb.append(StringUtil.addHeadZero("0", 6)); // 拨号成功、失败次数

        return sb.toString();
    }
}
