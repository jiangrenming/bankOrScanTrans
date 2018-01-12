package com.nld.cloudpos.payment.socket;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.nld.starpos.banktrade.exception.HttpStatusException;
import com.nld.starpos.banktrade.utils.NldPacketHandle;
import com.nld.starpos.banktrade.utils.ParamsUtil;

import org.apache.http.NameValuePair;
import org.apache.log4j.Logger;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.StringUtil;


public class HttpRequestJsIfc {
	
	private static final Logger log = Logger.getLogger(HttpRequestJsIfc.class);

	private static final String TAG = "LogActivity";
    private static final String httpresponse = "httpresponse";
    
    private final int tpduLen = 5; //TPDU 压缩的字节长度
	private Context context = null;
	private Handler handler = null;
//	private ParamConfigDao paramConfigDao = null; //参数配置表
	private NldPacketHandle isopacket = null;
	
	private String connectMode = "1"; //通讯模式, 0：3G专网, 1：公网, 2：其他
	
	private int timeout = 60;  //单位S
	
	private Bundle mBundle = new Bundle();
	private Map<String, String> activiteMap = new HashMap<String, String>();
	
	public HttpRequestJsIfc(Context context, Handler handler) throws Exception {
		this.context = context;
		this.handler = handler;
		if (this.isopacket==null) {
			this.isopacket=new NldPacketHandle(context);
		}
		connectMode = ParamsUtil.getInstance().getParam("connect_mode");
		if(StringUtil.isEmpty(connectMode)){
		    connectMode="1";
		}
	}
	
	public int getDealtimeout (){ //交易超时时间,默认值60秒
		//int timeout = 60;
		try {
			String transTime = ParamsUtil.getInstance().getParam("dealtimeout");
			if ("".equals(transTime) || transTime == null) {
				this.timeout = 60;
			} else {
				this.timeout = Integer.parseInt(transTime);
			}
		} catch (NumberFormatException e) {
			log.warn("从数据库获取交易超时时间失败,返回默认值["+timeout+"]", e);
		}
		if(this.timeout<60 || this.timeout>120){
			this.timeout =60;
		}
		return this.timeout;
	}
	
	private static String end = "\r\n";
	private static String twoHyphens = "--";
	private static String boundary = "---------------------------7da2137580612";
	
	public String postUrl(String url, List<NameValuePair> nameparams,
                          String encoding) throws Exception {
		URL urlobj = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) urlobj.openConnection();
		/* 允许Input、Output，不使用Cache */
		conn.setDoInput(true);
		conn.setDoOutput(true);
		HashMap<String, String> params = new HashMap<String, String>();
		
		for (int i = 0; i < nameparams.size(); i++) {
			params.put(nameparams.get(i).getName(), nameparams.get(i).getValue());
		}

		String formField = addFormField(params);

		if (Build.VERSION.SDK != null && Build.VERSION.SDK_INT > 13) {
			conn.setRequestProperty("Connection", "close");
		}
		conn.setUseCaches(false);
		int timeout = getDealtimeout()*1000;
		conn.setReadTimeout(timeout);
		conn.setConnectTimeout(3 * 1000);
		
		/* 设置传送的method=POST */
		conn.setRequestMethod("POST");
		/* setRequestProperty */
		conn.setRequestProperty("Charset", encoding);
		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="
				+ boundary);
		OutputStream out = conn.getOutputStream();
		out.write(formField.toString().getBytes(encoding));// 发送表单字段数据
		out.write((twoHyphens + boundary + twoHyphens + end).getBytes(encoding));// 数据结束标志
		out.flush();
		/* 取得Response内容 */
		InputStream in = conn.getInputStream();
		int ch;
		StringBuffer b = new StringBuffer();
		try {
			if (conn.getResponseCode()== HttpURLConnection.HTTP_OK) {
				while ((ch = in.read()) != -1) {
					b.append((char) ch);
				}
			}else {
				throw new HttpStatusException("HTTP返回错误状态,状态码["
						+ conn.getResponseCode() + "]");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			in.close();
			out.close();
			conn.disconnect();
			Log.w("httprequest", "close connection");
		}
		return new String(b.toString().getBytes("ISO8859-1"),encoding);
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
	
	private void messageSendResult(Bundle mBundle){
		Message mMessage = Message.obtain();
		mMessage.setData(mBundle);
		mMessage.what = 0x00;
		handler.sendMessage(mMessage);
	}
	private void messageSendProgress(int what,Bundle mBundle){
		Message mMessage = Message.obtain();
		if(null!=mBundle){
		    mMessage.setData(mBundle);
		}
		mMessage.what = what;
		handler.sendMessage(mMessage);
	}
	
	public final static Map<String, String> map = new HashMap<String, String>() {{
	    put("0820", "0830");    
	    put("0200", "0210");
	    put("0100", "0110");
	    put("0400", "0410");
	    put("0220", "0230");
	    put("0320", "0330");
	    put("0500", "0510");
	    put("0620", "0630");
	}};
	
}
