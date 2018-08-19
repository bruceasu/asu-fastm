package net.fastm.interceptors;


public class ContextInterceptor extends DelegatedInterceptor {
    public void setContext(Object context) {
        this.context = context;
    }

    private Object context;

	@Override
	protected Object getValue(Object bean, String propertyName, Object value) {
		if (value == null) {
			value = delegator.getProperty(context, propertyName);
		}
		return value;
	}

}
