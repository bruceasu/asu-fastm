package net.fastm;


import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DynamicPart implements ITemplate {

    String name = null;

    List<ITemplate> steps = new ArrayList<ITemplate>();

    protected Object globalObj;

    public DynamicPart(final String name) {
        setName(name);
        globalObj = null;
    }

    public void addStep(final ITemplate step) {
        if (steps == null) {
            steps = new ArrayList<ITemplate>();
        }

        steps.add(step);
    }

    public String getName() {
        return name;
    }
    public void setName(final String string) {
        name = string.trim();
    }

    public List<ITemplate> getSteps() {
        return steps;
    }

    @Override
    public void setGlobalObj(final Object obj) {
        globalObj = obj;
    }

    @Override
    public Object getGlobalObj() {
        return globalObj;
    }


    public void setSteps(final List<ITemplate> steps) {
        this.steps = steps;
    }

    public void stepDown(final Object obj,
                         final PrintWriter writer,
                         IValueInterceptor interceptor) {
        if (interceptor == null) {
            interceptor = DefaultInterceptor.getInstance();
        }

        if (steps == null || steps.isEmpty()) {
            return;
        }

        for (ITemplate step : steps) {
            step.setGlobalObj(getGlobalObj());
            if (step instanceof ConditionDynamicPart) {
                step.write(obj, writer, interceptor);
            } else if (step instanceof DynamicPart) {
                DynamicPart dynPart = (DynamicPart) step;
                String dynName = dynPart.getName();
                Object branch = interceptor.getProperty(obj, dynName);
                dynPart.write(branch, writer, interceptor);
            } else {
                step.write(obj, writer, interceptor);
            }
        }
    }

    @Override
    public String structure(int level) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < level; i++) {
            buf.append(" ");
        }
        buf.append("DYNAMIC: ").append(name).append("\n");
        level++;

        for (ITemplate step : steps) {
            buf.append(step.structure(level));
        }

        return buf.toString();
    }



    @Override
    public String toString() {
        if (steps == null || steps.isEmpty()) {
            return "";
        }

        StringBuilder buf = new StringBuilder();

        for (ITemplate step : steps) {
            if (step instanceof DynamicPart) {
                DynamicPart dynPart = (DynamicPart) step;
                String dynName = dynPart.getName();
                //buf.append("<!-- @for " + dynName + " -->\n");
                buf.append("<!-- for ").append(dynName).append(" -->\n");
                buf.append(dynPart);
                //buf.append("<!-- @done " + dynName + " -->\n");
                buf.append("<!-- done ").append(dynName).append(" -->\n");
            } else {
                buf.append(step);
            }
        }

        return buf.toString();
    }

    @Override
    public void write(final Object obj, final PrintWriter writer,
                      final IValueInterceptor interceptor) {
        if (obj == null) {
            return;
        }

        boolean isIterator = false;
        if (obj.getClass().isArray()) {
            int n = Array.getLength(obj);
            for (int i = 0; i < n; i++) {
                Object o = Array.get(obj, i);
                stepDown(o, writer, interceptor);
            }
        } else if (obj instanceof Collection || (isIterator = obj instanceof Iterator)) {
            Iterator iterator = isIterator ? (Iterator) obj : ((Collection) obj).iterator();
            while (iterator.hasNext()) {
                Object o = iterator.next();
                stepDown(o, writer, interceptor);
            }
        } else if (obj instanceof ResultSet) {
            ResultSet rs = (ResultSet) obj;
            try {
                while (rs.next()) {
                    stepDown(rs, writer, interceptor);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (obj instanceof ITemplate) {
            writer.write(obj.toString());
        } else {
            stepDown(obj, writer, interceptor);
        }
    }


}
