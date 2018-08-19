package net.fastm;

import java.io.PrintWriter;

public class GlobalVariablePart extends VariablePart {
	public GlobalVariablePart(String name) {
		super(name);
	}

    @Override
    public void write(Object obj, PrintWriter writer, IValueInterceptor interceptor) {
        super.write(getGlobalObj(), writer, interceptor);
    }
}
