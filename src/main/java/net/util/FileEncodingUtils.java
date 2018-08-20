package net.util;


import java.nio.charset.Charset;

public class FileEncodingUtils {
	private static String DEFAULT_ENCODING = "UTF-8";

	synchronized
	public static String getDefaultEncoding() {
		if (DEFAULT_ENCODING == null) {
			// 要不要直接指定UTF-8呢
			DEFAULT_ENCODING = System.getProperty("file.encoding", "UTF-8");
		}
		return DEFAULT_ENCODING;
	}

	public static void setDefaultEncoding(String encoding) {
		try {
			Charset charset = Charset.forName(encoding);
			DEFAULT_ENCODING = encoding;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
