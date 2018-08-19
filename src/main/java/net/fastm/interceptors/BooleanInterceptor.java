package net.fastm.interceptors;

public class BooleanInterceptor extends DelegatedInterceptor {

    @Override
    protected Object getValue(Object bean, String propertyName, Object value) {
        if (value != null && value instanceof Boolean) {
            boolean b = ((Boolean) value).booleanValue();
            if (!b) {
                return "";
            }
        }
        return value;
    }
}
