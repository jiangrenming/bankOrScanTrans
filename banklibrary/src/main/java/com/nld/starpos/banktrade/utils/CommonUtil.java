package com.nld.starpos.banktrade.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.util.Log;

import com.nld.logger.LogUtils;
import com.nld.netlibrary.https.HttpConnetionHelper;
import com.nld.starpos.banktrade.db.ParamConfigDao;
import com.nld.starpos.banktrade.db.local.ParamConfigDaoImpl;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import common.HexUtil;

public class CommonUtil {

	/**
	 * 判断是否为芯片卡
	 * 
	 * @param track2data
	 * @return
	 */
	public static boolean isIcCard(String track2data) {

		if ("".equals(track2data) || track2data == null) {
			return false;
		}
		if ((!track2data.contains("=")) && (!track2data.contains("D"))) {
			return false;
		}
		String temp[] = null;
		String key = "";
		if (track2data.contains("=")) {
			temp = track2data.split("=");
			key = temp[1].substring(4, 5);
		} else if (track2data.contains("D")) {
			temp = track2data.split("D");
			key = temp[1].substring(4, 5);
		} else {
			return false;
		}
		return "2".equals(key) || "6".equals(key);
	}

	/**
	 * 卡号加密保留前6位与末尾4位，中间用*号代替
	 * 
	 * @param carno
	 * @return
	 */
	public static String getMarkCarno(String carno) {
		String maskNo = "*********";
		String start = carno.substring(0, 6);
		String end = carno.substring(carno.length() - 4);
		maskNo = start + "******" + end;
		return maskNo;
	}

	/**
	 * 获取连接的银联IP与端口例：127.0.0.1:8888
	 * 
	 * @return
	 */
	public static String getIpAndPortUnionPay() {
		return BankConfig.getIpAndPortUnionPay();
	}
	
	
	public static void setDateTime(int year, int month, int day, int hour, int minute) throws IOException, InterruptedException {  
	    
        requestPermission();  
  
        Calendar c = Calendar.getInstance();  
  
        c.set(Calendar.YEAR, year);  
        c.set(Calendar.MONTH, month-1);  
        c.set(Calendar.DAY_OF_MONTH, day);  
        c.set(Calendar.HOUR_OF_DAY, hour);  
        c.set(Calendar.MINUTE, minute);  
          
          
        long when = c.getTimeInMillis();  
  
        if (when / 1000 < Integer.MAX_VALUE) {  
            SystemClock.setCurrentTimeMillis(when);  
        }  
  
        long now = Calendar.getInstance().getTimeInMillis();  
        //Log.d(TAG, "set tm="+when + ", now tm="+now);  
  
        if(now - when > 1000)  {
			throw new IOException("failed to set Date.");
		}
    }
	static void requestPermission() throws InterruptedException, IOException {  
        createSuProcess("chmod 666 /dev/alarm").waitFor();  
    }  
      
    static Process createSuProcess() throws IOException  {  
        File rootUser = new File("/system/xbin/ru");  
        if(rootUser.exists()) {  
            return Runtime.getRuntime().exec(rootUser.getAbsolutePath());  
        } else {  
            return Runtime.getRuntime().exec("su");  
        }  
    } 
    static Process createSuProcess(String cmd) throws IOException {  
        
        DataOutputStream os = null;  
        Process process = createSuProcess();  
  
        try {  
            os = new DataOutputStream(process.getOutputStream());  
            os.writeBytes(cmd + "\n");  
            os.writeBytes("exit $?\n");  
        } finally {  
            if(os != null) {  
                try {  
                    os.close();  
                } catch (IOException e) {  
                }  
            }  
        }  
  
        return process;  
    }  
    
    public static String getAppVersion(){
        String version="";
        PackageManager pm= HttpConnetionHelper.getmContext().getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo( HttpConnetionHelper.getmContext().getPackageName(),0);
            version=info.versionName;
        } catch (NameNotFoundException e) {
            version="0.0.0";
            e.printStackTrace();
        }
        version="V"+version;
        return version;
    }
    
    /**
	 * 判断网络是否可用
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			
			String name = info.getTypeName();
			Log.d("==","当前网络名称：" + name);
			return true;
		}
		return false;
	}
	
	/**
	 * 获取卡类型
	 * @param type
	 * @return
	 */
	public static String revCardType(String type){
		String typename = null;
		if(type == null || type.length()<3){
			return "";
		}
		Map<String,String> revNames = new HashMap<String,String>();
		revNames.put("CUP","银联卡");
	    revNames.put("VIS","威士卡");
	    revNames.put("MCC","万事达卡");
	    revNames.put("MAE","万事达卡");
	    revNames.put("JCB","JCB卡");
	    revNames.put("DCC","大莱卡");
	    revNames.put("AMX","运通卡");
		
		typename = revNames.get(type);
		if(typename == null){
			return type;
		}else{
			return typename;
		}
	}
	
	/**
	 * 获取收单行、发卡行
	 * @param issubank
	 * @return
	 */
	public static String revBankName(String issubank){
		String bankname = null;
		if(issubank == null || issubank.length()<4){
			return "";
		}
		String bankCode = issubank.substring(0, 4);
		Map<String,String> revNames = new HashMap<String,String>();
		revNames.put("0102","工商银行");
	    revNames.put("0103","农业银行");
	    revNames.put("0104","中国银行");
	    revNames.put("0105","建设银行");
	    revNames.put("0100","邮储银行");
	    revNames.put("0301","交通银行");
	    revNames.put("0302","中信银行");
	    revNames.put("0303","光大银行");
	    revNames.put("0304","华夏银行");
	    revNames.put("0305","民生银行");
	    revNames.put("0306","广发银行");
	    revNames.put("0307","平安银行");
	    revNames.put("0410","平安银行");
	    revNames.put("0308","招商银行");
	    revNames.put("0309","兴业银行");
	    revNames.put("0310","浦发银行");
	    revNames.put("0403","北京银行");
	    revNames.put("6403","北京银行");
	    revNames.put("0401","上海银行");
		revNames.put("1438","湖南农信");
		revNames.put("0570","华融湘江");
		revNames.put("0461","长沙银行");
		revNames.put("0425","东莞银行");
		revNames.put("0489","南粤银行");
		revNames.put("1418","北京农商");
		revNames.put("1401","上海农商");
		revNames.put("4802","银联商务");
		revNames.put("0311","恒丰银行");
		revNames.put("0316","浙商银行");
		revNames.put("0317","渤海银行");
		revNames.put("0402","厦门银行");
		revNames.put("0464","泉州银行");
		revNames.put("0405","海峡银行");
		revNames.put("1420","鄞州银行");
		
		bankname = revNames.get(bankCode);
		if(bankname == null){
			return issubank;
		}else{
			return bankname;
		}
	}

	// 配置应用参数数据
	public static String makeParamToEMV(Context context) {
		ParamConfigDao paramConfigDao = new ParamConfigDaoImpl();
		String mchntname = paramConfigDao.get("mchntname");    // 商户名称
		if ("".equals(mchntname) || mchntname == null)
			return null;
		String tag = "9F4E";
		String mchntname_hex = null;
		String len = null;
		try {
			mchntname_hex = HexUtil.bcd2str(mchntname.getBytes("GBK"));
			len = Integer.toHexString(mchntname.getBytes("GBK").length);
			if (len.length() % 2 != 0) {
				len = "0" + len;
			}
			LogUtils.i("商户名称 tlv = " + tag + len + mchntname_hex);
			return tag + len + mchntname_hex;
		} catch (UnsupportedEncodingException e) {
			LogUtils.e("配置应用参数数据异常");
			e.printStackTrace();
			return null;
		}

	}

}
