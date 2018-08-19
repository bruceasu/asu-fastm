package net.fastm;

import java.io.PrintWriter;

public class StaticPart implements ITemplate {

    String str = null;

    public StaticPart(String str) {
        this.str = str;
    }

    @Override
    public String structure(int level) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < level; i++) {
            buf.append(" ");
        }
        buf.append("Static \n");

        return buf.toString();
    }

    protected Object globalObj;

    @Override
    public Object getGlobalObj() {
        return globalObj;
    }

    @Override
    public void setGlobalObj(Object obj) {
        globalObj = obj;
    }

    @Override
    public String toString() {
        return str;
    }

    @Override
    public String toString(Object obj) {
        return str;
    }

    @Override
    public String toString(Object obj, IValueInterceptor valueInterceptor) {
        return str;
    }

    @Override
    public void write(Object obj, PrintWriter writer) {
        if (obj == null) {
            return;
        }
        writer.write(str);
    }

    @Override
    public void write(Object obj, PrintWriter writer, IValueInterceptor valueInterceptor) {
        write(obj, writer);
    }
}
