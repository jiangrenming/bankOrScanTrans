package com.nld.cloudpos.payment.base;

import android.os.RemoteException;

import com.nld.cloudpos.aidl.emv.AidlPbocStartListener;
import com.nld.cloudpos.aidl.emv.CardInfo;
import com.nld.cloudpos.aidl.emv.PCardLoadLog;
import com.nld.cloudpos.aidl.emv.PCardTransLog;

import org.apache.log4j.Logger;


public class PbocListener extends AidlPbocStartListener.Stub {

	protected Logger logger= Logger.getLogger(PbocListener.class);
	
	private PbocProcessListener pbocProcessListener;
	private PbocTransListener pbocTransListener;
	/**
	 * 用于标志是否启动PBOC流程。设置监听器时该值为true，表示已经启动pboc
	 * 为false表示停止PBOC流程。
	 */
	public  static boolean isStart=false;

	
	/**
	 * 设置PBOC过程监听
	 * @param pbocProcessListener
	 */
	public void setPbocProcessListener(PbocProcessListener pbocProcessListener) {
	    //设置监听器说明已经启动PBOC
	    isStart=true;
		this.pbocProcessListener = pbocProcessListener;
	}

	/**
	 * 设置PBOC交易监听
	 * @param pbocTransListener
	 */
	public void setPbocTransListener(PbocTransListener pbocTransListener) {
        //设置监听器说明已经启动PBOC
	    isStart=true;
		this.pbocTransListener = pbocTransListener;
	}
	
	/**
	 * 清空监听
	 */
	public void clearListener(){
		logger.info("清空PBOC监听");
		this.pbocProcessListener=null;
		this.pbocTransListener=null;
	}

	/**
	 * PBOC流程监听
	 * 
	 * @author lizhangping@centerm.com
	 * 
	 */
	public interface PbocProcessListener {
		public abstract void requestImportAmount(int paramInt)
				throws RemoteException;

		public abstract void requestTipsConfirm(String paramString)
				throws RemoteException;

		public abstract void requestAidSelect(int paramInt,
                                              String[] paramArrayOfString) throws RemoteException;

		public abstract void requestEcashTipsConfirm() throws RemoteException;

		public abstract void onConfirmCardInfo(CardInfo paramCardInfo)
				throws RemoteException;

		public abstract void requestImportPin(int paramInt,
                                              boolean paramBoolean, String paramString)
				throws RemoteException;

		public abstract void requestUserAuth(int paramInt, String paramString)
				throws RemoteException;

		public abstract void onRequestOnline() throws RemoteException;

		public abstract void onReadCardOffLineBalance(String paramString1,
                                                      String paramString2, String paramString3, String paramString4)
				throws RemoteException;

		public abstract void onReadCardTransLog(
                PCardTransLog[] paramArrayOfPCardTransLog)
				throws RemoteException;

		public abstract void onReadCardLoadLog(String paramString1,
                                               String paramString2, PCardLoadLog[] paramArrayOfPCardLoadLog)
				throws RemoteException;

		public abstract void onTransResult(int paramInt) throws RemoteException;

		public abstract void onError(int paramInt) throws RemoteException;
	}

	/**
	 * PBOC交易监听
	 * 
	 * @author lizhangping@centerm.com
	 * 
	 */
	public interface PbocTransListener {

		public abstract void requestImportPin(int paramInt,
                                              boolean paramBoolean, String paramString)
				throws RemoteException;
		
		public abstract void requestUserAuth(int paramInt, String paramString)
				throws RemoteException;
		
		public abstract void onRequestOnline() throws RemoteException;

		public abstract void onTransResult(int paramInt) throws RemoteException;

		public abstract void onError(int paramInt) throws RemoteException;
	}

	@Override
	public void onConfirmCardInfo(CardInfo arg0) throws RemoteException {
		if(pbocProcessListener!=null){
			pbocProcessListener.onConfirmCardInfo(arg0);
		}
	}

	@Override
	public void onError(int paramInt) throws RemoteException {
		if(pbocProcessListener!=null){
			pbocProcessListener.onError(paramInt);
		}
		if(pbocTransListener!=null){
			pbocTransListener.onError(paramInt);
		}
	}

	@Override
	public void onReadCardLoadLog(String paramString1, String paramString2, PCardLoadLog[] paramArrayOfPCardLoadLog)
			throws RemoteException {
		if(pbocProcessListener!=null){
			pbocProcessListener.onReadCardLoadLog(paramString1, paramString2, paramArrayOfPCardLoadLog);
		}
	}

	@Override
	public void onReadCardOffLineBalance(String paramString1, String paramString2, String paramString3,
                                         String paramString4) throws RemoteException {
		if(pbocProcessListener!=null){
			pbocProcessListener.onReadCardOffLineBalance(paramString1, paramString2, paramString3, paramString4);
		}
	}

	@Override
	public void onReadCardTransLog(PCardTransLog[] paramArrayOfPCardTransLog) throws RemoteException {
		if(pbocProcessListener!=null){
			pbocProcessListener.onReadCardTransLog(paramArrayOfPCardTransLog);
		}
	}

	@Override
	public void onRequestOnline() throws RemoteException {
		if(pbocProcessListener!=null){
			pbocProcessListener.onRequestOnline();
		}
		if(pbocTransListener!=null){
			pbocTransListener.onRequestOnline();
		}
	}

	@Override
	public void onTransResult(int paramInt) throws RemoteException {
		if(pbocProcessListener!=null){
			pbocProcessListener.onTransResult(paramInt);
		}
		if(pbocTransListener!=null){
			pbocTransListener.onTransResult(paramInt);
		}
		//交易结果返回表示已经停止交易
		isStart=false;
	}

	@Override
	public void requestAidSelect(int paramInt, String[] paramArrayOfString)
			throws RemoteException {
		if(pbocProcessListener!=null){
			pbocProcessListener.requestAidSelect(paramInt, paramArrayOfString);
		}
	}

	@Override
	public void requestEcashTipsConfirm() throws RemoteException {
		if(pbocProcessListener!=null){
			pbocProcessListener.requestEcashTipsConfirm();
		}
	}

	@Override
	public void requestImportAmount(int paramInt) throws RemoteException {
		if(pbocProcessListener!=null){
			pbocProcessListener.requestImportAmount(paramInt);
		}
	}

	@Override
	public void requestImportPin(int paramInt, boolean paramBoolean, String paramString)
			throws RemoteException {
		if(pbocProcessListener!=null){
			pbocProcessListener.requestImportPin(paramInt, paramBoolean, paramString);
		}
		if(pbocTransListener!=null){
			pbocTransListener.requestImportPin(paramInt, paramBoolean, paramString);
		}
	}

	@Override
	public void requestTipsConfirm(String paramString) throws RemoteException {
		if(pbocProcessListener!=null){
			pbocProcessListener.requestTipsConfirm(paramString);
		}
	}

	@Override
	public void requestUserAuth(int paramInt, String paramString) throws RemoteException {
		if(pbocProcessListener!=null){
			pbocProcessListener.requestUserAuth(paramInt, paramString);
		}
		if(pbocTransListener!=null){
			pbocTransListener.requestUserAuth(paramInt, paramString);
		}
	}
	
	
}
