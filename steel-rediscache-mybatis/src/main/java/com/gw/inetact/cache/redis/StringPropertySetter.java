package com.gw.inetact.cache.redis;

public class StringPropertySetter<B> extends AbstractPropertySetter<String, B> {

	public StringPropertySetter(String propertyKey, String propertyName,
			String defaultValue) {
		super(propertyKey, propertyName, defaultValue);		
	}

	@Override
	protected String convert(String value) throws Throwable {		
		return value;
	}
	
}
