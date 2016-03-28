package com.koolearn.klibrary.text.view;

import com.koolearn.klibrary.text.model.ExtensionEntry;

import java.util.List;
import java.util.Map;

public abstract class ExtensionElementManager {
	final List<? extends ExtensionElement> getElements(ExtensionEntry entry) {
		return getElements(entry.Type, entry.Data);
	}

	protected abstract List<? extends ExtensionElement> getElements(String type, Map<String,String> data);
}