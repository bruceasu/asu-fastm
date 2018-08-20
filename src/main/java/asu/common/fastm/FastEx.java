package asu.common.fastm;

import net.fastm.ITemplate;
import net.fastm.TemplateLoader;


/**
 * fastm扩展类
 *
 * @author Suk Honzeon(sukhonzeon@gmail.com)
 */
public class FastEx {

    /**
     * 构造模板数据
     *
     * @param templateName 模板名称/别名
     * @param valueSet 页面数据Map
     */

    public static String parse(final String templateName, final Object valueSet) throws Exception {
        TemplateLoader loader = FastmConfig.getTemplateLoader(templateName);
        ITemplate template = loader.getTemplate();

        if (template == null) {
            throw new Exception("Fastm 配置模板：" + loader.getFileName() + " 加载失败!首先请确认文件是否存在...");
        }

        return template.toString(valueSet);
    }

    /**
     * 构造模板数据
     *
     * @param templateFile 模板文件
     * @param valueSet 页面数据Map
     */

    public static String parseFile(final String templateFile, final Object valueSet)
            throws Exception {
        TemplateLoader loader = new TemplateLoader(templateFile);
        ITemplate template = loader.getTemplate();

        if (template == null) {
            throw new Exception("Fastm 配置模板：" + loader.getFileName() + " 加载失败!首先请确认文件是否存在...");
        }

        return template.toString(valueSet);
    }

}
