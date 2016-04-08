package com.koolearn.android.kooreader;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.koolearn.klibrary.text.view.ZLTextWordCursor;
import com.koolearn.klibrary.core.application.ZLApplication;

import com.koolearn.kooreader.kooreader.KooReaderApp;

abstract class PopupPanel extends ZLApplication.PopupPanel {
	public ZLTextWordCursor StartPosition;

	protected volatile SimplePopupWindow myWindow;
	private volatile KooReader myActivity;
	private volatile RelativeLayout myRoot;

	PopupPanel(KooReaderApp fbReader) {
		super(fbReader);
	}

	protected final KooReaderApp getReader() {
		return (KooReaderApp)Application;
	}

	@Override
	protected void show_() {
		if (myActivity != null) {
			createControlPanel(myActivity, myRoot);
		}
		if (myWindow != null) {
			myWindow.show();
		}
	}

	@Override
	protected void hide_() {
		if (myWindow != null) {
			myWindow.hide();
		}
	}

	private final void removeWindow(Activity activity) {
		if (myWindow != null && activity == myWindow.getContext()) {
			final ViewGroup root = (ViewGroup)myWindow.getParent();
			myWindow.hide();
			root.removeView(myWindow);
			myWindow = null;
		}
	}

	public static void removeAllWindows(ZLApplication application, Activity activity) {
		for (ZLApplication.PopupPanel popup : application.popupPanels()) {
			if (popup instanceof PopupPanel) {
				((PopupPanel)popup).removeWindow(activity);
			} else if (popup instanceof NavigationPopup) {
				((NavigationPopup)popup).removeWindow(activity);
			}
		}
	}

	public static void restoreVisibilities(ZLApplication application) {
		final ZLApplication.PopupPanel popup = application.getActivePopup();
		if (popup instanceof PopupPanel) {
			((PopupPanel)popup).show_();
		} else if (popup instanceof NavigationPopup) {
			((NavigationPopup)popup).show_();
		}
	}

	public final void initPosition() {
		if (StartPosition == null) {
			StartPosition = new ZLTextWordCursor(getReader().getTextView().getStartCursor());
		}
	}

	public final void storePosition() {
		if (StartPosition == null) {
			return;
		}

		final KooReaderApp reader = getReader();
		if (!StartPosition.equals(reader.getTextView().getStartCursor())) {
			reader.addInvisibleBookmark(StartPosition);
			reader.storePosition();
		}
	}

	public void setPanelInfo(KooReader activity, RelativeLayout root) {
		myActivity = activity;
		myRoot = root;
	}

	public abstract void createControlPanel(KooReader activity, RelativeLayout root);
}
