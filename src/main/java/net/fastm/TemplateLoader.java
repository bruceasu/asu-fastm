package net.fastm;

import net.util.FileEncodingUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class TemplateLoader implements ITemplate {
	protected String encoding = "";

    protected Object globalObj;

    @Override
    public Object getGlobalObj() {
        return globalObj;
    }

    @Override
    public void setGlobalObj(Object obj) {
        globalObj = obj;
    }

    public String getEncoding(){
        return this.encoding;
    }

	public static ITemplate getCachedTemplate(String name, String realPath,
			Map<String, TemplateLoader> templateCache) throws IOException {
		TemplateLoader templateLoader = templateCache.get(name);

		if (templateLoader == null) {
			templateLoader = new TemplateLoader(realPath);
			templateCache.put(name, templateLoader);
		} else {
			templateLoader.checkReload();
		}

		return templateLoader.getTemplate();
	}

    @Override
    public String toString(Object paramObject) {
        return getTemplate().toString(paramObject);
    }

    @Override
    public String toString(Object paramObject, IValueInterceptor paramIValueInterceptor) {
        return getTemplate().toString(paramObject, paramIValueInterceptor);
    }

    @Override
    public void write(Object paramObject, PrintWriter paramPrintWriter) {
        getTemplate().write(paramObject, paramPrintWriter);
    }

    @Override
    public void write(Object paramObject, PrintWriter paramPrintWriter, IValueInterceptor paramIValueInterceptor) {
        getTemplate().write(paramObject, paramPrintWriter, paramIValueInterceptor);
    }

    @Override
    public String structure(int level) {
        return getTemplate().structure(level);
    }

	volatile ITemplate template = null;

	protected String fileName = null;

    public String getFileName() {
        return this.fileName;
    }
	volatile long fileTime = 0L;

	public TemplateLoader(String fileName) throws IOException {
		this.fileName = fileName;
		encoding = FileEncodingUtils.getDefaultEncoding();
		forceReload();
	}

	public TemplateLoader(String fileName, String encoding) throws IOException {
		this.fileName = fileName;
		this.encoding = encoding;
		forceReload();
	}

	public void checkReload() {
		if (fileChanged()) {
			forceReload();
		}
	}

	public boolean fileChanged() {
		long theFiletime = new File(fileName).lastModified();
		return fileTime != theFiletime;
	}

	public void forceReload() {
		try {
			template = Parser.parse(fileName, encoding);
			fileTime = new File(fileName).lastModified();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ITemplate getTemplate() {
		if (template == null) {
			forceReload();
		}
		checkReload();

		return template;
	}



}
