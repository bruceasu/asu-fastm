package asu.common.fastm;

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import net.fastm.Parser;

/**
 * 初始化Fast template映射
 *
 * @author Suk Honzeon(sukhonzeon@gmail.com)
 */
public class InitFastTemplateListener implements ServletContextListener {

    private static final long serialVersionUID = -8791929552999295226L;

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {

    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        String contextRealPath = event.getServletContext().getRealPath("WEB-INF");
        // "deploy", "develop"
        String runstyle = event.getServletContext().getInitParameter(FastmConfig.runStyleKey);

        FastmConfig.setRunStyle(runstyle);
        FastmConfig.setTemplateDir(contextRealPath);
        try {
            // 重要，为了，web中可以直接传递相对webroot的文件路径，和include asChildIndude的扩展。
            Parser.setParserContext(contextRealPath);
            // classpath*:/META-INF/fastm/*.xml
            String filePath = event.getServletContext().getInitParameter(FastmConfig.fastmFilePath);
            if (isBlank(filePath)) {
                throw new Exception("fastmFilePath参数为空，无法初始化fastm. "
                                            + "请检查fastm.properties文件是否配置正确");
            }
            File file = new File(filePath);
            if (file.exists()) {
                FastmConfig
                        .loadFastmConfigByFilePath(file.getAbsolutePath());
                System.out.printf("成功从文件系统加载资源文件[%s]...%n",
                                  file.getAbsolutePath());
            } else {
                // try classpath
                InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
                if (is == null) {
                    System.err.println("没有配置文件，将不会加载模板。");
                } else {
                    // 资源文件不存在于文件系统
                    System.out.printf("资源文件[%s]不存在于文件系统,将通过ClassLoader进行加载,热修改功能关闭...%n",
                                      filePath);
                    FastmConfig.loadFastmConfigByInputStream(is);
                    FastmConfig.setRunStyle("deploy");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 如果此字符串为 null 或者全为空白字符，则返回 true
     *
     * @param cs 字符串
     * @return 如果此字符串为 null 或者全为空白字符，则返回 true
     */
    public static boolean isBlank(CharSequence cs) {
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
}
