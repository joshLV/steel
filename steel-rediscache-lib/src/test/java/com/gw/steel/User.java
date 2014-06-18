package com.gw.steel;

import java.io.Serializable;

public class User implements Serializable {
	private String id;
	
	private String name;
	
	private int age;
	
	public User(){
		
	}
	
	public User(String id){
		this.id=id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the age
	 */
	public int getAge() {
		return age;
	}

	/**
	 * @param age the age to set
	 */
	public void setAge(int age) {
		this.age = age;
	}
	
	
}
