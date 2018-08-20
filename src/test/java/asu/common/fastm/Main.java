package asu.common.fastm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fastm.Parser;

public class Main {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        String file = "test.html";
        Map valueSet = new HashMap();
        List list = new ArrayList();
        valueSet.put("list", list);

        Map v1 = new HashMap();
        v1.put("name", "a");
        v1.put("x", v1);
        list.add(v1);

        Map v2 = new HashMap();
        v2.put("name", "b");
        v2.put("y", v2);
        list.add(v2);

        v1 = new HashMap();
        v1.put("name", "c");
        v1.put("x", v1);
        list.add(v1);

        v2 = new HashMap();
        v2.put("name", "d");
        v2.put("y", v2);
        list.add(v2);

        valueSet.put("A", 3);
        valueSet.put("B", true);
        Map m = new HashMap();
        m.put("name2", "BBAAA");
        ArrayList<Map> ml1 = new ArrayList<Map>();
        valueSet.put("list", ml1);
        Map m1 = new HashMap();
        m1.put("x", "XXXX");
        Map m2 = new HashMap();
        m2.put("x", "YYYY");
        ml1.add(m1);
        ml1.add(m2);
        valueSet.putAll(m);
        m.put("name2", "123456");
        valueSet.put("m", m);
        FastmConfig.setTemplateDir("test");
        Parser.setParserContext("test");

        String absolutePath = new File("test\\" + file).getAbsolutePath();
        System.out.println(absolutePath);
        FastmConfig.addTemplateFile(absolutePath);

        String html = FastEx.parse(absolutePath, valueSet);
        System.out.println(html);

//        FastmConfig.setTemplateDir("test");
//        String templateName = "a.htm";
//        FastmConfig.addTemplateFile(templateName);
//        Map m = new HashMap();
//        //m.put("name", "xx");
//        m.put("name2", "BBAAA");
//        ArrayList<Map> ml1 = new ArrayList<Map>();
//        m.put("list", ml1);
//        Map m1 = new HashMap();
//        m1.put("x", "XXXX");
//        Map m2 = new HashMap();
//        m2.put("x", "YYYY");
//        ml1.add(m1);
//        ml1.add(m2);
//
//        String html = FastEx.parse(templateName, m);
//        System.out.println("html = " + html);

        System.out.println("=====================================");
        String s = FastEx.parseFile("test\\a.htm", valueSet);
        System.out.println("s = " + s);
    }
}
