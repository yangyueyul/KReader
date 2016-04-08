package com.koolearn.klibrary.core.application;

import com.koolearn.klibrary.core.view.ZLViewWidget;

public interface ZLApplicationWindow {
//	void setWindowTitle(String title);
	void showErrorMessage(String resourceKey);
	void showErrorMessage(String resourceKey, String parameter);
	ZLApplication.SynchronousExecutor createExecutor(String key);
	void processException(Exception e);

	void refresh();

	ZLViewWidget getViewWidget();

	void hideViewWidget(boolean flag);

	void close();

	int getBatteryLevel();
}
