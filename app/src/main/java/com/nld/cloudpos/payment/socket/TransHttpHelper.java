package com.nld.cloudpos.payment.socket;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.centerm.iso8583.util.DataConverter;
import com.nld.cloudpos.BankApplication;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;
import com.nld.starpos.banktrade.exception.HttpStatusException;
import com.nld.starpos.banktrade.exception.NldException;
import com.nld.starpos.banktrade.pinUtils.PinpadDev;
import com.nld.starpos.banktrade.utils.Cache;
import com.nld.starpos.banktrade.utils.NldPacketHandle;
import com.nld.starpos.banktrade.utils.ParamsConts;
import com.nld.starpos.banktrade.utils.ParamsUtil;
import com.nld.starpos.banktrade.utils.TransConstans;
import com.nld.starpos.banktrade.utils.TransParams;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import common.HexUtil;
import common.StringUtil;

public class TransHttpHelper {
    private static final Logger log = Logger.getLogger(HttpRequestJsIfc.class);

    //接口访问返回码
    public static final int TRANS_SUCCESS = 200;
    public static final int TRANS_REVERSE = 201;
    public static final int TRANS_FAILD = 202;


    private static final String TAG = "LogActivity";

    private final int tpduLen = 11; //TPDU 压缩的字节长度
    private Context context = null;
    private TransHandler handler = null;
    private NldPacketHandle isopacket = null;

    private String connectMode = "0"; //通讯模式, 0：3G专网, 1：公网, 2：其他

    HttpURLConnection conn = null;
    InputStream in = null;
    OutputStream out = null;
    private int timeout = 60;  //单位S
    private int reconntimes = 3;//重拨次数
    private String uriAPI = null;

    public TransHttpHelper(Context context, TransHandler handler) throws Exception {
        this.context = context;
        this.handler = handler;
        String timesStr = ParamsUtil.getInstance().getParam(TransParams.RECONN_TIMES);
        if (StringUtil.isEmpty(timesStr)) {
            timesStr = "3";
        }
        reconntimes = Integer.parseInt(timesStr);
        if (this.isopacket == null) {
            this.isopacket = new NldPacketHandle(context);
        }
        if (null == context) {
            throw new Exception("上下文Context不能为null");
        }
        connectMode = ParamsUtil.getInstance().getParam(ParamsConts.CONNECT_MODE);
    }

    /**
     * 交易开始
     *
     * @param deviceService
     * @param transCode
     * @param dataMap
     * @param transName
     * @return
     */
    public int transactionRequest(final AidlDeviceService deviceService, final String transCode, final Map<String, String> dataMap, String transName) {
        uriAPI = ParamsUtil.getInstance().getParam(ParamsConts.UNIONPAY_TRANS_URL);
        if (null == uriAPI || "".equals(uriAPI)) {
            //连接地址未设置，请先设置
            handler.messageSendProgressFaild(NldException.ERR_NET_URL_NULL_E108, false);
            return TRANS_FAILD;
        }
        Log.i("TAG", "http请求交易地址:[" + uriAPI + "]");
        if (uriAPI.startsWith("https:")) {
            return transactionRequestHttps(deviceService, transCode, dataMap, transName);
        }
        return TRANS_FAILD;
    }

    private static String end = "\r\n";
    private static String twoHyphens = "--";
    private static String boundary = "---------------------------7da2137580612";
    private int retry = 0;

    //*****************************HTTPS*******************************************

    /**
     * 交易开始，在交易中，对于交易成功的情况，
     * handler不发送交易成功的回调消息，通过接收函数的返回值的线程处理。
     * 交易状态变化，需要发送handler消息改变页面提示。
     * 交易失败分为两种情况，一个是需要冲正，一个不需要冲正。
     * 需要冲正的情况由接收函数返回值的线程处理，但此处需要保存错误信息，以便结果页显示。
     * 不需要冲正的直接发送handler交易失败的消息。
     * @param deviceService
     * @param transCode
     * @param dataMap
     * @return 返回值类型有三种，
     * </p>交易成功：TransHttpHelper.TRANS_SUCCESS
     * </p>   需要冲正：TransHttpHelper.TRANS_REVERSE
     * </p>  交易失败：TransHttpHelper.TRANS_FAILD
     */
    public int transactionRequestHttps(final AidlDeviceService deviceService, final String transCode, final Map<String, String> dataMap, String transName) {

        handler.messageSendStartProgress(getDealtimeout());      //发送消息开始倒计时
        handler.messageSendTipChange("正在发起" + transName + "..."); //动态改变界面显示文字
        byte[] msgData = null;
        try {
            msgData = isopacket.pack(deviceService, transCode, dataMap, 1);
            LogUtils.d("交易编码"+transCode+"/组包前dataMap的内容为：\r\n" + dataMap.toString()+"/组包后的msgData="+msgData);
        } catch (Exception e1) {
            LogUtils.e("组包过程发生异常.... transCode=[" + transCode + "]&&dataMap=[" + dataMap + "]", e1);
            e1.printStackTrace();
            handler.messageSendProgressFaild(NldException.ERR_DAT_PACK_E201, false);
            return TRANS_FAILD;
        }
        byte[] msg = isopacket.addMessageLen(msgData);
        LogUtils.d("发送加签的报文=[" + HexUtil.bcd2str(msg) + "]");
        try {
         //   String resultStr = postHttpsUrl(uriAPI, HexUtil.bcd2str(msg));
            String resultStr = HttpConnetionHelper.httpClient.postHttpsUrl(uriAPI, HexUtil.bcd2str(msg), handler);
            if (null == resultStr) {
                //网络访问失败
                handler.messageSendProgressFaild(NldException.ERR_NET_DEFAULT_E102, false);
                return TRANS_FAILD;
            }
            LogUtils.d("接收前置返回的交易数据：[" + resultStr + "]");

				/*
                 * 处理要求说明：
				 * 0	无处理要求
				 * 1 	下传终端磁条卡参数
				 * 2	上传终端磁条卡状态信息
				 * 3	重新签到
				 * 4	通知终端发起更新公钥信息操作
				 * 5	下载终端IC卡参数
				 * 6	TMS参数下载
				 * 7	卡BIN黑名单下载
				 * 8	币种汇率下载（仅在境外使用）/助弄取款[ 相关内容参见《助农取款涉及银联直连POS终端应用规范修订方案》。]手续费比率下载（仅在境内使用）
				 */
            String processRequire = resultStr.substring(19, 20);
            LogUtils.e("报文头处理要求："+ processRequire);
            if (processRequire.equals("6")) {
                //需要更新TMS参数
                ParamsUtil.getInstance().update("dowmloadparam", "6");
            } else if (processRequire.equals("4")) {
                //需要下载公钥
                ParamsUtil.getInstance().update(ParamsConts.UPDATA_STATUS, "1");
            } else if (processRequire.equals("5")) {
                //需要下载IC卡参数
                ParamsUtil.getInstance().update(ParamsConts.UPDATA_STATUS, "2");
            }

            byte[] pack = isopacket.subMessLen(HexUtil.hexStringToByte(resultStr));

            LogUtils.d("交易transCode=[" + transCode + "]" + "返回的报文=[" + HexUtil.bcd2str(pack) + "]");
            Map<String, String> resmap = isopacket.unPack(pack, "002311", 1);//解包
            LogUtils.d("解包后resmap " + StringUtil.map2LineStr(resmap));
            if ("002309".equals(transCode) || transCode.equals("002336") || transCode.equals("002337") || transCode.equals("002338")) {
                resmap.put("respcode", "00");
            }
            LogUtils.i("39域 rescode = " + resmap.get("respcode"));

            if (!"002308".equals(transCode) && !transCode.equals("500002")
                    && !transCode.equals("002329") && !transCode.equals("002331")
                    && !transCode.equals("002330") && !transCode.equals("002332")
                    && !transCode.equals("002333") && !transCode.equals("002309")
                    && !transCode.equals("002327") && !transCode.equals("002336")
                    && !transCode.equals("002337") && !transCode.equals("002338")) {       //结算\非签到\反激活\TC上送\IC卡公钥\IC卡参数交易\批上送，39域返回00时，校验mac

                if ("00".equals(resmap.get("respcode")) || isReverRequestSucc(transCode, resmap.get("respcode"))) {
                    String resultMac = resmap.get("mesauthcode");
                    String calMac = getMac(deviceService, pack);
                    LogUtils.d("计算mac:" + calMac + "  消息返回" + resultMac);
                    if (calMac.equals(resultMac)) { //MAC验证
                        Cache.getInstance().setResultMap(resmap);
                        //此处不发送成功消息，返回给上层。
                        return TRANS_SUCCESS;
                    } else {
                        //返回数据mac检验失败，保存错误信息方便结果页显示
                        Cache.getInstance().setReserverCode("A0");
                        Cache.getInstance().setErrCode(NldException.ERR_MER_CHECK_MAC_E403);
                        Cache.getInstance().setErrDesc(NldException.getMsg(NldException.ERR_MER_CHECK_MAC_E403));
                        return TRANS_REVERSE;
                    }
                } else {            //接到数据，但是交易处理不成功，不进行mac校验

                    Cache.getInstance().setResultMap(resmap);
                    //此处不发送成功消息，返回给上层。
                    String tip = NldException.getMsg(resmap.get("respcode"), NldException.MSG_RECEIVE_ERR);
                    handler.messageSendProgressFaild(resmap.get("respcode"), tip, false);
                    return TRANS_FAILD;
                }
            } else { //签到不需校验MAC
                if ("00".equals(resmap.get("respcode"))) {
                    LogUtils.i("签到交易返回MAP: " + resmap);
                    Cache.getInstance().setResultMap(resmap);
                    return TRANS_SUCCESS;
                } else {            //接到数据，但是交易处理不成功，不进行mac校验
                    Cache.getInstance().setResultMap(resmap);
                    String tip = NldException.getMsg(resmap.get("respcode"), NldException.MSG_RECEIVE_ERR);
                    handler.messageSendProgressFaild(resmap.get("respcode"), tip, false);
                    return TRANS_FAILD;
                }
            }

        } catch (Exception e) {
            LogUtils.d("交易异常：" + e.getMessage());
            //交易超时需要冲正。
            Cache.getInstance().setReserverCode("06");
            String expcode = NldException.getExpCode(e, NldException.ERR_NET_DEFAULT_E102);
            Cache.getInstance().setErrCode(expcode);
            Cache.getInstance().setErrDesc(NldException.getMsg(expcode));
            return TRANS_REVERSE;
        }
    }

    /**
     * 冲正成功的响应码是 00 ,12 ,25
     *
     * @param transCode
     * @return
     */
    private static boolean isReverRequestSucc(String transCode, String respcode) {
        if (TransConstans.TRANS_CODE_REVERSE.equals(transCode) && ("25".equals(respcode) || "12".equals(respcode))) {
            return true;
        } else {
            return false;
        }
    }

    private static final AllowAllHostnameVerifier HOSTNAME_VERIFIER = new AllowAllHostnameVerifier();
    private static X509TrustManager xtm = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };
    private static X509TrustManager[] xtmArray = new X509TrustManager[]{xtm};

    /**
     * HTTPS POST
     *
     * @param url     连接地址
     * @param sendStr 发送报文
     * @return
     * @throws Exception
     */
    public String postHttpsUrl(String url, String sendStr) throws Exception {
        boolean uscert = true;//使用证书
        retry++;
        URL urlobj = new URL(url);
        byte[] sendBytes = DataConverter.hexStringToByte(sendStr);
        conn = (HttpURLConnection) urlobj.openConnection();
        if (conn instanceof HttpsURLConnection) {
            // Trust all certificates
            SSLContext context = SSLContext.getInstance("TLS");

            if (uscert && BankApplication.mCacert != null) {
                log.debug("载入银联证书");
                KeyStore ksKeys = KeyStore.getInstance("BKS");
                ksKeys.load(null, null);
                // 导入根证书作为trustedEntry
                KeyStore.TrustedCertificateEntry trustedEntry = new KeyStore.TrustedCertificateEntry(BankApplication.mCacert);
                ksKeys.setEntry("ca_root", trustedEntry, null);

                TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");//密钥管理器,一般java应用传SunX509
                tmf.init(ksKeys);

                // 构建SSLContext，此处传入参数为TLS，也可以为SSL
                context.init(new KeyManager[0], tmf.getTrustManagers(), new SecureRandom());
            } else {
                context.init(new KeyManager[0], xtmArray, new SecureRandom());
            }
            SSLSocketFactory socketFactory = context.getSocketFactory();
            ((HttpsURLConnection) conn).setSSLSocketFactory(socketFactory);
            ((HttpsURLConnection) conn).setHostnameVerifier(HOSTNAME_VERIFIER);
        }

        /* 允许Input、Output，不使用Cache */
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
            conn.setRequestProperty("Connection", "close");
        }
        int timeout = getDealtimeout() * 1000;
        conn.setReadTimeout(timeout);
        conn.setConnectTimeout(6 * 1000);
        /* 设置传送的method=POST */
        conn.setRequestMethod("POST");
        /* 设置银联HTTPS报文头 */
        conn.setRequestProperty("User-Agent", "Donjin Http 0.1");
        conn.setRequestProperty("Cache-Control", "no-cache");
        conn.setRequestProperty("Content-Type", "x-ISO-TPDU/x-auth");
        conn.setRequestProperty("Accept", "*/*");
        conn.setRequestProperty("Content-Length", String.valueOf(sendBytes.length));

        try {
            out = conn.getOutputStream();
            out.write(sendBytes);// 发送8583报文数据
            out.flush();
        } catch (SocketTimeoutException e) {
            LogUtils.w("交易超时" + e.getMessage());
            if (retry < reconntimes) {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
                log.info("发起第" + retry + "次重试");
                handler.messageSendStartProgress(getDealtimeout());      //发送消息到networkactivity开始倒计时
                return postHttpsUrl(url, sendStr);
            } else {
                throw e;
            }
        }

        /* 取得Response内容 */
        in = conn.getInputStream();
        try {
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return inputStream2HexString(in);
            } else {
                throw new HttpStatusException("HTTP返回错误状态,状态码["
                        + conn.getResponseCode() + "]");
            }
        } catch (Exception e) {

            e.printStackTrace();
            throw e;
        } finally {
            if (in != null) {
                in.close();
                in = null;
            }
            if (out != null) {
                out.close();
                out = null;
            }
            conn.disconnect();
            conn = null;
            Log.w("httprequest", "close connection");
        }
    }

    /**
     * 将InputStream转换成某种字符编码的String
     *
     * @param in
     * @return
     * @throws Exception
     */
    public static String inputStream2HexString(InputStream in)
            throws Exception {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int count = -1;
        while ((count = in.read(data, 0, 1024)) != -1){
            outStream.write(data, 0, count);
        }
        data = null;
        return DataConverter.bytesToHexString(outStream.toByteArray());
    }

    /**
     * 中断网络连接
     */
    public void stopNetConn() {
    }

    private static String addFormField(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(twoHyphens + boundary + end);
            sb.append("Content-Disposition: form-data; name=\""
                    + entry.getKey() + "\"" + end);
            sb.append(end);
            sb.append(entry.getValue() + end);
        }
        return sb.toString();
    }


    public int getDealtimeout() { //交易超时时间,默认值60秒
        //int timeout = 60;
        try {
            String transTime = ParamsUtil.getInstance().getParam("dealtimeout");
            if ("".equals(transTime) || transTime == null) {
                this.timeout = 60;
            } else {
                this.timeout = Integer.parseInt(transTime);
            }
        } catch (NumberFormatException e) {
            log.warn("从数据库获取交易超时时间失败,返回默认值[" + timeout + "]", e);
        }
        if (this.timeout < 60 || this.timeout > 120) {
            this.timeout = 60;
        }
        return this.timeout;
    }

    private String getMac(AidlDeviceService deviceService, byte[] responce) {
        String ret = "";

        try {
            byte[] macBlock = new byte[responce.length - 8];
            System.arraycopy(responce, tpduLen, macBlock, 0, responce.length - 8 - tpduLen);

            byte[] macInfo = null;
            byte mkeyid = isopacket.getMkeyId(context);
            String macBloacStr = DataConverter.bytesToHexString(macBlock);
            macBloacStr = DataConverter.addZeroRightToMod16Equal0(macBloacStr);
            byte[] macBlockByte = DataConverter.getStringXor(macBloacStr);
            if ("0".equals(getPinPadDevSymbol(context))) {
                PinpadDev dev = new PinpadDev(deviceService, 0);
                macInfo = dev.getMac(mkeyid, HexUtil.hexStringToByte(macBloacStr));
            } else {
                PinpadDev dev = new PinpadDev(deviceService, 1);
                macInfo = dev.getMac(mkeyid, HexUtil.hexStringToByte(macBloacStr));
            }
            ret = DataConverter.bytesToHexString(macInfo);
        } catch (Exception e) {
            log.error("计算mac失败...", e);
            e.printStackTrace();
        }
        return ret;
    }
    public static String getPinPadDevSymbol(Context context) {
        ParamConfigDao mParamConfigDao = new ParamConfigDaoImpl();
        return mParamConfigDao.get("pinpadType");
    }
}
