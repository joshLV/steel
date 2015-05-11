package com.mybatis.caches.serial;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;


public class TypeRef<T> {
	private final Type type;
	protected TypeRef() {
		 Type superClass = getClass().getGenericSuperclass();
		 type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
	}
	
	public static void main(String[] args) {
		System.out.println(new StringTypeRef());
		List<String> u = new ArrayList<String>();
//		try {
//			System.out.println(Class.forName("java.util.ArrayList<String>"));
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//Type superClass = u.getClass().getGenericSuperclass();
		
		//System.out.println(((ParameterizedType) (u.getClass())).getActualTypeArguments()[0]);
		TypeVariable[] typeVar= u.getClass().getTypeParameters();
		for(TypeVariable t : typeVar){
			System.out.println(t.getName());
			System.out.println(ToStringBuilder.reflectionToString(t.getBounds()));
			System.out.println(t.getGenericDeclaration());
		}
	}
	
	static class StringTypeRef extends TypeRef<String>{
		
	}
}
