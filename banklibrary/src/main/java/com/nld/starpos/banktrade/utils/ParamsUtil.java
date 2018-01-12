package com.nld.starpos.banktrade.utils;

import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;

import java.util.Map;

public class ParamsUtil {

    private ParamConfigDao mParamDao;

    private ParamsUtil(){
        mParamDao=new ParamConfigDaoImpl();
    }
    private static  class ParamsUtilsInner{
        private static  final ParamsUtil paramsUtil = new ParamsUtil();
    }

    public static ParamsUtil getInstance(){
        return ParamsUtilsInner.paramsUtil;
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
