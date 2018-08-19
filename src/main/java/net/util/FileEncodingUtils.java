package net.util;


public class FileEncodingUtils {
	private static String DEFAULT_ENCODING = null;

	synchronized
	public static String getDefaultEncoding() {
		if (DEFAULT_ENCODING == null) {
			String encoding = null;
			String lang = System.getenv("LANG");
			if (isNotEmpty(lang)) {
				int idx = lang.indexOf('.');
				if (idx != -1) {
					encoding = lang.substring(idx + 1);
				}
			}

			encoding = System.getenv("FILE.ENCODING");
			if (isEmpty(encoding)) {
				encoding = System.getProperty("file.encoding", "UTF-8");
			}

			DEFAULT_ENCODING = encoding;
		}
		return DEFAULT_ENCODING;
	}

	private static boolean isEmpty(String str) {
		return str == null || str.replaceAll("\\s+", "").length() == 0;
	}

	private static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}
}
