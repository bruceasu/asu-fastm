package net.fastm;

import java.io.PrintWriter;

public class VariablePart implements ITemplate {

    String name     = null;
    String propName = null;
    Object globalObj;

    public VariablePart(final String name) {
        setName(name);
    }

    public String getPropName() {
        return propName;
    }

    public void setName(final String string) {
        propName = string.substring(2, string.length() - 1).trim();
        name = "${" + propName + "}";
    }

    public void setPropName(final String string) {
        propName = string.trim();
        name = "${" + propName + "}";
    }

    public String structure(final int level) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < level; i++) {
            buf.append(" ");
        }
        buf.append("Variable: " + name + "\n");

        return buf.toString();
    }

    @Override
    public String toString() {
        return name;
    }

    public String toString(final Object obj) {
        return toString(obj, DefaultInterceptor.getInstance());
    }

    public Object getGlobalObj() {
        return globalObj;
    }

    public void setGlobalObj(final Object obj) {
        globalObj = obj;
    }


    public String toString(Object obj, IValueInterceptor valueInterceptor) {
        if (valueInterceptor == null) {
            valueInterceptor = DefaultInterceptor.getInstance();
        }

        if (obj == null) {
            return name;
        }
        if (obj instanceof Object[]) {
            Object[] a = (Object[]) obj;
            if (a.length > 0) {
                obj = a[0];
            }
        }

        Object value = valueInterceptor.getProperty(obj, propName);

        if (value == null)
        // return name;
        {
            return "";
        } else {
            return value.toString();
        }
    }

    public void write(final Object obj, final PrintWriter writer) {
        write(obj, writer, DefaultInterceptor.instance);
    }

    public void write(final Object obj,
                      final PrintWriter writer,
                      final IValueInterceptor valueInterceptor) {
        writer.write(toString(obj, valueInterceptor));
    }
}
