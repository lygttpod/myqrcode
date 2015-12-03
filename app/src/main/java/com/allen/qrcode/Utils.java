package com.allen.qrcode;

public class Utils {

	public static final boolean isURL(String msg) {

		if (msg.startsWith("HTTP://") || msg.startsWith("HTTPS://")
				|| msg.startsWith("http://") || msg.startsWith("https://")) {
			return true;
		} else {
			return false;
		}

	}

}
