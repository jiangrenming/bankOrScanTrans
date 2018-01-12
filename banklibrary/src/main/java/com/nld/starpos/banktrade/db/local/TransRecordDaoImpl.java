package com.nld.starpos.banktrade.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.nld.logger.LogUtils;
import com.nld.starpos.banktrade.db.DBOpenHelper;
import com.nld.starpos.banktrade.db.TransRecordDao;
import com.nld.starpos.banktrade.db.bean.TransRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * 访问交易记录 的DAO实现层
 * 作者：cxy
 * 时间：2013.03.04
 */
public class TransRecordDaoImpl implements TransRecordDao {



    private DBOpenHelper openHelper;

    public TransRecordDaoImpl() {
        openHelper = DBOpenHelper.getInstance();
    }

    @Override
    public synchronized TransRecord getTransRecordByPage(int pageNum) {
        TransRecord record = null;
        SQLiteDatabase db = openHelper.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from transrecord where transprocode <> '900000' and (statuscode <> 'AC' or statuscode is null) order by translocaldate desc,translocaltime desc limit 1 offset " + pageNum,
                    new String[]{});  //按时间倒序排序
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                record = analysis2TransRecord(cursor);
            }
        } catch (Exception e) {
            LogUtils.e("获取最后一笔交易记录发生异常", e);
            e.printStackTrace();
        }
        cursor.close();
        //db.close();
        return record;
    }

    @Override
    public synchronized List<TransRecord> getEntities() {
        List<TransRecord> list = new ArrayList();
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from transrecord where transprocode <> '900000' and (statuscode <> 'AC' or statuscode is null) order by translocaldate desc,translocaltime desc ",
                new String[]{});  //按时间倒序排序
        if (cursor != null && cursor.getCount() > 0) {
            list = new ArrayList<TransRecord>();
            while (cursor.moveToNext()) {
                TransRecord record = analysis2TransRecord(cursor);
                list.add(record);
            }
        }
        cursor.close();
        //db.close();
        return list;
    }

    /**
     * 获取IC卡交易批上送数据
     * @return
     */
    @Override
    public synchronized List<TransRecord> getICUnBatchSendSucc() {
        List<TransRecord> list = new ArrayList();
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from transrecord where ( entrymode = '051' or entrymode = '052' ) and ( reserve5 < 3 or reserve5 is null ) order by reserve5 asc , translocaldate desc,translocaltime desc",
                new String[]{});  //按时间倒序排序
        if (cursor != null && cursor.getCount() > 0) {
            list = new ArrayList<TransRecord>();
            while (cursor.moveToNext()) {
                TransRecord record = analysis2TransRecord(cursor);
                list.add(record);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        LogUtils.d("插卡批上送数据条数： " + list.size());
        return list;
    }

    //获取最后一笔交易记录
    @Override
    public synchronized TransRecord getLastTransRecord() {
        TransRecord record = null;
        SQLiteDatabase db = openHelper.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from transrecord where transprocode <> '900000' and (statuscode <> 'AC' or statuscode is null) order by translocaldate desc,translocaltime desc ",
                    new String[]{});
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                record = analysis2TransRecord(cursor);
            }
        } catch (Exception e) {
            LogUtils.e("获取最后一笔交易记录发生异常", e);
            e.printStackTrace();
        }
        cursor.close();
        //db.close();
        return record;
    }

    /**
     * 按交易类型查询
     *
     * @return add by chenkehui
     */
    @Override
    public synchronized List<TransRecord> getCountTransRecord(String transprocode, String conditionmode, String reserve1) {
        List<TransRecord> list = null;
        TransRecord record = null;
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from transrecord where transprocode = ? and conditionmode = ? and reserve1 = ?" +
                            "and (statuscode <> 'AC' or statuscode is null) order by translocaldate desc,translocaltime desc ",
                    new String[]{transprocode, conditionmode, reserve1});
            if (cursor != null && cursor.getCount() > 0) {
                list = new ArrayList<TransRecord>();
                while (cursor.moveToNext()) {
                    record = analysis2TransRecord(cursor);
                    list.add(record);
                }
            }
        } catch (Exception e) {
            LogUtils.e("获取" + transprocode + "交易类型发生异常", e);
            e.printStackTrace();
        }
        cursor.close();
        //db.close();
        return list;
    }

    /**
     * 根据不同交易类型获取不同交易的数据
     *
     * @param type
     * @return
     */
    @Override
    public synchronized List<TransRecord> getTransRecordByType(String type) {
        List<TransRecord> list = null;
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from transrecord where type = ?", new String[]{type});
            if (null != cursor && cursor.getCount() > 0) {
                list = new ArrayList<>();
                while (cursor.moveToNext()) {
                    TransRecord record = analysis2TransRecord(cursor);
                    list.add(record);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cursor.close();
        return list;
    }

    @Override
    public String getTransCountByType(String type) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String count = "0";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select count(*) from transrecord where transType = ?", new String[]{type});
            if (cursor != null) {
                cursor.moveToFirst();
                return cursor.getString(0);
            }
        } catch (Exception e) {
            Log.i("TAG", "交易类型 ： " + type + " ，获取总数失败");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "0";
    }

    @Override
    public String getTransCountByKV(String key, String value) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String count = "0";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select count(*) from transrecord where " + key + " = ?", new String[]{value});
            if (cursor != null) {
                cursor.moveToFirst();
                return cursor.getString(0);
            }
        } catch (Exception e) {
            LogUtils.d("交易类型 ： " + key + " ，获取总数失败");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "0";
    }

    @Override
    public String getTransAmountByType(String type) {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String count = "0";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select sum(transamount) from transrecord where transType = ?", new String[]{type});
            if (cursor != null) {
                cursor.moveToFirst();
                String totalAmount = cursor.getString(0);
                return totalAmount;
            }
        } catch (Exception e) {
            Log.i("TAG", "交易类型 ： " + type + " ，获取总数失败");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "0";
    }

    @Override
    public String getBatchSendSucCount() {
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String count = "0";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select count(*) from transrecord where transprocode <> '900000' and reserve5 = '10'", new String[]{});
            if (cursor != null) {
                cursor.moveToFirst();
                return cursor.getString(0);
            }
        } catch (Exception e) {
            LogUtils.d(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "0";
    }

    /**
     * 获取刷卡交易批上送数据
     * @return
     */
    @Override
    public synchronized List<TransRecord> getUnBatchMagcardList() {
        List<TransRecord> list = new ArrayList();
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from transrecord where (entrymode = '021' or entrymode = '022') and (reserve5 < 3 or reserve5 is null ) order by translocaldate desc,translocaltime desc",
                new String[]{});  //按时间倒序排序
        if (cursor != null && cursor.getCount() > 0) {
            list = new ArrayList<TransRecord>();
            while (cursor.moveToNext()) {
                TransRecord record = analysis2TransRecord(cursor);
                list.add(record);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        LogUtils.d("刷卡批上送数据： " + list.toString());
        return list;
    }

    /**
     * 根据批次号和票据号获取单个消费
     */
    @Override
    public synchronized TransRecord getTransRecordByCondition(String batchno,
                                                              String billno) {
        LogUtils.i("<按票据凭证号查询>batchno=[" + batchno + "],billno=[" + billno + "]");
        TransRecord record = null;
        if (batchno == null || billno == null) {
            return record;
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from transrecord where batchbillno like ?",
                    new String[]{batchno + billno + "%"});

            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                record = analysis2TransRecord(cursor);
            }
        } catch (Exception e) {
            LogUtils.e("根据批次号和流水号获取单个交易记录对象发生异常", e);
            e.printStackTrace();
        }
        cursor.close();
        //db.close();
        return record;
    }

    /**
     * 根据批次号票据号获取单个消费
     */
    @Override
    public synchronized TransRecord getTransRecordByCondition(String batchnoBillno) {
        LogUtils.i("<按票据凭证号查询>batchnoBillno=[" + batchnoBillno + "]");
        TransRecord record = null;
        if (batchnoBillno == null) {
            return record;
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from transrecord where batchbillno like ?",
                    new String[]{batchnoBillno + "%"});

            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                record = analysis2TransRecord(cursor);
            }
        } catch (Exception e) {
            LogUtils.e("根据批次号和流水号获取单个交易记录对象发生异常", e);
            e.printStackTrace();
        }
        cursor.close();
        //db.close();
        return record;
    }

    /**
     * 根据批次号和票据号获取单个消费、预授权或预授权完成撤销
     */
    @Override
    public synchronized TransRecord getConsumeByCondition(String batchno,
                                                          String billno) {
        Log.i("ckh", "<按票据凭证号查询>batchno=[" + batchno + "],billno=[" + billno + "]");
        TransRecord record = null;
        if (batchno == null || billno == null) {
            return record;
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
//			cursor = db.rawQuery("select * from transrecord where transprocode='000000' and conditionmode='00' and batchbillno like ?", 
//					new String[]{batchno+billno });
            cursor = db.rawQuery("select * from transrecord where (transprocode='000000' or transprocode='030000' or transprocode='200000' or " +
                            "transprocode='630000') and (statuscode <> 'AC' or statuscode is null) and batchbillno like ?",
                    new String[]{batchno + billno + "%"});
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                record = analysis2TransRecord(cursor);
            }
        } catch (Exception e) {
            LogUtils.e("根据批次号和流水号获取单个交易记录对象发生异常", e);
            e.printStackTrace();
        }
        cursor.close();
        //db.close();
        return record;
    }

    /**
     * 根据批次号和票据号获取消费撤销记录
     */
    @Override
    public synchronized TransRecord getTransRevokeByCondition(String batchno,
                                                              String billno) {
        LogUtils.i("<按票据凭证号查询>batchno=[" + batchno + "],billno=[" + billno + "]");
        TransRecord record = null;
        if (batchno == null || billno == null) {
            return record;
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from transrecord where batchbillno like ?",
                    new String[]{batchno + "______" + billno});

            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                record = analysis2TransRecord(cursor);
            }
        } catch (Exception e) {
            LogUtils.e("根据批次号和流水号获取单个交易记录对象发生异常", e);
            e.printStackTrace();
        }
        cursor.close();
        //db.close();
        return record;
    }

    /**
     * 根据批次号和票据号获取消费撤销记录
     */
    @Override
    public synchronized TransRecord getTransAuthRevokeByCondition(String transprocode,
                                                                  String conditionmode, String idrespcode, String reserve1) {
        TransRecord record = null;
        if (transprocode == null || conditionmode == null || idrespcode == null) {
            return record;
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from transrecord where transprocode like ? and conditionmode like ? and idrespcode like ? and reserve1 like ?",
                    new String[]{transprocode, conditionmode, idrespcode, reserve1});

            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                record = analysis2TransRecord(cursor);
            }
        } catch (Exception e) {
            LogUtils.e("根据批次号和流水号获取单个交易记录对象发生异常", e);
            e.printStackTrace();
        }
        cursor.close();
        //db.close();
        return record;
    }


    @Override
    public synchronized TransRecord getTransRecordByCondition(String transprocode,
                                                              String systraceno, String terminalid, String acceptoridcode) {
        TransRecord record = null;
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from transrecord where transprocode = ? and systraceno = ? and terminalid = ? and acceptoridcode = ?",
                    new String[]{transprocode, systraceno, terminalid, acceptoridcode});

            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                record = analysis2TransRecord(cursor);
            }
        } catch (Exception e) {
            LogUtils.e("根据处理代码、POS流水号、终端号、商户号查询交易记录发生异常", e);
            e.printStackTrace();
        }
        cursor.close();
        //db.close();
        return record;
    }

    // 通过“状态码”查找
    @Override
    public synchronized List<TransRecord> getTransRecordsByStatuscode(String code) {
        List<TransRecord> list = new ArrayList();
        SQLiteDatabase db = openHelper.getReadableDatabase();
        String status1 = code;
        String status2 = code;
        if (code.equals("AACorARPC")) {    // AAC\ARPC交易信息上送
            status1 = "AC";
            status2 = "AR";
        }
        Cursor cursor = db.rawQuery("select * from transrecord where statuscode = ? or statuscode = ? order by translocaldate asc,translocaltime asc ",
                new String[]{status1, status2});  //按时间正序排序
        if (cursor != null && cursor.getCount() > 0) {
            list = new ArrayList<TransRecord>();
            while (cursor.moveToNext()) {
                TransRecord record = analysis2TransRecord(cursor);
                list.add(record);
            }
        }
        cursor.close();
        //db.close();
        return list;

    }

    @Override
    public synchronized int save(TransRecord record) {
        int result = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.execSQL("insert into transrecord(" +
                            "priaccount,transprocode,transamount,systraceno,translocaltime," +
                            "translocaldate,expireddate,entrymode,seqnumber,conditionmode," +
                            "updatecode,track2data,track3data,refernumber,idrespcode," +
                            "respcode,terminalid,acceptoridcode,acceptoridname,addrespkey," +
                            "adddataword,transcurrcode,pindata,secctrlinfo,balanceamount," +
                            "icdata,adddatapri,pbocdata,loadparams,cardholderid," +
                            "batchbillno,settledata,mesauthcode,statuscode,reversetimes," +
                            "reserve1,reserve2,reserve3,reserve4,reserve5,transType,transState)" +
                            "values(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,? ,?,?)",
                    new Object[]{record.getPriaccount(), record.getTransprocode(), record.getTransamount(), record.getSystraceno(), record.getTranslocaltime(),
                            record.getTranslocaldate(), record.getExpireddate(), record.getEntrymode(), record.getSeqnumber(), record.getConditionmode(),
                            record.getUpdatecode(), record.getTrack2data(), record.getTrack3data(), record.getRefernumber(), record.getIdrespcode(),
                            record.getRespcode(), record.getTerminalid(), record.getAcceptoridcode(), record.getAcceptoridname(), record.getAddrespkey(),
                            record.getAdddataword(), record.getTranscurrcode(), record.getPindata(), record.getSecctrlinfo(), record.getBalanceamount(),
                            record.getIcdata(), record.getAdddatapri(), record.getPbocdata(), record.getLoadparams(), record.getCardholderid(),
                            record.getBatchbillno(), record.getSettledata(), record.getMesauthcode(), record.getStatuscode(), record.getReversetimes(),
                            record.getReserve1(), record.getReserve2(), record.getReserve3(), record.getReserve4(), record.getReserve5(), record.getTransType(), record.getTransState()});

            result = 1;

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            result = -1;
            LogUtils.e("保存transrecord异常", e);
        }
        //db.close();
        return result;
    }


    @Override
    public synchronized int update(TransRecord record) {
        int result = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();

            cv.put("respcode", record.getRespcode());
            cv.put("terminalid", record.getTerminalid());
            cv.put("acceptoridcode", record.getAcceptoridcode());
            cv.put("acceptoridname", record.getAcceptoridname());
            cv.put("addrespkey", record.getAddrespkey());
            cv.put("adddataword", record.getAdddataword());
            cv.put("transcurrcode", record.getTranscurrcode());
            cv.put("pindata", record.getPindata());
            cv.put("secctrlinfo", record.getSecctrlinfo());
            cv.put("balanceamount", record.getBalanceamount());
            cv.put("icdata", record.getIcdata());
            cv.put("adddatapri", record.getAdddatapri());
            cv.put("pbocdata", record.getPbocdata());
            cv.put("loadparams", record.getLoadparams());
            cv.put("cardholderid", record.getCardholderid());
            cv.put("batchbillno", record.getBatchbillno());
            cv.put("settledata", record.getSettledata());
            cv.put("mesauthcode", record.getMesauthcode());
            cv.put("statuscode", record.getStatuscode());
            cv.put("reversetimes", record.getReversetimes());
            cv.put("reserve1", record.getReserve1());
            cv.put("reserve2", record.getReserve2());
            cv.put("reserve3", record.getReserve3());
            cv.put("reserve4", record.getReserve4());
            cv.put("reserve5", record.getReserve5());
            cv.put("priaccount", record.getPriaccount());
            cv.put("transprocode", record.getTransprocode());
            cv.put("transamount", record.getTransamount());
            cv.put("systraceno", record.getSystraceno());
            cv.put("translocaltime", record.getTranslocaltime());
            cv.put("translocaldate", record.getTranslocaldate());
            cv.put("expireddate", record.getExpireddate());
            cv.put("entrymode", record.getEntrymode());
            cv.put("seqnumber", record.getSeqnumber());
            cv.put("conditionmode", record.getConditionmode());
            cv.put("updatecode", record.getUpdatecode());
            cv.put("track2data", record.getTrack2data());
            cv.put("track3data", record.getTrack3data());
            cv.put("refernumber", record.getRefernumber());
            cv.put("idrespcode", record.getIdrespcode());
            result = db.update("transrecord", cv, " batchbillno=?", new String[]{record.getBatchbillno()});
            LogUtils.e("数据库更新结果：" + result);
            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            result = -1;
            LogUtils.e("更新transrecord异常", e);
        }
        //db.close();
        return result;
    }

    @Override
    public synchronized int update(int id, String key, String value) {
        int result = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.execSQL("update transrecord set " + key + "=? where id=?",
                    new Object[]{value, id});
            result = 1;

            //M3Utility.sync();  //同步命令

        } catch (Exception e) {
            result = -1;
            LogUtils.e("更新transrecord异常", e);
        }
        //db.close();
        return result;
    }

    /**
     * 消费撤销 更新被撤销的状态
     *
     * @param transState  1:交易成功，2：已撤销
     * @param batchbillno
     * @return
     */
    @Override
    public synchronized int updateByNo(int transState, String batchbillno) {
        int result = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.execSQL("update transrecord set transState =? where batchbillno =?", new Object[]{transState, batchbillno});
            result = 1;
        } catch (Exception e) {
            result = -1;
            LogUtils.e("更新transrecord异常", e);
        }
        return result;
    }

    @Override
    public synchronized int delete(TransRecord record) {
        int result = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.execSQL("delete from transrecord where id=?",
                    new Object[]{record.getId()});
            result = 1;

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            result = -1;
            LogUtils.e("删除transrecord异常", e);
        }
        //db.close();
        return result;
    }


    @Override
    public synchronized int deleteAll() {
        int result = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.execSQL("delete from transrecord",
                    new Object[]{});
            LogUtils.i("清除交易流水记录...");
            result = 1;

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            result = -1;
            LogUtils.e("清除交易流水记录异常..", e);
        }
        //db.close();
        return result;
    }

    @Override
    public int getTransCount() {
        int count = 0;
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from transrecord where transprocode <> '900000' ", new String[]{});
            count = cursor.getCount();
        } catch (Exception e) {
            LogUtils.e("获取交易记录数异常", e);
        } finally {
            cursor.close();
            //db.close();
        }
        return count;
    }

    @Override
    public int getConsumeCount() {
        int count = 0;
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from transrecord where transprocode='000000' and conditionmode='00'", new String[]{});
            count = cursor.getCount();
        } catch (Exception e) {
            LogUtils.e("获取消费交易记录数异常", e);
        } finally {
            cursor.close();
            //db.close();
        }
        return count;
    }

    @Override
    public int getRevokeCount() {
        int count = 0;
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from transrecord where transprocode='200000' and conditionmode='00'", new String[]{});
            count = cursor.getCount();
        } catch (Exception e) {
            LogUtils.e("获取消费撤销的交易记录数异常", e);
        } finally {
            cursor.close();
            //db.close();
        }
        return count;
    }

    //获取结算信息
    @Override
    public synchronized TransRecord getSettle() {
        TransRecord record = null;
        SQLiteDatabase db = openHelper.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from transrecord where transprocode='900000' and conditionmode='00' ",
                    new String[]{});
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                record = analysis2TransRecord(cursor);
            }
        } catch (Exception e) {
            LogUtils.e("获取结算信息发生异常", e);
            e.printStackTrace();
        }
        cursor.close();
        //db.close();
        return record;
    }

    /**
     * 获取未上送的脱机交易笔数
     */
    @Override
    public int getOfflineSaleCount() {
        int count = 0;
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from transrecord where transprocode='000000' and statuscode='OF'", new String[]{});
            count = cursor.getCount();
        } catch (Exception e) {
            LogUtils.e("获取脱机消费的交易记录数异常", e);
        } finally {
            cursor.close();
            //db.close();
        }
        return count;
    }


    private TransRecord analysis2TransRecord(Cursor cursor) {
        TransRecord record = new TransRecord();
        if (cursor == null) {
            return record;
        }
        record.setId(cursor.getInt(0));
        record.setPriaccount(cursor.getString(1));
        record.setTransprocode(cursor.getString(2));
        record.setTransamount(cursor.getString(3));
        record.setSystraceno(cursor.getString(4));
        record.setTranslocaltime(cursor.getString(5));
        record.setTranslocaldate(cursor.getString(6));
        record.setExpireddate(cursor.getString(7));
        record.setEntrymode(cursor.getString(8));
        record.setSeqnumber(cursor.getString(9));
        record.setConditionmode(cursor.getString(10));
        record.setUpdatecode(cursor.getString(11));
        record.setTrack2data(cursor.getString(12));
        record.setTrack3data(cursor.getString(13));
        record.setRefernumber(cursor.getString(14));
        record.setIdrespcode(cursor.getString(15));
        record.setRespcode(cursor.getString(16));
        record.setTerminalid(cursor.getString(17));
        record.setAcceptoridcode(cursor.getString(18));
        record.setAcceptoridname(cursor.getString(19));
        record.setAddrespkey(cursor.getString(20));
        record.setAdddataword(cursor.getString(21));
        record.setTranscurrcode(cursor.getString(22));
        record.setPindata(cursor.getString(23));
        record.setSecctrlinfo(cursor.getString(24));
        record.setBalanceamount(cursor.getString(25));
        record.setIcdata(cursor.getString(26));
        record.setAdddatapri(cursor.getString(27));
        record.setPbocdata(cursor.getString(28));
        record.setLoadparams(cursor.getString(29));
        record.setCardholderid(cursor.getString(30));
        record.setBatchbillno(cursor.getString(31));
        record.setSettledata(cursor.getString(32));
        record.setMesauthcode(cursor.getString(33));
        record.setStatuscode(cursor.getString(34));
        record.setReversetimes(cursor.getString(35));
        record.setReserve1(cursor.getString(36));
        record.setReserve2(cursor.getString(37));
        record.setReserve3(cursor.getString(38));
        record.setReserve4(cursor.getString(39));
        record.setReserve5(cursor.getString(40));
        record.setTransType(cursor.getString(41));
        record.setTransState(cursor.getString(42));
        return record;
    }
}
