package com.nld.starpos.banktrade.thread;

import android.content.Context;
import android.os.Handler;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.NldPacketHandle;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.TransConstans;

import java.util.Map;

import common.HexUtil;

import static com.nld.starpos.banktrade.utils.TransactionPackageUtil.getICCaDownInfo;


/**
 * Created by jiangrenming on 2017/9/21.
 * IC卡参数查询
 */

public class ParamsQueryThread extends ComonThread {

    private String ICCodeType;
    private NldPacketHandle isopacket = null;
    private String uri;
    private AidlDeviceService aidlDeviceService;

    public ParamsQueryThread(Context context, Handler handler,
                             String transCode,String uri,AidlDeviceService service) {
        super(context, handler);
        this.ICCodeType = transCode;
        this.uri = uri;
        this.aidlDeviceService = service;
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
        if (ICCodeType.equals(TransConstans.TRANS_CODE_IC_PARAM_QUERY)){ //IC卡参数查询
            Map<String, String> icParamQueryInfo =  getICCaDownInfo(context,"",ICCodeType);
            packAndRequest(ICCodeType,icParamQueryInfo);
        }
    }

    public void packAndRequest(String transType,Map<String,String> maps) {

        handler.sendMessage( handler.obtainMessage(0x01,"IC卡参数查询中..."));
        byte[] pack = getPack(maps, transType);
        if (pack !=null){
            try{
                String result =  HttpConnetionHelper.httpClient.postHttpsUrl(uri, HexUtil.bcd2str(pack),handler);
                if (result.isEmpty()){
                    Cache.getInstance().setErrCode(NldException.ERR_NET_DEFAULT_E102);
                    Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_DEFAULT_E102));
                    handler.sendMessage(handler.obtainMessage(0x02));
                }
                LogUtils.i("ic卡参数查询接收前置返回的交易数据：["+result+"]");
                String processRequire = result.substring(19, 20);
                LogUtils.e("ic卡参数查询报文头处理要求："+processRequire);
                if(processRequire.equals("6")){
                    //需要更新TMS参数
                    ParamsUtil.getInstance().update("dowmloadparam", "6");
                }else if(processRequire.equals("4")){
                    //需要下载公钥
                    ParamsUtil.getInstance().update(ParamsConts.UPDATA_STATUS, "1");
                }else if(processRequire.equals("5")){
                    //需要下载IC卡参数
                    ParamsUtil.getInstance().update(ParamsConts.UPDATA_STATUS, "2");
                }
                byte[] packResult  = isopacket.subMessLen(HexUtil.hexStringToByte(result));
                LogUtils.d("ic卡参数查询返回的报文=[" +HexUtil.bcd2str(packResult)+"]");
                Map<String, String> resmap = isopacket.unPack(packResult, "002311",1);//解包
                LogUtils.i("ic卡参数查询respcode=="+ resmap.get("respcode"));
                Cache.getInstance().setResultMap(resmap);
                if (!resmap.isEmpty() && "00".equals(resmap.get("respcode"))){
                    handler.sendMessage(handler.obtainMessage(0x03,transType));
                } else {            //接到数据，但是交易处理不成功，不进行mac校验
                    Cache.getInstance().setErrCode(resmap.get("respcode"));
                    Cache.getInstance().setErrDesc(NldException.getMsg(resmap.get("respcode")));
                    handler.sendMessage(handler.obtainMessage(0x02));
                }
            }catch (Exception e){
                e.printStackTrace();
                Cache.getInstance().setErrCode(NldException.ERR_NET_DEFAULT_E102);
                Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_DEFAULT_E102));
                handler.sendMessage(handler.obtainMessage(0x02));
            }
        }
    }


    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }

    //组包
    private byte[] getPack( Map<String, String> dataMap,String transCode){
        byte[] msgData = null;
        try {
            msgData =isopacket.pack(aidlDeviceService,transCode, dataMap,1);
            System.out.println("dataMap的内容为：");
            System.out.println(dataMap.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Cache.getInstance().setErrCode(NldException.ERR_NET_DEFAULT_E102);
            Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_DEFAULT_E102));
            handler.sendMessage(handler.obtainMessage(0x02));
        }
        byte[] msg = isopacket.addMessageLen(msgData);
        System.out.println("交易mct编码："+transCode);
        System.out.println("请求发送的报文=["+HexUtil.bcd2str(msg)+"]");
        return msg != null ? msg : null;
    }

}
