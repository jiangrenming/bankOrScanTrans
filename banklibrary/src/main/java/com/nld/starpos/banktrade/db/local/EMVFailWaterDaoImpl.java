package com.nld.starpos.banktrade.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nld.starpos.banktrade.db.DBOpenHelper;
import com.nld.starpos.banktrade.db.EmvFailWaterDao;
import com.nld.starpos.banktrade.db.bean.EMVFailWater;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiangrenming on 2017/10/14.
 */

public class EMVFailWaterDaoImpl implements EmvFailWaterDao {

    private DBOpenHelper openHelper;

    public EMVFailWaterDaoImpl() {
        openHelper = DBOpenHelper.getInstance();
    }

    //插入失败流水
    @Override
    public long insert(EMVFailWater emvFailWater) {
        if (emvFailWater == null || emvFailWater.getSystraceno() == null) {
            throw new RuntimeException("添加流水失败：null参数");
        }
        int result =0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put("type",emvFailWater.getType()); //交易类型
            values.put("oper",emvFailWater.getOper());  //操作员
            values.put("transamount",emvFailWater.getTransamount()); //金额
            values.put("systraceno",emvFailWater.getSystraceno());  //流水号
            values.put("batchbillno",emvFailWater.getBatchbillno()); //批次号
            values.put("translocaltime",emvFailWater.getTranslocaltime()); //时间
            values.put("translocaldate",emvFailWater.getTranslocaldate()); //日期
            values.put("translocalyear",emvFailWater.getTranslocalyear());  //年份
            values.put("terminalid",emvFailWater.getTerminalid()); //终端号
            values.put("acceptoridcode",emvFailWater.getAcceptoridcode()); //商户号
            values.put("acceptoridname",emvFailWater.getAcceptoridname()); //商户名称
            values.put("track2data",emvFailWater.getTrack2data()); //二磁道
            values.put("track3data",emvFailWater.getTrack3data()); //三磁道
            values.put("entrymode",emvFailWater.getEntrymode()); //输入方式
            values.put("seqnumber",emvFailWater.getSeqnumber()); //卡序列号
            values.put("settledata",emvFailWater.getSettledata()); //结算信息
            values.put("statuscode",emvFailWater.getStatuscode());  //交易结果状态码
            values.put("adddataword",emvFailWater.getAdddataword()); //附加文字信息
            values.put("transcurrcode",emvFailWater.getTranscurrcode()); //货币代码
            values.put("respcode",emvFailWater.getRespcode());  //返回码
            values.put("authCode",emvFailWater.getMesauthcode());  //消息认证码
            db.insert("emvfailwater",null,values);
            db.setTransactionSuccessful();
            result = 1;
        }catch (Exception e){
            result = -1;
            e.printStackTrace();
        }finally {
            db.endTransaction();
        }
        if (result < 1){
            throw new RuntimeException("添加流水失败");
        }
        return result;
    }

    //更新失败流水
    @Override
    public int update(EMVFailWater emvFailWater) {
        return 0;
    }

    //删除失败流水数据
    @Override
    public int deleteAll() {
        int result =0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.execSQL("delete from emvfailwater");
            result =1;
        } catch (Exception e) {
            result = -1;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void revertSeq() {

    }

    @Override
    public EMVFailWater findById(long id) {
        return null;
    }
    //获取失败流水的数量
    @Override
    public int getCount() {
        int totalCount = 0;
        try {
            SQLiteDatabase readableDatabase = openHelper.getReadableDatabase();
            Cursor cursor = readableDatabase.rawQuery("select count(*) from emvfailwater",null);
            if (cursor != null && cursor.getCount() > 0 ){
                while (!cursor.moveToNext()){
                    return  0;
                }
                totalCount = Integer.parseInt(cursor.getString(0));
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return totalCount;
    }

    //返回所有的失败流水
    @Override
    public List<EMVFailWater> findAll() {
        SQLiteDatabase readableDatabase = openHelper.getReadableDatabase();
        List<EMVFailWater> list = null;
        Cursor cursor = null;
        try{
            list = new ArrayList<>();
            cursor = readableDatabase.rawQuery("select * from emvfailwater",null);
            if (null != cursor && cursor.getCount() >0 ){
                while (cursor.moveToNext()){
                    EMVFailWater emvFailWater = new EMVFailWater();
                    emvFailWater.setId(cursor.getInt(0));
                    emvFailWater.setOper(cursor.getString(2));
                    emvFailWater.setTransamount(cursor.getString(4));
                    emvFailWater.setSystraceno(cursor.getString(5));
                    emvFailWater.setTranslocaltime(cursor.getString(6));
                    emvFailWater.setTranslocaldate(cursor.getString(7));
                    emvFailWater.setTranslocalyear(cursor.getString(8));
                    emvFailWater.setEntrymode(cursor.getString(10));
                    emvFailWater.setSeqnumber(cursor.getString(11));
                    emvFailWater.setTrack2data(cursor.getString(14));
                    emvFailWater.setTrack3data(cursor.getString(15));
                    emvFailWater.setRespcode(cursor.getString(18));
                    emvFailWater.setTerminalid(cursor.getString(19));
                    emvFailWater.setAcceptoridcode(cursor.getString(20));
                    emvFailWater.setAcceptoridname(cursor.getString(21));
                    emvFailWater.setAdddataword(cursor.getString(23));
                    emvFailWater.setAdddataword(cursor.getString(23));
                    emvFailWater.setTranscurrcode(cursor.getString(24));
                    emvFailWater.setBatchbillno(cursor.getString(33));
                    emvFailWater.setSettledata(cursor.getString(34));
                    emvFailWater.setMesauthcode(cursor.getString(35));
                    emvFailWater.setStatuscode(cursor.getString(36));
                    emvFailWater.setStatuscode(cursor.getString(43));
                    list.add(emvFailWater);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null){
                cursor.close();
            }
        }
        return list;
    }
}
