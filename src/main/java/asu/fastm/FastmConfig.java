package asu.fastm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import net.fastm.Parser;
import net.fastm.TemplateLoader;
import net.util.FileEncodingUtils;
import org.xml.sax.SAXException;

/**
 * 关于fastm模板框架的配置信息
 *
 * @author Suk Honzeon
 */
public class FastmConfig {

    /**
     * 模板文件路径
     */
    public static       String templateDir            = "";
    /**
     * 系统运行模式的配置信息关键key
     */
    public static final String KEY_RUN_STYLE          = "fastm.run.style";
    public static final String KEY_FASTM_FILE_PATH    = "fastm.file.path";
    public static final String KEY_FASTM_TEMPLATE_DIR = "fastm.template.path";
    public static final String KEY_FASTM_TEMPLATE_ENCODING = "fastm.template.encoding";

    /**
     * 系统运行模式,默认为deploy,还可选develop
     */
    private static String runStyle = "deploy";

    /**
     * 系统运行模式数组
     */
    private static final String[] runStyleArray = new String[]{"deploy", "develop"};

    /**
     * fastm配置文件路径集合,用于重新加载时使用
     */
    private static Map<String, FileItem> fastmConfigFilePathMapping = new HashMap<String, FileItem>();

    /**
     * fastm配置模板映射
     */
    private static volatile Map<String, TemplateLoader> templateMap = new HashMap<String, TemplateLoader>();

    public static void init(Map<String, String> configMap) throws Exception {
        String encoding = configMap.get(KEY_FASTM_TEMPLATE_ENCODING);
        if (encoding != null) {
            FileEncodingUtils.setDefaultEncoding(encoding);
        }
        String templateDir = configMap.get(KEY_FASTM_TEMPLATE_DIR);
        // "deploy", "develop"
        String runStyle = configMap.get(KEY_RUN_STYLE);
        setRunStyle(runStyle);
        setTemplateDir(templateDir);

        String filePath = configMap.get(KEY_FASTM_FILE_PATH);
        if (isBlank(filePath)) {
            System.out.println("没有配置文件，将不会加载模板。");
            return;
        }
        File file = new File(filePath);
        if (file.exists()) {
            loadFastmConfigByFilePath(file.getAbsolutePath());
            System.out.printf("成功从文件系统加载资源文件[%s]...%n",
                              file.getAbsolutePath());
        } else {
            // try classpath
            InputStream is = FastmConfig.class.getClassLoader().getResourceAsStream(filePath);
            if (is == null) {
                System.err.println("没有配置文件，将不会加载模板。");
            } else {
                // 资源文件不存在于文件系统
                System.out.printf("资源文件[%s]不存在于文件系统,将通过ClassLoader进行加载,热修改功能关闭...%n",
                                  filePath);
                loadFastmConfigByInputStream(is);
                setRunStyle("deploy");
            }
        }

    }

    /**
     * 依据绝对路径加载fastm模板配置映射文件
     *
     * @param configFile 映射文件路径
     * @throws SAXException 加载失败抛出异常
     */
    public static void loadFastmConfigByFilePath(String configFile) throws Exception {
        addFastmConfigFilePath(configFile);
        FileInputStream inputStream = new FileInputStream(configFile);
        loadFastmConfigByInputStream(inputStream);

        //TemplateMappingHandler handler = new TemplateMappingHandler();
        //SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        //saxParser.parse(configFile, handler);
        //Map<String, TemplateLoader> templateMapping = handler.getTemplateMap();
        //
        //addBatchTemplateMapping(templateMapping);
    }

    /**
     * 加载流形式的配置文件
     *
     * @param is 流
     */
    public static void loadFastmConfigByInputStream(InputStream is) throws Exception {
        TemplateMappingHandler handler = new TemplateMappingHandler();
        SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        saxParser.parse(is, handler);
        Map<String, TemplateLoader> templateMapping = handler.getTemplateMap();

        addBatchTemplateMapping(templateMapping);
    }

    /**
     * 添加配置模板映射的路径
     */
    public static void addFastmConfigFilePath(String filePath) {
        if (fastmConfigFilePathMapping.get(filePath) == null) {
            FileItem fileItem = new FileItem(filePath);
            fastmConfigFilePathMapping.put(filePath, fileItem);
        }
    }

    /**
     * 取配置映射文件路径等信息
     */
    public static Map<String, FileItem> getFastmConfigFilePathMapping() {
        return fastmConfigFilePathMapping;
    }

    /**
     * 强行重新加载修改的配置文件
     */
    public static void forceReloadFastmConfig() throws Exception {
        if (getFastmConfigFilePathMapping().isEmpty()) {
            // 无法热加载。
            return;
        }

        // Map fastmConfig = new HashMap();
        // 取配置值集合
        boolean checkReloadSign = false;
        Collection<FileItem> filePathMappingValueSet =
                FastmConfig.getFastmConfigFilePathMapping().values();
        for (Iterator<FileItem> iter = filePathMappingValueSet.iterator(); iter.hasNext(); ) {
            FileItem element = iter.next();
            File configMapping = new File(element.getConfigFile());
            if (configMapping.lastModified() != element.getLastModifyTime()) {
                checkReloadSign = true;
                break;
            }
        }
        // 配置文件无修改过,则不需要加载
        if (checkReloadSign == false) {
            return;
        }
        // 转移配置集合从Set到List
        List<FileItem> filePathMappingArray = new ArrayList<FileItem>();
        for (Iterator<FileItem> iter = filePathMappingValueSet.iterator(); iter.hasNext(); ) {
            FileItem element = iter.next();
            filePathMappingArray.add(element);
        }
        // 排序,按加载的顺序
        Collections.sort(filePathMappingArray);
        // 清空
        FastmConfig.clearTemplateMap();
        // 强行加载
        for (Iterator<FileItem> iter = filePathMappingArray.iterator(); iter.hasNext(); ) {
            FileItem item = iter.next();
            item.setLastModifyTime(new File(item.getConfigFile()).lastModified());
            loadFastmConfigByFilePath(item.getConfigFile());
        }
    }


    /**
     * 设置系统的运行模式
     */
    public static void setRunStyle(String runStyle) {
        for (int index = 0; index < runStyleArray.length; index++) {
            if (runStyleArray[index].equals(runStyle)) {
                FastmConfig.runStyle = runStyle;
                System.out.println("当前系统启动运行模式为：" + runStyle);
                return;
            }
        }
        FastmConfig.runStyle = runStyleArray[0];
        System.out.println("当前系统启动运行模式为不支持的运行模式："
                                   + runStyle
                                   + ",系统将采用默认的部署模式运行："
                                   + runStyleArray[0]);
    }

    /* -----------------------templates------------------------ */

    /**
     * 将加载的配置文件添加到缓存
     */
    public static void addBatchTemplateMapping(Map<String, TemplateLoader> templateMapping) {
        templateMap.putAll(templateMapping);
    }

    /**
     * 直接添加模板文件
     *
     * @param file 相对webroot的文件路径
     */
    public static void addTemplateFile(String file) {
        addTemplateFile(file, null);
    }

    /**
     * 直接添加模板文件
     *
     * @param file 相对webRoot的文件路径
     */
    public static void addTemplateFile(String file, String encoding) {
        if (templateMap.containsKey(file)) {
            return;
        }
        String realPath;
        if (file.startsWith("/") || file.matches("\\w:.+")) {
            realPath = file;
        } else {
            realPath = new File(getTemplateDir(), file).getAbsolutePath();
        }

        try {
            TemplateLoader templateLoader = new TemplateLoader(realPath, encoding);
            templateMap.put(file, templateLoader);
        } catch (Exception e) {
            // do not add it!
            e.printStackTrace();
        }
    }

    /**
     * 判断系统运行模式:系统现在支持两种运行模式,
     * 一种是deploy,对应返回值为0,
     * 一种是开发模式develop,对应返回值为1,
     * 没有配置或者配置有误的情况下返回0
     */
    public static int checkRunStyle() {
        for (int index = 0; index < runStyleArray.length; index++) {
            if (runStyleArray[index].equals(runStyle)) {
                return index;
            }
        }
        return 0;
    }

    /**
     * 重新加载配置文件时,先清空
     */
    public static void clearTemplateMap() {
        templateMap.clear();
    }


    public static String getRunStyle() {
        return runStyle;
    }

    public static String[] getRunstylearray() {
        return runStyleArray;
    }

    /**
     * 依据templateName取模板
     */
    public static TemplateLoader getTemplateLoader(String templateName) throws Exception {
        if (templateMap == null) {
            throw new NullPointerException("template map is not init yet.");
        }
        if (checkRunStyle() == 1) {
            forceReloadFastmConfig();
        }
        TemplateLoader templateLoader = templateMap.get(templateName);
        return templateLoader;
    }

    public static String getTemplateDir() {
        return templateDir;
    }

    public static void setTemplateDir(String templateDir) {
        FastmConfig.templateDir = templateDir;
        try {
            // 重要，为了，传递相对文件路径，和include asChildInclude的扩展。
            Parser.setParserContext(templateDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果此字符串为 null 或者全为空白字符，则返回 true
     *
     * @param cs 字符串
     * @return 如果此字符串为 null 或者全为空白字符，则返回 true
     */
    private static boolean isBlank(CharSequence cs) {
        if (null == cs) {
            return true;
        }
        int length = cs.length();
        for (int i = 0; i < length; i++) {
            if (!(Character.isWhitespace(cs.charAt(i)))) {
                return false;
            }
        }
        return true;
    }


    /**
     * 配置文件对象
     */
    public static class FileItem implements Comparable<Object> {

        /**
         * 配置映射文件路径
         */
        private final String configFile;

        /**
         * 加载的顺序
         */
        private final int order;

        /**
         * 配置文件最后修改时间
         */
        private long lastModifyTime = 0L;

        private static int globleOrder = 0;

        public FileItem(String configFile) {
            this.configFile = configFile;
            order = globleOrder;
            globleOrder++;
            lastModifyTime = getFileLastModifyTime(configFile);
        }

        @Override
        public int compareTo(Object arg0) {

            if (arg0 instanceof FileItem) {
                return getOrder() - ((FileItem) arg0).getOrder();
            }
            return 0;
        }

        public String getConfigFile() {
            return configFile;
        }

        public long getLastModifyTime() {
            return lastModifyTime;
        }

        public int getOrder() {
            return order;
        }

        public void setLastModifyTime(long lastModifyTime) {
            this.lastModifyTime = lastModifyTime;
        }


        private long getFileLastModifyTime(String fileName) {
            File file = new File(fileName);
            return file.lastModified();
        }
    }

}
