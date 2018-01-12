package com.nld.starpos.banktrade.db.local;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.db.DBOpenHelper;
import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.bean.ParamConfig;
import com.nld.starpos.banktrade.utils.BankConfig;
import com.nld.starpos.banktrade.utils.CommonUtil;
import com.nld.starpos.banktrade.utils.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParamConfigDaoImpl implements ParamConfigDao {


    private DBOpenHelper openHelper;

    public ParamConfigDaoImpl() {
        openHelper = DBOpenHelper.getInstance();
    }

    /**
     * 获取参数配置信息
     */
    @Override
    public synchronized List<ParamConfig> getEntities() {
        List<ParamConfig> list = null;
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select tagname, tagval from param_config ", new String[]{});

        if (cursor != null && cursor.getCount() > 0) {
            list = new ArrayList<ParamConfig>();
            while (cursor.moveToNext()) {
                ParamConfig param = new ParamConfig();
                param.setTagname(cursor.getString(0));
                param.setTagval(cursor.getString(1));
                list.add(param);
            }
        }
        if (null != cursor) {
            cursor.close();
        }
        //db.close();
        return list;
    }

    @Override
    public synchronized Map<String, String> get() {

        Map<String, String> data = null;
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select tagname, tagval from param_config",
                new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            data = new HashMap<String, String>();
            while (cursor.moveToNext()) {
                data.put(cursor.getString(0), cursor.getString(1));
            }
        }
        if (null != cursor) {
            cursor.close();
        }
        //db.close();
        return data;
    }

    @Override
    public synchronized String get(String tagname) {
        String tagval = null;
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select tagval from param_config where tagname=?",
                new String[]{tagname});
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            tagval = cursor.getString(0);
        }
        cursor.close();
        //db.close();
        return tagval;
    }

    /**
     * 保存参数配置信息
     */
    @Override
    public synchronized int save(ParamConfig config) {

        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.execSQL("insert into param_config(tagname, tagval) values(?,?)",
                    new Object[]{config.getTagname(), config.getTagval()});

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            LogUtils.e("保存param_config异常", e);
            return -1;
        } finally {
            //db.close();
        }
        return 1;
    }

    @Override
    public synchronized int save(List<ParamConfig> params) {
        int count = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (ParamConfig param : params) {
                if (isExist(db, param.getTagname())) {
                    db.execSQL("update param_config set tagval=? where tagname=?",
                            new Object[]{param.getTagval(), param.getTagname()});
                } else {
                    db.execSQL("insert into param_config(tagname, tagval) values(?,?)",
                            new Object[]{param.getTagname(), param.getTagval()});
                }
                count++;
            }
            db.setTransactionSuccessful();

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            LogUtils.e("批量保存param_config异常", e);
            return -1;
        } finally {
            db.endTransaction();
            //db.close();
        }
        return count;
    }

    @Override
    public synchronized int save(Map<String, String> map) {
        int count = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (isExist(db, entry.getKey())) {
                    db.execSQL("update param_config set tagval=? where tagname=?",
                            new Object[]{entry.getValue(), entry.getKey()});
                } else {
                    db.execSQL("insert into param_config(tagname, tagval) values(?,?)",
                            new Object[]{entry.getKey(), entry.getValue()});
                }
                count++;
            }
            db.setTransactionSuccessful();
            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            LogUtils.e("批量保存param_config异常", e);
            return -1;
        } finally {
            db.endTransaction();
            //db.close();
        }
        return count;
    }

    @Override
    public synchronized int save(String tagname, String tagval) {
        if (isExist(tagname)) {
            return update(tagname, tagval);
        }
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES(?,?)",
                    new Object[]{tagname, tagval});

//            db.execSQL("insert into param_config(tagname, tagval) values(?,?)",
//                    new Object[] { tagname, tagval });

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            LogUtils.e("保存param_config异常", e);
            return -1;
        }
        //db.close();
        return 1;
    }

    /**
     * 更新参数配置信息
     */
    @Override
    public synchronized int update(ParamConfig param) {
        SQLiteDatabase db = openHelper.getWritableDatabase();

        try {
            db.execSQL("update param_config set tagval=? where tagname=?",
                    new Object[]{param.getTagval(), param.getTagname()});

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            LogUtils.e("更新param_config异常", e);
            return -1;
        } finally {
            //db.close();
        }
        return 1;
    }

    @Override
    public synchronized int update(Map<String, String> map) {
        int count = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                db.execSQL("update param_config set tagval=? where tagname=?",
                        new Object[]{entry.getValue(), entry.getKey()});
                count++;
            }
            db.setTransactionSuccessful();

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            LogUtils.e("批量更新param_config异常", e);
            return -1;
        } finally {
            db.endTransaction();
            //db.close();
        }
        return count;
    }

    @Override
    public synchronized int update(String tagname, String tagval) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.execSQL("update param_config set tagval=? where tagname=?",
                    new Object[]{tagval, tagname});

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            LogUtils.e("更新param_config异常", e);
            return -1;
        } finally {
            //db.close();
        }
        return 1;
    }

    /**
     * 删除参数配置信息
     */
    @Override
    public synchronized int delete(ParamConfig param) {
        int result = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.execSQL("delete from transrecord where tagname=?",
                    new Object[]{param.getTagname()});
            result = 1;

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            result = -1;
            LogUtils.e("删除transrecord异常", e);
        } finally {
            //db.close();
        }
        return result;
    }

    @Override
    public synchronized int delete(String tagname) {
        int result = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.execSQL("delete from transrecord where tagname=?",
                    new Object[]{tagname});
            result = 1;

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            result = -1;
            LogUtils.e("删除transrecord异常", e);
        } finally {
            //db.close();
        }
        return result;
    }

    @Override
    public boolean isExist(String tagname) {
        int count = 0;
        Cursor cursor = null;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            cursor = db
                    .rawQuery(
                            "select tagname, tagval from param_config where tagname=? ",
                            new String[]{tagname});
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            LogUtils.e("系统错误，数据库操作异常", e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
            //db.close();
        }
        return count != 0;
    }

    /**
     * @param db
     * @param tagname
     * @return
     */
    @Override
    public boolean isExist(SQLiteDatabase db, String tagname) {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = db
                    .rawQuery(
                            "select tagname, tagval from param_config where tagname=? ",
                            new String[]{tagname});
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            LogUtils.e("系统错误，数据库操作异常", e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return count != 0;
    }

    @Override
    public synchronized int syncXmlUpdate(List<ParamConfig> params) {
        int count = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (ParamConfig param : params) {

                if (isExist(db, param.getTagname())) {
                    db.execSQL(
                            "update param_config set tagval=? where tagname=?",
                            new Object[]{param.getTagval(),
                                    param.getTagname()});
                } else {
                    db.execSQL(
                            "insert into param_config(tagname, tagval) values(?,?)",
                            new Object[]{param.getTagname(),
                                    param.getTagval()});
                }
                count++;
            }
            db.setTransactionSuccessful();

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            LogUtils.e("批量保存param_config异常", e);
            return -1;
        } finally {
            db.endTransaction();
            //db.close();
        }
        return count;
    }

    /**
     * 删除并重建数据库
     *
     * @return 1、成功 ; -1、失败
     * @author Xrh
     */
    @Override
    public synchronized int dropTable() {
        int result = 0;

        SQLiteDatabase db = openHelper.getReadableDatabase();
        db.beginTransaction();

        try {
            LogUtils.d("正在删除数据库表...");
            db.execSQL("drop table if exists param_config");
            db.execSQL("drop table if exists transrecord");
            db.execSQL("drop table if exists settledata");
            db.execSQL("drop table if exists reverse");

            db.execSQL("drop table if exists scriptnotity");
            db.execSQL("drop table if exists ScanTrans");
            db.execSQL("drop table if exists emvfailwater");

            LogUtils.d("正在创建参数表...");

            db.execSQL("CREATE TABLE IF NOT EXISTS param_config("
                    //+"id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "tagname VARCHAR(32) PRIMARY KEY,"
                    + "tagval VARCHAR(128))");

            //绑定终端的相关信息
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('md5_key','')"); //md5_key
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('unionpay_merid','')"); //银联商户号
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('scan_merid','')"); //扫码商户号
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('mchntname','')"); //商户名称
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('termid','')"); //终端号
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('shop_id','')"); //门店号
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('scan_account','')"); //二维账码结算号
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('unionpay_card_account','')"); //银行结算账号
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('unionpay_termid','')"); //银联终端号

            //流水号与批次号<银行卡与扫码共用>
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('batchno','000006')");// 批次号
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('systraceno','000001')"); //pos流水号

            //操作员
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('operatorcode','001')");//操作员号，默认

            LogUtils.d("正在初始化参数表...");
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('dealtimeout','60')");//交易超时
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('standbytimeout','60')");//待机超时,原生新添加记录
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('reversetimes','3')"); //冲正次数
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('tpdu','6009030000')"); //TPDU
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('unionpay_tpdu','" + BankConfig.TPDU_UNIONPAY + "')"); //银联TPDU
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('systracemax','500')"); //流水上限
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('neednopin','0')"); //非接免密 0为需要密码 1为不需要密码
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('nopinamount','300')"); //非接免密上限
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('printtimes','2')"); //打印联数
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('reconntimes','3')");
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('redowntimes','3')");
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('operatorpwd','0000')");//操作员密码
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('dowmloadparam','0')");//参数下载状态 0为签退后未执行参数下载，1为签退后有执行参数下载


            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('wxmerid','')"); //微信商户号
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('wxmchntname','')"); //微信商户名称
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('wxtermid','')"); //微信终端号
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('wxcapwd','')"); //微信卡密码
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('wxmerAddr','')"); //微信商户地址

            db.execSQL("Replace INTO param_config(tagname,tagval) VALUES('operpswd','6EB77D55F6C6EBB5EDDF310EAB6AA724')"); //运维密码12369874
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('adminpwd','E10ADC3949BA59ABBE56E057F20F883E')"); //主管密码

            String ipPortUnionPay = CommonUtil.getIpAndPortUnionPay();
            db.execSQL("Replace INTO param_config(tagname,tagval) VALUES('connect_mode','1')");//通讯模式0表示3g；1表示专网
            //---------------------------
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('unionpay_trans_url','https://" + ipPortUnionPay + "')"); //银联交易地址


            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('billno','000001')");
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('pinpadType','0')");        //密码键盘类型   -- 0外置;1内置

            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('enabled','0')");//是否激活
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('enabled2','0')");//是否确认激活
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('fid','YP')");//厂商标识   测试200021 ;  生产  207

            /**add for save settleData by chenkehui @2013.07.06 begin*/
            //打印状态，“”为无打印数据状态；“strans”为交易数据未打印完成；“settle”为结算数据未打印完成
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('printsymbol','')");

            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('settlebatchno','')"); //结算批次号
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('translocaldate','')"); //结算日期
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('translocaltime','')"); //结算时间
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('respcode','')"); //结算结果
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('requestSettleData','')"); //上送的结算信息
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('settledata','')"); //下发的结算信息
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('wxsettlebatchno','')"); //微信结算批次号
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('wxtranslocaldate','')"); //微信结算日期
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('wxtranslocaltime','')"); //微信结算时间
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('wxrespcode','')"); //微信结算结果
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('wxrequestSettleData','')"); //微信上送的结算信息
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('wxsettledata','')"); //微信下发的结算信息

            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('signsymbol','0')"); //签到状态，“0”为未签到
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('wxsignsymbol','0')"); //签到状态，“0”为未签到
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('settlesymbol','0')"); //结算状态，“0”为非结算状态

            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('printAppVersionId','SD_" + CommonUtil.getAppVersion() + "')"); //凭条打印收单版本号


            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('mkeyidsymbol','1')"); //密钥是否改变标志
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('" + Constant.FIELD_NEW_MKEY_ID + "','3')"); //新主密钥索引
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('" + Constant.FIELD_NEW_PIK_ID + "','3')"); //PIK索引
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('" + Constant.FIELD_NEW_TDK_ID + "','3')"); //TDK索引
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('" + Constant.FIELD_NEW_MAK_ID + "','3')"); //MAK索引
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('oldmkeyid','0')"); //旧主密钥索引
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('" + Constant.FIELD_WX_NEW_MKEY_ID + "','2')"); //新主密钥索引
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('" + Constant.FIELD_WX_NEW_PIK_ID + "','2')"); //WXPIK索引
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('" + Constant.FIELD_WX_NEW_TDK_ID + "','2')"); //WXTDK索引
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('" + Constant.FIELD_WX_NEW_MAK_ID + "','2')"); //WXMAK索引
//            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('tmkKeyId_setOff','1')"); //设置主密钥索引偏移量

            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('posparamsymbol','1')"); //第一次安装应用判断是否需弹出，未下发配置文件提示
            /**add for save settleData by chenkehui @2013.07.06 end*/
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('caversion','00')");    //当前IC卡公钥版本
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('paramversion','00')");    //当前IC卡参数版本
            db.execSQL("INSERT INTO param_config(tagname,tagval) VALUES('updatastatus','0')");    //更新状态，“1” 公钥需要更新， “2”参数需要更新

            LogUtils.d("正在创建交易表...");
            //交易过程记录表
            db.execSQL("CREATE TABLE IF NOT EXISTS transrecord("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "priaccount VARCHAR(19),"  //主账号
                    + "transprocode VARCHAR(6),"  //交易处理码
                    + "transamount  VARCHAR(12),"  //交易金额
                    + "systraceno VARCHAR(6),"     //POS流水号
                    + "translocaltime VARCHAR(6),"  //交易时间
                    + "translocaldate VARCHAR(4),"  //交易日期
                    + "expireddate VARCHAR(4),"     //卡有效期
                    + "entrymode VARCHAR(3),"       //POS输入方式码
                    + "seqnumber VARCHAR(3),"        //卡序列号
                    + "conditionmode VARCHAR(2),"   //服务条件码

                    + "updatecode VARCHAR(2),"      //更新标识码
                    + "track2data VARCHAR(38),"     //2磁道数据
                    + "track3data VARCHAR(104),"    //3磁道数据
                    + "refernumber VARCHAR(12),"    //系统参考号
                    + "idrespcode VARCHAR(6),"      //授权码
                    + "respcode VARCHAR(2),"        //返回码
                    + "terminalid VARCHAR(8),"     //终端号
                    + "acceptoridcode VARCHAR(15),"  //商户号
                    + "acceptoridname VARCHAR(40),"  //商户名称
                    + "addrespkey VARCHAR(64),"      //附加响应-密钥数据

                    + "adddataword VARCHAR(512),"    //附加数据-文字信息
                    + "transcurrcode VARCHAR(3),"    //交易货币代码
                    + "pindata VARCHAR(12),"         //个人密码PIN
                    + "secctrlinfo VARCHAR(16),"     //安全控制信息
                    + "balanceamount VARCHAR(26),"   //附加金额
                    + "icdata VARCHAR(255),"        //IC卡数据域
                    + "adddatapri VARCHAR(100),"    //附件数据-私有
                    + "pbocdata VARCHAR(100),"      //PBOC电子钱包标准的交易信息
                    + "loadparams VARCHAR(100),"    //参数下装信息
                    + "cardholderid VARCHAR(18),"   //持卡人身份证

                    + "batchbillno VARCHAR(12),"    //批次号票据号
                    + "settledata VARCHAR(126),"    //结算信息
                    + "mesauthcode VARCHAR(8),"     //消息认证码
                    + "statuscode VARCHAR(2),"     //交易结果状态码
                    + "reversetimes VARCHAR(2),"     //冲正次数
                    + "reserve1 VARCHAR(100),"
                    + "reserve2 VARCHAR(100),"
                    + "reserve3 VARCHAR(100),"
                    + "reserve4 VARCHAR(100),"
                    + "reserve5 VARCHAR(100))");

            LogUtils.d("正在创建冲正表...");
            //冲正表
            db.execSQL("CREATE TABLE IF NOT EXISTS reverse("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "priaccount VARCHAR(19),"  //主账号
                    + "transprocode VARCHAR(6),"  //交易处理码
                    + "transamount  VARCHAR(12),"  //交易金额
                    + "systraceno VARCHAR(6),"     //POS流水号
                    + "translocaltime VARCHAR(6),"  //交易时间
                    + "translocaldate VARCHAR(4),"  //交易日期
                    + "expireddate VARCHAR(4),"     //卡有效期
                    + "entrymode VARCHAR(3),"       //POS输入方式码
                    + "seqnumber VARCHAR(3),"        //卡序列号
                    + "conditionmode VARCHAR(2),"   //服务条件码
                    + "updatecode VARCHAR(2),"      //更新标识码
                    + "track2data VARCHAR(38),"     //2磁道数据
                    + "track3data VARCHAR(104),"    //3磁道数据
                    + "refernumber VARCHAR(12),"    //系统参考号
                    + "idrespcode VARCHAR(6),"      //授权码
                    + "respcode VARCHAR(2),"        //返回码
                    + "terminalid VARCHAR(8),"     //终端号
                    + "acceptoridcode VARCHAR(15),"  //商户号
                    + "acceptoridname VARCHAR(40),"  //商户名称
                    + "addrespkey VARCHAR(64),"      //附加响应-密钥数据
                    + "adddataword VARCHAR(512),"    //附加数据-文字信息
                    + "transcurrcode VARCHAR(3),"    //交易货币代码
                    + "pindata VARCHAR(12),"         //个人密码PIN
                    + "secctrlinfo VARCHAR(16),"     //安全控制信息
                    + "balanceamount VARCHAR(26),"   //附加金额
                    + "icdata VARCHAR(255),"        //IC卡数据域
                    + "adddatapri VARCHAR(100),"    //附件数据-私有
                    + "pbocdata VARCHAR(100),"      //PBOC电子钱包标准的交易信息
                    + "loadparams VARCHAR(100),"    //参数下装信息
                    + "cardholderid VARCHAR(18),"   //持卡人身份证
                    + "batchbillno VARCHAR(12),"    //批次号票据号
                    + "settledata VARCHAR(126),"    //结算信息
                    + "mesauthcode VARCHAR(8),"     //消息认证码
                    + "statuscode VARCHAR(2),"     //交易结果状态码
                    + "reversetimes VARCHAR(2),"     //冲正次数
                    + "reserve1 VARCHAR(100),"
                    + "reserve2 VARCHAR(100),"
                    + "reserve3 VARCHAR(100),"
                    + "reserve4 VARCHAR(100),"
                    + "reserve5 VARCHAR(100))");

            LogUtils.d("正在创建结算表...");
            //结算信息表
            db.execSQL("CREATE TABLE IF NOT EXISTS settledata("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "priaccount VARCHAR(19),"  //主账号
                    + "transprocode VARCHAR(6),"  //交易处理码
                    + "transamount  VARCHAR(12),"  //交易金额
                    + "systraceno VARCHAR(6),"     //POS流水号
                    + "translocaltime VARCHAR(6),"  //交易时间
                    + "translocaldate VARCHAR(4),"  //交易日期
                    + "expireddate VARCHAR(4),"     //卡有效期
                    + "entrymode VARCHAR(3),"       //POS输入方式码
                    + "seqnumber VARCHAR(3),"        //卡序列号
                    + "conditionmode VARCHAR(2),"   //服务条件码
                    + "updatecode VARCHAR(2),"      //更新标识码
                    + "track2data VARCHAR(38),"     //2磁道数据
                    + "track3data VARCHAR(104),"    //3磁道数据
                    + "refernumber VARCHAR(12),"    //系统参考号
                    + "idrespcode VARCHAR(6),"      //授权码
                    + "respcode VARCHAR(2),"        //返回码
                    + "terminalid VARCHAR(8),"     //终端号
                    + "acceptoridcode VARCHAR(15),"  //商户号
                    + "acceptoridname VARCHAR(40),"  //商户名称
                    + "addrespkey VARCHAR(64),"      //附加响应-密钥数据
                    + "adddataword VARCHAR(512),"    //附加数据-文字信息
                    + "transcurrcode VARCHAR(3),"    //交易货币代码
                    + "pindata VARCHAR(12),"         //个人密码PIN
                    + "secctrlinfo VARCHAR(16),"     //安全控制信息
                    + "balanceamount VARCHAR(26),"   //附加金额
                    + "icdata VARCHAR(255),"        //IC卡数据域
                    + "adddatapri VARCHAR(100),"    //附件数据-私有
                    + "pbocdata VARCHAR(100),"      //PBOC电子钱包标准的交易信息
                    + "loadparams VARCHAR(100),"    //参数下装信息
                    + "cardholderid VARCHAR(18),"   //持卡人身份证
                    + "batchbillno VARCHAR(12),"    //批次号票据号
                    + "settledata VARCHAR(126),"    //结算信息
                    + "mesauthcode VARCHAR(8),"     //消息认证码
                    + "statuscode VARCHAR(2),"     //交易结果状态码
                    + "reversetimes VARCHAR(2),"     //冲正次数
                    + "reserve1 VARCHAR(100),"
                    + "reserve2 VARCHAR(100),"
                    + "reserve3 VARCHAR(100),"
                    + "reserve4 VARCHAR(100),"
                    + "reserve5 VARCHAR(100))");
            //脚本表
            db.execSQL("CREATE TABLE IF NOT EXISTS scriptnotity("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "priaccount VARCHAR(19),"  //主账号
                    + "transprocode VARCHAR(6),"  //交易处理码
                    + "transamount  VARCHAR(12),"  //交易金额
                    + "systraceno VARCHAR(6),"     //POS流水号
                    + "translocaltime VARCHAR(6),"  //交易时间
                    + "translocaldate VARCHAR(4),"  //交易日期
                    + "expireddate VARCHAR(4),"     //卡有效期
                    + "entrymode VARCHAR(3),"       //POS输入方式码
                    + "seqnumber VARCHAR(3),"        //卡序列号
                    + "conditionmode VARCHAR(2),"   //服务条件码
                    + "updatecode VARCHAR(2),"      //更新标识码
                    + "track2data VARCHAR(38),"     //2磁道数据
                    + "track3data VARCHAR(104),"    //3磁道数据
                    + "refernumber VARCHAR(12),"    //系统参考号
                    + "idrespcode VARCHAR(6),"      //授权码
                    + "respcode VARCHAR(2),"        //返回码
                    + "terminalid VARCHAR(8),"     //终端号
                    + "acceptoridcode VARCHAR(15),"  //商户号
                    + "acceptoridname VARCHAR(40),"  //商户名称
                    + "addrespkey VARCHAR(64),"      //附加响应-密钥数据
                    + "adddataword VARCHAR(512),"    //附加数据-文字信息
                    + "transcurrcode VARCHAR(3),"    //交易货币代码
                    + "pindata VARCHAR(12),"         //个人密码PIN
                    + "secctrlinfo VARCHAR(16),"     //安全控制信息
                    + "balanceamount VARCHAR(26),"   //附加金额
                    + "icdata VARCHAR(255),"        //IC卡数据域
                    + "adddatapri VARCHAR(100),"    //附件数据-私有
                    + "pbocdata VARCHAR(100),"      //PBOC电子钱包标准的交易信息
                    + "loadparams VARCHAR(100),"    //参数下装信息
                    + "cardholderid VARCHAR(18),"   //持卡人身份证
                    + "batchbillno VARCHAR(12),"    //批次号票据号
                    + "settledata VARCHAR(126),"    //结算信息
                    + "mesauthcode VARCHAR(8),"     //消息认证码
                    + "statuscode VARCHAR(2),"     //交易结果状态码
                    + "reversetimes VARCHAR(2),"     //冲正次数
                    + "reserve1 VARCHAR(100),"
                    + "reserve2 VARCHAR(100),"        //受理方标识码
                    + "reserve3 VARCHAR(100),"
                    + "reserve4 VARCHAR(100),"
                    + "reserve5 VARCHAR(100))");


            //微信结算信息表
            db.execSQL("CREATE TABLE IF NOT EXISTS wxsettledata("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "priaccount VARCHAR(19),"  //主账号
                    + "transprocode VARCHAR(6),"  //交易处理码
                    + "transamount  VARCHAR(12),"  //交易金额
                    + "systraceno VARCHAR(6),"     //POS流水号
                    + "translocaltime VARCHAR(6),"  //交易时间
                    + "translocaldate VARCHAR(4),"  //交易日期
                    + "expireddate VARCHAR(4),"     //卡有效期
                    + "entrymode VARCHAR(3),"       //POS输入方式码
                    + "seqnumber VARCHAR(3),"        //卡序列号
                    + "conditionmode VARCHAR(2),"   //服务条件码
                    + "updatecode VARCHAR(2),"      //更新标识码
                    + "track2data VARCHAR(38),"     //2磁道数据
                    + "track3data VARCHAR(104),"    //3磁道数据
                    + "refernumber VARCHAR(12),"    //系统参考号
                    + "idrespcode VARCHAR(6),"      //授权码
                    + "respcode VARCHAR(2),"        //返回码
                    + "terminalid VARCHAR(8),"     //终端号
                    + "acceptoridcode VARCHAR(15),"  //商户号
                    + "acceptoridname VARCHAR(40),"  //商户名称
                    + "addrespkey VARCHAR(64),"      //附加响应-密钥数据
                    + "adddataword VARCHAR(512),"    //附加数据-文字信息
                    + "transcurrcode VARCHAR(3),"    //交易货币代码
                    + "pindata VARCHAR(12),"         //个人密码PIN
                    + "secctrlinfo VARCHAR(16),"     //安全控制信息
                    + "balanceamount VARCHAR(26),"   //附加金额
                    + "icdata VARCHAR(255),"        //IC卡数据域
                    + "adddatapri VARCHAR(100),"    //附件数据-私有
                    + "pbocdata VARCHAR(100),"      //PBOC电子钱包标准的交易信息
                    + "loadparams VARCHAR(100),"    //参数下装信息
                    + "cardholderid VARCHAR(18),"   //持卡人身份证
                    + "batchbillno VARCHAR(12),"    //批次号票据号
                    + "settledata VARCHAR(126),"    //结算信息
                    + "mesauthcode VARCHAR(8),"     //消息认证码
                    + "statuscode VARCHAR(2),"     //交易结果状态码
                    + "reversetimes VARCHAR(2),"     //冲正次数
                    + "reserve1 VARCHAR(100),"
                    + "reserve2 VARCHAR(100),"
                    + "reserve3 VARCHAR(100),"
                    + "reserve4 VARCHAR(100),"
                    + "reserve5 VARCHAR(100))");


            //微信交易过程记录表
            db.execSQL("CREATE TABLE IF NOT EXISTS wxtransrecord("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "priaccount VARCHAR(19),"  //主账号
                    + "transprocode VARCHAR(6),"  //交易处理码
                    + "transamount  VARCHAR(12),"  //交易金额
                    + "systraceno VARCHAR(6),"     //POS流水号
                    + "translocaltime VARCHAR(6),"  //交易时间
                    + "translocaldate VARCHAR(4),"  //交易日期
                    + "expireddate VARCHAR(4),"     //卡有效期
                    + "entrymode VARCHAR(3),"       //POS输入方式码
                    + "seqnumber VARCHAR(3),"        //卡序列号
                    + "conditionmode VARCHAR(2),"   //服务条件码

                    + "updatecode VARCHAR(2),"      //更新标识码
                    + "track2data VARCHAR(38),"     //2磁道数据
                    + "track3data VARCHAR(104),"    //3磁道数据
                    + "refernumber VARCHAR(12),"    //系统参考号
                    + "idrespcode VARCHAR(6),"      //授权码
                    + "respcode VARCHAR(2),"        //返回码
                    + "terminalid VARCHAR(8),"     //终端号
                    + "acceptoridcode VARCHAR(15),"  //商户号
                    + "acceptoridname VARCHAR(40),"  //商户名称
                    + "addrespkey VARCHAR(64),"      //附加响应-密钥数据

                    + "adddataword VARCHAR(512),"    //附加数据-文字信息
                    + "transcurrcode VARCHAR(3),"    //交易货币代码
                    + "pindata VARCHAR(12),"         //个人密码PIN
                    + "secctrlinfo VARCHAR(16),"     //安全控制信息
                    + "balanceamount VARCHAR(26),"   //附加金额
                    + "icdata VARCHAR(255),"        //IC卡数据域
                    + "adddatapri VARCHAR(100),"    //附件数据-私有
                    + "pbocdata VARCHAR(100),"      //PBOC电子钱包标准的交易信息
                    + "loadparams VARCHAR(100),"    //参数下装信息
                    + "cardholderid VARCHAR(18),"   //持卡人身份证

                    + "batchbillno VARCHAR(12),"    //批次号票据号
                    + "settledata VARCHAR(126),"    //结算信息
                    + "mesauthcode VARCHAR(8),"     //消息认证码
                    + "statuscode VARCHAR(2),"     //交易结果状态码
                    + "reversetimes VARCHAR(2),"     //冲正次数
                    + "reserve1 VARCHAR(100),"       //应答的消息类型
                    + "reserve2 VARCHAR(100),"       //受理方标识码
                    + "reserve3 VARCHAR(100),"       //TC、AAC、ARPC上送报文中F55
                    + "reserve4 VARCHAR(100),"       //基于PBOC的交易中需要在备注中打印的数据
                    + "reserve5 VARCHAR(100))");


            db.execSQL("CREATE TABLE IF NOT EXISTS ScanTrans("
                    +"id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    +"transamount  VARCHAR(12),"  //交易金额
                    +"systraceno VARCHAR(6),"     //pos交易流水号
                    +"logNo VARCHAR(32),"     //系统流水号
                    +"member VARCHAR(100),"     //会员信息
                    +"payType VARCHAR(2),"     //支付类型
                    +"translocaltime VARCHAR(6),"  //交易时间
                    +"translocaldate VARCHAR(4),"  //交易日期
                    +"settledata VARCHAR(126),"    //结算日期
                    +"terminalid VARCHAR(8),"     //终端号
                    +"acceptoridcode VARCHAR(15),"  //商户号
                    +"acceptoridname VARCHAR(40),"  //商户名称
                    +"batchbillno VARCHAR(12),"    //批次号
                    +"statuscode VARCHAR(2),"     //交易结果状态码
                    +"oper VARCHAR(10),"     //操作员
                    +"adddataword VARCHAR(512),"    //附加数据-文字信息
                    +"transcurrcode VARCHAR(3),"    //交易货币代码
                    +"payChannel VARCHAR(20),"  //扫码交易渠道
                    +"orderNo VARCHAR(20),"  //订单号
                    +"respcode VARCHAR(2),"        //返回码
                    +"transprocode VARCHAR(6),"  //交易处理码
                    +"transtotalamount VARCHAR(6),"  //交易总金额
                    +"authCode VARCHAR(6),"  //授权码
                    +"isrevoke VARCHAR(6),"  //交易是否撤销
                    +"oldType VARCHAR(10),"  //原交易类型
                    +"type VARCHAR(100))");    //交易类型


            db.execSQL("CREATE TABLE IF NOT EXISTS emvfailwater("
                    +"id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    +"priaccount VARCHAR(19),"  //主账号
                    +"transprocode VARCHAR(6),"  //交易处理码
                    +"transamount  VARCHAR(12),"  //交易金额
                    +"systraceno VARCHAR(6),"     //POS流水号
                    +"translocaltime VARCHAR(6),"  //交易时间
                    +"translocaldate VARCHAR(4),"  //交易日期
                    +"expireddate VARCHAR(4),"     //卡有效期
                    +"entrymode VARCHAR(3),"       //POS输入方式码
                    +"seqnumber VARCHAR(3),"        //卡序列号
                    +"conditionmode VARCHAR(2),"   //服务条件码
                    +"updatecode VARCHAR(2),"      //更新标识码
                    +"track2data VARCHAR(38),"     //2磁道数据
                    +"track3data VARCHAR(104),"    //3磁道数据
                    +"refernumber VARCHAR(12),"    //系统参考号
                    +"idrespcode VARCHAR(6),"      //授权码
                    +"respcode VARCHAR(2),"        //返回码
                    +"terminalid VARCHAR(8),"     //终端号
                    +"acceptoridcode VARCHAR(15),"  //商户号
                    +"acceptoridname VARCHAR(40),"  //商户名称
                    +"addrespkey VARCHAR(64),"      //附加响应-密钥数据
                    +"adddataword VARCHAR(512),"    //附加数据-文字信息
                    +"transcurrcode VARCHAR(3),"    //交易货币代码
                    +"pindata VARCHAR(12),"         //个人密码PIN
                    +"secctrlinfo VARCHAR(16),"     //安全控制信息
                    +"balanceamount VARCHAR(26),"   //附加金额
                    +"icdata VARCHAR(255),"        //IC卡数据域
                    +"adddatapri VARCHAR(100),"    //附件数据-私有
                    +"pbocdata VARCHAR(100),"      //PBOC电子钱包标准的交易信息
                    +"loadparams VARCHAR(100),"    //参数下装信息
                    +"cardholderid VARCHAR(18),"   //持卡人身份证
                    +"batchbillno VARCHAR(12),"    //批次号票据号
                    +"settledata VARCHAR(126),"    //结算信息
                    +"mesauthcode VARCHAR(8),"     //消息认证码
                    +"statuscode VARCHAR(2),"     //交易结果状态码
                    +"reversetimes VARCHAR(2),"     //冲正次数
                    +"reserve1 VARCHAR(100),"		//应答的消息类型
                    +"reserve2 VARCHAR(100),"		//受理方标识码
                    +"reserve3 VARCHAR(100),"		//TC、AAC、ARPC上送报文中F55
                    +"reserve4 VARCHAR(100),"		//基于PBOC的交易中需要在备注中打印的数据
                    +"reserve5 VARCHAR(100),"
                    +"type VARCHAR(100))");       //失败流水类型

            db.setTransactionSuccessful();
            result = 1;
            LogUtils.d("重建数据库表结束");
        } catch (Exception e) {
            result = -1;
            LogUtils.e("重建数据库表异常", e);
        } finally {
            db.endTransaction();
            //db.close();
        }
        return result;
    }

}