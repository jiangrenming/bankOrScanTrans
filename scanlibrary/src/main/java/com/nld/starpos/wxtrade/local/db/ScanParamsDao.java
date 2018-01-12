package com.nld.starpos.wxtrade.local.db;


import android.database.sqlite.SQLiteDatabase;

import com.nld.starpos.wxtrade.local.db.bean.ScanParams;

import java.util.List;
import java.util.Map;


public interface ScanParamsDao {
    /**
     * 获取参数配置信息
     */
    public List<ScanParams> getEntities();

    public Map<String, String> get();

    public String get(String tagname);

    /**
     * 保存参数配置信息
     */
    public int save(ScanParams config);

    public int save(List<ScanParams> params);

    public int save(Map<String, String> map);

    public int save(String tagname, String tagval);

    /**
     * 更新参数配置信息
     */
    public int update(ScanParams param);

    public int update(Map<String, String> map);

    public int update(String tagname, String tagval);

    /**
     * 删除参数配置信息
     */
    public int delete(ScanParams param);

    public int delete(String tagname);

    /**
     * 判断是否存在
     */
    public boolean isExist(String tagname);

    public boolean isExist(SQLiteDatabase db, String tagname);

}
