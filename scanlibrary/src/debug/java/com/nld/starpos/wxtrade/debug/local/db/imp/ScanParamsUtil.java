package com.nld.starpos.wxtrade.debug.local.db.imp;

import com.nld.starpos.wxtrade.debug.local.db.ScanParamsDao;

import java.util.Map;

public class ScanParamsUtil {

    private ScanParamsDao mParamDao;

    private ScanParamsUtil(){
        mParamDao=new ScanParamsDaoImpl();
    }
    private static  class ParamsUtilsInner{
        private static  final ScanParamsUtil SCAN_PARAMS_UTIL = new ScanParamsUtil();
    }

    public static ScanParamsUtil getInstance(){
        return ParamsUtilsInner.SCAN_PARAMS_UTIL;
    }
    public String getParam(String key){
        return mParamDao.get(key);
    }

    public void save(String key,String value){
         mParamDao.save(key,value);
    }

    public int save(Map<String,String> params){
         return mParamDao.save(params);
    }
    public void update(String key,String value){
    	mParamDao.update(key, value);
    }
    
}
