package com.gw.inetact.cache.redis;

import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;

/**
 * 
 * Converts a keyed property string in the Config to a proper Java
 * object representation.
 * 
 * @author Dongpo.wu
 *
 * @param <T>
 */
public abstract class AbstractPropertySetter<T, B> {

	/**
     * The Config property key.
     */
    private final  String propertyKey;

    /**
     * The <B> property name.
     */
    private final String propertyName;
    /**
     * The default value used if something goes wrong during the conversion or
     * the property is not set in the config.
     */
    private final T defaultValue;
    
    /**
     * Build a new property setter.
     *
     * @param propertyKey the Config property key.
     * @param propertyName the <B> property name.
     * @param defaultValue the property default value.
     */
    public AbstractPropertySetter(final String propertyKey, final String propertyName, final T defaultValue) {
        this.propertyKey = propertyKey;
        this.propertyName = propertyName;
        this.defaultValue = defaultValue;
    }

    /**
     * Extract a property from the, converts and puts it to the
     * {@link MemcachedConfiguration}.
     *
     * @param config the Config
     * @param bean the ref to bean
     */
    public void set(Properties config, B bean) {
        String propertyValue = config.getProperty(propertyKey);
        T value;

        try {
            value = this.convert(propertyValue);
            if (value == null) {
                value = defaultValue;
            }
        } catch (Throwable e) {
            value = defaultValue;
        }

        try {
            BeanUtils.setProperty(bean, propertyName, value);
        } catch (Exception e) {
            throw new RuntimeException("Impossible to set property '"
                    + propertyName
                    + "' with value '"
                    + value
                    + "', extracted from ('"
                    + propertyKey
                    + "'="
                    + propertyValue
                    + ")", e);
        }
    }

    /**
     * Convert a string representation to a proper Java Object.
     *
     * @param value the value has to be converted.
     * @return the converted value.
     * @throws Throwable if any error occurs.
     */
    protected abstract T convert(String value) throws Throwable;

}
