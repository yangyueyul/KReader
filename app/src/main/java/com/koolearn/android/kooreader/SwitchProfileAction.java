package com.koolearn.android.kooreader;

import com.koolearn.kooreader.kooreader.KooReaderApp;

class SwitchProfileAction extends KooAndroidAction {
	private String myProfileName;

	SwitchProfileAction(KooReader baseActivity, KooReaderApp fbreader, String profileName) {
		super(baseActivity, fbreader);
		myProfileName = profileName;
	}

	@Override
	public boolean isVisible() {
		return !myProfileName.equals(Reader.ViewOptions.ColorProfileName.getValue());
	}

	@Override
	protected void run(Object ... params) {
		Reader.ViewOptions.ColorProfileName.setValue(myProfileName);
		Reader.getViewWidget().reset();
		Reader.getViewWidget().repaint();
	}
}
