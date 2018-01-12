package common;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
	
	protected static MessageDigest messagedigest = null;

	protected static char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	
	static {
		try {
			messagedigest = MessageDigest.getInstance("MD5");
		}catch (NoSuchAlgorithmException nsaex) {
			nsaex.printStackTrace();
		}
	}
	
	/**
	 * 锟斤拷锟斤拷址锟斤拷md5校锟斤拷值 
	 * @param str
	 * @return
	 */
	public static String getStringMD5(String str) {
		return getMD5String(str.getBytes());
	}
	

	public static boolean checkStringMD5(String str, String md5Str) {
		String md5 = getStringMD5(str);
		return md5.equals(md5Str.trim());
	}

	public static String getFileMD5(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
	    byte[] buffer = new byte[1024];
	    int numRead = 0;
	    while ((numRead = fis.read(buffer)) > 0) {
	    	messagedigest.update(buffer, 0, numRead);
	    }
	    fis.close();
		return bufferToHex(messagedigest.digest());
	}
	
	/**
	 * add by chenkehui @2013.07.23
	 */
	public static String getInputStreamMD5(InputStream ins) throws IOException {
	    byte[] buffer = new byte[1024];
	    int numRead = 0;
	    while ((numRead = ins.read(buffer)) > 0) {
	    	messagedigest.update(buffer, 0, numRead);
	    }
	    ins.close();
		return bufferToHex(messagedigest.digest());
	}

	public static boolean checkFileMD5(File file, String md5Str) {
		String md5 = null;
		try {
			md5 = getFileMD5(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return md5.equals(md5Str.trim());
	}

	public static String getFileMD5_old(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		FileChannel ch = in.getChannel();
		MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0,
				file.length());
		messagedigest.update(byteBuffer);
		return bufferToHex(messagedigest.digest());
	}

	public static String getMD5String(byte[] bytes) {
		messagedigest.update(bytes);
		return bufferToHex(messagedigest.digest());
	}

	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & 0xf0) >> 4];
		char c1 = hexDigits[bt & 0xf];
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

	/**
	 * 取字符串带盐值的MD5码(生成32位密文)
	 *
	 * @param str
	 * @param salt 盐
	 * @return
	 */
	public static String get32MD5Str(String str, String salt) {

		Log.i("MD5Util", "Original data: " + str + ", salt: " + salt);
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			if (!TextUtils.isEmpty(salt)) {
				messageDigest.update(salt.getBytes());
			}
			messageDigest.update(str.getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		byte[] byteArray = messageDigest.digest();
		StringBuffer md5StrBuff = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
				md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			} else {
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
			}
		}

		return md5StrBuff.toString();
	}

	public static String cryptsxfMd5(byte[] source) {
		String s = null;
		char[] hexDigits = new char[] { '0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest e = MessageDigest.getInstance("MD5");
			e.update(source);
			byte[] tmp = e.digest();
			char[] str = new char[32];
			int k = 0;

			for (int i = 0; i < 16; ++i) {
				byte byte0 = tmp[i];
				str[k++] = hexDigits[byte0 >>> 4 & 15];
				str[k++] = hexDigits[byte0 & 15];
			}

			s = new String(str);
		} catch (Exception arg9) {
			arg9.printStackTrace();
		}

		return s;
	}

	public static String addHmac(String data) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] bResult = md5.digest(data.getBytes());
			String hmac = bcdToString(bResult);
			return hmac;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 将BCD编码的字节数组转换为字符串
	 *
	 * @param bcds 字节数组
	 * @return 字符串
	 */
	public static String bcdToString(byte[] bcds) {
		if (bcds == null || bcds.length == 0) {
			return null;
		}
		byte[] temp = new byte[2 * bcds.length];
		for (int i = 0; i < bcds.length; i++) {
			temp[i * 2] = (byte) ((bcds[i] >> 4) & 0x0f);
			temp[i * 2 + 1] = (byte) (bcds[i] & 0x0f);
		}
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < temp.length; i++) {
			res.append(ascii[temp[i]]);
		}
		return res.toString();
	}
	public static final char[] ascii = "0123456789ABCDEF".toCharArray();
}
