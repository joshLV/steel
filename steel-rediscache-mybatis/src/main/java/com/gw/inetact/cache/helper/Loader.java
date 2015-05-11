package com.gw.inetact.cache.helper;

import java.io.InputStream;
import java.net.URL;

public  class Loader {
	public static URL getResource(String resource){
		ClassLoader classLoader = Loader.class.getClassLoader();
		URL url =classLoader.getResource(resource);
		return url;
	}
	
	public static InputStream getResourceAsStream(String resource){
		ClassLoader classLoader = Loader.class.getClassLoader();
		InputStream is =classLoader.getResourceAsStream(resource);
		return is;
	}
}
