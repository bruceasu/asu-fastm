package net.fastm.interceptors;

import net.fastm.DefaultInterceptor;
import net.fastm.IValueInterceptor;

public abstract class DelegatedInterceptor implements IValueInterceptor {

    protected IValueInterceptor delegator = DefaultInterceptor.getInstance();

    public void setDelegator(IValueInterceptor delegator) {
        this.delegator = delegator;
    }

    @Override
    public Object getProperty(Object bean, String propertyName) {
        if (delegator == null) {
            return null;
        }
        Object value = delegator.getProperty(bean, propertyName);
        return getValue(bean, propertyName, value);
    }

    protected abstract Object getValue(Object bean, String propertyName, Object value);
}
