package com.gw.steel.httpinvocation.demo.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gw.steel.httpinvocation.api.annotation.HttpService;
import com.gw.steel.httpinvocation.demo.User;
import com.gw.steel.httpinvocation.demo.UserService;

@HttpService(serviceInterfaceClass=UserService.class)
public class UserServiceImpl implements UserService {     
    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);     
    
    
    public User getUser(String username){     
     if (logger.isDebugEnabled()) {     
         logger.debug("username:[" + username + "]");     
     }     
     User user = new User(username, new Date());     
     if (logger.isDebugEnabled()) {     
         logger.debug("user:[" + user + "]");     
     }     
     return user;     
 }     
}
