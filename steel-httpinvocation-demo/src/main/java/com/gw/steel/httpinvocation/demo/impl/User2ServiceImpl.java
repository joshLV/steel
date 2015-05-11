package com.gw.steel.httpinvocation.demo.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gw.steel.httpinvocation.api.annotation.HttpService;
import com.gw.steel.httpinvocation.demo.User;
import com.gw.steel.httpinvocation.demo.User2Service;

@HttpService(serviceInterfaceClass = User2Service.class)
public class User2ServiceImpl implements User2Service {
    private Logger logger = LoggerFactory.getLogger(User2ServiceImpl.class);

    public User getUser(String username) {
        if (logger.isDebugEnabled()) {
            logger.debug("username:[" + username + "]");
        }
        User user = new User(username + "2", new Date());
        if (logger.isDebugEnabled()) {
            logger.debug("user:[" + user + "]");
        }
        return user;
    }
}
