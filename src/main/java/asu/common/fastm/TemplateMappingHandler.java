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
import java.util.HashMap;
import java.util.Map;
import net.fastm.TemplateLoader;
import net.util.FileEncodingUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Suk Honzeon(sukhonzeon@gmail.com)
 *         功能：
 *
 * @date Apr 15, 2012
 */
class TemplateMappingHandler extends DefaultHandler {

    Map<String, TemplateLoader> templateMap = new HashMap<String, TemplateLoader>();

    public TemplateMappingHandler() {

    }

    /**
     *
     * @return
     */
    public Map<String, TemplateLoader> getTemplateMap() {
        return templateMap;
    }

    /**
     * override DefaultHandler.startElement()
     *
     * @param namespaceURI
     * @param localName
     * @param qName
     * @param atts
     * @throws SAXException
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        // read "mapping" entry into the dispatch map
        if (qName.equals("template-mapping")) {
            String name = atts.getValue("name");
            String file = atts.getValue("file");
            String encoding = atts.getValue("encoding");
            if (isEmpty(encoding)) {
                encoding = FileEncodingUtils.getDefaultEncoding();
            }
            if (name != null && file != null) {
                String realPath;
                if (!file.startsWith("/")) {
                    realPath = new File(FastmConfig.getTemplateDir(), file).getAbsolutePath();
                } else {
                    realPath = file;
                }
                System.err.println("name = " + name + "; file = " + file + "; encoding = " + encoding);
                try {
                    TemplateLoader templateLoader = new TemplateLoader(realPath, encoding);
                    templateMap.put(name, templateLoader);
                } catch (Exception e) {
                    throw new SAXException(e);
                }
            }
        }
    }

    /**
     * 如果此字符串为 null 或者为空串（""），则返回 true
     *
     * @param cs
     *            字符串
     * @return 如果此字符串为 null 或者为空，则返回 true
     */
    public static boolean isEmpty(CharSequence cs) {
        return null == cs || cs.length() == 0;
    }
}
