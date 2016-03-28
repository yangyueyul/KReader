package com.koolearn.klibrary.core.application;

import com.koolearn.android.util.LogInfo;
import com.koolearn.klibrary.core.filesystem.ZLFile;
import com.koolearn.klibrary.core.options.Config;
import com.koolearn.klibrary.core.options.ZLStringListOption;
import com.koolearn.klibrary.core.options.ZLStringOption;
import com.koolearn.klibrary.core.util.XmlUtil;
import com.koolearn.kooreader.Paths;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class ZLKeyBindings {
	private static final String ACTION = "Action";
	private static final String LONG_PRESS_ACTION = "LongPressAction";

	private final String myName;
	private ZLStringListOption myKeysOption;
	private final TreeMap<Integer,ZLStringOption> myActionMap = new TreeMap<Integer,ZLStringOption>();
	private final TreeMap<Integer,ZLStringOption> myLongPressActionMap = new TreeMap<Integer,ZLStringOption>();

	public ZLKeyBindings() {
		this("Keys");
	}

	private ZLKeyBindings(String name) {
		myName = name;
		Config.Instance().runOnConnect(new Initializer());
	}

	private class Initializer implements Runnable {
		public void run() {
			LogInfo.i("application");
			final Set<String> keys = new TreeSet<String>();
			new Reader(keys).readQuietly(Paths.systemShareDirectory() + "/keymap.xml");
			myKeysOption = new ZLStringListOption(myName, "KeyList", new ArrayList<String>(keys), ",");
		}
	}

	private ZLStringOption createOption(int key, boolean longPress, String defaultValue) {
		LogInfo.i("application");

		final String group = myName + ":" + (longPress ? LONG_PRESS_ACTION : ACTION);
		return new ZLStringOption(group, String.valueOf(key), defaultValue);
	}

	public ZLStringOption getOption(int key, boolean longPress) {
		LogInfo.i("application");

		final TreeMap<Integer,ZLStringOption> map = longPress ? myLongPressActionMap : myActionMap;
		ZLStringOption option = map.get(key);
		if (option == null) {
			option = createOption(key, longPress, ZLApplication.NoAction);
			map.put(key, option);
		}
		return option;
	}

	public void bindKey(int key, boolean longPress, String actionId) {
		if (myKeysOption == null) {
			return;
		}
		final String stringKey = String.valueOf(key);
		List<String> keys = myKeysOption.getValue();
		if (!keys.contains(stringKey)) {
			keys = new ArrayList<String>(keys);
			keys.add(stringKey);
			Collections.sort(keys);
			myKeysOption.setValue(keys);
		}
		getOption(key, longPress).setValue(actionId);
	}

	public String getBinding(int key, boolean longPress) {
		return getOption(key, longPress).getValue();
	}

	public boolean hasBinding(int key, boolean longPress) {
		return !ZLApplication.NoAction.equals(getBinding(key, longPress));
	}

	private class Reader extends DefaultHandler {
		private final Set<String> myKeySet;

		Reader(Set<String> keySet) {
			myKeySet = keySet;
		}

		public void readQuietly(String path) {
			LogInfo.i("application");

			XmlUtil.parseQuietly(ZLFile.createFileByPath(path), this);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			LogInfo.i("application");

			if ("binding".equals(localName)) {
				final String stringKey = attributes.getValue("key");
				final String actionId = attributes.getValue("action");
				if (stringKey != null && actionId != null) {
					try {
						final int key = Integer.parseInt(stringKey);
						myKeySet.add(stringKey);
						myActionMap.put(key, createOption(key, false, actionId));
					} catch (NumberFormatException e) {
					}
				}
			}
		}
	}
}