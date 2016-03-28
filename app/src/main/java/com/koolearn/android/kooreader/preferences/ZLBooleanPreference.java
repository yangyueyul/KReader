package com.koolearn.android.kooreader.preferences;

import android.content.Context;

import com.koolearn.klibrary.core.options.ZLBooleanOption;
import com.koolearn.klibrary.core.resources.ZLResource;

class ZLBooleanPreference extends ZLCheckBoxPreference {
	private final ZLBooleanOption myOption;

	ZLBooleanPreference(Context context, ZLBooleanOption option, ZLResource resource) {
		super(context, resource);
		myOption = option;
		setChecked(option.getValue());
	}

	@Override
	protected void onClick() {
		super.onClick();
		myOption.setValue(isChecked());
	}
}
