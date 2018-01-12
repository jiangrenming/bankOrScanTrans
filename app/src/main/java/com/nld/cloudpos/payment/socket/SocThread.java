package com.nld.cloudpos.payment.socket;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;
import com.nld.logger.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import common.HexUtil;

/**
 * socket数据发送线程
 * 
 * @ClassName: SocThread
 * @author tianxiaobo@centerm.com
 * @date 2015年5月27日 下午7:39:43
 * 
 */
public class SocThread extends Thread {
	private String ip = "127.0.0.1";
	private int port = 0x80;
	private int timeout = 65000;

	private String TAG = "socket thread";

	public Socket client = null;
	private OutputStream out = null; // 输出流发送数据
	private InputStream ins = null; // 输入流接收数据
	public boolean isRun = true; // 是否接收数据
	private Handler handler = null; // 消息句柄
	private Context ctx;

	private byte[] sendData = null; // 要发送的数据
	private byte[] retData = null; // 接收回来的数据

	public byte[] getSendData() {
		return sendData;
	}

	public void setSendData(byte[] sendData) {
		this.sendData = sendData;
	}

	public byte[] getRetData() {
		return retData;
	}

	public void setRetData(byte[] retData) {
		this.retData = retData;
	}

	/**
	 * 构造函数
	 * <p>
	 * Title:
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * 
	 * @param handler
	 * @param context
	 * @param ip
	 * @param port
	 * @param timeout
	 */
	public SocThread(Handler handler, Context context, String ip, int port,
                     int timeout) {
		this.handler = handler;
		this.ctx = context;
		this.ip = ip;
		this.port = port;
		this.timeout = timeout * 1000;
		LogUtils.d(TAG, "创建线程socket");
        LogUtils.d(TAG, "ip = " + ip + ";port = " + port + ";timeout = "
				+ timeout);
		;
	}

	/**
	 * 连接socket服务器
	 */
	private int conn() {

		try {
			Log.i(TAG, "连接中……");
			// 判断网络是否可用
			if (!this.isNetworkAvailable()) {
				handler.obtainMessage(0x01).sendToTarget(); // 本地网络异常
				return -1;
			}
			client = new Socket(ip, port);
			client.setSoTimeout(timeout - 5000);// 设置阻塞时间，60sLogUtils.d();(TAG, "连接成功");
			ins = client.getInputStream();
			out = client.getOutputStream();
            LogUtils.d(TAG, "输入输出流获取成功");
			handler.obtainMessage(0x10).sendToTarget();
			return 0x00;
		} catch (UnknownHostException e) {
            LogUtils.d(TAG, "连接错误UnknownHostException 重新获取");
			e.printStackTrace();
			handler.obtainMessage(0x02).sendToTarget();
		} catch (IOException e) {
            LogUtils.d(TAG, "连接服务器io错误");
			handler.obtainMessage(0x03).sendToTarget();
			e.printStackTrace();
		} catch (Exception e) {
            LogUtils.d(TAG, "连接服务器错误Exception" + e.getMessage());
			handler.obtainMessage(0x03).sendToTarget();
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * 实时接受数据
	 */
	@Override
	public void run() {
        LogUtils.d(TAG, "线程socket开始运行");
        LogUtils.d(TAG, "1.run开始");

		// 1 打开连接
		int result = conn();
		if (result == -1) {
			return;
		}
		// 2 发送数据
		if (null == sendData) {
			return;
		}
		int ret = send(this.sendData);
		if (-1 == ret) { // 数据发送失败
			return;
		}
		// 3 接收数据
		byte[] data = null; // 存储接收到的数据
		try {
			if (client != null) {
                LogUtils.d(TAG, "2.接收数据");
				data = this.receiveData(client, this.timeout);
                LogUtils.d(
						TAG,
						"接收到的数据为:"
								+ (null != data ? HexUtil
										.bcd2str(data) : null));
				handler.obtainMessage(0x04,
						null != data ? HexUtil.bcd2str(data) : null)
						.sendToTarget(); // 发送接收的数据
			} else {
				handler.obtainMessage(0x06).sendToTarget();
				// conn();
			}
		} catch (Exception e) {
            LogUtils.d(TAG, "数据接收错误" + e.getMessage());
			e.printStackTrace();
			handler.obtainMessage(0x06).sendToTarget();
		}
		close();

		// 4 关闭连接
	}

	/**
	 * 发送数据
	 * 
	 */
	public int send(byte[] data) {
		int ret = -1;
		try {
			if (client != null && data != null) {
                LogUtils.d(TAG, "发送" + HexUtil.bcd2str(data) + "至"
						+ client.getInetAddress().getHostAddress() + ":"
						+ String.valueOf(client.getPort()));
				out.write(data);
				out.flush();
                LogUtils.d(TAG, "发送成功");
				handler.obtainMessage(0x07, data != null ? data : "")
						.sendToTarget();
				return 0x00;
			} else {
                LogUtils.d(TAG, "client 不存在");
				handler.obtainMessage(0x09).sendToTarget();
				// conn();
			}

		} catch (Exception e) {
            LogUtils.d(TAG, "send error");
			e.printStackTrace();
			handler.obtainMessage(0x09).sendToTarget();
		} finally {
            LogUtils.d(TAG, "发送完毕");
		}
		return -1;
	}

	/**
	 * 关闭连接
	 */
	public void close() {
		try {
			if (client != null) {
                LogUtils.d(TAG, "close in");
				ins.close();
                LogUtils.d(TAG, "close out");
				out.close();
                LogUtils.d(TAG, "close client");
				client.close();
			}
		} catch (Exception e) {
            LogUtils.d(TAG, "close err");
			e.printStackTrace();
		}
	}

	/**
	 * @Title: isNetworkAvailable
	 * @Description: 判断网络状态是否可用
	 * @return
	 * @throws
	 */
	public boolean isNetworkAvailable() {
		Context context = this.ctx.getApplicationContext();
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 方法描述：从socket输入流获取数据
	 * 
	 * @createtor：Xiaobo Tian
	 * @date:2013-4-22 下午5:47:15
	 * @param socket
	 * @param timeout
	 * @return byte[]
	 */
	public byte[] receiveData(Socket socket, int timeout) {
		byte[] retData = null;
		try {
			InputStream ins = socket.getInputStream();
			socket.setSoTimeout(timeout - 5000);
			byte[] msgLen = new byte[2];
			int len = ins.read(msgLen);
			if (2 == len) {
				int realLen = HexUtil.bytes2short(msgLen);
				byte[] data = new byte[realLen];
				int count = 0;
				while (count < realLen) {
					int temp = ins.read(data, count, realLen);
					count += temp;
				}
				retData = new byte[2 + data.length];
				System.arraycopy(msgLen, 0, retData, 0, msgLen.length);
				System.arraycopy(data, 0, retData, 2, data.length);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return retData;
	}
}