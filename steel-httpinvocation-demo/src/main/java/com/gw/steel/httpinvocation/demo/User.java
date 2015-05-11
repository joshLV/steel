package com.gw.steel.httpinvocation.demo;

import java.io.Serializable;
import java.util.Date;

public class User  implements Serializable{     
    
    private static final long serialVersionUID = 5590768569302443813L;     
    private String username;     
    private Date birthday;     
    
  
    public User(String username, Date birthday) {     
        this.username = username;     
        this.birthday = birthday;     
    }     
  
    @Override    
    public String toString() {     
        return String.format("%s\t%s\t", username, birthday);     
    }     

    public String getUsername() {
		return username;
	}
}
