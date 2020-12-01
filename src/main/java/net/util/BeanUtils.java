package net.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class BeanUtils {

    static final Map<String, BeanGetter> beanMap = new CopyOnWriteMap<String, BeanGetter>();

    /**
     * 将一个 JavaBean 对象转化为一个 Map
     *
     * @param bean 要转化的JavaBean 对象
     * @return 转化出来的 Map 对象
     * @throws IntrospectionException 如果分析类属性失败
     * @throws IllegalAccessException 如果实例化 JavaBean 失败
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map convertBean(final Object bean) throws IntrospectionException,
                                                            IllegalAccessException,
                                                            InvocationTargetException {
        Class type = bean.getClass();
        Map returnMap = new HashMap();
        BeanInfo beanInfo = Introspector.getBeanInfo(type);

        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();
            if (!propertyName.equals("class")) {
                Method readMethod = descriptor.getReadMethod();
                Object result = readMethod.invoke(bean, new Object[0]);
                if (result != null) {
                    // TODO：如果result 不是基本对象，应该递归。
                    returnMap.put(propertyName, result);
                } else {
                    returnMap.put(propertyName, null);
                }
            }
        }
        return returnMap;
    }

    /**
     * 将一个 Map 对象转化为一个 JavaBean
     *
     * @param type 要转化的类型
     * @param map 包含属性值的 map
     * @return 转化出来的 JavaBean 对象
     * @throws IntrospectionException 如果分析类属性失败
     * @throws IllegalAccessException 如果实例化 JavaBean 失败
     * @throws InstantiationException 如果实例化 JavaBean 失败
     * @throws InvocationTargetException 如果调用属性的 setter 方法失败
     */
    @SuppressWarnings("rawtypes")
    public static Object convertMap(final Class type, final Map map) throws IntrospectionException,
                                                                            IllegalAccessException,
                                                                            InstantiationException,
                                                                            InvocationTargetException {
        // 获取类属性
        BeanInfo beanInfo = Introspector.getBeanInfo(type);
        // 创建 JavaBean 对象
        Object obj = type.newInstance();

        // 给 JavaBean 对象的属性赋值
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();

            if (map.containsKey(propertyName)) {
                // TODO: 下面一句可以 try 起来，这样当一个属性赋值失败的时候就不会影响其他属性赋值。
                Object value = map.get(propertyName);

                Object[] args = new Object[1];
                args[0] = value;
                // TODO:　应检查value类型
                descriptor.getWriteMethod().invoke(obj, args);
            }
        }
        return obj;
    }

    public static Object getProperty(Object bean, String propertyName) {
        if (bean == null || propertyName == null) {
            return null;
        }

        boolean isMap = bean instanceof Map;
        if (isMap) {
            Object value = ((Map) bean).get(propertyName);
            if (value != null) {
                return value;
            }

        }

        int dot = propertyName.indexOf('.');
        if (dot >= 0) {
            String beanName = propertyName.substring(0, dot);
            if (beanName.length() > 0) {
                bean = getProperty(bean, beanName);
                if (bean == null) {
                    return null;
                }
            }
            propertyName = propertyName.substring(dot + 1, propertyName.length());
            return getProperty(bean, propertyName);
        }
        if (bean instanceof ResultSet) {
            ResultSet rs = (ResultSet) bean;
            try {
                return rs.getObject(propertyName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        int leftBracket = propertyName.lastIndexOf("[");
        if (leftBracket >= 0) {
            int rightBracket = propertyName.indexOf("]", leftBracket);
            String preName = propertyName.substring(0, leftBracket);

            Object value = getProperty(bean, preName);
            if (rightBracket > 0) {
                String str = propertyName.substring(leftBracket + 1, rightBracket);
                int index = Integer.parseInt(str);

                if (value.getClass().isArray()) {
                    value = Array.get(value, index);
                } else if (value instanceof ResultSet) {
                    ResultSet rs = (ResultSet) value;
                    try {
                        value = rs.getObject(index);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return value;
        }

        if (isMap) {
            return null;
        }

        Class<?> clazz = bean.getClass();
        String className = clazz.getName();
        BeanGetter beanGetter = beanMap.get(className);
        if (beanGetter == null) {
            beanGetter = new BeanGetter();
            beanGetter.setBeanClass(clazz);
            beanMap.put(className, beanGetter);
        }

        Object propertyValue = beanGetter.get(bean, propertyName);
        if (propertyValue == null && "current".equalsIgnoreCase(propertyName)) {
            return bean;
        }
        return propertyValue;
    }
}
