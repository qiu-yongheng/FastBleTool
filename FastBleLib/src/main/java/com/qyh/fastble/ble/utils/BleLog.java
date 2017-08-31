package com.qyh.fastble.ble.utils;


import com.elvishew.xlog.XLog;

public final class BleLog {
	public static void i(Object o) {
		XLog.i(o);
	}

	public static void i(String m) {
		XLog.i(m);
	}

	/*********************** Log ***************************/
	public static void v(String tag, String msg) {
		XLog.tag(tag).v(msg);
	}

	public static void d(String tag, String msg) {
		XLog.tag(tag).d(msg);
	}

	public static void i(String tag, String msg) {
		XLog.tag(tag).i(msg);
	}

	public static void w(String tag, String msg) {
		XLog.tag(tag).i(msg);
	}

	public static void e(String tag, String msg) {
		XLog.tag(tag).e(msg);
	}

	/*********************** Log with object list ***************************/
	public static void v(String tag, Object... msg) {
		XLog.tag(tag).v(getLogMessage(msg));
	}

	public static void d(String tag, Object... msg) {
		XLog.tag(tag).d(getLogMessage(msg));
	}

	public static void i(String tag, Object... msg) {
		XLog.tag(tag).i(getLogMessage(msg));
	}

	public static void w(String tag, Object... msg) {
		XLog.tag(tag).w(getLogMessage(msg));
	}

	public static void e(String tag, Object... msg) {
		XLog.tag(tag).e(getLogMessage(msg));
	}

	private static String getLogMessage(Object... msg) {
		if (msg != null && msg.length > 0) {
			StringBuilder sb = new StringBuilder();
			for (Object s : msg) {
				sb.append(s.toString());
			}
			return sb.toString();
		}
		return "";
	}

	/*********************** Log with Throwable ***************************/
	public static void v(String tag, String msg, Throwable tr) {
		XLog.tag(tag).v(msg, tr);
	}

	public static void d(String tag, String msg, Throwable tr) {
		XLog.tag(tag).d(msg, tr);
	}

	public static void i(String tag, String msg, Throwable tr) {
		XLog.tag(tag).i(msg, tr);
	}

	public static void w(String tag, String msg, Throwable tr) {
		XLog.tag(tag).w(msg, tr);
	}

	public static void e(String tag, String msg, Throwable tr) {
		XLog.tag(tag).e(msg, tr);
	}

	/*********************** TAG use Object Tag ***************************/
	public static void v(Object tag, String msg) {
		XLog.tag(tag.getClass().getSimpleName()).v(msg);
	}

	public static void d(Object tag, String msg) {
		XLog.tag(tag.getClass().getSimpleName()).d(msg);
	}

	public static void i(Object tag, String msg) {
		XLog.tag(tag.getClass().getSimpleName()).i(msg);
	}

	public static void w(Object tag, String msg) {
		XLog.tag(tag.getClass().getSimpleName()).w(msg);
	}

	public static void e(Object tag, String msg) {
		XLog.tag(tag.getClass().getSimpleName()).e(msg);
	}
}
