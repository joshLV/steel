package com.gw.steel.httpinvocation.demo;

import com.gw.steel.httpinvocation.api.annotation.HttpService;

@HttpService(serviceName="userService")
public interface  UserService {
	User getUser(String username);
}
