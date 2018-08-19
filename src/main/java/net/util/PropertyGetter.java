package net.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PropertyGetter {
	Method method = null;
	Field field = null;

	public Object get(Object bean) {
		try {
			if (method != null) {
				Object[] values = null;
				return method.invoke(bean, values);
			}
			if (field != null)
				return field.get(bean);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
}
