package net.fastm;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fastm.interceptors.NullInterceptor;
import net.util.BeanUtils;
import net.util.express.ExpressUtils;

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

    /**
     * @param obj Object
     * @return DynamicPart
     */
    public DynamicPart findDepthFirst(final Object obj) {
        if (obj == null) {
            return null;
        }

        List<DynamicPart> dynChildren = findDynamicParts();

        for (DynamicPart dynPart : dynChildren) {
            if (obj.equals(dynPart.getName())) {
                return dynPart;
            }

            dynPart = dynPart.findDepthFirst(obj);

            if (dynPart != null) {
                return dynPart;
            }
        }

        return null;
    }

    /**
     * @param obj Object
     * @return DynamicPart
     */
    public DynamicPart findWidthFirst(final Object obj) {
        if (obj == null) {
            return null;
        }
        List<DynamicPart> dynChildren = findDynamicParts();

        List<DynamicPart> queue = new LinkedList<DynamicPart>();
        queue.addAll(dynChildren);

        while (queue.size() > 0) {
            DynamicPart dynPart = queue.remove(0);

            if (obj.equals(dynPart.getName())) {
                return dynPart;
            }

            queue.addAll(dynPart.findDynamicParts());
        }

        return null;
    }

    /**
     * find out the DynamicPart step as List
     *
     * @return List&lt;DynamicPart&gt;
     */
    public List<DynamicPart> findDynamicParts() {
        List<DynamicPart> dynList = new ArrayList<DynamicPart>();
        for (ITemplate step : steps) {
            if (step instanceof DynamicPart) {
                dynList.add((DynamicPart) step);
            }
        }
        return dynList;
    }

    public Map<String, StaticPart> getIncludes(final Object model, final String[] includeNames,
                                               final String prefix) {
        Map<String, StaticPart> includes = new HashMap<String, StaticPart>();
        for (String includeName : includeNames) {
            DynamicPart part = findWidthFirst(prefix + includeName);
            String str = part.toString(model, new NullInterceptor());
            StaticPart text = new StaticPart(str);
            includes.put(includeName, text);
        }
        return includes;
    }

    public String getName() {
        return name;
    }

    public List<ITemplate> getSteps() {
        return steps;
    }

    /**
     * find the variables step as list
     *
     * @return List<VariablePart>
     */
    public List<VariablePart> findVariables() {
        List<VariablePart> variables = new ArrayList<VariablePart>();
        for (ITemplate step : steps) {
            if (step instanceof VariablePart) {
                variables.add((VariablePart) step);
            }
        }
        return variables;
    }

    public ITemplate replaceNode(final String name, final ITemplate newTemplate) {
        if (steps == null || steps.isEmpty()) {
            return null;
        }
        if (this.name.compareTo(name) == 0) {
            return newTemplate;
        }
        if (findDepthFirst(name) == null) {
            return this;
        }

        DynamicPart newDyn = new DynamicPart(this.name);
        for (ITemplate step : steps) {
            if (step instanceof DynamicPart) {
                DynamicPart dyn = (DynamicPart) step;
                step = dyn.replaceNode(name, newTemplate);
            }
            newDyn.addStep(step);
        }

        return newDyn;
    }

    @Override
    public void setGlobalObj(final Object obj) {
        globalObj = obj;
    }


    @SuppressWarnings("unchecked")
    public void setGlobalObjByTop(final Object obj) {
        Map<String, Object> globalMap = new HashMap<String, Object>();
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

        Iterator<Map.Entry> iter = map.entrySet().iterator();
        for (int i = 0; i < map.size(); i++) {
            Map.Entry entry = iter.next();
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            if (!key.startsWith("global_")) {
                continue;
            }
            globalMap.put(key, value);
        }

        globalObj = globalMap;
    }

    public void setName(final String string) {
        name = string.trim();
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
                ConditionDynamicPart dynPart = (ConditionDynamicPart) step;
                String dynName = dynPart.getName();
                boolean conditionResult = getComplexConditionResult(dynName, obj, interceptor);
                if (conditionResult) {
                    dynPart.setGlobalObj(globalObj);
                    dynPart.write(obj, writer, interceptor);
                }
//                step.write(obj, writer, interceptor);
            } else if (step instanceof DynamicPart) {
                DynamicPart dynPart = (DynamicPart) step;
                String dynName = dynPart.getName();
                Object branch = interceptor.getProperty(obj, dynName);
                dynPart.setGlobalObj(globalObj);
                dynPart.write(branch, writer, interceptor);
            } else if (step instanceof GlobalVariablePart) {
                step.write(globalObj, writer, interceptor);
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
    public Object getGlobalObj() {
        return globalObj;
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
                buf.append("<!-- @for ").append(dynName).append(" -->\n");
                buf.append(dynPart);
                //buf.append("<!-- @done " + dynName + " -->\n");
                buf.append("<!-- @done ").append(dynName).append(" -->\n");
            } else {
                buf.append(step);
            }
        }

        return buf.toString();
    }

    @Override
    public String toString(final Object obj) {
        return toString(obj, DefaultInterceptor.getInstance());
    }
    @Override
    public String toString(final Object obj, final IValueInterceptor interceptor) {
        setGlobalObjByTop(obj);

        StringWriter strWriter = new StringWriter();
        PrintWriter printer = new PrintWriter(strWriter);

        write(obj, printer, interceptor);
        return strWriter.toString();
    }
    @Override
    public void write(final Object obj, final PrintWriter writer) {
        write(obj, writer, DefaultInterceptor.getInstance());
    }

    @SuppressWarnings("unchecked")
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

    private boolean getComplexConditionResult(String express,
                                              Object obj,
                                              IValueInterceptor interceptor) {
        String targetStr = express.trim().trim();
        targetStr = targetStr.replaceAll("&&", "&");
        targetStr = targetStr.replaceAll("\\|\\|", "\\|");
        ArrayList expression = new ArrayList();
        StringTokenizer st = new StringTokenizer(targetStr, "()|&", true);
        while (st.hasMoreElements()) {
            expression.add(st.nextToken());
        }
        StringBuffer complexExpress = new StringBuffer();
        for (Iterator iter = expression.iterator(); iter.hasNext(); ) {
            String item = (String) iter.next();
            item = item == null ? "" : item.trim();
            if ("".equals(item)) {
                continue;
            }
            if (("(".equals(item)) || (")".equals(item)) || ("|".equals(item)) || ("&"
                    .equals(item))) {
                complexExpress.append(item);
            } else {
                boolean result = getConditionResult(item, obj, interceptor);
                complexExpress.append(String.valueOf(result));
            }
        }
        return ExpressUtils.judge(complexExpress.toString());
    }

    private boolean getConditionResult(String dynName, Object obj, IValueInterceptor interceptor) {
        try {
            String targetStr = dynName.trim();
            String regEx = "[\"}0123456789]\\s*(==|!=|>=|\\*|!\\*|<=|>|<)\\s*[\"{0123456789]";
            Pattern pattern = Pattern.compile(regEx);
            Matcher matcher = pattern.matcher(targetStr);
            matcher.find();
            int n1 = matcher.start();
            int n2 = matcher.end();
            String compare1 = targetStr.substring(0, n1 + 1).trim();
            String logicStr = targetStr.substring(n1 + 1, n2 - 1).trim();
            String compare2 = targetStr.substring(n2 - 1).trim();
            compare1 = getCompareValue(compare1, obj, interceptor);
            compare2 = getCompareValue(compare2, obj, interceptor);
            if (logicStr.equals("==")) {
                return compare1.equals(compare2);
            }
            if (logicStr.equals("!=")) {
                return !compare1.equals(compare2);
            }
            if (logicStr.equals("*")) {
                return compare1.matches(compare2);
            }
            if (logicStr.equals("!*")) {
                return !compare1.matches(compare2);
            }
            long compareNum2;
            long compareNum1;
            try {
                compareNum1 = Long.parseLong(compare1);
                compareNum2 = Long.parseLong(compare2);
            } catch (Exception ex) {
                System.out.print("ERROR! fail to get number:" + compare1 + "," + compare2);
                return false;
            }

            if (logicStr.equals(">")) {
                return compareNum1 > compareNum2;
            }
            if (logicStr.equals("<")) {
                return compareNum1 < compareNum2;
            }
            if (logicStr.equals(">=")) {
                return compareNum1 >= compareNum2;
            }
            if (logicStr.equals("<=")) {
                return compareNum1 <= compareNum2;
            }
            System.out.print("ERROR! fail to parse logicStr:" + logicStr);
            return false;
        } catch (Exception ex) {
            System.out.print("ERROR! fail to parse: method getConditionResult");
        }
        return false;
    }

    private String getCompareValue(String compareStr, Object obj, IValueInterceptor interceptor) {
        compareStr = compareStr.trim();
        if ((compareStr.startsWith("${")) && (compareStr.endsWith("}"))) {
            VariablePart varPart = new VariablePart(compareStr);
            if (compareStr.startsWith("{global_")) {
                return varPart.toString(this.globalObj, interceptor);
            }
            return varPart.toString(obj, interceptor);
        }
        if ((compareStr.startsWith("\"")) && (compareStr.endsWith("\""))) {
            compareStr = compareStr.substring(1, compareStr.length() - 1);
            return compareStr.replaceAll("\\\\\"", "\"");
        }
        return compareStr;
    }
}
