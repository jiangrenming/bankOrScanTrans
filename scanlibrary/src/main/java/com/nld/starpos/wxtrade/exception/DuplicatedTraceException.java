package com.nld.starpos.wxtrade.exception;

/**
 * 重复的流水
 * @author  jiangrenming
 * @date 2015年5月12日 下午2:48:58
 *
 */
public class DuplicatedTraceException extends Exception {
	private static final long serialVersionUID = 6988627598890967434L;

	public DuplicatedTraceException(String message) {
		super(message);
	}
}
