package com.nld.starpos.banktrade.thread;

import android.content.Context;
import android.os.Handler;

import com.nld.logger.LogUtils;
import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.starpos.banktrade.db.UpDateDBUtils;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.pinUtils.AidlUtils;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.NldPacketHandle;
import com.nld.starpos.banktrade.utils.TransConstans;

import java.util.Map;

import common.HexUtil;
import common.StringUtil;

/**
 *
 * @author jiangrenming
 * @date 2017/12/13
 * 预授权，预授权撤销，预授权完成，预授权完成撤销 线程
 */

public class PreConsumeThread  extends ComonThread {

    private Map<String,String> mPreConsume;
    private String url;
    private NldPacketHandle nldPacketHandle;
    private String mTransCode;

    public PreConsumeThread(Context context, Handler handler, Map<String,String> preConsume
                             ,String uri,String transCode) {
        super(context, handler);
        this.mPreConsume = preConsume ;
        this.url = uri;
        this.mTransCode = transCode;
        try {
            nldPacketHandle = new NldPacketHandle(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void run() {
        if (mTransCode.equals(TransConstans.TRANS_CODE_PRE)){
            handler.sendMessage(handler.obtainMessage(0x01,"预授权正在发起中..."));
        }else if (mTransCode.equals(TransConstans.TRANS_CODE_PRE_COMPLET)){
            handler.sendMessage(handler.obtainMessage(0x01,"预授权完成正在发起中..."));
        }else if (mTransCode.equals(TransConstans.TRANS_CODE_PRE_CX)){
            handler.sendMessage(handler.obtainMessage(0x01,"预授权撤销正在发起中..."));
        }else if (mTransCode.equals(TransConstans.TRANS_CODE_PRE_COMPLET_CX)){
            handler.sendMessage(handler.obtainMessage(0x01,"预授权完成撤销正在发起中..."));
        }

        byte[] msgData = null;
        try {
            msgData = nldPacketHandle.pack(AidlUtils.getInstance().getmService(), mTransCode, mPreConsume, 1);
        } catch (Exception e1) {
            LogUtils.e("预授权相关组包过程发生异常.... transCode=[" + mTransCode + "]&&dataMap=[" + mPreConsume + "]", e1);
            e1.printStackTrace();
            Cache.getInstance().setTransCode(NldException.ERR_DAT_PACK_E201);
            Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DAT_PACK_E201));
            handler.sendMessage(handler.obtainMessage(0x02));
        }
        byte[] msg = nldPacketHandle.addMessageLen(msgData);
        LogUtils.d("预授权相关发送加签的报文=[" + HexUtil.bcd2str(msg) + "]");

        try{
            String resultStr = HttpConnetionHelper.httpClient.postHttpsUrl(url, HexUtil.bcd2str(msg),handler);
            if (null == resultStr) {  //网络访问失败
                Cache.getInstance().setErrCode(NldException.ERR_NET_DEFAULT_E102);
                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_DEFAULT_E102));
                handler.sendMessage(handler.obtainMessage(0x02));
            }
            LogUtils.d("预授权相关接收前置返回的交易数据：[" + resultStr + "]");
            byte[] pack = nldPacketHandle.subMessLen(HexUtil.hexStringToByte(resultStr));
            LogUtils.d("预授权相关返回的报文=[" + HexUtil.bcd2str(pack) + "]");
            Map<String, String> resmap = nldPacketHandle.unPack(pack, "002311", 1);//解包
            LogUtils.d("预授权相关解包后resmap " + StringUtil.map2LineStr(resmap)+"/39域 rescode ="+ resmap.get("respcode"));
            if ("00".equals(resmap.get("respcode"))) {
                String resultMac = resmap.get("mesauthcode");
                String calMac = nldPacketHandle.getMac(AidlUtils.getInstance().getmService(), pack);
                LogUtils.d("预授权相关计算mac:" + calMac + "预授权相关消息返回" + resultMac);
                if (calMac.equals(resultMac)) { //MAC验证
                    Cache.getInstance().setResultMap(resmap);
                    try{
                        UpDateDBUtils.getInstance().deleteReverseRecord(mPreConsume.get("batchbillno"), AidlUtils.getInstance().getmService());
                        Map<String, String> result = Cache.getInstance().getResultMap();
                        //非接Q的打印参数信息
                        if (null != Cache.getInstance().getPrintIcData()) {
                            result.put("reserve4", Cache.getInstance().getPrintIcData());
                        }
                        result.put("statuscode", "00");//交易成功状态改为00；
                        //交易成功后保存交易记录
                        UpDateDBUtils.getInstance().saveOrUpdateTransRecord(AidlUtils.getInstance().getmService(),mPreConsume, result);
                        //保存结算
                        UpDateDBUtils.getInstance().saveSettleRecordToDB(mPreConsume,result, AidlUtils.getInstance().getmService());
                        //发送成功
                        handler.sendMessage(handler.obtainMessage(0x03));
                    }catch (Exception e){
                        e.printStackTrace();
                        Cache.getInstance().setErrCode("0007");
                        Cache.getInstance().setErrDesc(NldException.getMsg("0007"));
                        handler.sendMessage(handler.obtainMessage(0x02));
                    }
                } else {
                    //返回数据mac检验失败，保存错误信息方便结果页显示
                    Cache.getInstance().setReserverCode("A0");
                    Cache.getInstance().setErrCode(NldException.ERR_MER_CHECK_MAC_E403);
                    Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_MER_CHECK_MAC_E403));
                    //发送需要冲正的操作
                    handler.sendMessage(handler.obtainMessage(0x02));
                }
            } else {  //返回交易失败
                try{
                    UpDateDBUtils.getInstance().deleteReverseRecord(mPreConsume.get("batchbillno"), AidlUtils.getInstance().getmService());
                    Cache.getInstance().setResultMap(resmap);
                    String tip = NldException.getMsg(resmap.get("respcode"), NldException.MSG_RECEIVE_ERR);
                    Cache.getInstance().setErrCode(resmap.get("respcode"));
                    Cache.getInstance().setErrDesc(tip);
                    handler.sendMessage(handler.obtainMessage(0x02));
                }catch (Exception e){
                    e.printStackTrace();
                    Cache.getInstance().setErrCode("0007");
                    Cache.getInstance().setErrDesc(NldException.getMsg("0007"));
                    handler.sendMessage(handler.obtainMessage(0x02));
                }
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
}
