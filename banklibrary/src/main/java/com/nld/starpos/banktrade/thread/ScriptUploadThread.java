package com.nld.starpos.banktrade.thread;

import android.content.Context;
import android.os.Handler;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.starpos.banktrade.db.ScriptNotityDao;
import com.nld.starpos.banktrade.db.bean.ScriptNotity;
import com.nld.starpos.banktrade.db.local.ScriptNotityDaoImpl;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.NldPacketHandle;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.TransactionPackageUtil;

import java.util.List;
import java.util.Map;

import common.HexUtil;
import common.StringUtil;

/**
 * Created by jiangrenming on 2017/12/11.
 * 脚本上送的线程
 */

public class ScriptUploadThread extends ComonThread {

    private AidlDeviceService mService;
    private String mUri;
    //交易笔数
    private int number = 1;
    // 发送次数，
    private int mSendTimes = 3;
    private ScriptNotityDao mScriptNotitiesDao;
    private NldPacketHandle nldPacketHandle;

    public ScriptUploadThread(Context context, Handler handler,AidlDeviceService service,String uri) {
        super(context, handler);
        this.mService = service;
        this.mUri = uri;
        String time = ParamsUtil.getInstance().getParam("reversetimes");
        if (!StringUtil.isEmpty(time)) {
            try {
                mSendTimes = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                LogUtils.e("脚本上送次数格式化错误"+ e.getMessage());
                mSendTimes = 3;
            }
        }
        try {
            mScriptNotitiesDao=new ScriptNotityDaoImpl();
            nldPacketHandle = new NldPacketHandle(context);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.i("NldPacketHandle is not initlized");
        }
    }

    @Override
    public void run() {
        List<ScriptNotity> mScriptNotities = mScriptNotitiesDao.getEntities();
        if(null==mScriptNotities){
            LogUtils.d("无脚本上送结束脚本上送");
            return;
        }
        LogUtils.d("存在需要上送脚本处理结果个数："+mScriptNotities.size());
        if (!mScriptNotities.isEmpty()) { // 脚本结果通知处理
            number = 1; // 置1
            final int maxTimes = mSendTimes;
            for (final ScriptNotity mSN : mScriptNotities) {
                int times = Integer.parseInt(mSN.getReversetimes()) + 1;
                while (times <= maxTimes) { // 需要重发
                    handler.sendMessage(handler.obtainMessage(0x01,"正在第[" + number + "]笔\\n第[" + times+"]次脚本上送，请稍等..."));
                    Map<String, String> infoMap = TransactionPackageUtil.getScriptNotityInfo(context, mSN);
                    byte[] msgData = null;
                    try {
                        msgData = nldPacketHandle.pack(mService, "002318", infoMap, 1);
                    } catch (Exception e1) {
                        LogUtils.e("脚本上送组包过程发生异常:dataMap=[" + infoMap + "]", e1);
                        e1.printStackTrace();
                        Cache.getInstance().setTransCode(NldException.ERR_DAT_PACK_E201);
                        Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DAT_PACK_E201));
                        handler.sendMessage(handler.obtainMessage(0x02));
                        break;
                    }
                    byte[] msg = nldPacketHandle.addMessageLen(msgData);
                    LogUtils.d("脚本上送发送加签的报文=[" + HexUtil.bcd2str(msg) + "]");
                    try{
                        String resultStr = HttpConnetionHelper.httpClient.postHttpsUrl(mUri, HexUtil.bcd2str(msg),handler);
                        if (null == resultStr) {  //网络访问失败
                            Cache.getInstance().setErrCode(NldException.ERR_NET_DEFAULT_E102);
                            Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_DEFAULT_E102));
                            handler.sendMessage(handler.obtainMessage(0x02));
                            break;
                        }
                        LogUtils.d("脚本上送接收前置返回的交易数据：[" + resultStr + "]");
                        byte[] pack = nldPacketHandle.subMessLen(HexUtil.hexStringToByte(resultStr));
                        LogUtils.d("脚本上送返回的报文=[" + HexUtil.bcd2str(pack) + "]");
                        Map<String, String> resmap = nldPacketHandle.unPack(pack, "002311", 1);//解包
                        LogUtils.d("脚本上送解包后resmap " + StringUtil.map2LineStr(resmap)+"/39域 rescode ="+ resmap.get("respcode"));

                        if ("00".equals(resmap.get("respcode"))||"25".equals(resmap.get("respcode")) || "12".equals(resmap.get("respcode"))) {
                            String resultMac = resmap.get("mesauthcode");
                            String calMac = nldPacketHandle.getMac(mService, pack);
                            LogUtils.d("脚本上送计算mac:" + calMac + " 脚本上送消息返回" + resultMac);
                            if (calMac.equals(resultMac)) { //MAC验证
                                number++; // 计数
                                mScriptNotitiesDao.delete(mSN);
                                break;
                            } else {
                                if (times == maxTimes) {
                                    number++; // 计数
                                    mScriptNotitiesDao.delete(mSN);
                                    break;
                                } else {
                                    mScriptNotitiesDao.update(mSN.id, "reversetimes", String.valueOf((times))); // 上送失败，更新重发次数
                                }
                            }
                        } else {  //冲正失败
                            if (times == maxTimes) {
                                number++; // 计数
                                mScriptNotitiesDao.delete(mSN);
                                break;
                            } else {
                                mScriptNotitiesDao.update(mSN.id, "reversetimes", String.valueOf((times))); // 上送失败，更新重发次数
                            }
                        }
                        times++;
                    }catch (Exception e){
                        e.printStackTrace();
                        LogUtils.d("脚本上送交易异常：" + e.getMessage());
                        String expcode = NldException.getExpCode(e, NldException.ERR_NET_DEFAULT_E102);
                        Cache.getInstance().setErrCode(expcode);
                        Cache.getInstance().setErrDesc(NldException.getMsg(expcode));
                        handler.sendMessage(handler.obtainMessage(0x02));
                        break;
                    }
                }
            }
            //所有交易结束，重新开始一轮，确保所有的脚本均已上送。
            run();
        } else { // 无脚本信息
            LogUtils.d("脚本上送结束结束，number=" + number);
            handler.sendMessage(handler.obtainMessage(0x03)) ;
            number = 1; // 置1
        }
    }

    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }
}
