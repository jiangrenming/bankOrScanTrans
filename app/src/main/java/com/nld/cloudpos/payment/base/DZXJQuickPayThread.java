package com.nld.cloudpos.payment.base;

import android.content.Context;
import android.os.RemoteException;
import com.centerm.iso8583.util.DataConverter;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.emv.AidlPboc;
import com.nld.cloudpos.aidl.emv.AidlPbocStartListener;
import com.nld.cloudpos.aidl.emv.CardInfo;
import com.nld.cloudpos.aidl.emv.EmvTransData;
import com.nld.cloudpos.aidl.emv.PCardLoadLog;
import com.nld.cloudpos.aidl.emv.PCardTransLog;
import com.nld.cloudpos.payment.socket.TransHandler;
import com.nld.cloudpos.payment.socket.TransHttpHelper;
import com.nld.cloudpos.util.TlvUtil;
import com.nld.cloudpos.util.TransResultUtil;
import com.nld.starpos.banktrade.db.ReverseDao;
import com.nld.starpos.banktrade.db.ScriptNotityDao;
import com.nld.starpos.banktrade.db.SettleDataDao;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.bean.Reverse;
import com.nld.starpos.banktrade.db.bean.ScriptNotity;
import com.nld.starpos.banktrade.db.bean.SettleData;
import com.nld.starpos.banktrade.db.bean.TransRecord;
import com.nld.starpos.banktrade.db.local.ReverseDaoImpl;
import com.nld.starpos.banktrade.db.local.ScriptNotityDaoImpl;
import com.nld.starpos.banktrade.db.local.SettleDataDaoImpl;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.pinUtils.EMVTAGStr;
import com.nld.starpos.banktrade.pinUtils.PbocDev;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.ParseDataUtils;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.banktrade.utils.TransactionPackageUtil;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.HexUtil;
import common.StringUtil;

public class DZXJQuickPayThread extends BaseAbstractThread {

    protected Logger logger= Logger.getLogger(this.getClass());
    protected TransHandler mHandler;
    protected Context mContext;
    protected TransHttpHelper mHttpRequest;
    protected Map<String,String> dataMap=null;
    protected AidlDeviceService mDeviceService;
    private String mTransCode="";
    private AidlPboc mDev;
    private int mTransType=0;
    
    /**
     * 
     * @param context
     * @param handler
     * @param deviceService
     * @param transtype 0表示快速支付；1表示电子现金普通消费
     */
    public DZXJQuickPayThread(Context context, TransHandler handler, AidlDeviceService deviceService, int transtype) {
        mContext=context.getApplicationContext();
        mHandler=handler;
        mDeviceService=deviceService;
        mTransType=transtype;
        try {
            mHttpRequest=new TransHttpHelper(mContext, mHandler);
        } catch (Exception e) {
            logger.error("网络访问失败，创建网络请求对象失败。"+e);
            handler.messageSendProgressFaild(NldException.ERR_NET_NETREQUEST_E110, false);
            e.printStackTrace();
        }
        
        try {
            mDev= PbocDev.getInstance(mContext, mDeviceService).getOriginalDev();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    @Override
    public boolean stopThread() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void run() {
        mTransCode= Cache.getInstance().getTransCode();
        startPboc();
        super.run();
    }


    public void startPboc(){
        EmvTransData transData=null;
        if(0==mTransType){
        //获取PBOC启动参数
//        EmvTransData transData=PbocDev.getPbocEmvTransData(mTransCode);
            logger.debug("非接消费emv启动参数");
         transData = new EmvTransData((byte) 0x00 // 交易类型0x00消费
                , (byte) 0x01// 请求输入金额位置 0x01 显示卡号前 0x02后
                , true// 是否支持电子现金
                        , false// 是否支持国密算法
                        , false// 是否强制联机
                        , (byte) 0x02// 0x01PBOC 0x02QPBOC
                        , (byte) 0x01 // 界面类型：0x00接触 0x01非接
                        , new byte[] { 0x00, 0x00, 0x00 });
        }else if(1==mTransType){
            logger.debug("插卡消费emv启动参数");
             transData = new EmvTransData((byte) 0x00 // 交易类型0x00消费
                    , (byte) 0x01// 请求输入金额位置 0x01 显示卡号前 0x02后
                    , true// 是否支持电子现金
                            , false// 是否支持国密算法
                            , false// 是否强制联机
                            , (byte) 0x01// 0x01PBOC 0x02QPBOC
                            , (byte) 0x00 // 界面类型：0x00接触 0x01非接
                            , new byte[] { 0x00, 0x00, 0x00 });
            
        }
        logger.debug("PBOC开始交易");
        try {
            mDev.processPBOC(transData, pbocTransListener);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }
    
    AidlPbocStartListener pbocTransListener=new AidlPbocStartListener.Stub() {

        
        @Override
        public void requestImportPin(int arg0, boolean arg1, String arg2)
                throws RemoteException {
            logger.info("PBOC：请求输入PIN arg0="+arg0+";arg1="+arg1+";arg2="+arg2);
            if (3!=arg0){   // 请求输入脱机pin
                try {
                    mDev.importPin(Cache.getInstance().getPinBlock());
                } catch (Exception e) {
                    logger.error(e);
                    e.printStackTrace();
                }
            } else {  
                try {
                    mDev.importPin("26888888FFFFFFFF");
                } catch (Exception e) {
                    logger.error("联机交易时导入PIN失败");
                    e.printStackTrace();
                }
            }
        }
        
        @Override
        public void requestUserAuth(int arg0, String arg1) throws RemoteException {
            logger.info("PBOC：请求输入PIN");
            if (3 != arg0) { // 请求输入脱机pin
                try {
                    PbocDev.getInstance(mContext, mDeviceService).getOriginalDev().importPin(Cache.getInstance().getPinBlock());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    PbocDev.getInstance(mContext, mDeviceService).getOriginalDev().importPin("26888888FFFFFFFF");
                } catch (Exception e) {
                    logger.error("联机交易时导入PIN失败");
                    e.printStackTrace();
                }
            }
        }
        
        @Override
        public void onTransResult(int arg0) throws RemoteException {
            logger.info("PBOC，交易结果："+arg0);
            switch(arg0){
            case 0x01://交易批准
            case 0x02://交易拒绝
                try {
                    byte[] temp=new byte[1024];
                    int count;
                    count = mDev.readKernelData(EMVTAGStr.getLakalaScriptResultTag(), temp);
                    if(count<=0){
                    	logger.error("成功导入交易结果后，读取内核失败");
                        mHandler.messageSendProgressFaild(NldException.ERR_DEV_READ_KERNEL_E303, false);
                        break;
                    }
                    byte[] datas=new byte[count];
                    System.arraycopy(temp, 0, datas, 0, count);
                  //读取内核数据，脚本处理结果
                    String scriptExeResponse = HexUtil.bcd2str(datas);
                    logger.debug("读取脚本上送数据 == "+scriptExeResponse);
                    String tagDF31 = TlvUtil.tlvToMap(scriptExeResponse).get("DF31");
                    logger.debug("发卡行脚本结果 Tag DF31："+tagDF31);
                    String transCode=mTransCode;
                    if(transCode.equals("002322") || transCode.equals("002323")
                            || transCode.equals("002321")){
                        if (tagDF31==null || "".equals(tagDF31)) { // 无脚本下发
                            logger.error("无发卡行脚本执行结果，引发冲正");
                            mHandler.messageSendProgressFaild(NldException.ERR_DEV_NO_ISSUE_SCRIPT_E306, true);
                            mDev.endPBOC();
                          break;
                        }
                    }
                    
                    
                    String ic55data = Cache.getInstance().getResultMap().get("icdata");
                    boolean hasScriptData = TransResultUtil.haveScriptData(ic55data);
                    logger.debug("返回55域是否有脚本处理结果："+hasScriptData);
                 // 保存发卡行脚本结果 
                    //返回结果包含有发卡行脚本时时才执行脚本结果上送
                    if ( hasScriptData) { // 交易联机成功（指拉卡拉平台F39返回00）
                        logger.info("返回55域含脚本处理结果，保存结果待上送");
                        Map<String,String> resultMap= Cache.getInstance().getResultMap();
                        ScriptNotity record = ParseDataUtils.mapObjToScriptNotity (dataMap,resultMap, scriptExeResponse);
                        ScriptNotityDao mScriptNotitiesDao=new ScriptNotityDaoImpl();
                        mScriptNotitiesDao.save(record);
                    }
                    
                    
                    //读取内核数据，F55用法一，用于CT或AAC、ARPC上送
                    logger.info("读取内核55域结果");
                    byte[] temp2=new byte[1024];
                    int count2=mDev.readKernelData(EMVTAGStr.getLakalaF55UseModeOne(), temp2);
                    logger.info("F55域内核长度："+count2);
                    if(count2<=0){
                    	logger.error("成功导入交易结果后，读取内核失败");
                        mHandler.messageSendProgressFaild(NldException.ERR_DEV_READ_KERNEL_E303, false);
                        break;
                    }
                    byte[] datas2=new byte[count2];
                    System.arraycopy(temp2, 0, datas2, 0, count2);
                    String f55Data = HexUtil.bcd2str(datas2);
                    logger.debug("读取内核 F55Data == "+f55Data);
                    dataMap.put("reserve3", f55Data);
                    
                    // 圈存交易需验证脚本是否执行成功，若脚本执行失败，交易则为失败
                    String tag95 = TlvUtil.tlvToMap(scriptExeResponse).get("95");
                    logger.debug("脚本处理结果TVR Tag 95："+tag95);
                    if (0x01==arg0 && TransResultUtil.transferScriptSuc(tag95)) {    //交易接受  判断圈存交易是否脚本执行成功
                      logger.info("交易接受");
                      // IC卡交易接受，结算时需上送TC或者ARPC
                      // 发卡行认证是否执行成功Tag 95 第5字节b7=1，结算时上送ARPC
                      if (!TransResultUtil.arpcSucess(tag95)){ // ARPC（发卡行认证）执行失败,需上送ARPC
                          logger.info("发卡行认证执行失败");
                          dataMap.put("statuscode", "AR");
                      } else { // 上送TC
                          logger.info("发卡行认证执行成功");
                          dataMap.put("statuscode", "TC");
                      }
                      
                      //打印设置
                      if (!mTransCode.equals("002301")) { // 除余额查询之外的完整PBOC流程，都有打印
                          byte[] temp3=new byte[1024];
                          int count3=mDev.readKernelData(EMVTAGStr.getkernelDataForPrint(), temp3);
                          byte[] data3=new byte[count3];
                          System.arraycopy(temp3, 0, data3, 0, count3);
                          String printData = HexUtil.bcd2str(data3);
                          String resData= TransResultUtil.getKernelDataForPrint(printData); // 读取需要打印的数据
                          dataMap.put("reserve4", resData); // 将要打印的数据存放在预留字段4中
                      }
                      
                      //保存结算
                      if (!mTransCode.equals("002321") 
                              && !mTransCode.equals("002301")) { //完整PBOC流程中除了预授权其他都需要参与结算
                          logger.debug("保存结算");
                              saveSettleRecordToDB(dataMap, Cache.getInstance().getResultMap());
                      }

                      mHandler.messageSendProgressSuccess("交易成功");
                  }else if (0x02==arg0 || !TransResultUtil.transferScriptSuc(tag95)){    //交易拒绝
                          logger.info("交易拒绝"); // 交易拒绝原因有二：1、内核拒绝，2脚本执行失败（或者不存在脚本）
                          
                          //IC卡交易拒绝，结算时需上送AAC
                          dataMap.put("statuscode", "AC");
                          // 平台交易成功，内核拒绝引发冲正,冲正记录在交易前保存了
                          mHandler.messageSendProgressFaild(NldException.ERR_DEV_TRANS_REFUSE_E307, true);
                  }else{

                      //删除交易前保存的冲正记录
                      deleteReverseRecord(dataMap.get("batchbillno"));
                      logger.error("交易结果未知");
                      mHandler.messageSendProgressFaild(NldException.ERR_DEV_RESULT_UNKNOW_E308, false);
                  }
                  //保存交易报文成功数据
                    Map<String, String> resMap= Cache.getInstance().getResultMap();
                    resMap.put("", "00");
                    saveOrUpdateTransRecord(dataMap, resMap);
                    //删除交易前保存的冲正记录
                    deleteReverseRecord(dataMap.get("batchbillno"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
                
            case 0x03://终止
                logger.info("交易终止");
                mHandler.messageSendProgressFaild(NldException.ERR_DEV_TRANS_END_E309, false);
                break;
            case 0x04://FALLBACK
                break;
            case 0x05://采用其它页面
            	logger.info("交易异常"+0x05);
                mHandler.messageSendProgressFaild(NldException.ERR_DEV_TRANS_EXCEPTION_E310, false);
                break;
            case 0x06:
            	logger.info("交易异常"+0x06);
                mHandler.messageSendProgressFaild(NldException.ERR_DEV_TRANS_EXCEPTION_E310, false);
                break;
            case 0x07:
                //不处理
                break;
            default:
                break;
            }
            try {
                mDev.endPBOC();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
//            mDev.abortPBOC();
        }
        
        @Override
        public void onRequestOnline() throws RemoteException {
            logger.debug("PBOC，请求联机");
            new Thread(){

                @Override
                public void run() {
                    
            byte[] results=new byte[512];
            //获取读取内核数据
            try {
                int count=0;
               count= PbocDev.getInstance(mContext, mDeviceService)
                .getOriginalDev().readKernelData(PbocDev.getKernalTag(mTransCode)
                        , results);
               logger.debug("读取内核数据结果："+ DataConverter.bytesToHexString(results)+"长度："+count);

               byte[] datas=null;
                if(count>0){

                    datas= new byte[count]; 
                    System.arraycopy(results, 0, datas, 0, count);
                }
//                endPboc();
                String tag55= HexUtil.bcd2str(datas);
                Cache.getInstance().setTlvTag55(tag55);
                logger.debug("55域数据："+tag55);
                
            } catch (RemoteException e) {
                logger.error("55域获取失败，PBOC对象获取失败",e);
                e.printStackTrace();
            } catch (Exception e) {
                logger.error("55域获取失败",e);
                e.printStackTrace();
            }
            //此处重新获取交易报文Map是因为PBOC流程中有更新参数
            dataMap=getTransMap();
            dataMap.put("statuscode", "-1");
            //交易前保存交易数据
            saveOrUpdateTransRecord(dataMap,null);
            //交易前保存冲正，防止交易过程中出现异常情况需要进行冲正。
            saveReverseRecord(dataMap);
            logger.debug("磁条卡或IC卡降级交易");

            mHandler.messageSendTipChange("联机请求...");
            //交易发起联机请求
            int code=mHttpRequest.transactionRequest(mDeviceService, mTransCode, dataMap,getTransName());
           
            switch(code){
            case TransHttpHelper.TRANS_SUCCESS:
                boolean transResult=true;
                String resIcdata="";//55域数据
                Map<String,String> map = Cache.getInstance().getResultMap();
                String resultCode=map.get("respcode");
//                if (!Cache.getInstance().getSerInputCode().startsWith("05")) {//非IC卡交易
//                    //保存数据到结算明细表
//                    mHandler.messageSendProgressSuccess("交易成功");
//                    break;          //标记有交易打印状态
//                }else if(mTransCode.equals(TransConstans.TRANS_CODE_DZXJ_BALANCE_QUERY)){
//                    //余额查询进行PBOC处理
//                    logger.debug("余额查询成功");
//                    mHandler.messageSendProgressSuccess("余额查询成功");
//                    break;
//                }
                     resIcdata = map.get("icdata");
                    logger.debug("交易结果55域数据："+resIcdata);
                    if(StringUtil.isEmpty(resIcdata)){
                        resIcdata="";
                    }
                    
                boolean pbocResult=false;
                try {
                    logger.debug("交易结果导入联机返回IC卡数据："+resIcdata+"\n交易结果码："+resultCode);
                    pbocResult = PbocDev.getInstance(mContext, mDeviceService).getOriginalDev().importOnlineResp(transResult, resultCode, resIcdata);
                } catch (RemoteException e) {
                    logger.error("IC卡交易结果导入失败，设备获取失败",e);
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.error("IC卡交易结果导入失败",e);
                    e.printStackTrace();
                }
                logger.info("ic卡交易结果："+pbocResult);
                if(!pbocResult){
                	logger.error("交易结果导入内核失败");
                    mHandler.messageSendProgressFaild(NldException.ERR_DEV_INPUT_KERNELE305, false);
                    try {
                        mDev.endPBOC();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case TransHttpHelper.TRANS_FAILD:
                //删除交易前保存的冲正记录
                deleteReverseRecord(dataMap.get("batchbillno"));
                deleteTransRecord(dataMap.get("batchbillno"));
                logger.debug("交易失败");
                break;
            case TransHttpHelper.TRANS_REVERSE:
                logger.debug("交易失败需要冲正");
                //冲正数据在交易前已经保存，在此无须再保存
                //冲正处理结束，通知页面跳转至结果页
                deleteTransRecord(dataMap.get("batchbillno"));
                mHandler.messageSendProgressFaild(Cache.getInstance().getErrCode(), Cache.getInstance().getErrDesc(), true);
                break;
            }

            if(code!= TransHttpHelper.TRANS_SUCCESS){
                //交易只要不是成功“00”，中断PBOC
                try {
                    mDev.abortPBOC();
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
//            try {
//                mDev.endPBOC();
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
            super.run();
        }}.start();
        }
        
       
        
        @Override
        public void onError(int arg0) throws RemoteException {
            logger.debug("第二次PBOC,onError: "+arg0);
            if(null!=dataMap){
                saveReverseRecord(dataMap);
            }
            mHandler.messageSendProgressFaild(NldException.ERR_DEV_TRANS_FAILD_E304, false);
            mDev.endPBOC();
        }

        @Override
        public void requestImportAmount(int paramInt) throws RemoteException {
            logger.debug("PBOC,请求输入金额参数="+paramInt);
            mDev.importAmount(Cache.getInstance().getTransMoney());
        }

        @Override
        public void requestTipsConfirm(String paramString)
                throws RemoteException {
            logger.debug("PBOC,请求信息确认参数="+paramString);
            mDev.importMsgConfirmRes(true);
        }

        @Override
        public void requestAidSelect(int paramInt, String[] paramArrayOfString)
                throws RemoteException {
            logger.debug("PBOC,请求多应用选择参数1="+paramInt+";参数2="+paramArrayOfString);
            if(null!=paramArrayOfString && paramArrayOfString.length>0){
                for(int i=0;i<paramArrayOfString.length;i++){
                    logger.debug("PBOC,请求多应用选择参数1="+paramInt+";参数2="+paramArrayOfString[i]);
                }
            }
//            DialogFactory.showMessage(mContext, "多应用选择", "请选择多应用", "确定", new OnClick, "取消", new OnClick);
            //显示多应用选择对话框。
            mHandler.messageShowDialog(paramArrayOfString, TransHandler.DIALOG_TYPE_AID_SELECT);
        }

        @Override
        public void requestEcashTipsConfirm() throws RemoteException {
            logger.debug("PBOC,请求电子现金确认");
            mHandler.messageShowDialog(new String[]{"电子现金选择"}, TransHandler.DIALOG_TYPE_ECASH_TIP);
        }

        @Override
        public void onConfirmCardInfo(CardInfo paramCardInfo)
                throws RemoteException {
            logger.debug("PBOC,请求卡信息确认");
            mDev.importConfirmCardInfoRes(true);
        }

        @Override
        public void onReadCardOffLineBalance(String paramString1,
                                             String paramString2, String paramString3, String paramString4)
                throws RemoteException {

            logger.debug("PBOC,请求读取卡脱机账户信息确认");
        }

        @Override
        public void onReadCardTransLog(PCardTransLog[] paramArrayOfPCardTransLog)
                throws RemoteException {
            logger.debug("PBOC,请求读取卡交易日志");
            
        }

        @Override
        public void onReadCardLoadLog(String paramString1, String paramString2,
                                      PCardLoadLog[] paramArrayOfPCardLoadLog) throws RemoteException {
            logger.debug("PBOC,请求读取卡加载日志");
        }
    };
    
    /**
     * 修改结算表数据（保存需要结算的数据，或者删除反交易数据
     * @param dataobj
     * @param resobj
     */
        public void saveSettleRecordToDB (Map<String, String> dataobj, Map<String, String> resobj) {
            String code=mTransCode;
            if(code.equals(TransConstans.TRANS_CODE_CONSUME)
                    ||code.equals(TransConstans.TRANS_CODE_PRE_COMPLET)
                    ||code.equals(TransConstans.TRANS_CODE_LJTH)
                    ||code.equals(TransConstans.TRANS_CODE_DZXJCZ)){
                SettleData settleData = ParseDataUtils.mapObjToSettleBean(dataobj, resobj);
                SettleDataDao dao=new SettleDataDaoImpl();
                dao.save(settleData);   
            }else if(code.equals(TransConstans.TRANS_CODE_CONSUME_CX)
                ||code.equals(TransConstans.TRANS_CODE_PRE_COMPLET_CX)){
                logger.debug("消费撤销、预授权完成撤销成功后，从结算表中删除对应数据");
                deleteSettleRecordFromDB(dataobj.get("batchbillno"));
            }
        }
        

        /**
         * 反交易时，从结算明细表中删除已撤销的交易记录
        */
        public int deleteSettleRecordFromDB (String batchbillno) {
            String transCode=mTransCode;
            //除预授权完成撤销和消费撤销外，其它交易不需要删除结算信息。
            if(!transCode.equals(TransConstans.TRANS_CODE_CONSUME_CX)
                    && !transCode.equals(TransConstans.TRANS_CODE_PRE_COMPLET_CX)){
                return 0;
            }
            logger.debug("删除结算表中的记录");
            SettleDataDao settleDataDao=new SettleDataDaoImpl();
            int ret = 0;
            List<SettleData> sDatas = settleDataDao.getSettleData();
            logger.debug("结算表中的记录个数："+sDatas.size());
            if (sDatas == null || batchbillno.length()<12) {
                return -1;
            }
            String oldbillno = batchbillno.substring(6,12);
            logger.debug("batchbillno原交易流水/凭证号："+oldbillno);
            if(StringUtil.isEmpty(oldbillno)){
                return -1;
            }
            for(SettleData sData:sDatas){
                if(StringUtil.isEmpty(sData.batchbillno) || sData.batchbillno.length()<12){
                    logger.debug("改记录票据号异常："+sData.batchbillno);
                    continue;
                }
                if (oldbillno.equals(sData.batchbillno.subSequence(6, 12))){
                    ret = settleDataDao.delete(sData);
                }
            }
            return ret;
        }
        

        /**
         * 删除冲正记录
         * @param batchbillno
         */
        public int deleteReverseRecord(String batchbillno){

            ReverseDao mReverseDao=new ReverseDaoImpl();
            List<Reverse> reverList=mReverseDao.getEntities() ;
            if (batchbillno.length()<12) {
                logger.debug("删除冲正记录失败，票据号异常");
                return -1;
            }
            String oldbillno = batchbillno.substring(6,12);
            logger.debug("batchbillno原交易流水/凭证号："+oldbillno);
            if(StringUtil.isEmpty(oldbillno)){
                logger.debug("删除冲正记录失败，流水/凭证号异常");
                return -1;
            }
            int ret=-1;
            for(Reverse rever:reverList){
                if(StringUtil.isEmpty(rever.batchbillno) || rever.batchbillno.length()<12){
                    logger.debug("改记录票据号异常："+rever.batchbillno);
                    continue;
                }
                if (oldbillno.equals(rever.batchbillno.subSequence(6, 12))){
                    ret = mReverseDao.delete(rever);
                }
            }
            return ret;
        }
        

        /**
         * 保存或更新交易记录
         * @param transMap
         */
        public void saveOrUpdateTransRecord(Map<String,String> transMap, Map<String,String> resMap){
            String transCode=mTransCode;
            if(transCode.equals(TransConstans.TRANS_CODE_DZXJ_BALANCE_QUERY)
                    || transCode.equals(TransConstans.TRANS_CODE_QUERY_BALANCE)){
                return;
            }
            if(null==resMap){
                resMap=new HashMap<String, String>();
            }
          //保存交易报文成功数据
            TransRecord transRecord = ParseDataUtils.mapObjToTransBean(transMap, resMap);
            TransRecordDao mTransRecordDao=new TransRecordDaoImpl();
            logger.debug("交易保存记录："+transRecord.toString());
            TransRecord record=mTransRecordDao.getTransRecordByCondition(transRecord.getBatchbillno());
            if(null!=record){
                mTransRecordDao.update(transRecord);
            }else{
                mTransRecordDao.save(transRecord);   
            } 
        }

        /**
         * 删除交易记录
         * @param batchBillno
         */
        private void deleteTransRecord(String batchBillno){
            TransRecordDao transDao=new TransRecordDaoImpl();
            TransRecord record=transDao.getTransRecordByCondition(batchBillno);
            transDao.delete(record);
        }

        /**
         * 保存冲正记录
         */
        public void saveReverseRecord(Map<String,String> transMap){
            String code=mTransCode;
            if(code.equals(TransConstans.TRANS_CODE_CONSUME)//消费
                    ||code.equals(TransConstans.TRANS_CODE_CONSUME_CX)//消费撤销
                    ||code.equals(TransConstans.TRANS_CODE_PRE)//预授权
                    ||code.equals(TransConstans.TRANS_CODE_PRE_COMPLET)//预授权完成
                    ||code.equals(TransConstans.TRANS_CODE_PRE_CX)//预授权撤销
                    ||code.equals(TransConstans.TRANS_CODE_PRE_COMPLET_CX)//预授权完成撤销
                    ){//
                ReverseDao mReverseDao=new ReverseDaoImpl();
                Reverse transReverse = ParseDataUtils.mapObjToReverse(transMap);
                mReverseDao.save(transReverse);
            }
        }
        
        /**
         * 获取交易组包map
         * @return
         */
        public Map<String,String> getTransMap(){
            Map<String,String> datamap= TransactionPackageUtil.getConsumeParam(mContext,mDeviceService);
            return datamap;
            
        }
        public String getTransName(){
            String name="";
            if(mTransCode.equals(TransConstans.TRANS_CODE_CONSUME)){
                name= "消费";
            }
            return name;
        }
        
        @Override
        public void cancel() {
            // TODO Auto-generated method stub
            if(mHandler!=null){
                mHandler.removeCallbacksAndMessages(null);
            }
        }
}
