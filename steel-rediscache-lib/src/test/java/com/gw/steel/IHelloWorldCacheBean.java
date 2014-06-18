package com.gw.steel;

public interface IHelloWorldCacheBean {

	public String getHelloWorld(String key);
	
	public void putHelloWorld(String key);
	
	public void removeHelloWorld(String key);
	
	public User  getUser(User u);
	
	public void putUser(User u);
	
	public void removeUser(User u);

}