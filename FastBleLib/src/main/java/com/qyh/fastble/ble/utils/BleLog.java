package com.qyh.fastble.ble.utils;

public final class BleLog {
	public static void i(Object o) {
		LogUtils.i(o.toString());
	}

	public static void i(String m) {
		LogUtils.i(m);
	}

	/*********************** Log ***************************/
	public static void v(String tag, String msg) {
		LogUtils.v(tag, msg);
	}

	public static void d(String tag, String msg) {
		LogUtils.d(tag, msg);
	}

	public static void i(String tag, String msg) {
		LogUtils.i(tag, msg);
	}

	public static void w(String tag, String msg) {
		LogUtils.i(tag, msg);
	}

	public static void e(String tag, String msg) {
		LogUtils.e(tag, msg);
	}

	/*********************** Log with object list ***************************/
	public static void v(String tag, Object... msg) {
		LogUtils.v(tag, getLogMessage(msg));
	}

	public static void d(String tag, Object... msg) {
		LogUtils.d(tag, getLogMessage(msg));
	}

	public static void i(String tag, Object... msg) {
		LogUtils.i(tag, getLogMessage(msg));
	}

	public static void w(String tag, Object... msg) {
		LogUtils.w(tag, getLogMessage(msg));
	}

	public static void e(String tag, Object... msg) {
		LogUtils.e(tag, getLogMessage(msg));
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
}
