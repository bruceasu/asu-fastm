package asu.common.fastm;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.fastm.ITemplate;
import net.fastm.TemplateLoader;


/**
 * fastm扩展类
 *
 * @author Suk Honzeon(sukhonzeon@gmail.com)
 */
@SuppressWarnings("unchecked")
public class FastEx {
    // html 头
    public static String HTML_CONTENT_TYPE = "text/html;charset=UTF-8";

    // xml头
    public static String XML_CONTENT_TYPE = "text/xml;charset=UTF-8";

    // Remote Javascript头
    public static String RJS_CONTENT_TYPE = "text/javascript;charset=UTF-8";

    // word 头
    public static String WORD_CONTENT_TYPE  = "application/msword;charset=iso-8859-1";
    // excel 头
    public static String EXCEL_CONTENT_TYPE = "application/vnd.ms-excel;charset=iso-8859-1";

    /**
     * 直接生成页面数据，供Action方法调用,使用默认的模板编码TEMPLATE_DEF_CHARSET，使用默认的文件头
     *
     * HTML_CONTENT_TYPE
     */
    public static void genResponseContent(final String templateName, final Map valueSet,
                                          final HttpServletResponse response) throws Exception {
        // 使用HTML头
        genResponseContent(templateName, valueSet, response, HTML_CONTENT_TYPE);
    }

    /**
     * 解析web响应内容,并且使用默认的编码集
     */
    public static void genResponseContent(final String templateName,
                                          Map valueSet,
                                          final HttpServletResponse response,
                                          final String contentType) throws Exception {
        response.setContentType(contentType);
        valueSet = valueSet == null ? new HashMap() : valueSet;
        response.getWriter().write(parse(templateName, valueSet));

    }

    /**
     * 生成xml内容
     */
    public static void genResponseContentXML(final String templateName, final Map valueSet,
                                             final HttpServletResponse response) throws Exception {
        genResponseContent(templateName, valueSet, response, XML_CONTENT_TYPE);
    }

    /**
     * 构造模板数据
     *
     * @param templateName 模板名称/别名
     * @param valueSet 页面数据Map
     */

    public static String parse(final String templateName, final Map valueSet) throws Exception {
        String fastm_content = null;
        TemplateLoader loader = FastmConfig.getTemplateLoader(templateName);
        ITemplate template = loader.getTemplate();

        if (template == null) {
            throw new Exception("Fastm 配置模板：" + loader.getFileName() + " 加载失败!首先请确认文件是否存在...");
        }

        fastm_content = template.toString(valueSet);
        return fastm_content;
    }

}
