package net.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class BeanGetter {
	final Map<String, PropertyGetter> propertyMap = new CopyOnWriteMap<String, PropertyGetter>();
	Class<?> beanClass = null;

	public Object get(Object bean, String propertyName) {
		if (bean == null || propertyName == null)
			return null;
		PropertyGetter propGetter = propertyMap.get(propertyName);

		if (propGetter == null) {
			boolean hit = false;
			Object propertyValue = null;

			String propName = Character.toUpperCase(propertyName.charAt(0))
					+ propertyName.substring(1);
			String methodName = "get" + propName;

			Method method = null;
			try {
				Class<?>[] args = null;
				method = beanClass.getMethod(methodName, args);
				Object[] params = null;
				propertyValue = method.invoke(bean, params);
				hit = true;
			} catch (Exception e) {
				method = null;
			}

			if (!hit) {
				try {
					methodName = "is" + propName;
					Class<?>[] args = null;
					method = beanClass.getMethod(methodName, args);
					Object[] values = null;
					propertyValue = method.invoke(bean, values);
					hit = true;
				} catch (Exception e) {
					method = null;
				}
			}

			Field field = null;
			if (!hit) {
				try {
					field = beanClass.getField(propertyName);
					propertyValue = field.get(bean);
					hit = true;
				} catch (Exception e) {
					field = null;
				}
			}

			propGetter = new PropertyGetter();
			propGetter.setMethod(method);
			propGetter.setField(field);

			propertyMap.put(propertyName, propGetter);

			return propertyValue;
		}
		return propGetter.get(bean);
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	public void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}
}
