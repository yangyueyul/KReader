package com.koolearn.klibrary.core.language;

import com.koolearn.android.util.LogInfo;
import com.koolearn.klibrary.core.filesystem.ZLFile;
import com.koolearn.klibrary.core.filesystem.ZLResourceFile;

import java.util.*;

public abstract class ZLLanguageUtil {
	private static ArrayList<String> ourLanguageCodes = new ArrayList<String>();

	private ZLLanguageUtil() {
		LogInfo.i("language");
	}

	public static String defaultLanguageCode() {
		LogInfo.i("language"+Locale.getDefault().getLanguage());

		return Locale.getDefault().getLanguage();
	}

	public static List<String> languageCodes() {
		if (ourLanguageCodes.isEmpty()) {
			TreeSet<String> codes = new TreeSet<String>();
			for (ZLFile file : patternsFile().children()) {

				String name = file.getShortName();
				LogInfo.i("language"+name);
				final int index = name.indexOf("_");
				if (index != -1) {
					String str = name.substring(0, index);
					if (!codes.contains(str)) {
						codes.add(str);
					}
				}
			}
			codes.add("id");
			codes.add("de-traditional");

			ourLanguageCodes.addAll(codes);
		}

		return Collections.unmodifiableList(ourLanguageCodes);
	}

	public static ZLFile patternsFile() {
		LogInfo.i("language");

		return ZLResourceFile.createResourceFile("languagePatterns");
	}
}
