package org.mybatis.caches.jredis.help;

import java.util.Collection;
import java.util.Map;


public final class ParameterizedTypParseHelper {
	public static ParameterizedType parse(Object obj) {
		ParameterizedType paraType = null;

		if (obj instanceof Collection) {
			Collection<?> c = (Collection<?>) obj;
			Class<?> itemClass = null;
			if (c.size() > 0) {
				itemClass = c.iterator().next().getClass();
				Class[] actualTypeArguments = new Class[] { itemClass };
				paraType = new ParameterizedType(actualTypeArguments, null,
						obj.getClass());
			}
		} else if (obj instanceof Map) {
			Map<?, ?> m = (Map<?, ?>) obj;
			Class<?> keyClass = null;
			Class<?> itemClass = null;
			if (!m.isEmpty()) {
				Object firstKey = m.keySet().iterator().next();
				keyClass = firstKey.getClass();
				itemClass = m.get(firstKey).getClass();
				Class[] actualTypeArguments = new Class[] { keyClass, itemClass };
				paraType = new ParameterizedType(actualTypeArguments, null,
						obj.getClass());
			}
		}
		return paraType;
	}
}