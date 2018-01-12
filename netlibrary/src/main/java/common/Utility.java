package common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import com.nld.logger.LogUtils;
import com.nld.netlibrary.https.HttpConnetionHelper;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;


public class Utility {


    public static Map<String, String> jsonToMap(String json) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        JSONObject jsonPacket = new JSONObject(json);
        Iterator it = jsonPacket.keys();
        while (it.hasNext()) {
            String temp = it.next().toString();
            map.put(temp, jsonPacket.getString(temp));
        }
        return map;
    }

    /**
     * 获取IP地址
     *
     * @return
     * @throws Exception
     */
    public static String getLocalIpAddress() throws Exception {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {

                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                        .hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e("获取IP地址失败....................", e);
            return null;
        }
        return null;
    }

    /**
     * 获取SIM卡号
     *
     * @param context
     * @return
     */
    public static String getPhoneNo(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imsi = tm.getSubscriberId();
        Log.d("Utility", "getImsiNo=" + imsi);
        return imsi;
    }


    /**
     * 计算MAC域
     *
     * @return
     * @throws Exception
     */
    public static String getMacBlock(List domainList) throws Exception {
        if (domainList == null || domainList.isEmpty()) {
            throw new IllegalArgumentException("domain json is null");
        }
        StringBuffer macBlock = new StringBuffer();
        Iterator it = domainList.iterator();
        while (it.hasNext()) {
            String temp = (String) it.next();
            //判断域是否为空，若为空，则不选择
            if (temp == null || "".equals(temp.trim()) || temp.equals("null") || temp.equals("undefined")) {
                continue;
            } else {
                String macdomain = getMacDomain(temp);
                if (macdomain.equals("")) {
                    continue;
                } else {
                    macBlock.append(macdomain);
                    if (it.hasNext()) {
                        macBlock.append(" ");
                    }
                }
            }
        }
        Log.d("MAB", "MAB=" + macBlock.toString());
        //byte[] orgMacBlock = macBlock.toString().getBytes();
        //byte[] filledMacBlock = SecurityUtil.fillBytes(orgMacBlock);
        return macBlock.toString();
    }

    /**
     * 银行卡号校验规则
     *
     * @param cardno
     * @return
     */
    public static int PaymentENumCheck(String cardno) {
        char[] ChkData = cardno.toCharArray();
        int ChkDataLen = cardno.length();
        int i;
        int sum = 0;
        int tmp1, tmp2;
        int chk = 10;
        chk = ChkData[ChkDataLen - 1] - '0';
        for (i = ChkDataLen - 2; i >= 0; i--) {
            tmp2 = 0;
            tmp1 = ChkData[i] - '0';
            if ((ChkDataLen - 2 - i) % 2 == 0) {
                tmp1 *= 2;
                if (tmp1 / 10 != 0) {
                    tmp2 = tmp1 % 10;
                    tmp1 /= 10;
                }
            }
            sum += tmp1 + tmp2;
        }
        if (chk > 9 || chk < 0 || ((10 - sum % 10) % 10 != chk)) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 过滤MAC域
     *
     * @param source
     * @return
     * @throws Exception
     */
    public static String getMacDomain(String source) throws Exception {
        if (source == null || "".equals(source.trim())) {
            throw new IllegalArgumentException("source data empty get domain fail");
        }
        //取出左右空格
        String temp = source.trim();
        StringBuffer destString = new StringBuffer();
        char[] srcCharArr = temp.toCharArray();
        for (int i = 0; i < srcCharArr.length; i++) {
            char tc = srcCharArr[i];
            //筛选指定字符
            if ((tc <= 'Z' && tc >= 'A') || (tc <= 'z' && tc >= 'a') || (tc <= '9' && tc >= '0') || (tc == ' ') || (tc == ',') || (tc == '.')) {
                destString.append(tc);
            }
        }
        return destString.toString();
    }

    /**
     * 临时函数,替代小波的DataConverter.jsonStrToMap函数bug
     *
     * @param jsonStr
     * @return
     */
    public static Map jsonStrToMap(String jsonStr) {
        LogUtils.i("jsonStrToMap =[" + jsonStr + "]");
        Map<String, String> map = new HashMap<String, String>();
        try {
            JSONObject json = new JSONObject(jsonStr);
            Iterator it = json.keys();
            while (it.hasNext()) {
                String key = String.valueOf(it.next());
                String value = String.valueOf(json.get(key));
                map.put(key, value);
            }
        } catch (JSONException e) {
            LogUtils.e("json字符串转map发生异常,json=[" + jsonStr + "]", e);
        }
        return map;
    }

    /*
     * HashMap转String
     */
    public static String mapTojsonStr(Map<String, String> map) {
        String string = "{";
        for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
            Entry e = (Entry) it.next();
            string += "'" + e.getKey() + "':";
            string += "'" + e.getValue() + "',";
        }
        string = string.substring(0, string.lastIndexOf(","));
        string += "}";
        return string;
    }

    /**
     * 获取交易的本地时间
     * @return
     */
    public static String getTransLocalDate() {
        Time t = new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料

        t.setToNow(); // 取得系统时间
        String month = String.valueOf(t.month);
        if (!"".equals(month) && month != null) {            //月份取出来要加1
            month = String.valueOf(Integer.valueOf(month) + 1);
        }
        String date = String.valueOf(t.monthDay);
        if (month.length() < 2) {
            month = "0" + month;
        }
        if (date.length() < 2) {
            date = "0" + date;
        }
        return month + date;
    }
    /**
     * @return
     * @末尾填充FF长度满8字节整数倍
     */
    public static String fillBackChar(String src, char character) {
        if (src == null || src.length() % 16 == 0) {
            return src;
        }
        StringBuffer sbf = new StringBuffer(src);
        while (sbf.length() % 16 != 0) {
            sbf.append(character);
        }
        return sbf.toString(); // 将补位后的值返回
    }

    /*
     * 数字不足6位，左边补“0”
     */
    public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str);//左补0
                // sb.append(str).append("0");//右补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    /**
     * 数字不足strLength位，右边补空格
     */
    public static String addSpaceForStr(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append(str).append(" ");//右补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    /**
     * 数字不足strLength位，左边补空格
     */
    public static String addSpaceForStrLeft(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append(" ").append(str);//左补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    /*
     * 0000000001250转化成12.50
     */
    public static String unformatMount(String mount) {
        if (StringUtil.isEmpty(mount)) {
            return "0.00";
        }
        double money = 0;
        try {
            money = (double) (Long.parseLong(mount) * 0.01);
        } catch (Exception e) {
            LogUtils.e("格式化错误：" + mount, e);
            return "0.00";
        }
        if (money > 0) {
            DecimalFormat df = new DecimalFormat("##0.00");
            Log.i("ckh", "unformatMount == " + df.format(money));
            return df.format(money);

        } else {
            return "0.00";
        }
    }

    /**
     * 1.251234转化成1.25
     */
    public static String formatMountTow(double money) {
        DecimalFormat df = new DecimalFormat("##0.00");
        Log.i("ckh", "unformatMount == " + df.format(money));
        return df.format(money);

    }

    /*
     * 12.50转化成0000000001250
     */
    public static String formatMount(String mount) {
        if (StringUtil.isEmpty(mount)) {
            return "000000000000";
        }
        mount = mount.replace(".", "");
        Log.i("ckh", "formatMount == " + addZeroForNum(mount, 12));
        return addZeroForNum(mount, 12);
    }

    /*
     * 将卡号中间部分用*代替显示
     */
    public static String formatCardno(String cardno) {
        if (cardno.length() < 12 || cardno == null || "".equals(cardno)) {
            return cardno;
        }
        String midString = "********************".substring(0, cardno.length() - 10);
        String preString = cardno.substring(0, 6);
        String lasString = cardno.substring(cardno.length() - 4, cardno.length());
        return preString + midString + lasString;
    }

    /*
     * 获取系统当前时间，转化成组包12、13域数据
     */
    public static String getTransLocalTime() {
        Time t = new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料

        t.setToNow(); // 取得系统时间
        String hour = String.valueOf(t.hour); // 0-23
        String minute = String.valueOf(t.minute);
        String second = String.valueOf(t.second);
        if (hour.length() < 2) {
            hour = "0" + hour;
        }
        if (minute.length() < 2) {
            minute = "0" + minute;
        }
        if (second.length() < 2) {
            second = "0" + second;
        }
        return hour + minute + second;
    }

    // 打印凭条上面的时间
    public static String printFormatDateTime(String datetime) {
        Calendar c = Calendar.getInstance();
        String year = String.valueOf(c.get(Calendar.YEAR));
        String month = datetime.substring(0, 2);
        String day = datetime.substring(2, 4);

        String hour = datetime.substring(4, 6);
        String min = datetime.substring(6, 8);
        String sec = datetime.substring(8, 10);

        datetime = year + "/" + month + "/" + day + " " + hour + ":" + min + ":" + sec;

        return datetime;
    }

    public static String printFormateDateTime(String date, String time) {
        String dateTime = date + time;
        try {
            SimpleDateFormat sdf1 = new SimpleDateFormat("MMddHHmmss");
            SimpleDateFormat sdf2 = new SimpleDateFormat("MM/dd  HH:mm:ss");
            dateTime = sdf2.format(sdf1.parse(dateTime));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.w("格式化时间异常date=" + date + ";time=" + time, e);
            dateTime = "";
        }
        return dateTime;
    }

    /**
     * 格式化日期，如：“131031”转化成“2013/10/31”
     *
     * @param date
     * @return
     */
    public static String formatDate(String date) {
        String year = date.substring(0, 2);
        String month = date.substring(2, 4);
        String day = date.substring(4, 6);

        return "20" + year + "/" + month + "/" + day;
    }

    /**
     * 格式化时间，如:"011119"转化成“01:11:19”
     *
     * @param time
     * @return
     */
    public static String formatTime(String time) {
        String hour = time.substring(0, 2);
        String min = time.substring(2, 4);
        String sec = time.substring(4, 6);

        return hour + ":" + min + ":" + sec;
    }


    //获取外部版本号
    public static String getVersion() {
        String version = "SD_" + getAppVersion();
        return version;
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
    //计算字符串中含中文字符串长度
/*	public static int getLength(String str){
		str =  str.replaceAll( "[^x00-xff]" , "xx" );
		return str.length();
	}*/
    public static int length(String value) {
        int valueLength = 0;
        String chinese = "[\u0391-\uFFE5]";
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; i < value.length(); i++) {
            /* 获取一个字符 */
            String temp = value.substring(i, i + 1);
            /* 判断是否为中文字符 */
            if (temp.matches(chinese)) {
                /* 中文字符长度为2 */
                valueLength += 2;
            } else {
                /* 其他字符长度为1 */
                valueLength += 1;
            }
        }
        return valueLength;
    }


    /*
     * 用途：字符串格式化输出,固定长度前补空格
     * 输入：data字符串,长度length
     * 返回：data
     * @20130717 modify by chenkehui
     */
    public static String printFillSpace(String str, int length) {

        int str_length = length(str);
        if (str_length < length) {
            for (; str_length < length; length--) {
                str = " " + str;
            }
        } else {
            str = str.substring(0, length);
        }
        return str;
    }

    /*
     * 字符串str，左补空格/右补空格
     */
    public static String strFillSpace(String str, int strLength, boolean isLeft) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                if (isLeft) {
                    sb.append(" ").append(str);
                } else {
                    sb.append(str).append(" ");
                }
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    /*
     * 打印结算信息时，交易笔数格式
     */
    public static String printInteger(String intStr) {
        int value = 0;
        try {
            value = Integer.valueOf(intStr);
        } catch (Exception e) {
            LogUtils.e("格式化数据错误：" + intStr, e);
            value = 0;
            return "";
        }

        intStr = String.valueOf(value);
        return intStr;
    }

    /**
     * 手输入卡号格式化
     *
     * @return
     * @author Xrh
     * @20130815
     */
    public static String formatCardNo(String cardno) {
        if (cardno == null || "".equals(cardno)) {
            return cardno;
        }
        LogUtils.d("待格式化卡号 ：cardno = " + cardno);
        cardno = cardno.replaceAll(" ", "");
        LogUtils.d("待格式化卡号 ：cardno = " + cardno);

        if (cardno.indexOf("-") != -1) {
            return cardno;
        }
        int size = ((cardno.length()) % 4 == 0) ? ((cardno.length()) / 4) : ((cardno.length()) / 4 + 1);

        String card = "";

        for (int i = 0; i < size; i++) {
            int endIndex = (i + 1) * 4;
            if ((i + 1) == size) {
                endIndex = cardno.length();
            }
            if (i == 0) {
                card += cardno.substring(i, endIndex);
            } else {
                card += "  " + cardno.substring(i * 4, endIndex);
            }
        }

        LogUtils.d("格式化后卡号 ：card = " + card);
        return card;
    }

    /*
     * 不同交易类型对应完整或者简化pboc流程
     */
    public static Map<String, Boolean> transTypeMap = new HashMap<String, Boolean>() {{
        put("002301", true);
        put("002302", true);
        put("002303", false);
        put("002313", true);
        put("002314", false);
        put("002315", false);
        put("002316", false);
        put("002317", false);
        put("002322", true);
    }};

    /**
     * 获取基站信息
     *
     * @throws JSONException
     */
    public static String getGSMCellLocationInfo(Context context) {

        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String operator = manager.getNetworkOperator();
        if (StringUtil.isEmpty(operator) || operator.length() < 4) {
            return "";
        }
        /**通过operator获取 MCC 和MNC */
        int mcc = Integer.parseInt(operator.substring(0, 3));
        int mnc = Integer.parseInt(operator.substring(3));
        CellLocation commonLocation = manager.getCellLocation();
        CdmaCellLocation cdmaLocation = null;
        GsmCellLocation gsmLocation = null;
        if (commonLocation instanceof GsmCellLocation) {
            gsmLocation = (GsmCellLocation) commonLocation;
        } else if (commonLocation instanceof CdmaCellLocation) {
            cdmaLocation = (CdmaCellLocation) commonLocation;
        }
        int lac = 0;
        int cid = 0;
        if (null != gsmLocation) {
            /**通过GsmCellLocation获取中国移动和联通 LAC 和cellID */
            lac = gsmLocation.getLac();
            cid = gsmLocation.getCid();
        } else if (null != cdmaLocation) {
            /**通过CdmaCellLocation获取电信 LAC 和cellID */
            lac = cdmaLocation.getNetworkId();
            cid = cdmaLocation.getBaseStationId();
        }
        LogUtils.d("基站信息：mnc:" + mnc + ",lac:" + lac + ",cid:" + cid);
        String strMnc = addSpaceForStr(mnc + "", 2);
        String strLac = addSpaceForStrLeft(lac + "", 5);
        String strCid = addSpaceForStrLeft(cid + "", 8);
        String baseStation = strMnc + strLac + strCid;
        return baseStation;
    }

    public static boolean sysForSH() {
        LogUtils.d("系统版本号:[" + android.os.Build.BRAND + "]");
        return android.os.Build.BRAND.contains("Src2");//上海特有的版本号包含Src2
    }

    /**
     * 获取系统版本
     *
     * @return
     */
    public static String getSystemVersion() {
        LogUtils.i("系统版本号：[" + android.os.Build.BRAND + "]");
        return android.os.Build.BRAND;
    }

    // 生成随机密钥
    public static String getCryptData() {
        char[] ascii = "0123456789ABCDEF".toCharArray();
        Random rd = new Random();
        StringBuffer res = new StringBuffer();
        for (int i = 0; i < 16; i++) {
            Integer index = rd.nextInt(16);
            res.append(ascii[index]);
        }
        return res.toString();
    }

    // 生成随机密钥
    public static byte[] getCryptByte() {
        byte[] data = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
                13, 14, 15};
        byte[] random = new byte[8];
        for (int i = 0; i < 8; i++) {
            Integer h = new Random().nextInt(16);
            Integer l = new Random().nextInt(16);
            random[i] = (byte) (data[h] << 4 | l);
        }
        return random;
    }




    /**
     * 判断当前终端
     *
     * @return I代\II代分别返回1,2
     */
    public static int currentDevice() {
        String brand = android.os.Build.BRAND; // 系统版本号
        if (brand.startsWith("1") || brand.startsWith("2")) { // I代终端
            return 1;
        } else if (brand.startsWith("3")) { // II代终端
            return 2;
        }
        return 1;
    }

    // 保存到shared_prefs
    public static void saveShared_prefs(Context context, Map<String, String> map) {
        SharedPreferences sp = context.getSharedPreferences("OneKeyActivate", Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor mEditor = sp.edit();
        for (String key : map.keySet()) {
            mEditor.putString(key, map.get(key));
        }
        mEditor.commit();
    }

    public static void clearShared_prefs(Context context) {
        SharedPreferences sp = context.getSharedPreferences("OneKeyActivate", Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor mEditor = sp.edit();
        mEditor.clear();
        mEditor.commit();
    }


    public boolean checkApkExist(Context context, String packageName) {

        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        final PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

		if (packageInfo == null) {
            LogUtils.d(packageName + " is not install ");
			return false;
		} else {
            LogUtils.d(packageName + " is installed");
			return true;
		}
	}
	
	/**
	 * 把TransRecord转HashMap
	 * 
	 * @param record
	 * @return
	 */
	public static Map<String, String> transformToMap(Object record) {
		Map<String, String> map = new HashMap<String, String>();
        LogUtils.d("交易记录转map：record="+record);
		if(record==null){
			return null;
		}
		java.lang.reflect.Field[] fields = record.getClass()
				.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			fields[i].setAccessible(true);
			String key = fields[i].getName();
			if (fields[i].getType().getName()
					.equals(String.class.getName())) {
				// String 类型
				try {
					String value = String.valueOf(fields[i].get(record));
					map.put(key, value);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}

        }
        LogUtils.d("交易记录：" + map);
        return map;
    }

}
