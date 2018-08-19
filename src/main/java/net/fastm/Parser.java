package net.fastm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.util.FileEncodingUtils;

public class Parser {

    /**
     * to record a word position in a String. for example,
     * .............${.........}.........${..........}....... etc
     * ^          ^         ^           ^
     * begin      end       begin       end
     */
    static class PosPair {

        int begin = 0;
        int end   = 0;
    }

    ;

    public static class Token {

        /**
         * indicate the string is like ....${...}....${....}....
         */
        public static final int HAS_VARIABLE = 5;

        /**
         * indicate this is a normal static string
         */
        public static final int ORDINARY = 0;

        public static final int BEGIN = 10;

        public static final int End = 20;

        public static final int DYNAMIC = 1;

        public static final int IGNORED = 2;

        public static final int If = 3;

        /**
         * indicate the string is like <!-- @for name -->
         */
        public static final int BEGIN_FOR = BEGIN + DYNAMIC;

        /**
         * indicate the string is like <!-- @done name -->
         */
        public static final int END_FOR = End + DYNAMIC;

        /**
         * indicate the string is like <!-- @# name -->
         */
        public static final int BEGIN_IGNORED = BEGIN + IGNORED;

        /**
         * indicate the string is like <!-- @% name -->
         */
        public static final int END_IGNORED = End + IGNORED;

        /**
         * indicate the string is like <!-- @if condition -->
         */
        public static final int BEGIN_IF = BEGIN + If;

        /**
         * indicate the string is like <!-- @end condition -->
         */
        public static final int END_IF = End + If;

        /**
         * to indicate if this line is a comment definition, or has variable, or
         * just a static line
         */
        int type = 0;

        /**
         * DynamicPart or IgnoredPart name
         */
        String name = null;

        /**
         * variables {...} positions
         */
        List<PosPair> posPairs = null;
    }


    public static final String START_TAG = "<!--";
    public static final String STOP_TAG  = "-->";
    public static final String START     = "${";
    public static final String END       = "}";


    public static final Pattern BEGIN_FOR_PATTERN = Pattern.compile("\\s*@for\\s+([^\\s]*)\\s*");
    public static final Pattern END_FOR_PATTERN   = Pattern.compile("\\s*@done\\s+([^\\s]*)\\s*");

    public static final Pattern BEGIN_IGN_PATTERN = Pattern.compile("\\s*@#\\s+([^\\s]*)\\s*");
    public static final Pattern END_IGN_PATTERN   = Pattern.compile("\\s*@%\\s+([^\\s]*)\\s*");

    public static final Pattern BEGIN_IF_PATTERN = Pattern.compile("\\s*@if\\s+([^\\s]*)\\s*");
    public static final Pattern END_IF_PATTERN   = Pattern.compile("\\s*@end\\s+([^\\s]*)\\s*");

    public static final Pattern BEGIN_FOR_PATTERN_SCRIPT = Pattern
            .compile("\\s*//\\s*@for\\s+([^\\s]*)\\s*");
    public static final Pattern END_FOR_PATTERN_SCRIPT   = Pattern
            .compile("\\s*//\\s*@done\\s+([^\\s]*)\\s*");

    public static final Pattern BEGIN_IGN_PATTERN_SCRIPT = Pattern
            .compile("\\s*//\\s*@#\\s*([^\\s]*)\\s*");
    public static final Pattern END_IGN_PATTERN_SCRIPT   = Pattern
            .compile("\\s*//\\*@%\\s*([^\\s]*)\\s*");

    public static final Pattern BEGIN_IF_PATTERN_SCRIPT = Pattern
            .compile("\\s*//\\s*@if\\s+(.+)\\s*");
    public static final Pattern END_IF_PATTERN_SCRIPT   = Pattern
            .compile("\\s*//\\s*@end\\s+(.+)\\s*");


    public static final Pattern[] PATTERN_GROUP = {BEGIN_FOR_PATTERN, END_FOR_PATTERN,
            BEGIN_IGN_PATTERN, END_IGN_PATTERN, BEGIN_IF_PATTERN, END_IF_PATTERN};

    public static final int[] TYPE_GROUP = {Token.BEGIN_FOR, Token.END_FOR,
            Token.BEGIN_IGNORED, Token.END_IGNORED, Token.BEGIN_IF, Token.END_IF};

    private static String PARSER_CONTEXT;

    /**
     * @return // 0:name 1:file, 2:encoding
     */
    private static String[] getChildFilePath(final String vpartName) throws Exception {
        if (!isParserValid()) {
            throw new IOException("无效的解析器设置 PARSER_CONTEXT没设置");
        }
        String[] result = new String[3];
        String[] strarr = vpartName.split("(\\s)+");
        if (strarr.length == 2) {
            result[0] = strarr[1].substring(0, strarr[1].length() - 1);
            result[1] = PARSER_CONTEXT + File.separator
                    + strarr[1].substring(0, strarr[1].length() - 1);
            result[2] = FileEncodingUtils.getDefaultEncoding();
        } else if (strarr.length == 3) {
            result[0] = strarr[1];
            result[1] = PARSER_CONTEXT + File.separator
                    + strarr[2].substring(0, strarr[2].length() - 1);
            result[2] = FileEncodingUtils.getDefaultEncoding();
        } else if (strarr.length == 4) {
            result[0] = strarr[1];
            result[1] = PARSER_CONTEXT + File.separator
                    + strarr[2];
            result[2] = strarr[3].substring(0, strarr[3].length() - 1);
        } else {
            throw new Exception("错误的 子模板包含格式，个元素之间用 空格 隔开");
        }

        return result;
    }

    public static String getParserContext() {
        return PARSER_CONTEXT;
    }

    /**
     * @return // 0:file, 1:encoding
     */
    private static String[] getSubFilePath(final String vPartName) throws Exception {
        if (!isParserValid()) {
            throw new IOException("无效的解析器设置 PARSER_CONTEXT没设置");
        }
        String[] result = new String[2];
        String[] strArr = vPartName.split("(\\s)+");
        if (strArr.length == 2) {
            result[0] = PARSER_CONTEXT + File.separator
                    + strArr[1].substring(0, strArr[1].length() - 1);
            result[1] = FileEncodingUtils.getDefaultEncoding();
        } else if (strArr.length == 3) {
            result[0] = PARSER_CONTEXT + File.separator + strArr[1];
            result[1] = strArr[2].substring(0, strArr[2].length() - 1);
        } else {
            throw new Exception("错误的 子模板包含格式，个元素之间用 空格 隔开");
        }

        return result;
    }

    private static boolean isParserValid() {
        return PARSER_CONTEXT != null;
    }

    /**
     * parse the template
     */
    public static ITemplate parse(final BufferedReader reader) throws IOException {
        StringBuffer staticLines = new StringBuffer();
        Stack<DynamicPart> stack = new Stack<DynamicPart>();
        DynamicPart top = new DynamicPart("top");

        StaticPart staticPart = null;
        String ignoredName = null;
        int lineNo = 0;
        List<String> lineQueue = new LinkedList<String>();
        String commentPart = null;
        while (true) {
            String line = null;
            if (commentPart != null) {
                line = commentPart;
            } else if (lineQueue.isEmpty()) {
                line = reader.readLine();
                lineNo++;
            } else {
                line = lineQueue.remove(0);
            }

            if (line == null) {
                break;
            }

            if (commentPart != null) {
                commentPart = null;
            } else {
                int commentBegin = line.indexOf(START_TAG);
                if (commentBegin >= 0) {
                    String lineBefore = null;
                    if (commentBegin > 0) {
                        lineBefore = line.substring(0, commentBegin);
                    }

                    StringBuffer commentBuf = new StringBuffer();

                    String commentLine = line;
                    int commentEnd = commentLine.indexOf(STOP_TAG);
                    if (commentEnd < 0) {
                        commentBuf.append(line.substring(commentBegin));
                        commentBegin = 0;
                    }

                    while (commentEnd < 0) {
                        commentLine = reader.readLine();
                        lineNo++;
                        if (commentLine == null) {
                            break;
                        }
                        commentEnd = commentLine.indexOf(STOP_TAG);
                        if (commentEnd >= 0) {
                            break;
                        }
                        commentBuf.append(commentLine + "\n");
                    }

                    if (commentLine == null || commentEnd < 0) {
                        commentPart = commentBuf.toString();
                    } else {
                        int pos = commentEnd + STOP_TAG.length();
                        String postfix = commentLine.substring(commentBegin, pos);
                        commentBuf.append(postfix);

                        if (commentLine.length() > pos) {
                            String lineAfter = commentLine.substring(pos);
                            lineQueue.add(lineAfter);
                        }
                        // 注释文本，先保存在一个变量中。
                        commentPart = commentBuf.toString();
                    }

                    if (lineBefore != null) {
                        // 注释前有文本，应先处理。
                        line = lineBefore;
                    } else {
                        continue;
                    }
                }

            }

            // parse a line
            Token token = parseLine(line);

            // ignore block between <!-- @# name -->
            // <!-- @% name-->

            // if End of the IgnoredPart, then back to normal procedure
            if (ignoredName != null) {
                if (token.type == Token.END_IGNORED && ignoredName.equals(token.name)) {
                    ignoredName = null;
                } else {
                    continue; // ignore this line
                }
            }

            // normal line, put it to static lines buffer
            if (token.type == Token.ORDINARY) {
                staticLines.append(line + "\n");
                continue; // fetch next line
            }

            // code goes here, means this line contains DynamicPart definition
            // or Variable
            if (staticLines.length() > 0) {
                // create a StaticPart to hold the curret static lines in buf.
                staticPart = new StaticPart(staticLines.toString());
                top.addStep(staticPart);
                staticLines.setLength(0); // clean the static lines buf.
            }
            DynamicPart dynamicPart;
            switch (token.type) {
                case Token.BEGIN_IGNORED: // <!-- @# -->
                    ignoredName = token.name;
                    break;
                case Token.BEGIN_FOR: // <!-- @for name -->
                    dynamicPart = new DynamicPart(token.name);
                    top.addStep(dynamicPart);
                    stack.push(top);
                    top = dynamicPart;

                    break;
                case Token.BEGIN_IF: // <!-- @if condition-->
                    dynamicPart = new ConditionDynamicPart(token.name);
                    top.addStep(dynamicPart);
                    stack.push(top);
                    top = dynamicPart;
                    break;
                case Token.END_FOR: // <!-- @done name-->
                    if (!top.getName().equals(token.name)) {
                        throw new IOException("line " + lineNo + ": @done " + top.getName()
                                                      + " instead of " + token.name
                                                      + " is expected.");
                    }
                    top = stack.pop();
                    if (top == null) {
                        throw new IOException("line " + lineNo + ": @done top = null, why?");
                    }
                    processHasVariable(token, line, top);
                    break;

                case Token.END_IF: // <!-- @end condition -->
                    if (!top.getName().equals(token.name)) {
                        throw new IOException("line " + lineNo + ": @done " + top.getName()
                                                      + " instead of " + token.name
                                                      + " is expected.");
                    }
                    top = stack.pop();
                    if (top == null) {
                        throw new IOException("line " + lineNo + ": @end top = null, why?");
                    }
                    processHasVariable(token, line, top);
                    break;
                case Token.HAS_VARIABLE: // this line contains ${..}
                    processHasVariable(token, line, top);
                default:
                    break;
            } // end switch (token.type)

        } // end while

        if (stack.size() > 0) {
            DynamicPart left = stack.pop();
            throw new IOException("line " + lineNo + ": @for " + left.getName()
                                          + " is expected but not found.");
        }

        if (staticLines.length() > 0) {
            staticPart = new StaticPart(staticLines.toString());
            top.addStep(staticPart);
        }

        return top;
    }

    public static ITemplate parse(final InputStream stream, String charsetName) throws IOException {
        if (charsetName == null || charsetName.trim().length() == 0) {
            charsetName = FileEncodingUtils.getDefaultEncoding();
        }

        InputStreamReader streamReader = new InputStreamReader(stream, charsetName);
        BufferedReader reader = new BufferedReader(streamReader);

        ITemplate template = parse(reader);
        streamReader.close();
        reader.close();
        return template;
    }

    public static ITemplate parse(final String fileName) throws IOException {
        return parse(fileName, FileEncodingUtils.getDefaultEncoding());
    }

    public static ITemplate parse(final String fileName, final String charsetName)
            throws IOException {
        System.out.println("start parsing " + fileName);
        FileInputStream fileStream = new FileInputStream(fileName);
        ITemplate template = parse(fileStream, charsetName);
        fileStream.close();

        return template;
    }

    public static Token parseLine(final String line) {
        Token token = new Token();

        if (line.startsWith(START_TAG) && line.endsWith(STOP_TAG)) {
            int commentBeginPos = line.indexOf(START_TAG) + START_TAG.length();
            int commentEndPos = line.indexOf(STOP_TAG, commentBeginPos);

            String tag = line.substring(commentBeginPos, commentEndPos).trim();

            int nPatterns = PATTERN_GROUP.length;
            for (int i = 0; i < nPatterns; i++) {
                Pattern pattern = PATTERN_GROUP[i];
                Matcher matcher = pattern.matcher(tag);
                if (matcher.matches()) {
                    token.type = TYPE_GROUP[i];
                    if (token.type >= Token.BEGIN && token.type <= Token.END_IF) {
                        token.name = matcher.group(1);
                    }
                    break;
                }
            }

            return token;
        }

        Matcher matcher = BEGIN_FOR_PATTERN_SCRIPT.matcher(line);
        if (matcher.matches()) {
            token.type = Token.BEGIN_FOR;
            token.name = matcher.group(1);

            return token;
        }

        matcher = END_FOR_PATTERN_SCRIPT.matcher(line);
        if (matcher.matches()) {
            token.type = Token.END_FOR;
            token.name = matcher.group(1);

            return token;
        }

        matcher = BEGIN_IF_PATTERN_SCRIPT.matcher(line);
        if (matcher.matches()) {
            token.type = Token.BEGIN_IF;
            token.name = matcher.group(1);

            return token;
        }

        matcher = END_IF_PATTERN_SCRIPT.matcher(line);
        if (matcher.matches()) {
            token.type = Token.END_IF;
            token.name = matcher.group(1);

            return token;
        }

        matcher = BEGIN_IGN_PATTERN_SCRIPT.matcher(line);
        if (matcher.matches()) {
            token.type = Token.BEGIN_IGNORED;
            token.name = matcher.group(1);

            return token;
        }

        matcher = END_IGN_PATTERN_SCRIPT.matcher(line);
        if (matcher.matches()) {
            token.type = Token.END_IGNORED;
            token.name = matcher.group(1);

            return token;
        }

        int begin = 0;
        List<PosPair> posPairs = null;
        while (true) {
            int lBracketPos = line.indexOf(START, begin);
            if (lBracketPos < 0) {
                break;
            }
            int rBracketPos = line.indexOf(END, lBracketPos);
            if (rBracketPos < 0) {
                break;
            }
            int nestedLeftBracketPos = line.indexOf(START, lBracketPos + 2);
            while (nestedLeftBracketPos >= 0 && nestedLeftBracketPos < rBracketPos) {
                lBracketPos = nestedLeftBracketPos;
                nestedLeftBracketPos = line.indexOf(START, lBracketPos + 2);
            }

            if (posPairs == null) {
                posPairs = new ArrayList<PosPair>();
            }

            PosPair posPair = new PosPair();
            posPair.begin = lBracketPos;
            posPair.end = rBracketPos;

            posPairs.add(posPair);

            begin = rBracketPos + 1;
        }

        if (posPairs != null) {
            token.type = Token.HAS_VARIABLE;
            token.posPairs = posPairs;
        }

        return token;
    }

    private static void parseVariablePart(final DynamicPart top, final String vpartName)
            throws Exception {
        VariablePart varPart = null;

        if (vpartName.startsWith("${global_")) {
            varPart = new GlobalVariablePart(vpartName);

            top.addStep(varPart);
        } else if (vpartName.startsWith("${include ")) { // with top
            String[] result = getSubFilePath(vpartName);
            String filepath = result[0];
            String encoding = result[1];
            DynamicPart sub = (DynamicPart) parse(filepath, encoding);

            List<ITemplate> steps = top.getSteps();
            steps.addAll(sub.getSteps());
            top.setSteps(steps);
        } else if (vpartName.startsWith("${asChildInclude ")) { // in top
            String[] result = getChildFilePath(vpartName);
            String filepath = result[1];
            String subDynaName = result[0];
            String encoding = result[2];
            DynamicPart sub = (DynamicPart) parse(filepath, encoding);

            sub.setName(subDynaName);
            top.addStep(sub);
        } else {
            varPart = new VariablePart(vpartName);
            top.addStep(varPart);
        }
    }

    private static void processHasVariable(final Token token,
                                           final String line,
                                           final DynamicPart top) throws IOException {
        List<PosPair> posPairs = token.posPairs;

        // separate this line to
        // .............${.........}.........${..........}....... etc
        // ^ ^ ^ ^
        // StaticPart VariablePart Static Variable Static etc

        if (posPairs == null) {
            return;
        }
        int nPairs = posPairs.size();

        int begin = 0;
        int end = line.length() - 1;
        StaticPart staticPart;
        for (int k = 0; k < nPairs; k++) {
            PosPair posPair = posPairs.get(k);

            if (begin < posPair.begin) {
                staticPart = new StaticPart(line.substring(begin, posPair.begin));
                top.addStep(staticPart);
            }

            String varName = line.substring(posPair.begin, posPair.end + 1);
            try {
                parseVariablePart(top, varName);
            } catch (Exception e) {
                e.printStackTrace();
                throw new IOException("分析variablePart失败");
            }

            begin = posPair.end + 1;
        } // end for (int k = 0; k < nPairs; k++)

        String tail = "\n";

        if (begin <= end) {
            tail = line.substring(begin, end + 1) + "\n";
        }
        staticPart = new StaticPart(tail);
        top.addStep(staticPart);

    }

    /**
     * set the PARSER_CONTEXT
     *
     * @param parserContext must be a exists path
     */
    public static void setParserContext(final String parserContext) throws IOException {
        File contextFile = new File(parserContext);
        if (!contextFile.exists()) {
            throw new FileNotFoundException("错误的解析器环境设置");
        }

        PARSER_CONTEXT = parserContext;
    }
}
