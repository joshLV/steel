package com.gw.steel.spring.rediscache;

public class BooleanPropertySetter<B> extends AbstractPropertySetter<Boolean, B> {

	public BooleanPropertySetter(String propertyKey, String propertyName,
			Boolean defaultValue) {
		super(propertyKey, propertyName, defaultValue);		
	}

	@Override
	protected Boolean convert(String value) throws Throwable {		
		 return Boolean.valueOf(value);
	}

}
