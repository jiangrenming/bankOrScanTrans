package com.nld.cloudpos.payment.socket;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.bankline.R;
import com.nld.cloudpos.payment.NldPaymentActivityManager;
import com.nld.cloudpos.payment.activity.BaseActivity;
import com.nld.logger.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 网络请求页面
 * 
 * @ClassName: NetWorkActivity
 * @author tianxiaobo@centerm.com
 * @date 2015年5月27日 下午2:05:49
 * 
 */
public class NetWorkActivity extends BaseActivity {

	public static final String TAG = "NetWorkActivity";

	public static String NETWORK_IP_ADDRESS = "144.80.12.179";
	// 端口号
	public static int NETWORK_PORT = 7020;
	// 默认超时时间
	public static int NETWORK_TIMEOUT = 65;

	public static String ERRORMSG = "errormsg";

	public static String RESP_DATA = "respdata";
	private String netProgressTip = null; // 网络请求提示信息，如果不为null，则显示该提示信息

	private TextView count_time = null;
	private TextView nettip = null;
	private ImageView imgView = null; // 显示动画图标
	
	private String className = null;	//跳转类名
	private String tipMessage = null;	//显示内容
	public static final String CLASS_NAME = "classname";
	public static final String TIP_MESSAGE = "tipmessage";

	// 网络错误信息
	private static final Map<Integer, String> netMapErrMsg = new HashMap<Integer, String>() {
		{
			put(0x01, "本地网络异常");
			put(0x02, "未知的服务器地址");
			put(0x03, "连接服务器异常");
			put(0x04, "接收数据成功");
			put(0x06, "接收数据错误");
			put(0x07, "发送数据成功");
			put(0x09, "发送数据异常");
			put(0x10, "网络初始化成功");
		}
	};

	private Timer timer = new Timer();
	TimerTask task = null;

	// 要发送的数据
	private byte[] sendData = null;

	// socket数据发送线程
	private SocThread socketThread = null;
	private Intent respIntent = null;
	private Handler timerHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0x00:
				String curTimeStr = count_time.getText().toString();
				//curTimeStr = curTimeStr.substring(0, curTimeStr.length() - 1);
				int curTime = Integer.parseInt(curTimeStr);
				if (curTime > 62) {
					curTime--;
				} else {
					handler.obtainMessage(0x11).sendToTarget();
					timer.cancel();
				}
				count_time.setText(curTime + "");
				break; // 非网络消息不结束当前Activity
			}
		};
	};

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			respIntent = new Intent();
			try {
				switch (msg.what) {
				case 0x01:
					respIntent.putExtra("status", false);
					respIntent.putExtra(ERRORMSG, netMapErrMsg.get(0x01)
							.toString());
					break;
				case 0x02:
					respIntent.putExtra("status", false);
					respIntent.putExtra(ERRORMSG, netMapErrMsg.get(0x02)
							.toString());
					break;
				case 0x03:
					respIntent.putExtra("status", false);
					respIntent.putExtra(ERRORMSG, netMapErrMsg.get(0x03)
							.toString());
					break;
				case 0x04: // 接收数据成功
					respIntent.putExtra("status", true);
					respIntent.putExtra(NetWorkActivity.RESP_DATA,
							msg.obj.toString());
					break;
				case 0x06: // 接收数据异常
					respIntent.putExtra("status", false);
					respIntent.putExtra(ERRORMSG, netMapErrMsg.get(0x06)
							.toString());
					break;

				case 0x07: // 发送数据成功，开始接收数据
					count_time.setText(NETWORK_TIMEOUT + "");
					if (netProgressTip == null) {
						nettip.setText("正在接收数据");
					} else {
						nettip.setText(netProgressTip);
					}
					// socketThread.start(); //开始接收数据
					return; // 结束当前执行，防止页面finish
				case 0x09: // 发送数据失败
					respIntent.putExtra("status", false);
					respIntent.putExtra(ERRORMSG, netMapErrMsg.get(0x09)
							.toString());
					break;
				case 0x10: // 网络初始化成功，发送数据
					// socketThread.Send(sendData);
					return;

				case 0x11:
					LogUtils.d(TAG, "Handler收到未知消息类型");
					respIntent.putExtra("status", false);
					respIntent.putExtra(ERRORMSG, "未知网络异常");
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				respIntent.putExtra("status", false);
				respIntent.putExtra(ERRORMSG, "未知网络异常");
			}
			LogUtils.d(netMapErrMsg.toString());
			LogUtils.d(respIntent.getStringExtra(ERRORMSG));
			response(respIntent);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.network);

		imgView = (ImageView) super.findViewById(R.id.network_imgview);

		Animation operatingAnim = AnimationUtils.loadAnimation(this,
				R.anim.rotate);
		LinearInterpolator lin = new LinearInterpolator();
		operatingAnim.setInterpolator(lin);
		imgView.setAnimation(operatingAnim);
		operatingAnim.start();
		
		try {
			Intent intent = this.getIntent();
			className = intent.getStringExtra(CLASS_NAME);
			tipMessage = intent.getStringExtra(TIP_MESSAGE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// initStaticParam(); //初始化静态参数

		count_time = (TextView) super.findViewById(R.id.net_coutdowntime_tv);
		count_time.setText(NETWORK_TIMEOUT + "");
		nettip = (TextView) super.findViewById(R.id.nettip_tv);
		nettip.setText(tipMessage);
		task = new TimerTask() {
			@Override
			public void run() {
				timerHandler.obtainMessage(0x00).sendToTarget();
			}
		};
		timer.schedule(task, 0, 1000);

		socketThread = new SocThread(this.handler, this, NETWORK_IP_ADDRESS,
				NETWORK_PORT, NETWORK_TIMEOUT); // 网络请求线程
	}

	/**
	 * 发送接收数据
	 * 
	 * @param data
	 *            要发送的数据
	 * @param netTip
	 *            网络提示信息
	 * @createtor：Administrator
	 * @date:2015-6-23 下午5:28:40
	 */
	public void sendAndReceiveData(byte[] data, String netTip) {
		try {
			count_time.setText(NETWORK_TIMEOUT + "");
			this.netProgressTip = netTip;
			byte[] len = new byte[2];
			len[0] = (byte) (data.length / 256);
			len[1] = (byte) (data.length % 256);
			this.nettip.setText(netTip);
			// 2 发送数据给网络
			socketThread = null;
			socketThread = new SocThread(this.handler, this,
					NETWORK_IP_ADDRESS, NETWORK_PORT, NETWORK_TIMEOUT); // 网络请求线程
			socketThread.setSendData(data);
			socketThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HOME
				|| keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (null != task && null != timer) {
			task.cancel();
			timer.cancel();
		}
	}

	@Override
	protected void onStop() {

		super.onStop();
	}

	/**
	 * @Title: response
	 * @Description: 应答数据给请求者
	 * @param data
	 *            应答数据，包含两个字段status true/false
	 *            status为true的话处理成功，应答数据存储在respdata字段中
	 *            ，字节数组表示，status为false的话处理失败，应答数据存储在errmsg中
	 * @throws
	 */
	private void response(Intent data) {
		// 跳转到结果页，提示开通成功
		NldPaymentActivityManager.getActivityManager().removeActivity(this);
		Intent intent = new Intent();
		intent.setClassName(this, className);
		startActivity(intent);
	}

	// 错误应答
	public void onResponseError(String errorCode, String errorMsg) {

	}

	@Override
	public void goback(View v) {
		NldPaymentActivityManager.getActivityManager().removeActivity(this);
	}

	@Override
	public void setTopTitle() {

	}

	@Override
	public void onDeviceConnected(AidlDeviceService deviceManager) {

	}

    @Override
    public void onDeviceConnectFaild() {

    }

}
