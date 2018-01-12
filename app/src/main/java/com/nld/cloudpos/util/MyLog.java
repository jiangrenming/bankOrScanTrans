package com.nld.cloudpos.util;

import android.util.Log;

import org.apache.log4j.Logger;

/**
 * @description Log操作封装类
 * @author Xrh
 * @date 2015-4-16 19:57:17
 */
public class MyLog {

	// 打印日志
	private Logger logger;
	private Class cls;

	public static MyLog getLogger(Class<?> cls) {
		return new MyLog(cls);
	}

	public MyLog(Class<?> cls) {
		try {
			this.cls = cls;
			logger = Logger.getLogger(cls);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void d(String s) {
		logger.debug(s);
	}

	public void i(String s) {
		logger.info(s);
	}

	public void t(String s) {
		logger.trace(s);
	}

	public void w(String s) {
		logger.warn(s);
	}

	public void e(String s) {
		logger.error(s);
	}
	
	public void e(Throwable t) {
		logger.error("", t);
	}

	public void e(String s, Throwable t) {
		logger.error(s, t);
	}

	public void log(LogType type, Object o) {
		log(type, o.toString());
	}

	public void log(LogType type, String s) {
		try {
			switch (type) {
			case TEST:
				Log.v(cls.getName(), s);
			case TRACE: // 交易日志
				i(s);
				break;
			case DEBUG: // 调试日志
				d(s);
				break;
			case INFO:
				i(s);
				break;
			case WARN:
				w(s);
				break;
			case ERROR:
				e(s);
				break;
			default:
				logger.fatal(s);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * LOG类型枚举
	 */
	public enum LogType {
		// DEBUG, INFO, TRACE, WARN, ERROR;
		TEST(0), DEBUG(1), INFO(2), TRACE(3), WARN(4), ERROR(5), FATAL(6);
		private int _level;// 定义私有变量

		// 构造函数，枚举类型只能为私有
		private LogType(int level) {
			this._level = level;
		}

		@Override
		public String toString() {
			return String.valueOf(this._level);
		}
	}

}
