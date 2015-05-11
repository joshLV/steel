package com.mybatis.caches.serial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.caches.jredis.help.ParameterizedTypParseHelper;
import org.mybatis.caches.jredis.help.ParameterizedType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;

public class FastJsonSerialTest {
	public static void main(String[] args) {
		List<User> users = new ArrayList<User>();
		User user1 =  new User();
		user1.setAge("14");
		user1.setName("John");
		User user2 = new User ();
		user2.setAge("34");
		user2.setName("Mike");
		
		users.add(user1);
		users.add(user2);
		System.out.println("users info "+users);
		String usersStr = JSON.toJSONString(users);
		System.out.println("Users in json format : "+usersStr);
		//System.out.println("Class int "+users.getClass().get);
		
		ParameterizedType pt = ParameterizedTypParseHelper.parse(users);
		List<User> aUsers = JSON.parseObject(usersStr, new ParameterizedTypeImpl(pt.getActualTypeArguments(), pt.getOwnerType(), pt.getRawType()));
		
		Map<User, User> map = new HashMap<User, User>();
		//map.put(user1,user2);
		String mapStr = JSON.toJSONString(map);
		System.out.println("Map in json format : "+mapStr);
		 pt = ParameterizedTypParseHelper.parse(map);
		Map<User, User> map2 = JSON.parseObject(mapStr, new ParameterizedTypeImpl(pt.getActualTypeArguments(), pt.getOwnerType(), pt.getRawType())) ;
		
		System.out.println("Users in json format : "+aUsers);
		System.out.println("Users in json format : "+map2);
		
	}
}
