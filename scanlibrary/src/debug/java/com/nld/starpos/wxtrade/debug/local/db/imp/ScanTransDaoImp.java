package com.nld.starpos.wxtrade.debug.local.db.imp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lidroid.xutils.util.LogUtils;
import com.nld.starpos.wxtrade.debug.exception.DuplicatedTraceException;
import com.nld.starpos.wxtrade.debug.local.ScanDbHelper;
import com.nld.starpos.wxtrade.debug.local.db.ScanTransDao;
import com.nld.starpos.wxtrade.debug.local.db.bean.ScanTransRecord;
import com.nld.starpos.wxtrade.debug.utils.params.TransType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiangrenming on 2017/9/25.
 * 扫码流水数据库实现类
 */

public class ScanTransDaoImp implements ScanTransDao {

    private ScanDbHelper openHelper;

    public ScanTransDaoImp() {
        openHelper = ScanDbHelper.getInstance();
    }

    /**
     * 增添一个去除重复流水号的流水
     *
     * @param water
     * @return
     * @throws :DuplicatedTraceException
     */
    @Override
    public long addWater(ScanTransRecord water) throws DuplicatedTraceException {
        if (water == null || water.getSystraceno() == null) {
            throw new RuntimeException("添加流水失败：null参数");
        }
        // 判断是否有重复的流水号
        if (this.findByTrace(water.getSystraceno()) != null) {
            throw new DuplicatedTraceException("重复的流水号，添加失败:" + water.getSystraceno());
        }
        int result = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put("transamount", water.getTransamount());
            values.put("systraceno", water.getSystraceno());
            values.put("translocaltime", water.getScanTime());
            values.put("translocaldate", water.getScanDate());
            values.put("settledata", water.getSettledata());
            values.put("terminalid", water.getTerminalId());
            values.put("acceptoridcode", water.getMemberId());
            values.put("acceptoridname", water.getSettledata());
            values.put("terminalid", water.getMemberName());
            values.put("batchbillno", water.getBatchbillno());
            values.put("statuscode", water.getStatuscode());
            values.put("oper", water.getOper());
            values.put("adddataword", water.getAdddataword());
            values.put("transcurrcode", water.getTranscurrcode());
            values.put("payChannel", water.getPayChannel());
            values.put("orderNo", water.getOrderNo());
            values.put("respcode", water.getRespcode());
            values.put("transprocode", water.getTransprocode());
            values.put("transtotalamount", water.getTotalAmount());
            values.put("authCode", water.getAuthCode());
            values.put("isrevoke", water.getIsrevoke());
            values.put("type", water.getTransType());
            values.put("transyear", water.getScanYear());
            db.insert("Scan_Trans", null, values);
            db.setTransactionSuccessful();
            result = 1;
        } catch (Exception e) {
            result = -1;
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        if (result < 1) {
            throw new RuntimeException("添加流水失败");
        }
        return result;
    }

    @Override
    public long addWaterCanRepeat(ScanTransRecord water) {
        if (water == null || water.getSystraceno() == null) {
            throw new RuntimeException("添加流水失败：null参数");
        }
        int result = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put("transamount", water.getTransamount());
            values.put("systraceno", water.getSystraceno());
            values.put("translocaltime", water.getScanTime());
            values.put("translocaldate", water.getScanDate());
            values.put("settledata", water.getSettledata());
            values.put("terminalid", water.getTerminalId());
            values.put("acceptoridcode", water.getMemberId());
            values.put("acceptoridname", water.getSettledata());
            values.put("terminalid", water.getMemberName());
            values.put("batchbillno", water.getBatchbillno());
            values.put("statuscode", water.getStatuscode());
            values.put("oper", water.getOper());
            values.put("adddataword", water.getAdddataword());
            values.put("transcurrcode", water.getTranscurrcode());
            values.put("payChannel", water.getPayChannel());
            values.put("orderNo", water.getOrderNo());
            values.put("respcode", water.getRespcode());
            values.put("transprocode", water.getTransprocode());
            values.put("transtotalamount", water.getTotalAmount());
            values.put("authCode", water.getAuthCode());
            values.put("isrevoke", water.getIsrevoke());
            values.put("type", water.getTransType());
            values.put("transyear", water.getScanYear());
            db.insert("Scan_Trans", null, values);
            db.setTransactionSuccessful();
            result = 1;
        } catch (Exception e) {
            result = -1;
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        if (result < 1) {
            throw new RuntimeException("添加流水失败");
        }
        return result;
    }

    /**
     * 更新一个流水
     *
     * @param water
     * @return
     */
    @Override
    public int updateWater(ScanTransRecord water) {
        if (water == null) {
            return 0;
        }
        if (water.getId() == -1) {
            ScanTransRecord tmp = findByTrace(water.getSystraceno());
            if (tmp == null) {
                return -1;
            }
            water.setId(tmp.getId());
        }
        SQLiteDatabase db = openHelper.getWritableDatabase();
        int scanTrans = 0;
        try {
            ContentValues values = new ContentValues();
            values.put("isrevoke", water.getIsrevoke());
            scanTrans = db.update("Scan_Trans", values, "id =?", new String[]{String.valueOf(water.getId())});
        } catch (Exception e) {
            e.printStackTrace();
        }

        return scanTrans;
    }

    @Override
    public int deleteById(long id) {
        return 0;
    }

    /**
     * 根据交易类型删除数据库数据
     *
     * @param transType
     * @return
     */
    @Override
    public int deleteByTransType(int transType) {

        SQLiteDatabase db = openHelper.getWritableDatabase();
        int count = 0;
        try {
            count = db.delete("Scan_Trans", "type = ?", new String[]{String.valueOf(transType)});
        } catch (Exception e) {
            e.printStackTrace();
            count = -1;
        }
        if (count <= 0) {
            return -1;
        }
        return count;
    }

    /**
     * 根据pos流水号删除流水
     *
     * @param trace 流水号
     * @return
     */
    @Override
    public int deleteByTrace(String trace) {
        if (trace == null) {
            return -1;
        }
        int result = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.execSQL("delete from Scan_Trans where systraceno=?", new Object[]{trace});
            result = 1;
        } catch (Exception e) {
            e.printStackTrace();
            return result = -1;
        }
        return result;
    }

    /**
     * 根据pos流水号查找记录
     *
     * @param trace POS流水号
     * @return
     */
    @Override
    public ScanTransRecord findByTrace(String trace) {
        if (trace == null) {
            return null;
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        List<ScanTransRecord> list = null;
        Cursor cursor = null;
        try {
            list = new ArrayList<>();
            cursor = db.rawQuery("select * from Scan_Trans where systraceno = ?", new String[]{trace});
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    ScanTransRecord record = new ScanTransRecord();
                    record.setSystraceno(cursor.getString(2));
                    record.setTransamount(cursor.getString(1));
                    record.setBatchbillno(cursor.getString(12));
                    record.setTranscurrcode("156");
                    record.setScanDate(cursor.getString(7));
                    record.setOper("001"); //操作员  ，暂时是默认的
                    record.setPayChannel(cursor.getString(17));
                    record.setIsrevoke(cursor.getString(23));
                    record.setTransType(cursor.getInt(25)); //交易类型
                    list.add(record);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        Log.d("wqz", list.size() + "");
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public ScanTransRecord findByThirdOrderNo(String thirdOrderNo) {
        return null;
    }

    @Override
    public ScanTransRecord findByReferNum(String referNum) {
        return null;
    }

    @Override
    public ScanTransRecord findById(long id) {
        return null;
    }

    /**
     * 获取最后一条流水
     *
     * @return
     */
    @Override
    public ScanTransRecord findLastWater() {
        return null;
    }

    @Override
    public int getWaterCount() {
        int totalCount = 0;
        try {
            SQLiteDatabase readableDatabase = openHelper.getReadableDatabase();
            Cursor cursor = readableDatabase.rawQuery("select count(*) from Scan_Trans", null);
            if (cursor != null && cursor.getCount() > 0) {
                while (!cursor.moveToNext()) {
                    return 0;
                }
                totalCount = Integer.parseInt(cursor.getString(0));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalCount;
    }

    @Override
    public List<ScanTransRecord> findByPage(int pageNo, int pageSize) {
        return null;
    }

    @Override
    public List<ScanTransRecord> findAllLikeTrace(String trace) {
        return null;
    }

    @Override
    public List<ScanTransRecord> findAll() {
        SQLiteDatabase readableDatabase = openHelper.getReadableDatabase();
        List<ScanTransRecord> list = null;
        Cursor cursor = null;
        try {
            list = new ArrayList<>();
            cursor = readableDatabase.rawQuery("select * from Scan_Trans", null);
            if (null != cursor && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    ScanTransRecord scanTransRecord = new ScanTransRecord();
                    scanTransRecord.setId(cursor.getInt(0));
                    scanTransRecord.setTransamount(cursor.getString(1));
                    scanTransRecord.setSystraceno(cursor.getString(2));
                    scanTransRecord.setLogNo(cursor.getString(3));
                    scanTransRecord.setMember(cursor.getString(4));
                    scanTransRecord.setPayType(cursor.getString(5));
                    scanTransRecord.setScanTime(cursor.getString(6));
                    scanTransRecord.setScanDate(cursor.getString(7));
                    scanTransRecord.setSettledata(cursor.getString(8));
                    scanTransRecord.setTerminalId(cursor.getString(9));
                    scanTransRecord.setMemberId(cursor.getString(10));
                    scanTransRecord.setMemberName(cursor.getString(11));
                    scanTransRecord.setBatchbillno(cursor.getString(12));
                    scanTransRecord.setStatuscode(cursor.getString(13));
                    scanTransRecord.setOper(cursor.getString(14));
                    scanTransRecord.setAdddataword(cursor.getString(15));
                    scanTransRecord.setTranscurrcode(cursor.getString(16));
                    scanTransRecord.setPayChannel(cursor.getString(17));
                    scanTransRecord.setOrderNo(cursor.getString(18));
                    scanTransRecord.setRespcode(cursor.getString(19));
                    scanTransRecord.setTransprocode(cursor.getString(20));
                    scanTransRecord.setTotalAmount(cursor.getString(21));
                    scanTransRecord.setAuthCode(cursor.getString(22));
                    scanTransRecord.setIsrevoke(cursor.getString(23));
                    scanTransRecord.setOldTransType(cursor.getInt(24));
                    scanTransRecord.setTransType(cursor.getInt(25));
                    scanTransRecord.setScanYear(cursor.getString(26));
                    list.add(scanTransRecord);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }


    /**
     * 根据交易类型和交易状态获取流水
     *
     * @param transType
     * @param transStatus
     * @return
     */
    @Override
    public List<ScanTransRecord> findByTransTypeAndTransStatus(int transType, int transStatus) {
        SQLiteDatabase readableDatabase = openHelper.getReadableDatabase();
        List<ScanTransRecord> records = null;
        Cursor cursor = null;
        try {
            records = new ArrayList<>();
            cursor = readableDatabase.rawQuery("select * from Scan_Trans where type = ? and statuscode = ?", new String[]{String.valueOf(transType), String.valueOf(transStatus)});
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    ScanTransRecord scanTransRecord = new ScanTransRecord();
                    scanTransRecord.setId(cursor.getInt(0));
                    scanTransRecord.setTransamount(cursor.getString(1));
                    scanTransRecord.setSystraceno(cursor.getString(2));
                    scanTransRecord.setLogNo(cursor.getString(3));
                    scanTransRecord.setMember(cursor.getString(4));
                    scanTransRecord.setPayType(cursor.getString(5));
                    scanTransRecord.setScanTime(cursor.getString(6));
                    scanTransRecord.setScanDate(cursor.getString(7));
                    scanTransRecord.setSettledata(cursor.getString(8));
                    scanTransRecord.setTerminalId(cursor.getString(9));
                    scanTransRecord.setMemberId(cursor.getString(10));
                    scanTransRecord.setMemberName(cursor.getString(11));
                    scanTransRecord.setBatchbillno(cursor.getString(12));
                    scanTransRecord.setStatuscode(cursor.getString(13));
                    scanTransRecord.setOper(cursor.getString(14));
                    scanTransRecord.setAdddataword(cursor.getString(15));
                    scanTransRecord.setTranscurrcode(cursor.getString(16));
                    scanTransRecord.setPayChannel(cursor.getString(17));
                    scanTransRecord.setOrderNo(cursor.getString(18));
                    scanTransRecord.setRespcode(cursor.getString(19));
                    scanTransRecord.setTransprocode(cursor.getString(20));
                    scanTransRecord.setTotalAmount(cursor.getString(21));
                    scanTransRecord.setAuthCode(cursor.getString(22));
                    scanTransRecord.setIsrevoke(cursor.getString(23));
                    scanTransRecord.setOldTransType(cursor.getInt(24));
                    scanTransRecord.setTransType(cursor.getInt(25));
                    scanTransRecord.setScanYear(cursor.getString(26));
                    records.add(scanTransRecord);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return records;
    }

    /**
     * 根据交易类型来查找流水
     *
     * @param transType
     * @return
     */
    @Override
    public List<ScanTransRecord> findByTransType(int transType) {
        SQLiteDatabase readableDatabase = openHelper.getReadableDatabase();
        List<ScanTransRecord> records = null;
        Cursor cursor = null;
        try {
            records = new ArrayList<>();
            cursor = readableDatabase.rawQuery("select * from Scan_Trans where type = ?", new String[]{String.valueOf(transType)});
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    ScanTransRecord record = new ScanTransRecord();
                    record.setTransamount(cursor.getString(1));
                    records.add(record);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return records;
    }

    /**
     * 根据交易订单号获取流水信息
     *
     * @param orderNo
     * @return
     */
    @Override
    public List<ScanTransRecord> findByOrderNo(String orderNo) {

        if (orderNo == null) {
            return null;
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        List<ScanTransRecord> list = null;
        Cursor cursor = null;
        try {
            list = new ArrayList<>();
            cursor = db.rawQuery("select * from Scan_Trans where orderNo = ?", new String[]{orderNo});
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    ScanTransRecord record = new ScanTransRecord();
                    record.setId(cursor.getInt(0));
                    record.setTransamount(cursor.getString(1));
                    record.setSystraceno(cursor.getString(2));
                    record.setLogNo(cursor.getString(3));
                    record.setMember(cursor.getString(4));
                    record.setPayType(cursor.getString(5));
                    record.setScanTime(cursor.getString(6));
                    record.setScanDate(cursor.getString(7));
                    record.setSettledata(cursor.getString(8));
                    record.setTerminalId(cursor.getString(9));
                    record.setMemberId(cursor.getString(10));
                    record.setMemberName(cursor.getString(11));
                    record.setBatchbillno(cursor.getString(12));
                    record.setStatuscode(cursor.getString(13));
                    record.setOper(cursor.getString(14));
                    record.setAdddataword(cursor.getString(15));
                    record.setTranscurrcode(cursor.getString(16));
                    record.setPayChannel(cursor.getString(17));
                    record.setOrderNo(cursor.getString(18));
                    record.setRespcode(cursor.getString(19));
                    record.setTransprocode(cursor.getString(20));
                    record.setTotalAmount(cursor.getString(21));
                    record.setAuthCode(cursor.getString(22));
                    record.setIsrevoke(cursor.getString(23));
                    record.setOldTransType(cursor.getInt(24));
                    record.setTransType(cursor.getInt(25));
                    record.setScanYear(cursor.getString(26));
                    list.add(record);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 根据不同的订单号查找原支付交易
     * @param orderNo
     * @param type
     * @return
     */
    @Override
    public List<ScanTransRecord> findByOrderNoAndType(String orderNo, int type) {
        if (orderNo == null) {
            return null;
        }
        SQLiteDatabase db = openHelper.getReadableDatabase();
        List<ScanTransRecord> list = null;
        Cursor cursor = null;
        try {
            list = new ArrayList<>();
            cursor = db.rawQuery("select * from Scan_Trans where orderNo = ? and isrevoke = ?", new String[]{orderNo,String.valueOf(type)});
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    ScanTransRecord record = new ScanTransRecord();
                    record.setId(cursor.getInt(0));
                    record.setTransamount(cursor.getString(1));
                    record.setSystraceno(cursor.getString(2));
                    record.setLogNo(cursor.getString(3));
                    record.setMember(cursor.getString(4));
                    record.setPayType(cursor.getString(5));
                    record.setScanTime(cursor.getString(6));
                    record.setScanDate(cursor.getString(7));
                    record.setSettledata(cursor.getString(8));
                    record.setTerminalId(cursor.getString(9));
                    record.setMemberId(cursor.getString(10));
                    record.setMemberName(cursor.getString(11));
                    record.setBatchbillno(cursor.getString(12));
                    record.setStatuscode(cursor.getString(13));
                    record.setOper(cursor.getString(14));
                    record.setAdddataword(cursor.getString(15));
                    record.setTranscurrcode(cursor.getString(16));
                    record.setPayChannel(cursor.getString(17));
                    record.setOrderNo(cursor.getString(18));
                    record.setRespcode(cursor.getString(19));
                    record.setTransprocode(cursor.getString(20));
                    record.setTotalAmount(cursor.getString(21));
                    record.setAuthCode(cursor.getString(22));
                    record.setIsrevoke(cursor.getString(23));
                    record.setOldTransType(cursor.getInt(24));
                    record.setTransType(cursor.getInt(25));
                    record.setScanYear(cursor.getString(26));
                    list.add(record);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * 清除扫码交易类型的流水数据
     *
     * @return
     */
    @Override
    public int clearScanWater() {
        int deleteNum = 0;
        deleteNum += deleteByTransType(TransType.ScanTransType.TRANS_SCAN_ALIPAY);
        deleteNum += deleteByTransType(TransType.ScanTransType.TRANS_QR_ALIPAY);
        deleteNum += deleteByTransType(TransType.ScanTransType.TRANS_SCAN_WEIXIN);
        deleteNum += deleteByTransType(TransType.ScanTransType.TRANS_QR_WEIXIN);
        deleteNum += deleteByTransType(TransType.ScanTransType.TRANS_SCAN_REFUND);
        return deleteNum;
    }

    /**
     * 清除数据库数据
     */
    @Override
    public void clearWater() {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.delete("Scan_Trans", "", new String[]{});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量导入数据库数据
     * @param scanTransRecords
     */
    @Override
    public long copyOldWater(List<ScanTransRecord> scanTransRecords) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        int result = 0;
        db.beginTransaction();
        try{
            for (int i = 0; i < scanTransRecords.size(); i++) {
                ScanTransRecord scanTransRecord = scanTransRecords.get(i);
                ContentValues values = new ContentValues();
                values.put("transamount", scanTransRecord.getTransamount());
                values.put("systraceno", scanTransRecord.getSystraceno());
                values.put("translocaltime", scanTransRecord.getScanTime());
                values.put("translocaldate", scanTransRecord.getScanDate());
                values.put("settledata", scanTransRecord.getSettledata());
                values.put("terminalid", scanTransRecord.getTerminalId());
                values.put("acceptoridcode", scanTransRecord.getMemberId());
                values.put("acceptoridname", scanTransRecord.getSettledata());
                values.put("terminalid", scanTransRecord.getMemberName());
                values.put("batchbillno", scanTransRecord.getBatchbillno());
                values.put("statuscode", scanTransRecord.getStatuscode());
                values.put("oper", scanTransRecord.getOper());
                values.put("adddataword", scanTransRecord.getAdddataword());
                values.put("transcurrcode", scanTransRecord.getTranscurrcode());
                values.put("payChannel", scanTransRecord.getPayChannel());
                values.put("orderNo", scanTransRecord.getOrderNo());
                values.put("respcode", scanTransRecord.getRespcode());
                values.put("transprocode", scanTransRecord.getTransprocode());
                values.put("transtotalamount", scanTransRecord.getTotalAmount());
                values.put("authCode", scanTransRecord.getAuthCode());
                values.put("isrevoke", scanTransRecord.getIsrevoke());
                values.put("type", scanTransRecord.getTransType());
                values.put("transyear", scanTransRecord.getScanYear() == null ? "" : scanTransRecord.getScanYear());
                db.insert("Scan_Trans", null, values);
            }
            LogUtils.i("复制新流水表成功");
            db.setTransactionSuccessful();
            result = 1;
        }catch (Exception e){
            result = -1;
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        if (result < 1) {
            throw new RuntimeException("批量导入流水失败");
        }
        return result;
    }
}
