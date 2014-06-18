package com.gw.steel.helper;


public class RedisKeyUtil {
	private static final String KEY_SPLITTER = ":";

	public static String getKey(String appKey, String secKey,
			String key) {
		StringBuilder sb = new StringBuilder();
		sb.append(appKey != null ? appKey : "");
		sb.append(KEY_SPLITTER);
		sb.append(secKey != null ? secKey : "");
		sb.append(KEY_SPLITTER);
		sb.append(key);
		return sb.toString();
	}

	
}
