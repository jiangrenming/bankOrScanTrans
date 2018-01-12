package com.nld.starpos.banktrade.thread;

import android.content.Context;
import android.os.Handler;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.starpos.banktrade.db.UpDateDBUtils;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.NldPacketHandle;

import java.util.Map;

import common.HexUtil;
import common.StringUtil;

/**
 * Created by jiangrenming on 2017/12/14.
 * 联机退货
 */

public class OnLineRefundThread extends ComonThread {

    private String transCode;
    private String url;
    private AidlDeviceService aidlDeviceService;
    private NldPacketHandle isopacket = null;
    private Map<String,String > onlineMap;

    public OnLineRefundThread(Context context, Handler handler, String transCode,
                              String uri, AidlDeviceService service,Map<String,String > online) {
        super(context, handler);
        this.transCode = transCode;
        this.url = uri;
        this.aidlDeviceService = service;
        this.onlineMap = online;
        if (null == isopacket){
            try{
                this.isopacket = new NldPacketHandle(context);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    @Override
    public void run() {
        handler.sendMessage(handler.obtainMessage(0x01,"联机退货正在请求中..."));

        byte[] msgData = null;
        try {
            msgData = isopacket.pack(aidlDeviceService, transCode, onlineMap, 1);
        } catch (Exception e1) {
            LogUtils.e("联机退货组包过程发生异常.... transCode=[" + transCode + "]&&dataMap=[" + onlineMap + "]", e1);
            e1.printStackTrace();
            Cache.getInstance().setTransCode(NldException.ERR_DAT_PACK_E201);
            Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DAT_PACK_E201));
            handler.sendMessage(handler.obtainMessage(0x02));
        }
        byte[] msg = isopacket.addMessageLen(msgData);
        LogUtils.d("联机退货发送加签的报文=[" + HexUtil.bcd2str(msg) + "]");

        try{
            String resultStr = HttpConnetionHelper.httpClient.postHttpsUrl(url, HexUtil.bcd2str(msg),handler);
            if (null == resultStr) {  //网络访问失败
                Cache.getInstance().setErrCode(NldException.ERR_NET_DEFAULT_E102);
                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_DEFAULT_E102));
                handler.sendMessage(handler.obtainMessage(0x02));
            }
            LogUtils.d("联机退货接收前置返回的交易数据：[" + resultStr + "]");
            byte[] pack = isopacket.subMessLen(HexUtil.hexStringToByte(resultStr));
            LogUtils.d("联机退货返回的报文=[" + HexUtil.bcd2str(pack) + "]");
            Map<String, String> resmap = isopacket.unPack(pack, "002311", 1);//解包
            LogUtils.d("联机退货解包后resmap " + StringUtil.map2LineStr(resmap)+"/39域 rescode ="+ resmap.get("respcode"));
            if ("00".equals(resmap.get("respcode"))) {
                String resultMac = resmap.get("mesauthcode");
                String calMac = isopacket.getMac(aidlDeviceService, pack);
                LogUtils.d("联机退货计算mac:" + calMac + "  消息返回" + resultMac);
                if (calMac.equals(resultMac)) { //MAC验证
                    Cache.getInstance().setResultMap(resmap);
                    try{
                        UpDateDBUtils.getInstance().deleteReverseRecord(onlineMap.get("batchbillno"),aidlDeviceService);
                        Map<String, String> result = Cache.getInstance().getResultMap();
                        //非接Q的打印参数信息
                        if (null != Cache.getInstance().getPrintIcData()) {
                            result.put("reserve4", Cache.getInstance().getPrintIcData());
                        }
                        result.put("statuscode", "00");//交易成功状态改为00；
                        //交易成功后保存交易记录
                        UpDateDBUtils.getInstance().saveOrUpdateTransRecord(aidlDeviceService,onlineMap, result);
                        //保存结算
                        UpDateDBUtils.getInstance().saveSettleRecordToDB(onlineMap, result,aidlDeviceService);
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
                    UpDateDBUtils.getInstance().deleteReverseRecord(onlineMap.get("batchbillno"), aidlDeviceService);
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
            LogUtils.d("联机退货交易异常：" + e.getMessage());
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
