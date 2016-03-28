package com.koolearn.klibrary.core.util;

import android.util.Xml;

import com.koolearn.klibrary.core.filesystem.ZLFile;

import org.xml.sax.helpers.DefaultHandler;

//y 静静地解析...
public abstract class XmlUtil {
	public static boolean parseQuietly(ZLFile file, DefaultHandler handler) {
		try {
			Xml.parse(file.getInputStream(), Xml.Encoding.UTF_8, handler);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
