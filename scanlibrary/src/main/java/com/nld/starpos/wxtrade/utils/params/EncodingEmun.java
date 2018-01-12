package com.nld.starpos.wxtrade.utils.params;

/**
 * Created by jiangrenming on 2017/10/25.
 * 共同参数体枚举
 */

public enum EncodingEmun {

    GBK("00"),GB2312("01"),UTF("02"),USCURRENCY("USD"),CNYCURRENCY("CNY"),AUDCURRENCY("AUD"),REQID_TYEP("1"),ORDERNO_TYPE("2"),LONNO_TYPE("3"),antCompany("1"),maCaoProject("2");
    private String type;
    EncodingEmun(String type){
        this.type = type;
    }
    public String getType() {
        return type;
    }

}
