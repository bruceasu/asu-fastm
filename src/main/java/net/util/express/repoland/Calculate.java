package net.util.express.repoland;

public class Calculate {

    public static boolean isOperator(String operator) {
        return "+".equals(operator) || "-".equals(operator) || "*".equals(operator)
                || "/".equals(operator) || "(".equals(operator) || ")".equals(operator)
                || "|".equals(operator) || "&".equals(operator) || "!".equals(operator)
                || "=".equals(operator);
    }

    public static int operatorNum(String operator) {
        if ("!".equals(operator)) {
            return 1;
        }
        return 2;
    }

    public static int priority(String operator) {
        if ("|".equals(operator)) {
            return 1;
        }
        if ("&".equals(operator)) {
            return 2;
        }
        if ("=".equals(operator)) {
            return 3;
        }
        if ("!".equals(operator)) {
            return 4;
        }
        if (operator.equals("+") || operator.equals("-")) {
            return 5;
        }
        if (operator.equals("*") || operator.equals("/")) {
            return 6;
        }
        if (operator.equals("(") || operator.equals(")")) {
            return 7;
        }

        return 0;
    }

    public static String singleOp(String op) {
        boolean opValue = Boolean.valueOf(op).booleanValue();
        opValue = !opValue;
        return String.valueOf(opValue);
    }

    public static String twoResult(String operator, String a, String b) {
        try {
            String op = operator;
            String rs = new String();
            if ("=".equals(operator)) {
                return String.valueOf(String.valueOf(a).equals(String.valueOf(b)));
            }
            if ("&".equals(operator)) {
                return String.valueOf(Boolean.valueOf(a).booleanValue()
                                              && Boolean.valueOf(b).booleanValue());
            }
            if ("|".equals(operator)) {
                return String.valueOf(Boolean.valueOf(a).booleanValue()
                                              || Boolean.valueOf(b).booleanValue());
            }
            double x = Double.parseDouble(b);
            double y = Double.parseDouble(a);
            double z = 0.0D;
            if (op.equals("+")) {
                z = x + y;
            } else if (op.equals("-")) {
                z = x - y;
            } else if (op.equals("*")) {
                z = x * y;
            } else if (op.equals("/")) {
                z = x / y;
            } else {
                z = 0.0D;
            }
            return rs + z;
        } catch (NumberFormatException e) {
            throw new RuntimeException(e.getMessage());
        }

    }
}