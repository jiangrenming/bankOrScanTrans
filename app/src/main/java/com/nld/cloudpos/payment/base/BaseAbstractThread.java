package com.nld.cloudpos.payment.base;

public  abstract class BaseAbstractThread extends Thread {
	public boolean isCancel=false;
    public abstract boolean stopThread();
    public abstract void cancel();
}
