package com.nld.starpos.banktrade.pinUtils;

import android.os.RemoteException;

import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.cpucard.AidlCPUCard;
import com.nld.cloudpos.aidl.emv.AidlPboc;
import com.nld.cloudpos.aidl.iccard.AidlICCard;
import com.nld.cloudpos.aidl.magcard.AidlMagCard;
import com.nld.cloudpos.aidl.pinpad.AidlPinpad;
import com.nld.cloudpos.aidl.printer.AidlPrinter;
import com.nld.cloudpos.aidl.psam.AidlPsam;
import com.nld.cloudpos.aidl.rfcard.AidlRFCard;
import com.nld.cloudpos.aidl.scan.AidlScanner;
import com.nld.cloudpos.aidl.serialport.AidlSerialport;
import com.nld.cloudpos.aidl.shellmonitor.AidlShellMonitor;

public class AidlUtils {

	public  static  AidlDeviceService mService;

	private static  class InnerBankAidl{
		private static  final AidlUtils AIDL_UTILS = new AidlUtils();
	}

	public static AidlUtils getInstance(){
		return  InnerBankAidl.AIDL_UTILS;
	}

	public  AidlDeviceService getmService() {
		return mService;
	}

	public  void setmService(AidlDeviceService mService) {
		AidlUtils.mService = mService;
	}

	/** 接触式IC卡设备实例  */
	protected static AidlICCard aidlICCard;
	/** 获取打印机操作实例  */
	protected static AidlPrinter aidlPrinter;
	/** cpu卡的读写*/
	protected static AidlCPUCard aidlCPUCard;
	/** 获取EMV操作实例  */
	protected static AidlPboc aidlEmv;
	/** 获取PSAM卡设备操作实例 */
	protected static AidlPsam aidlPsam;
	/** 获取磁条卡设备操作实例  */
	protected static AidlMagCard aidlMagCard;
	/** 获取密码键盘操作实例  */
	protected static AidlPinpad aidlPinpad;
    /** 获取扫描枪句柄  */
	protected static AidlScanner aidlScanner;
	/** 非接触式IC卡设备实例  */
	protected static AidlRFCard aidlRFCard;
	/** 获取串口操作实例  */
	protected static AidlSerialport aidlSerialport;
	/** 获取ShellMonitor操作实例  */
	protected static AidlShellMonitor aidlShellMonitor;
	
	public void init(){
		try {
			aidlICCard = AidlICCard.Stub.asInterface(mService.getInsertCardReader());
			aidlPrinter = AidlPrinter.Stub.asInterface(mService.getPrinter());
			aidlCPUCard = AidlCPUCard.Stub.asInterface(mService.getCPUCard());
			aidlEmv = AidlPboc.Stub.asInterface(mService.getEMVL2());
			aidlPsam = AidlPsam.Stub.asInterface(mService.getPSAMReader(0));
			aidlMagCard = AidlMagCard.Stub.asInterface(mService.getMagCardReader());
			aidlPinpad = AidlPinpad.Stub.asInterface(mService.getPinPad(0));
			aidlScanner = AidlScanner.Stub.asInterface(mService.getScanner());
			aidlRFCard = AidlRFCard.Stub.asInterface(mService.getRFIDReader());
			aidlSerialport = AidlSerialport.Stub.asInterface(mService.getSerialPort(0));
			aidlShellMonitor = AidlShellMonitor.Stub.asInterface(mService.getShellMonitor());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
;;