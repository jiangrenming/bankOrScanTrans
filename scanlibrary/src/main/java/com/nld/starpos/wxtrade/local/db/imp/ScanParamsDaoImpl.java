package com.nld.starpos.wxtrade.local.db.imp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lidroid.xutils.util.LogUtils;
import com.nld.starpos.wxtrade.local.ScanDbHelper;
import com.nld.starpos.wxtrade.local.db.ScanParamsDao;
import com.nld.starpos.wxtrade.local.db.bean.ScanParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanParamsDaoImpl implements ScanParamsDao {


    private ScanDbHelper openHelper;

    public ScanParamsDaoImpl() {
        openHelper = ScanDbHelper.getInstance();
    }

    @Override
    public synchronized List<ScanParams> getEntities() {
        List<ScanParams> list = null;
        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select tagname, tagval from Scan_params ", new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            list = new ArrayList<ScanParams>();
            while (cursor.moveToNext()) {
                ScanParams param = new ScanParams();
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
        Cursor cursor = db.rawQuery("select tagname, tagval from Scan_params",
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
                "select tagval from Scan_params where tagname=?",
                new String[]{tagname});
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
            tagval = cursor.getString(0);
        }
        cursor.close();
        //db.close();
        return tagval;
    }


    @Override
    public synchronized int save(ScanParams config) {

        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.execSQL("insert into Scan_params(tagname, tagval) values(?,?)",
                    new Object[]{config.getTagname(), config.getTagval()});

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            return -1;
        } finally {
            //db.close();
        }
        return 1;
    }

    @Override
    public synchronized int save(List<ScanParams> params) {
        int count = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (ScanParams param : params) {
                if (isExist(db, param.getTagname())) {
                    db.execSQL("update Scan_params set tagval=? where tagname=?",
                            new Object[]{param.getTagval(), param.getTagname()});
                } else {
                    db.execSQL("insert into Scan_params(tagname, tagval) values(?,?)",
                            new Object[]{param.getTagname(), param.getTagval()});
                }
                count++;
            }
            db.setTransactionSuccessful();

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
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
        LogUtils.i("复制旧数据到新数据库里");
        SQLiteDatabase db = openHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (isExist(db, entry.getKey())) {
                    db.execSQL("update Scan_params set tagval=? where tagname=?",
                            new Object[]{entry.getValue(), entry.getKey()});
                    LogUtils.i("新数据库的值222"+"key="+entry.getKey()+"/value="+entry.getValue());
                } else {
                    db.execSQL("insert into Scan_params(tagname, tagval) values(?,?)",
                            new Object[]{entry.getKey(), entry.getValue()});
                    LogUtils.i("新数据库的值"+"key="+entry.getKey()+"/value="+entry.getValue());
                }
                count++;
            }
            db.setTransactionSuccessful();
            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
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
            db.execSQL("INSERT INTO Scan_params(tagname,tagval) VALUES(?,?)",
                    new Object[]{tagname, tagval});


            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            return -1;
        }
        //db.close();
        return 1;
    }


    @Override
    public synchronized int update(ScanParams param) {
        SQLiteDatabase db = openHelper.getWritableDatabase();

        try {
            db.execSQL("update Scan_params set tagval=? where tagname=?",
                    new Object[]{param.getTagval(), param.getTagname()});

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
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
                db.execSQL("update Scan_params set tagval=? where tagname=?",
                        new Object[]{entry.getValue(), entry.getKey()});
                count++;
            }
            db.setTransactionSuccessful();

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
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
            db.execSQL("update Scan_params set tagval=? where tagname=?",
                    new Object[]{tagval, tagname});

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            return -1;
        } finally {
            //db.close();
        }
        return 1;
    }

    @Override
    public synchronized int delete(ScanParams param) {
        int result = 0;
        SQLiteDatabase db = openHelper.getWritableDatabase();
        try {
            db.execSQL("delete from transrecord where tagname=?",
                    new Object[]{param.getTagname()});
            result = 1;

            //M3Utility.sync();  //同步命令
        } catch (Exception e) {
            result = -1;
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
                            "select tagname, tagval from Scan_params where tagname=? ",
                            new String[]{tagname});
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
            //db.close();
        }
        return count != 0;
    }

    @Override
    public boolean isExist(SQLiteDatabase db, String tagname) {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = db
                    .rawQuery(
                            "select tagname, tagval from Scan_params where tagname=? ",
                            new String[]{tagname});
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return count != 0;
    }
}
