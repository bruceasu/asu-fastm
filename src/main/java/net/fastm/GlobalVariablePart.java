package net.fastm;

import java.io.PrintWriter;

public class GlobalVariablePart extends VariablePart {
	public GlobalVariablePart(String name) {
		super(name);
	}

    @Override
    public void write(Object obj, PrintWriter writer, IValueInterceptor interceptor) {
        Object globalObj = getGlobalObj();
        if (globalObj == null) {
            setGlobalObjByTop(obj);
        }
        super.write(getGlobalObj(), writer, interceptor);
    }

    @Override
    public String structure(final int level) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < level; i++) {
            buf.append(" ");
        }
        buf.append("GlobalVariable: " + name + "\n");

        return buf.toString();
    }
}
