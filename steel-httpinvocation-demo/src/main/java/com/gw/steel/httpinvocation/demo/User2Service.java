package com.gw.steel.httpinvocation.demo;

import com.gw.steel.httpinvocation.api.annotation.HttpService;

@HttpService(serviceName="user2Service")
public interface User2Service {
	User getUser(String username);
}
