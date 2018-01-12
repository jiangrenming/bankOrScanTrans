package com.nld.starpos.wxtrade.utils.jsonUtils;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * JSON格式数据解析
 */
public class DataAnalysisByJson {

    private static DataAnalysisByJson dataAna = null;
    private DataAnalysisByJson() {}
    public static DataAnalysisByJson getInstance() {
        if (dataAna == null) {
            synchronized (DataAnalysisByJson.class) {
                if (dataAna == null) {
                    dataAna = new DataAnalysisByJson();
                }
            }
        }
        return dataAna;
    }

    private static  String transType;
    public  static  void setTranChannlePay(String type){
        transType = type ;
    }

    /**
     * 解析数据，返回数据Bean对象
     */
    public <T> T getObjectByString(String result, Class<T> clazz) {
        result = parseRspString2Json(result);
        if (clazz == null) {
            return null;
        }

        T obj = null;
        try {
            obj = JSONObject.parseObject(result, clazz);
        } catch (Exception e) {
        }
        Log.i("DataAnalysisByJson", "Json->" + clazz.getSimpleName() + ": " + ParseObjectUtils.toString(obj));
        return obj;
    }

    /**
     * 解析数据，返回数据Bean对象(包含Json数组)
     */
    public <T> T getObjectByString2(String result, Class<T> clazz) {
        if (result.contains("[") && result.contains("]")) {
            result = parseRspString2Json2(result);
        } else {
            result = parseRspString2Json(result);
        }

        if (clazz == null) {
            return null;
        }

        T obj = null;
        try {
            obj = JSONObject.parseObject(result, clazz);
        } catch (Exception e) {
        }
        Log.i("DataAnalysisByJson", "Json->" + clazz.getSimpleName() + ": " + ParseObjectUtils.toString(obj));
        return obj;
    }

    /**
     * 解析数据，返回数据Bean对象集合
     */
    public <T> List<T> getObjectsByString(String result, Class<T> clazz) {
        if (clazz == null) {
            return null;
        }

        List<T> resultObj = null;

        try {
            resultObj = JSONObject.parseArray(result, clazz);
        } catch (Exception e) {
        }
        return resultObj;
    }

    /**
     * 将Object对象转成JSON格式的字符串
     */
    public String getJsonStringByObject(Object object) {
        String jsonStr = null;
        try {
            jsonStr = JSONObject.toJSONString(object, false);
        } catch (Exception e) {
        }
        return jsonStr;
    }

    private String parseRspString2Json(String rspString) {
        String[] strArray = rspString.split("&");
        String json = "{";
        int length = strArray.length;
        for (int i = 0; i < length; i++) {
            if (strArray[i].startsWith("2DCode")){
                strArray[i] = strArray[i].replace("2DCode","qc_Code");
                String[] str = strArray[i].split("=");
                StringBuilder sb = new StringBuilder();
                if (str.length > 2 ){
                    sb.append(str[0]).append("\"").append(":\"").append(str[1]).append("=").append(str[2]);
                    json += "\"" +sb.toString()+"\"";
                }else {
                    json += "\"" + strArray[i].replace("=", "\"" + ":\"") + "\"";
                }
            }else {
                json += "\"" + strArray[i].replace("=", "\"" + ":\"") + "\"";
            }
            if (i < length - 1) {
                json += ",";
            }
        }
        return json + "}";
    }

    private String parseRspString2Json2(String rspString) {
        String[] strArray = rspString.split("&");
        String json = "{";
        int length = strArray.length;
        for (int i = 0; i < length; i++) {
            if (strArray[i].contains("[") && strArray[i].contains("]")) {
                json += "\"" + strArray[i].replace("=", "\"" + ":");
            } else {
                json += "\"" + strArray[i].replace("=", "\"" + ":\"") + "\"";
            }

            if (i < length - 1) {
                json += ",";
            }
        }
        return json + "}";
    }

}