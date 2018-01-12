package com.nld.starpos.banktrade.thread;

import android.content.Context;
import android.os.Handler;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.starpos.banktrade.db.ReverseDao;
import com.nld.starpos.banktrade.db.bean.Reverse;
import com.nld.starpos.banktrade.db.local.ReverseDaoImpl;
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
 * 冲正线程
 */
public class ReversalThread extends ComonThread {

    private ReverseDao mReverseDao;
    private NldPacketHandle nldPacketHandle;
    //交易笔数
    private int number = 1;
    // 发送次数
    private int mSendTimes = 3;
    private AidlDeviceService service;
    private String uri;

    public ReversalThread(Context context, Handler handler ,AidlDeviceService service,String uri) {
        super(context, handler);
        this.service = service;
        this.uri = uri;
        String time = ParamsUtil.getInstance().getParam("reversetimes");
        if (!StringUtil.isEmpty(time)) {
            try {
                mSendTimes = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                LogUtils.e("冲正次数格式化错误"+ e.getMessage());
                mSendTimes = 3;
            }
        }
        try {
            mReverseDao = new ReverseDaoImpl();
            nldPacketHandle = new NldPacketHandle(context);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.i("NldPacketHandle is not initlized");
        }
    }

    @Override
    public void run() {
        List<Reverse> mReverses = mReverseDao.getEntities();
        LogUtils.d("存在需要冲正的数据个数："+mReverses.size());
        if (!mReverses.isEmpty()) { // 冲正结果通知处理
            number = 1; // 置1
            final int maxTimes = mSendTimes;
            for (final Reverse mR : mReverses) {
                int times = Integer.parseInt(mR.getReversetimes()) + 1;
                while (times <= maxTimes) { // 需要冲正
                    LogUtils.d("正在第[" + number + "]笔\n第[" + times + "]次冲正，请稍等......");
                    handler.sendMessage(handler.obtainMessage(0x01,"正在第[" + number + "]笔\\n第[" + times+"]次冲正，请稍等..."));
                    Map<String, String> infoMap = TransactionPackageUtil.getReverseInfo(context, mR);
                    byte[] msgData = null;
                    try {
                        msgData = nldPacketHandle.pack(service, "002304", infoMap, 1);
                    } catch (Exception e1) {
                        LogUtils.e("冲正组包过程发生异常:dataMap=[" + infoMap + "]", e1);
                        e1.printStackTrace();
                        Cache.getInstance().setTransCode(NldException.ERR_DAT_PACK_E201);
                        Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_DAT_PACK_E201));
                        handler.sendMessage(handler.obtainMessage(0x02));
                        break;
                    }
                    byte[] msg = nldPacketHandle.addMessageLen(msgData);
                    LogUtils.d("冲正发送加签的报文=[" + HexUtil.bcd2str(msg) + "]");

                    try{
                        String resultStr = HttpConnetionHelper.httpClient.postHttpsUrl(uri, HexUtil.bcd2str(msg),handler);
                        if (null == resultStr) {  //网络访问失败
                            Cache.getInstance().setErrCode(NldException.ERR_NET_DEFAULT_E102);
                            Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_NET_DEFAULT_E102));
                            handler.sendMessage(handler.obtainMessage(0x02));
                            break;
                        }
                        LogUtils.d("冲正接收前置返回的交易数据：[" + resultStr + "]");
                        byte[] pack = nldPacketHandle.subMessLen(HexUtil.hexStringToByte(resultStr));
                        LogUtils.d("冲正返回的报文=[" + HexUtil.bcd2str(pack) + "]");
                        Map<String, String> resmap = nldPacketHandle.unPack(pack, "002311", 1);//解包
                        LogUtils.d("冲正解包后resmap " + StringUtil.map2LineStr(resmap)+"/39域 rescode ="+ resmap.get("respcode"));

                        if ("00".equals(resmap.get("respcode"))||"25".equals(resmap.get("respcode")) || "12".equals(resmap.get("respcode"))) {
                            String resultMac = resmap.get("mesauthcode");
                            String calMac = nldPacketHandle.getMac(service, pack);
                            LogUtils.d("冲正计算mac:" + calMac + " 冲正消息返回" + resultMac);
                            if (calMac.equals(resultMac)) { //MAC验证
                                number++; // 计数
                                mReverseDao.delete(mR);
                                break;
                            } else {
                                if (times == maxTimes) {
                                    number++; // 计数
                                    mReverseDao.delete(mR);
                                    break;
                                } else {
                                    mReverseDao.update(mR.id, "reversetimes", String.valueOf((times))); // 上送失败，更新重发次数
                                }
                            }
                        } else {  //冲正失败
                            if (times == maxTimes) {
                                number++; // 计数
                                mReverseDao.delete(mR);
                                break;
                            } else {
                                mReverseDao.update(mR.id, "reversetimes", String.valueOf((times))); // 上送失败，更新重发次数
                            }
                        }
                        times++;
                    }catch (Exception e){
                        e.printStackTrace();
                        LogUtils.d("冲正交易异常：" + e.getMessage());
                        String expcode = NldException.getExpCode(e, NldException.ERR_NET_DEFAULT_E102);
                        Cache.getInstance().setErrCode(expcode);
                        Cache.getInstance().setErrDesc(NldException.getMsg(expcode));
                        handler.sendMessage(handler.obtainMessage(0x02));
                        break;
                    }
                }
            }
            //所有交易结束，重新开始一轮，确保所有的冲正数据均已上送。
            run();
        } else { // 无冲正数据或冲正数据结束
            handler.sendMessage(handler.obtainMessage(0x03)) ;
            LogUtils.d("冲正结束，number=" + number);
            number = 1; // 置1
        }
    }

    @Override
    public byte[] getIsopack() {
        return new byte[0];
    }
}
