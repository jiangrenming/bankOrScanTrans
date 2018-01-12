package com.nld.netlibrary.https;

import android.os.Build;
import android.os.Handler;
import android.util.Log;
import com.centerm.iso8583.util.DataConverter;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by jiangrenming on 2017/12/6.
 * 网络请求的类
 */

public class HttpClient {

  // private HttpURLConnection conn = null;
  // private InputStream in = null;
  // private OutputStream out = null;
   private int retry = 0;

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
     * @param url  连接地址
     * @param sendStr 发送报文
     * @return
     * @throws Exception
     */
    public String  postHttpsUrl(String url, String sendStr, Handler handler) throws Exception {
        boolean uscert = true;//使用证书
        retry++;
        URL urlobj = new URL(url);
        byte[] sendBytes = DataConverter.hexStringToByte(sendStr);
        HttpURLConnection  conn = (HttpURLConnection) urlobj.openConnection();
        if (conn instanceof HttpsURLConnection) {
            // Trust all certificates
            SSLContext context = SSLContext.getInstance("TLS");
            if (uscert && HttpConnetionHelper.getX509CertFicated() != null) {
                KeyStore ksKeys = KeyStore.getInstance("BKS");
                ksKeys.load(null, null);
                // 导入根证书作为trustedEntry
                KeyStore.TrustedCertificateEntry trustedEntry = new KeyStore.TrustedCertificateEntry(HttpConnetionHelper.getX509CertFicated());
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
        int timeout = HttpConnetionHelper.getConnectTimes() * 1000;
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
        OutputStream out = null;
        InputStream in = null;
        try {
            out = conn.getOutputStream();
            out.write(sendBytes);// 发送8583报文数据
            out.flush();
        } catch (SocketTimeoutException e) {
            if (retry < HttpConnetionHelper.getRetryTimes()) {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
                handler.sendMessage(handler.obtainMessage(0x05)); //重新更新界面的倒计时
                return postHttpsUrl(url, sendStr,handler);
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
}
