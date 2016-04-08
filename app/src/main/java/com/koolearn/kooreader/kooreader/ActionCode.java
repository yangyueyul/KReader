package com.koolearn.kooreader.kooreader;

public interface ActionCode {
    String TURN_PAGE_BACK = "previousPage";
    String TURN_PAGE_FORWARD = "nextPage";

    String SHOW_NAVIGATION = "navigate";

    String EXIT = "exit";

    String DISPLAY_BOOK_POPUP = "displayBookPopup";
    String PROCESS_HYPERLINK = "processHyperlink";

    String OPEN_VIDEO = "video";
    String SELECTION_BOOKMARK = "selectionBookmark";
    String SELECTION_CLEAR = "selectionClear";
    String SELECTION_COPY_TO_CLIPBOARD = "selectionCopyToClipboard";
    String SELECTION_SHARE = "selectionShare";

    String SELECTION_SHOW_PANEL = "selectionShowPanel";
    String SELECTION_HIDE_PANEL = "selectionHidePanel";

    String HIDE_TOAST = "hideToast";
}