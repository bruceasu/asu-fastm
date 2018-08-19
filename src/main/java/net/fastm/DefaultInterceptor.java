package net.fastm;

import net.util.BeanUtils;

public class DefaultInterceptor implements IValueInterceptor {
	protected static final DefaultInterceptor instance = new DefaultInterceptor();

	synchronized
	public static final DefaultInterceptor getInstance() {
		return instance;
	}

	@Override
	public Object getProperty(Object bean, String propertyName) {
		return BeanUtils.getProperty(bean, propertyName);
	}
}
