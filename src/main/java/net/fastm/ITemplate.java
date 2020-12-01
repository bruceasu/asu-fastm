package net.fastm;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.util.BeanUtils;

public interface ITemplate {

    String START = "${";
    String END   = "}";

    default String toString(Object obj) {
        return toString(obj, DefaultInterceptor.getInstance());
    }

    default String toString(final Object obj, IValueInterceptor interceptor)
    {
        if (interceptor == null) {
            interceptor = DefaultInterceptor.getInstance();
        }
        // 提取global_属性？
        setGlobalObjByTop(obj);

        StringWriter strWriter = new StringWriter();
        PrintWriter printer = new PrintWriter(strWriter);

        write(obj, printer, interceptor);
        return strWriter.toString();
    }

    default void write(Object obj, PrintWriter writer)
    {
        write(obj, writer, DefaultInterceptor.getInstance());
    }

    void write(Object obj, PrintWriter writer, IValueInterceptor interceptor);

    String structure(int paramInt);

    Object getGlobalObj();

    void setGlobalObj(final Object obj);

    @SuppressWarnings("unchecked")
    default void setGlobalObjByTop(final Object obj) {
        Map<String, Object> globalMap = new HashMap<>();
        Map map = new HashMap();
        if (obj instanceof Map) {
            map.putAll((Map) obj);
        } else {
            try {
                map = BeanUtils.convertBean(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Iterator<Entry> iter = map.entrySet().iterator();
        for (int i = 0; i < map.size(); i++) {
            Map.Entry entry = iter.next();
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            if (!key.startsWith("global_")) {
                continue;
            }
            globalMap.put(key, value);
        }

        setGlobalObj(globalMap);
    }
}
