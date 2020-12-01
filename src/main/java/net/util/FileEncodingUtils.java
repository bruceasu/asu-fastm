package net.util;

public class FileEncodingUtils {
	private static String DEFAULT_ENCODING = System.getProperty("file.encoding", "UTF-8");

	public static String getDefaultEncoding() {
		return DEFAULT_ENCODING;
	}

	public static void setDefaultEncoding(String encoding) {
		if (encoding!=null) {
			DEFAULT_ENCODING = encoding;
		}
	}
}
