package com.gw.steel.httpinvocation.demo;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gw.steel.httpinvocation.api.annotation.HttpWired;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/testApplicationContext.xml" })
public class TestHttpInvocation {
    private UserService  userService;

    private User2Service user2Service;

    @Test
    public void testHttpInvocation() {
        User u = userService.getUser("John");
        Assert.assertEquals("John", u.getUsername());

        u = user2Service.getUser("John");
        Assert.assertEquals("John2", u.getUsername());
    }

    
    @HttpWired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @HttpWired
    public void setUser2Service(User2Service user2Service) {
        this.user2Service = user2Service;
    }

}
