package net.fastm;

import java.io.PrintWriter;

public abstract interface ITemplate {

    public static final String START = "${";
    public static final String END   = "}";

    public abstract String toString(Object paramObject);

    public abstract String toString(Object paramObject, IValueInterceptor paramIValueInterceptor);

    public abstract void write(Object paramObject, PrintWriter paramPrintWriter);

    public abstract void write(Object paramObject, PrintWriter paramPrintWriter,
                               IValueInterceptor paramIValueInterceptor);

    public abstract String structure(int paramInt);

    public Object getGlobalObj();

    public void setGlobalObj(final Object obj);

}
