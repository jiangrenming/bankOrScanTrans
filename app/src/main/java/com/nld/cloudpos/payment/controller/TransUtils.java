package com.nld.cloudpos.payment.controller;

import android.content.Context;

import com.nld.cloudpos.BankApplication;
import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.db.EmvFailWaterDao;
import com.nld.starpos.banktrade.db.ReverseDao;
import com.nld.starpos.banktrade.db.ScriptNotityDao;
import com.nld.starpos.banktrade.db.SettleDataDao;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.local.EMVFailWaterDaoImpl;
import com.nld.starpos.banktrade.db.local.ReverseDaoImpl;
import com.nld.starpos.banktrade.db.local.ScriptNotityDaoImpl;
import com.nld.starpos.banktrade.db.local.SettleDataDaoImpl;
import com.nld.starpos.banktrade.db.local.TransRecordDaoImpl;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.wxtrade.local.db.ScanTransDao;
import com.nld.starpos.wxtrade.local.db.imp.ScanParamsUtil;
import com.nld.starpos.wxtrade.local.db.imp.ScanTransDaoImp;
import com.nld.starpos.wxtrade.utils.ShareScanPreferenceUtils;
import com.nld.starpos.wxtrade.utils.params.TransParamsValue;
import com.nld.starpos.wxtrade.utils.params.TransType;

import common.StringUtil;

/**
 * Created by jiangrenming on 2017/10/12.
 */

public class TransUtils {


    /**
     * 在进行扫码交易的时候判断的条件
     * @return
     */
    public static  boolean isExitLocalScanData(){
        String md5Key = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.MD5_KEY);
        String scanMerchantId = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID);
        String terminal = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID);
        String merchanName = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME);
        if (StringUtil.isEmpty(md5Key)
                || StringUtil.isEmpty(scanMerchantId)
                || StringUtil.isEmpty(terminal)
                || StringUtil.isEmpty(merchanName)) {
            return false;
        }
        return true;
    }

    /**
     * 判断本地是否存在绑定数据
     * @return
     */
    public static  boolean isExitLocalData(){
        String bankMerchantId = ParamsUtil.getInstance().getParam(ParamsConts.BindParamsContns.PARAMS_KEY_BASE_MERCHANTID);
        String md5Key = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.MD5_KEY);
        String terminal = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID);
        String merchanName = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME);
        String scanMerchantId = ScanParamsUtil.getInstance().getParam(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID);
        LogUtils.i("进入新数据库升级md5_key="+md5Key+"/bankMerchantId="+bankMerchantId+"/terminal="+terminal+"/merchanName="+merchanName+"/scanMerchantId="+scanMerchantId);
        if (StringUtil.isEmpty(md5Key)
                || StringUtil.isEmpty(bankMerchantId)
                || StringUtil.isEmpty(terminal)
                || StringUtil.isEmpty(merchanName)|| StringUtil.isEmpty(scanMerchantId)) {
            return false;
        }
        return true;
    }
    /**
     * 更新解绑后重新绑定的商终信息，并清除数据
     */
   /* public  static  void  upDateBindInfos(BindTermailInfos bindTermailInfos, Context context){
        try{
            //更新商终信息
            ParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.MD5_KEY, bindTermailInfos.getMd5Key());  //md5
            ParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_POSID,bindTermailInfos.getTerminalNo()); //终端号
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.PARAMS_KEY_CARD_ACCOUNT,bindTermailInfos.getPosStlAc()); //银行卡结算账号
            ParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_QR_CODE_ACCOUNT,bindTermailInfos.getPayStlAc()); //扫码结算账号
            ParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_SCAN_MERCHANTID,bindTermailInfos.getPayMercId()); //扫码商户号
            ParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_KEY_BASE_MERCHANTNAME,bindTermailInfos.getMerNam()); //商户名称
            ParamsUtil.getInstance().update(TransParamsValue.BindParamsContns.PARAMS_SHOP_ID,bindTermailInfos.getStoreNo()); //门店号
            //更新配置数据库信息
            ParamsUtil.getInstance().update(ParamsConts.TransParamsContns.TYANS_BATCHNO,"000001"); //银行卡批次号
            ParamsUtil.getInstance().update(ParamsConts.TransParamsContns.SYSTRANCE_NO,"000001"); //银行卡流水号
            ParamsUtil.getInstance().update(TransParamsValue.TransParamsContns.SCAN_TYANS_BATCHNO,"000001"); //扫码批次号
            ParamsUtil.getInstance().update(TransParamsValue.TransParamsContns.SCAN_SYSTRANCE_NO,"000001"); //扫码流水号

            //签到,参数更新状态
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.PARAMS_CAVERSION,"00"); //IC卡公钥
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.PARAMS_PARAMVERSION,"00"); //IC卡参数
            ParamsUtil.getInstance().update(ParamsConts.BindParamsContns.PARAMS_UPDATASTATUS,"0"); //更新状态
            ParamsUtil.getInstance().update(ParamsConts.SIGN_SYMBOL, TransParams.SingValue.UnSingedValue); //签到更为未签到
            //参数传递
            ShareScanPreferenceUtils.putBoolean(context,TransParamsValue.PARAMS_IS_PARAM_DOWN, true);
            //是否是第一次启动
            ShareScanPreferenceUtils.putBoolean(context,TransParamsValue.PARAMS_KEY_IS_FIRST,true);

        }catch (Exception e){
            e.printStackTrace();
            LogUtils.e("更新终端绑定信息异常:"+e.getMessage());
        }
    }*/

    /**
     * 根据所给的交易类型获得交易的中英文名称
     * @param transType
     * @return 中英文标题数组
     */
    public static String[] getTransType(int transType) {
        String[] transName = new String[2];
        switch (transType) {
            case TransType.ScanTransType.TRANS_SCAN_WEIXIN:// 微信
            case TransType.ScanTransType.TRANS_QR_WEIXIN:
                transName[0] = "微信支付";
                transName[1] = "WEIXIN PAY";
                break;
            case TransType.ScanTransType.TRANS_SCAN_ALIPAY:// 支付宝
            case TransType.ScanTransType.TRANS_QR_ALIPAY:
                transName[0] = "支付宝支付";
                transName[1] = "ALIPAY";
                break;
            case TransType.ScanTransType.TRANS_SCAN_REFUND:
                transName[0] = "扫码退货";
                transName[1] = "REFUND";
                break;
            default:
                transName[0] = "未定义的交易";
                transName[1] = "NOT DIFINE TRANS";
                break;
        }
        return transName;
    }

    /**
     * 获取引起结算中断的步骤标志。
     *
     * @return
     */
    public static String getSettleHaltStep(Context context) {
        String settleHaltStep = "";
        if (ShareScanPreferenceUtils.getBoolean( context, TransParamsValue.SettleConts.SETTLE_ALL_FLAG,false)) {
            if (ShareScanPreferenceUtils.getBoolean(context, TransParamsValue.SettleConts.PARAMS_IS_SCAN_SETTLT_HALT,false)) {
                settleHaltStep = "[1]";
            } else if (ShareScanPreferenceUtils.getBoolean(context, TransParamsValue.SettleConts.PARAMS_IS_PRINT_SETTLE_HALT,false)) {
                settleHaltStep = "[2]";
            } else if (ShareScanPreferenceUtils.getBoolean(context, TransParamsValue.SettleConts.PARAMS_IS_PRINT_ALLWATER_HALT,false)) {
                settleHaltStep = "[3]";
            } else if (ShareScanPreferenceUtils.getBoolean(context, TransParamsValue.SettleConts.PARAMS_IS_CLEAR_SETTLT_HLAT,false)) {
                settleHaltStep = "[4]";
            }
        }
        return settleHaltStep;
    }

    /**
     * 清除数据库所有表的数据
     */
    public  static  void  clearWater(){
        try{
            //清除扫码相关数据
            ScanTransDao dao = new ScanTransDaoImp();
            dao.clearScanWater();
            //清除银行批结数据
            SettleDataDao settle = new SettleDataDaoImpl();
            settle.delete();
            //银行流水表
            TransRecordDao transRecordDao =  new TransRecordDaoImpl();
            transRecordDao.deleteAll();
            //脚本表
            ScriptNotityDao scriptNotityDao = new ScriptNotityDaoImpl();
            scriptNotityDao.deleteAll();
            //冲正表

            ReverseDao reverseDao = new ReverseDaoImpl();
            reverseDao.deleteAll();
            //失败流水表
            EmvFailWaterDao emvFailWaterDao = new EMVFailWaterDaoImpl();
            emvFailWaterDao.deleteAll();

        }catch (Exception e){
            e.printStackTrace();
            LogUtils.e("清除所有数据库异常:"+e.getMessage());
        }
    }

    /**
     * 扫码批结后的操作
     */
    public  static  void clearWaterForScanTrans(){
        try{
            //清除扫码相关数据
            ScanTransDao dao = new ScanTransDaoImp();
            dao.clearScanWater();
            //清除批结失败时存储的数据和结算的时间
            ShareScanPreferenceUtils.clearData(BankApplication.context, TransParamsValue.SettleConts.PARAMS_SETTLE_DATA);
            ShareScanPreferenceUtils.clearData(BankApplication.context, TransParamsValue.SettleConts.PARAMS_SETTLE_TIME);
            //清除结算数据的标志
            ShareScanPreferenceUtils.putBoolean(BankApplication.context, TransParamsValue.SettleConts.PARAMS_IS_CLEAR_SETTLT_HLAT,false);
            //中断总标志
            ShareScanPreferenceUtils.putBoolean(BankApplication.context, TransParamsValue.SettleConts.SETTLE_ALL_FLAG,false);
        } catch (Exception e){
            e.printStackTrace();
            LogUtils.e("清除流水数据异常:"+e.getMessage());
        }
    }
}
