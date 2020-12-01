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
        propName = string.substring(START.length(), string.length() - END.length()).trim();
        name = string;
    }

    public void setPropName(final String string) {
        propName = string.trim();
        name = START + propName + END;
    }

    @Override
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

    @Override
    public Object getGlobalObj() {
        return globalObj;
    }

    @Override
    public void setGlobalObj(final Object obj) {
        globalObj = obj;
    }

    @Override
    public void write(Object obj,
                      PrintWriter writer,
                      IValueInterceptor interceptor) {
        if (interceptor == null) {
            interceptor = DefaultInterceptor.getInstance();
        }

        if (obj == null) {
            writer.write(name);
        }
        if (obj instanceof Object[]) {
            Object[] a = (Object[]) obj;
            if (a.length > 0) {
                obj = a[0];
            }
        }

        Object value = interceptor.getProperty(obj, propName);

        if (value == null)
        {
            writer.write("");
        } else {
            writer.write(value.toString());
        }
    }
}
