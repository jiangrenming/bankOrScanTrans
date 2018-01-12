package com.nld.starpos.wxtrade.exception;

import android.os.Bundle;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * Created by jiangrenming on 2017/10/30.
 */

public class TransException extends Exception{

    private String errorCode;

    public TransException(String errorCode) {
        super(getMsg(errorCode));
        this.errorCode = errorCode;
    }

    public TransException() {
    }

    public final static String MSG_RECEIVE_ERR="交易返回异常结果";


    //网络错误
    public final static String ERR_NET_TRANS_TIMEOUT_E101="E101";//交易超过60S
    public final static String ERR_NET_DEFAULT_E102="E102";//未知网络异常的默认提示
    public final static String ERR_NET_CONNECT_E103="E103";//没有网络
    public final static String ERR_NET_SOCKET_E104="E104";//连接服务器超时,服务器Socket端口被占用（一般是链接还未关闭又发起链接）
    public final static String ERR_NET_HTTPSTATUS_E105="E105";//服务器无法访问
    public final static String ERR_NET_SOCKET_TIMEOUT_E106="E106";//服务器响应超时（已发送内容）
    public final static String ERR_NET_CONNECT_TIMEOUT_E107="E107";//服务器请求超时（未发送内容）
    public final static String ERR_NET_URL_NULL_E108="E108";//连接地址未设置，请先设置
    public final static String ERR_NET_UNKNOWNHOST_E109="E109";//主机地址无法解析
    public final static String ERR_NET_NETREQUEST_E110="E110";//网络访问失败，创建网络请求对象失败

    //数据错误
    public final static String ERR_DAT_PACK_E201="E201";//组包过程发生异常
    public final static String ERR_DAT_ENCRY_E202="E202";//数据加密异常
    public final static String ERR_DAT_RESULT_E203="E203";//交易返回异常结果
    public final static String ERR_DAT_FILENOTFOUND_E204="E204";//文件不存在
    public final static String ERR_DAT_GET_E205="E205";//数据获取异常
    public final static String ERR_DAT_UNPACK_E206="E206";//数据解析错误
    public final static String ERR_DAT_JSON_E207="E207";//JSON数据解析错误
    public final static String ERR_DAT_EOF_E208="E208";//数据获取异常

    //设备错误
    public final static String ERR_DEV_READ_CARD_E301="E301";//读卡失败
    public final static String ERR_DEV_READ_CARD_E302="E302";//读卡失败，交易终止
    public final static String ERR_DEV_READ_KERNEL_E303="E303";//成功导入交易结果后，读取内核失败
    public final static String ERR_DEV_TRANS_FAILD_E304="E304";//PBOC交易失败
    public final static String ERR_DEV_INPUT_KERNELE305="E305";//交易结果导入内核失败
    public final static String ERR_DEV_NO_ISSUE_SCRIPT_E306="E306";//无发卡行脚本执行结果
    public final static String ERR_DEV_TRANS_REFUSE_E307="E307";//PBOC交易拒绝
    public final static String ERR_DEV_RESULT_UNKNOW_E308="E308";//交易结果未知
    public final static String ERR_DEV_TRANS_END_E309="E309";//交易终止
    public final static String ERR_DEV_TRANS_EXCEPTION_E310="E310";//交易异常
    public final static String ERR_DEV_DEV_CONNECT_E311="E311";//设备连接失败
    public final static String ERR_DEV_TRANS_FINISH_E312="E312";//交易结束
    public final static String ERR_DEV_PK_UPDATE_E313="E313";//更新本地IC卡公钥失败
    public final static String ERR_DEV_AID_UPDATE_E314="E314";//更新本地AID参数失败
    public final static String ERR_DEV_PINPAD_E315="E315";//内置密码键盘错误
    public final static String ERR_DEV_EXPINPAD_E316="E316";//外置密码键盘错误
    public final static String ERR_DEV_MKEY_E317="E317";//主密钥注入失败
    public final static String ERR_DEV_SN_E318="E318";//终端序列号读取失败
    public final static String ERR_DEV_WKEY_E319="E319";//银行卡商户工作密钥注入失败
    public final static String ERR_DEV_PIK_E320="E320";//银行卡商户工作密钥注入失败
    public final static String ERR_DEV_MAK_E321="E321";//银行卡商户工作密钥注入失败
    public final static String ERR_DEV_NOSDKSERVICE_E322="E322";//未找到服务程序
    public final static String ERR_DEV_SYSDEV_E323="E323";//获取系统设备失败
    public final static String ERR_DEV_TRANS_OTHERPAGE_E324="E324";//请使用其他页面交易
    public final static String ERR_DEV_RF_ONLINE__E325="E325";//电子现金余额不足或超出卡片非接限额
    public final static String ERR_DEV_READ_CARD_TIMEOUT_E326="E326";//检卡超时


    //商户
    public final static String ERR_MER_LOGOUT_E401="E401";//商户信息注销失败
    public final static String ERR_MER_ANTI_ACTIVATION_E402="E402";//反激活失败
    public final static String ERR_MER_CHECK_MAC_E403="E403";//返回报文mac校验失败
    public final static String ERR_MER_CHECK_SIGN_E404="E404";//终端验签不通过
    public final static String ERR_MER_CER_NOFOUND_E405="E405";//找不到证书文件,请联系客服
    public final static String ERR_MER_LOCAL_CODE_E406="E406";//下载失败，本地处理码不对
    public final static String ERR_MER_NOTOPEN_E407="E407";//商户未开通
    public final static String ERR_MER_NOTOPEN_WX_E408="E408";//微信商户未开通
    public final static String ERR_MER_NOTOPEN_BANK_E409="E409";//银行卡商户未开通




    //微信
    public final static String ERR_WX_MKEY_E501="E501";//微信主密钥注入失败
    public final static String ERR_WX_WKEY_E502="E502";//微信工作密钥注入失败
    public final static String ERR_WX_PIK_E503="E503";//微信工作密钥注入失败
    public final static String ERR_WX_MAK_E504="E504";//微信工作密钥注入失败
    public final static String ERR_WX_REVERSE_E505="E505";//微信撤销成功返回交易失败
    public final static String ERR_WX_REVERSE_E506="E506";//微信撤销失败返回交易失败


    /**
     * 错误提示
     */
    private static HashMap<String, String> errorTip = new HashMap<String, String>(){
        {
            //POSP
            put("00" ,"交易成功");
            put("01" ,"请持卡人与发卡银行联系");
            put("02" ,"CALL BANK 查询");
            put("03" ,"无效商户");
            put("04" ,"此卡被没收");
            put("05" ,"持卡人认证失败");

            put("10" ,"承兑部分金额");
            put("11" ,"成功,VIP客户");
            put("12" ,"无效交易");
            put("13" ,"无效金额");
            put("14" ,"无效卡号");
            put("15" ,"此卡无对应发卡行");
            put("21" ,"该卡未初始化或睡眠卡");
            put("22" ,"操作有误,或超出交易允许天数");

            put("25" ,"没有原始交易,请联系发卡行");
            put("30" ,"报文格式错误,请重试");

            put("34" ,"作弊卡,吞卡");
            put("38" ,"密码错误次数超限,请联系发卡行");
            put("40" ,"发卡行不支持的交易类型");
            put("41" ,"挂失卡, 请没收");
            put("43" ,"被盗卡,请没收");
            put("45" ,"不允许降级,请使用芯片卡");
            put("51" ,"可用余额不足");

            put("54" ,"该卡已过期");
            put("55" ,"密码错");

            put("57" ,"不允许此卡交易");
            put("58" ,"发卡方不允许该卡在本终端进行此交易");
            put("59" ,"卡片效验错");
            put("61" ,"交易金额超限");
            put("62" ,"受限制的卡");
            put("64" ,"交易金额与原交易不匹配");
            put("65" ,"超出消费次数限制");
            put("68" ,"交易超时,请重试");
            put("75" ,"密码错误次数超限");
            put("77" ,"请重新签到再进行交易！");
            put("90" ,"系统日切,请稍后重试");
            put("91" ,"发卡方状态不正常, 请稍后重试");
            put("92" ,"发卡方线路异常,请稍后重试");
            put("94" ,"拒绝,重复交易,请稍后重试");
            put("95" ,"对帐不平");
            put("96" ,"拒绝,交换中心异常,请稍后重试");
            put("97" ,"终端未登记");
            put("98" ,"发卡方超时");
            put("99" ,"PIN 格式错,请重新签到");
            put("A0" ,"MAC 校验错,请重新签到");
            put("A1" ,"转账货币不一致");
            put("A2" ,"交易成功,请向资金转入行确认");
            put("A3" ,"资金到账行账号不正确");
            put("A4" ,"交易成功,请向资金到账行确认");
            put("A5" ,"交易成功,请向资金到账行确认");
            put("A6" ,"交易成功,请向资金到账行确认");
            put("A7" ,"安全处理失败");

            put("C0", "交易处理中");

            //互联网前置返回的错误码
            put("0000","无错误");
            put("0001","上送数据错误");
            put("0002","插入数据失败");
            put("0003","安全校验失败");
            put("0004","交易被拒绝");
            put("0005","创建POSP连接失败");
            put("0006","接收POSP返回失败");
            put("0007","数据库操作异常");
            put("0008","网络异常");
            put("0009","数值转换异常");
            put("0010","找不到服务器");
            put("0011","不支持的字符");
            put("0012","HTTP请求异常");
            put("0013","数据读取异常");
            put("0014","数据异常");
            put("0015","未找到该证书信息");
            put("0016","证书密码错误");
            put("0017","证书文件不存在");
            put("0018","下载证书出错");
            put("0019","找不到应用信息");
            put("0020","应用数据有误");
            put("0021","应用文件不存在");
            put("0022","未找到设备序列号对应的数据");
            put("0023","未找到设备编号");
            put("0024","应用文件不存在");
            put("0025","未找到任务编号");
            put("0026","未找到卡应用信息");
            put("0027","未找到商户信息");
            put("0028","未找到商户签购单名称");
            put("9999","系统繁忙(其他未知异常)");

            //网络错误
            put("E101","交易超时");
            put("E102","网络通讯异常");
            put("E103","网络信号差");
            put("E104","连接服务器超时");
            put("E105","服务器无法访问");
            put("E106","服务器响应超时");
            put("E107","服务器请求超时");
            put("E108","连接地址未设置，请先设置");
            put("E109","主机地址无法解析");
            put("E110","创建请求对象异常");

            //数据错误
            put("E201","组包异常");
            put("E202","数据加密异常");
            put("E203","未知错误");
            put("E204","网络通讯异常");//文件不存在
            put("E205","数据获取异常");
            put("E206","数据解析错误");
            put("E207","数据解析错误");
            put("E208","数据获取异常");

            //设备错误
            put("E301","读卡失败");
            put("E302","读卡失败，交易终止");
            put("E303","成功导入交易结果后，读取内核失败");
            put("E304","交易失败");
            put("E305","交易结果导入内核失败");
            put("E306","无发卡行脚本执行结果");
            put("E307","交易拒绝");
            put("E308","交易结果未知");
            put("E309","交易终止");
            put("E310","交易异常");
            put("E311","设备连接失败");
            put("E312","交易结束");
            put("E313","更新本地IC卡公钥失败");
            put("E314","更新本地AID参数失败");
            put("E315","内置密码键盘错误");
            put("E316","外置密码键盘错误");
            put("E317","主密钥注入失败");
            put("E318","终端序列号读取失败");
            put("E319","银行卡商户工作密钥注入失败");
            put("E320","银行卡商户工作密钥注入失败");
            put("E321","银行卡商户工作密钥注入失败");
            put("E322","未找到服务程序");
            put("E323","获取系统设备失败");
            put("E324","交易拒绝");//请使用其他页面交易
            put("E325","请使用联机消费");//电子现金余额不足或超出卡片非接限额或其他原因
            put("E326","检卡超时");

            //商户
            put("E401","商户信息注销失败");
            put("E402","反激活失败");
            put("E403","返回报文mac校验失败");
            put("E404","终端验签不通过");
            put("E405","找不到证书文件,请联系客服");
            put("E406","本地处理码错误");
            put("E407","商户未开通");
            put("E408","微信商户未开通");
            put("E409","银行卡商户未开通");


            //微信
            put("E501","微信主密钥注入失败");
            put("E502","微信工作密钥注入失败");
            put("E503","微信工作密钥注入失败");
            put("E504","微信工作密钥注入失败");
            put("E505","交易失败");
            put("E506","交易失败");
        }
    };



    /**
     * 获取错误编号
     * @return
     */
    public String getErrorCode(){
        return errorCode;
    }

    /**
     * 获取错误提示
     * @param errorCode
     * @return
     */
    public static String getMsg(String errorCode) {
        return getMsg(errorCode, "未知错误");

    }

    /**
     * 获取错误提示
     * @param errorCode
     * @return
     */
    public static String getMsg(String errorCode,String defmsg) {
        if (errorTip.containsKey(errorCode)) {
            return errorTip.get(errorCode);
        } else {
            return defmsg;
        }

    }

    /**
     * 处理异常
     * @param expcode
     */
    public static Bundle getExpBundle(String expcode){
        Bundle bd=new Bundle();
        bd.putString("retCode",expcode);
        bd.putString("errMsg",getMsg(expcode));
        bd.putBoolean("isSuc", false);
        return bd;
    }

    /**
     * 获取异常错误码
     * @param e
     * @return
     */
    public static String getExpCode(Exception e){
        return getExpCode(e, "");
    }

    /**
     * 获取异常错误码
     * @param e
     * @return
     */
    public static String getExpCode(Exception e,String defcode){
        String retCode="";
        if (e instanceof JSONException) {
            retCode= TransException.ERR_DAT_JSON_E207;
        } else if (e instanceof HttpStatusException) {
            retCode= TransException.ERR_NET_HTTPSTATUS_E105;
        } else if (e instanceof ConnectException) {
            retCode= TransException.ERR_NET_CONNECT_E103;
        } else if (e instanceof SocketException) {
            retCode= TransException.ERR_NET_SOCKET_E104;
        } else if (e instanceof SocketTimeoutException) {
            retCode= TransException.ERR_NET_SOCKET_TIMEOUT_E106;
        } else if (e instanceof ConnectTimeoutException) {
            retCode= TransException.ERR_NET_CONNECT_TIMEOUT_E107;
        } else if (e instanceof FileNotFoundException) {
            retCode= TransException.ERR_DAT_FILENOTFOUND_E204;
        } else if (e instanceof NullConnectAddressException) {
            retCode= TransException.ERR_NET_URL_NULL_E108;
        } else if (e instanceof UnknownHostException) {
            retCode= TransException.ERR_NET_UNKNOWNHOST_E109;
        } else if (e instanceof EOFException) {
            retCode= TransException.ERR_DAT_EOF_E208;
        }
        if(retCode.equals("")){
            return defcode;
        }
        return retCode;
    }
}
