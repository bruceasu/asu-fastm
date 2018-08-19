package net.fastm.interceptors;

public class NullInterceptor extends DelegatedInterceptor {

    @Override
    protected Object getValue(Object bean, String propertyName, Object value) {
        if (value == null) {
            if (propertyName.startsWith("if_")) {
                return null;
            }
            return "";
        }
        return value;
    }
}
