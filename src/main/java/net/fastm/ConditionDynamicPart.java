package net.fastm;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.util.express.ExpressUtils;

public class ConditionDynamicPart extends DynamicPart {

    public static final String  GLOBAL  = "${global_";
    //String regEx = "[\"}0123456789]\\s*(==|!=|>=|\\*|!\\*|<=|>|<)\\s*[\"{0123456789]";
    private static      String  regEx   = "[\"0123456789}]\\s*(==|!=|>=|\\*|!\\*|<=|>|<)\\s*[\"\\${0123456789}]";
    private static      Pattern pattern = Pattern.compile(regEx);

    public ConditionDynamicPart(String name) {
        super(name);
    }

    @Override
    public void write(Object obj, PrintWriter writer, IValueInterceptor interceptor) {
        boolean conditionResult = getComplexConditionResult(getName(), obj, interceptor);
        if (conditionResult) {
            DynamicPart dPart = new DynamicPart("temp"
                                                        + new Random(System.currentTimeMillis())
                    .nextInt());

            dPart.setSteps(getSteps());
            dPart.setGlobalObj(globalObj);
            dPart.write(obj, writer, interceptor);
        }
    }


    private boolean getComplexConditionResult(final String express, final Object obj,
                                              final IValueInterceptor interceptor) {
        String targetStr = express.trim();
        targetStr = targetStr.replaceAll("&&", "&");
        targetStr = targetStr.replaceAll("\\|\\|", "\\|");
        ArrayList<String> expression = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(targetStr, "()|&", true);
        while (st.hasMoreElements()) {
            expression.add(st.nextToken());
        }
        StringBuilder complexExpress = new StringBuilder();
        for (String item : expression) {
            item = item == null ? "" : item.trim();
            if ("".equals(item)) {
                continue;
            }
            if ("(".equals(item) || ")".equals(item) || "|".equals(item) || "&".equals(item)) {
                complexExpress.append(item);
            } else {
                boolean result = getConditionResult(item, obj, interceptor);
                complexExpress.append(String.valueOf(result));
            }
        }
        return ExpressUtils.judge(complexExpress.toString());
    }


    /**
     * get condition result
     *
     * @param dynName dynamic Name Not null
     * @param obj object Nullable
     * @param interceptor IValueInterceptor Nullable
     * @return true or false
     */
    private boolean getConditionResult(final String dynName, final Object obj,
                                       final IValueInterceptor interceptor) {
        if (dynName == null) {
            return false;
        }

        boolean result;
        try {
            String targetStr = dynName.trim();
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
                result = compare1.equals(compare2);
            } else if (logicStr.equals("!=")) {
                result = !compare1.equals(compare2);
            } else if (logicStr.equals("*")) {
                result = compare1.matches(compare2);
            } else if (logicStr.equals("!*")) {
                result = !compare1.matches(compare2);
            } else {
                long compareNum1 = Long.parseLong(compare1);
                long compareNum2 = Long.parseLong(compare2);
                if (logicStr.equals(">")) {
                    result = compareNum1 > compareNum2;
                } else if (logicStr.equals("<")) {
                    result = compareNum1 < compareNum2;
                } else if (logicStr.equals(">=")) {
                    result = compareNum1 >= compareNum2;
                } else if (logicStr.equals("<=")) {
                    result = compareNum1 <= compareNum2;
                } else {
                    System.err.print("ERROR! fail to parse logicStr:" + logicStr);
                    result = false;
                }
            }

        } catch (Exception ex) {
            System.err.print("ERROR! fail to parse: method getConditionResult");
            result = false;
        }
        return result;
    }

    private String getCompareValue(String compareStr, final Object obj,
                                   final IValueInterceptor interceptor) {
        compareStr = compareStr.trim();
        if (compareStr.startsWith(START) && compareStr.endsWith(END)) {
            VariablePart varPart = new VariablePart(compareStr);
            if (compareStr.startsWith(GLOBAL)) {
                return varPart.toString(globalObj, interceptor);
            }

            return varPart.toString(obj, interceptor);
        }
        if (compareStr.startsWith("\"") && compareStr.endsWith("\"")) {
            compareStr = compareStr.substring(1, compareStr.length() - 1);
            return compareStr.replaceAll("\\\\\"", "\"");
        }
        return compareStr;
    }


}
