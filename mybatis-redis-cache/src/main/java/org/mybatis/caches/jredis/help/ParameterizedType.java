package org.mybatis.caches.jredis.help;

import java.lang.reflect.Type;

public class ParameterizedType {

	private  Class[] actualTypeArguments ;
    private  Class   ownerType ;
    private  Class   rawType ;

    public ParameterizedType(){
    	
    }
    public ParameterizedType(Class[] actualTypeArguments, Class ownerType, Class rawType){
        this.actualTypeArguments = actualTypeArguments;
        this.ownerType = ownerType;
        this.rawType = rawType;
    }

    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    public Type getOwnerType() {
        return ownerType;
    }

    public Type getRawType() {
        return rawType;
    }
	/**
	 * @param actualTypeArguments the actualTypeArguments to set
	 */
	public void setActualTypeArguments(Class[] actualTypeArguments) {
		this.actualTypeArguments = actualTypeArguments;
	}
	/**
	 * @param ownerType the ownerType to set
	 */
	public void setOwnerType(Class ownerType) {
		this.ownerType = ownerType;
	}
	/**
	 * @param rawType the rawType to set
	 */
	public void setRawType(Class rawType) {
		this.rawType = rawType;
	}
    
    

}
