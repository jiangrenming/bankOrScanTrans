package com.nld.starpos.banktrade.db;

import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.shellmonitor.AidlShellMonitor;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.db.bean.Reverse;
import com.nld.starpos.banktrade.db.bean.SettleData;
import com.nld.starpos.banktrade.db.bean.TransRecord;
import com.nld.starpos.banktrade.db.local.ReverseDaoImpl;
import com.nld.starpos.banktrade.db.local.SettleDataDaoImpl;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.ParseDataUtils;
import com.nld.starpos.banktrade.utils.TransConstans;

import java.util.List;
import java.util.Map;

import common.StringUtil;

/**
 * Created by jiangrenming on 2017/12/7.
 * 主要处理有关数据库的操作
 */

public class UpDateDBUtils {

    private static  class  InnerUpDateDBUtils{
        private static  final UpDateDBUtils update = new UpDateDBUtils();
    }
    private UpDateDBUtils(){}
    public  static UpDateDBUtils getInstance(){
        return  InnerUpDateDBUtils.update;
    }

    public void saveOrUpdateTransRecord(AidlDeviceService service,Map<String, String> consume, Map<String, String> result) {
        /*************************************/
        String transCode = Cache.getInstance().getTransCode();
        if (!StringUtil.isEmpty(transCode) &&transCode.equals(TransConstans.TRANS_CODE_DZXJ_BALANCE_QUERY)
                || transCode.equals(TransConstans.TRANS_CODE_QUERY_BALANCE)) {
            return;
        }
        //保存交易报文成功数据
        TransRecord transRecord = ParseDataUtils.mapObjToTransBean(consume, result);
        TransRecordDao mTransRecordDao = new TransRecordDaoImpl();
        LogUtils.d("交易保存记录：" + transRecord.toString());
        TransRecord record = mTransRecordDao.getTransRecordByCondition(transRecord.getBatchbillno());
        int saveSuccess;
        if (null != record) {
            saveSuccess = mTransRecordDao.update(transRecord);
        } else {
            saveSuccess = mTransRecordDao.save(transRecord);
        }
        Log.e("TAG", "saveSuccess =" + saveSuccess);
        // 消费撤销更新撤销对应流水的状态
        if (transCode == TransConstans.TRANS_CODE_CONSUME_CX) {
            String batchbillno = transRecord.getBatchbillno();
            if (!TextUtils.isEmpty(batchbillno) && batchbillno.length() == 18) {
                // 获取消费被撤销的batchBillno
                String oldBatchBillno = batchbillno.substring(0, 6) + batchbillno.substring(12, 18);
                int updateResult = mTransRecordDao.updateByNo(2, oldBatchBillno);
                Log.i("TAG", "消费撤销更新状态 " + (updateResult == 1 ? true : false));
            }
        }
        try {
            AidlShellMonitor moniter = AidlShellMonitor.Stub.asInterface(service.getShellMonitor());
            if (null != moniter) {
                moniter.executeCmd("sync");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            LogUtils.d("数据同步异常", e);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d("数据同步异常", e);
        }
    }

    /**
     * /交易前保存冲正，防止交易过程中出现异常情况需要进行冲正。
     * @param consume
     */
    public  void saveReverseRecord(Map<String, String> consume,AidlDeviceService service) {
        String transCode = Cache.getInstance().getTransCode();
        if (!StringUtil.isEmpty(transCode) && transCode.equals(TransConstans.TRANS_CODE_QUERY_BALANCE)){
            return  ;
        }
        LogUtils.d("保存冲正记录");
        ReverseDao mReverseDao = new ReverseDaoImpl();
        Reverse transReverse = ParseDataUtils.mapObjToReverse(consume);
        mReverseDao.save(transReverse);
        try {
            AidlShellMonitor moniter = AidlShellMonitor.Stub.asInterface(service.getShellMonitor());
            moniter.executeCmd("sync");
        } catch (RemoteException e) {
            e.printStackTrace();
            LogUtils.d("数据同步异常", e);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d("数据同步异常", e);
        }
    }
    /**
     * 删除之前保存需要冲正的数据
     * @param batchbillno
     */
    public int deleteReverseRecord(String batchbillno,AidlDeviceService service) {
        String transCode = Cache.getInstance().getTransCode();
        if (!StringUtil.isEmpty(transCode) && transCode.equals(TransConstans.TRANS_CODE_QUERY_BALANCE)){
            return -1 ;
        }
        ReverseDao mReverseDao = new ReverseDaoImpl();
        List<Reverse> reverList = mReverseDao.getEntities();
        if (batchbillno.length() < 12) {
            LogUtils.d("删除冲正记录失败，票据号异常");
            return -1;
        }
        String oldbillno = batchbillno.substring(6, 12);
        LogUtils.d("batchbillno原交易流水/凭证号：" + oldbillno);
        if (StringUtil.isEmpty(oldbillno)) {
            LogUtils.d("删除冲正记录失败，流水/凭证号异常");
            return -1;
        }
        int ret = -1;
        for (Reverse rever : reverList) {
            if (StringUtil.isEmpty(rever.batchbillno) || rever.batchbillno.length() < 12) {
                LogUtils.d("改记录票据号异常：" + rever.batchbillno);
                continue;
            }
            if (oldbillno.equals(rever.batchbillno.subSequence(6, 12))) {
                ret = mReverseDao.delete(rever);
            }
        }
        try {
            AidlShellMonitor moniter = AidlShellMonitor.Stub.asInterface(service.getShellMonitor());
            moniter.executeCmd("sync");
        } catch (RemoteException e) {
            e.printStackTrace();
            LogUtils.d("数据同步异常", e);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d("数据同步异常", e);
        }
        return ret;
    }

    /**
     * 修改结算表数据（保存需要结算的数据，或者删除反交易数据
     * @param dataobj
     * @param resobj
     */
    public void saveSettleRecordToDB(Map<String, String> dataobj, Map<String, String> resobj, AidlDeviceService service) {
        String code = Cache.getInstance().getTransCode();
        if (code.equals(TransConstans.TRANS_CODE_CONSUME)
                || code.equals(TransConstans.TRANS_CODE_CONSUME_CX)
                || code.equals(TransConstans.TRANS_CODE_PRE_COMPLET)
                || code.equals(TransConstans.TRANS_CODE_PRE_CX)
                || code.equals(TransConstans.TRANS_CODE_PRE_COMPLET_CX)
                || code.equals(TransConstans.TRANS_CODE_LJTH)
                || code.equals(TransConstans.TRANS_CODE_QC_FZD)
                || code.equals(TransConstans.TRANS_CODE_QC_ZD)
                || code.equals(TransConstans.TRANS_CODE_DZXJCZ)) { // 除了预授权之外，消费、消费撤销,预授权相关、联机退货、（非）指定圈存账户，电子现金
            SettleData settleData = ParseDataUtils.mapObjToSettleBean(dataobj, resobj);
            SettleDataDao dao = new SettleDataDaoImpl();
            dao.save(settleData);
            try {
                AidlShellMonitor moniter = AidlShellMonitor.Stub.asInterface(service.getShellMonitor());
                if (null != moniter) {
                    moniter.executeCmd("sync");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                LogUtils.d("数据同步异常", e);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.d("数据同步异常", e);
            }
        }
    }
}
