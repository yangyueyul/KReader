package com.koolearn.kooreader.kooreader;

import com.koolearn.android.kooreader.OpenPhotoAction;
import com.koolearn.klibrary.core.filesystem.ZLFile;
import com.koolearn.klibrary.core.filesystem.ZLResourceFile;
import com.koolearn.klibrary.core.library.ZLibrary;
import com.koolearn.klibrary.core.util.ZLColor;
import com.koolearn.klibrary.core.view.SelectionCursor;
import com.koolearn.klibrary.core.view.ZLPaintContext;
import com.koolearn.klibrary.core.view.ZLViewEnums;
import com.koolearn.klibrary.text.model.ZLTextModel;
import com.koolearn.klibrary.text.view.ExtensionElementManager;
import com.koolearn.klibrary.text.view.ZLTextHighlighting;
import com.koolearn.klibrary.text.view.ZLTextHyperlink;
import com.koolearn.klibrary.text.view.ZLTextHyperlinkRegionSoul;
import com.koolearn.klibrary.text.view.ZLTextImageRegionSoul;
import com.koolearn.klibrary.text.view.ZLTextPosition;
import com.koolearn.klibrary.text.view.ZLTextRegion;
import com.koolearn.klibrary.text.view.ZLTextVideoRegionSoul;
import com.koolearn.klibrary.text.view.ZLTextView;
import com.koolearn.klibrary.text.view.ZLTextWordRegionSoul;
import com.koolearn.klibrary.text.view.style.ZLTextStyleCollection;
import com.koolearn.klibrary.ui.android.library.ZLAndroidLibrary;
import com.koolearn.kooreader.bookmodel.FBHyperlinkType;
import com.koolearn.kooreader.kooreader.options.ColorProfile;
import com.koolearn.kooreader.kooreader.options.MiscOptions;
import com.koolearn.kooreader.kooreader.options.PageTurningOptions;
import com.koolearn.kooreader.kooreader.options.ViewOptions;
import com.koolearn.kooreader.util.FixedTextSnippet;
import com.koolearn.kooreader.util.TextSnippet;

public final class KooView extends ZLTextView {
    private final KooReaderApp myReader;
    private final ViewOptions myViewOptions;
    private final BookElementManager myBookElementManager;

    KooView(KooReaderApp reader) {
        super(reader);
        myReader = reader;
        myViewOptions = reader.ViewOptions;
        myBookElementManager = new BookElementManager(this); // get扩展管理器
    }

    public void setModel(ZLTextModel model) {
        super.setModel(model);
//        if (myFooter != null) {
//            myFooter.resetTOCMarks();
//        }
    }

    private int myStartY;
    private boolean myIsBrightnessAdjustmentInProgress;
    private int myStartBrightness;

    private TapZoneMap myZoneMap;

    private TapZoneMap getZoneMap() {
        final PageTurningOptions prefs = myReader.PageTurningOptions;
        String id = prefs.TapZoneMap.getValue();
        if ("".equals(id)) {
            id = prefs.Horizontal.getValue() ? "right_to_left" : "up"; // 这里设置了只有两种
        }
        if (myZoneMap == null || !id.equals(myZoneMap.Name)) {
            myZoneMap = TapZoneMap.zoneMap(id);
        }
        return myZoneMap;
    }

    /**
     * 单击后根据设置的Zonemap来判断action
     * navigate nextpage previousPage
     * 执行action
     */
    private void onFingerSingleTapLastResort(int x, int y) {
        myReader.runAction(getZoneMap().getActionByCoordinates( //y 运行功能   根据x,y,w,h,tap判断
                x, y, getContextWidth(), getContextHeight(),
                isDoubleTapSupported() ? TapZoneMap.Tap.singleNotDoubleTap : TapZoneMap.Tap.singleTap
        ), x, y); //y 传入actionId,x,y的值 进行功能显示
    }

    @Override
    public void onFingerSingleTap(int x, int y) {
        Application.hideActivePopup(); // 隐藏popup
//        Application.runAction(ActionCode.SELECTION_CLEAR);

        final ZLTextRegion hyperlinkRegion = findRegion(x, y, maxSelectionDistance(), ZLTextRegion.HyperlinkFilter);
        if (hyperlinkRegion != null) { //y 超链接点击
            outlineRegion(hyperlinkRegion); //y 画框
            myReader.getViewWidget().reset(); // 画框后应用
            myReader.getViewWidget().repaint();
            myReader.runAction(ActionCode.PROCESS_HYPERLINK);
            return;
        }

        final ZLTextRegion bookRegion = findRegion(x, y, 0, ZLTextRegion.ExtensionFilter);
        if (bookRegion != null) {
            myReader.runAction(ActionCode.DISPLAY_BOOK_POPUP, bookRegion);
            return;
        }

        final ZLTextRegion videoRegion = findRegion(x, y, 0, ZLTextRegion.VideoFilter);
        if (videoRegion != null) {
            outlineRegion(videoRegion);
            myReader.getViewWidget().reset();
            myReader.getViewWidget().repaint();
            myReader.runAction(ActionCode.OPEN_VIDEO, (ZLTextVideoRegionSoul) videoRegion.getSoul());
            return;
        }

        final ZLTextHighlighting highlighting = findHighlighting(x, y, maxSelectionDistance());
        if (highlighting instanceof BookmarkHighlighting) {
            myReader.runAction(
                    ActionCode.SELECTION_BOOKMARK,
                    ((BookmarkHighlighting) highlighting).Bookmark
            );
            return;
        }

        if (myReader.isActionEnabled(ActionCode.HIDE_TOAST)) {
            myReader.runAction(ActionCode.HIDE_TOAST);
            return;
        }

        ZLTextRegion region = findRegion(x, y, maxSelectionDistance(), ZLTextRegion.AnyRegionFilter);
        if (region != null) {
            final ZLTextRegion.Soul soul = region.getSoul();
            if (soul instanceof ZLTextImageRegionSoul) { //y 如果选的是图片的
                String url = ((ZLTextImageRegionSoul) soul).ImageElement.URL;
                if (!OpenPhotoAction.isOpen) { // 防止多次点击
                    OpenPhotoAction.openImage(url, region.getLeft(), region.getTop(), region.getRight(), region.getBottom());
                    return;
                }
            }
        }
        onFingerSingleTapLastResort(x, y);
    }

    @Override
    public boolean isDoubleTapSupported() {
        return myReader.MiscOptions.EnableDoubleTap.getValue();
    }

    @Override
    public void onFingerDoubleTap(int x, int y) {
        myReader.runAction(ActionCode.HIDE_TOAST);

        myReader.runAction(getZoneMap().getActionByCoordinates(
                x, y, getContextWidth(), getContextHeight(), TapZoneMap.Tap.doubleTap
        ), x, y);
    }

    @Override
    public void onFingerPress(int x, int y) {
        Application.hideActivePopup(); //隐藏进度选择
//        Application.runAction(ActionCode.SELECTION_CLEAR);
        myReader.runAction(ActionCode.HIDE_TOAST);

        final float maxDist = ZLibrary.Instance().getDisplayDPI() / 4;
        final SelectionCursor.Which cursor = findSelectionCursor(x, y, maxDist * maxDist);
        if (cursor != null) {
            myReader.runAction(ActionCode.SELECTION_HIDE_PANEL);
            moveSelectionCursorTo(cursor, x, y);
            return;
        }

        if (myReader.MiscOptions.AllowScreenBrightnessAdjustment.getValue() && x < getContextWidth() / 10) {
            myIsBrightnessAdjustmentInProgress = true;
            myStartY = y;
            myStartBrightness = myReader.getViewWidget().getScreenBrightness();
            return;
        }
        startManualScrolling(x, y);
    }

    private boolean isFlickScrollingEnabled() {
        final PageTurningOptions.FingerScrollingType fingerScrolling = myReader.PageTurningOptions.FingerScrolling.getValue();
        return fingerScrolling == PageTurningOptions.FingerScrollingType.byFlick ||
                fingerScrolling == PageTurningOptions.FingerScrollingType.byTapAndFlick;
    }

    private void startManualScrolling(int x, int y) {
        if (!isFlickScrollingEnabled()) { // 是否支持拖动翻页
            return;
        }

        final boolean horizontal = myReader.PageTurningOptions.Horizontal.getValue();
        final ZLViewEnums.Direction direction = horizontal ? ZLViewEnums.Direction.rightToLeft : ZLViewEnums.Direction.up;
        myReader.getViewWidget().startManualScrolling(x, y, direction);
    }

    @Override
    public void onFingerMove(int x, int y) {
        final SelectionCursor.Which cursor = getSelectionCursorInMovement();
        if (cursor != null) {
            moveSelectionCursorTo(cursor, x, y);
            return;
        }

        synchronized (this) {
            if (myIsBrightnessAdjustmentInProgress) {
                if (x >= getContextWidth() / 5) {
                    myIsBrightnessAdjustmentInProgress = false;
                    startManualScrolling(x, y);
                } else {
                    final int delta = (myStartBrightness + 30) * (myStartY - y) / getContextHeight();
                    myReader.getViewWidget().setScreenBrightness(myStartBrightness + delta);
                    return;
                }
            }
            if (isFlickScrollingEnabled()) {
                myReader.getViewWidget().scrollManuallyTo(x, y);
            }
        }
    }

    @Override
    public void onFingerRelease(int x, int y) {
        final SelectionCursor.Which cursor = getSelectionCursorInMovement();
        if (cursor != null) {
            releaseSelectionCursor();
        } else if (myIsBrightnessAdjustmentInProgress) {
            myIsBrightnessAdjustmentInProgress = false;
        } else if (isFlickScrollingEnabled()) {
            myReader.getViewWidget().startAnimatedScrolling(
                    x, y, myReader.PageTurningOptions.AnimationSpeed.getValue()
            );
        }
    }


    @Override
    public boolean onFingerLongPress(int x, int y) {
        myReader.runAction(ActionCode.HIDE_TOAST);
        final ZLTextRegion region = findRegion(x, y, maxSelectionDistance(), ZLTextRegion.AnyRegionFilter);
        if (region != null) {
            final ZLTextRegion.Soul soul = region.getSoul();
            boolean doSelectRegion = false;
            if (soul instanceof ZLTextWordRegionSoul) {
                switch (myReader.MiscOptions.WordTappingAction.getValue()) {
                    case startSelecting:
                        myReader.runAction(ActionCode.SELECTION_HIDE_PANEL);
                        initSelection(x, y);
                        final SelectionCursor.Which cursor = findSelectionCursor(x, y);
                        if (cursor != null) {
                            moveSelectionCursorTo(cursor, x, y);
                        }
                        return true;
                    case selectSingleWord:
                    case openDictionary:
                        doSelectRegion = true;
                        break;
                }
//            } else if (soul instanceof ZLTextImageRegionSoul) { //y 长按选择图片
//                doSelectRegion =
//                        myReader.ImageOptions.TapAction.getValue() !=
//                                ImageOptions.TapActionEnum.doNothing;
            } else if (soul instanceof ZLTextHyperlinkRegionSoul) {
                doSelectRegion = true;
            }

            if (doSelectRegion) {
                outlineRegion(region);
                myReader.getViewWidget().reset();
                myReader.getViewWidget().repaint();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onFingerMoveAfterLongPress(int x, int y) {
        final SelectionCursor.Which cursor = getSelectionCursorInMovement();
        if (cursor != null) {
            moveSelectionCursorTo(cursor, x, y);
            return;
        }

        ZLTextRegion region = getOutlinedRegion();
        if (region != null) {
            ZLTextRegion.Soul soul = region.getSoul();
            if (soul instanceof ZLTextHyperlinkRegionSoul ||
                    soul instanceof ZLTextWordRegionSoul) {
                if (myReader.MiscOptions.WordTappingAction.getValue() !=
                        MiscOptions.WordTappingActionEnum.doNothing) {
                    region = findRegion(x, y, maxSelectionDistance(), ZLTextRegion.AnyRegionFilter);
                    if (region != null) {
                        soul = region.getSoul();
                        if (soul instanceof ZLTextHyperlinkRegionSoul
                                || soul instanceof ZLTextWordRegionSoul) {
                            outlineRegion(region);
                            myReader.getViewWidget().reset();
                            myReader.getViewWidget().repaint();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onFingerReleaseAfterLongPress(int x, int y) {
        final SelectionCursor.Which cursor = getSelectionCursorInMovement();
        if (cursor != null) {
            releaseSelectionCursor();
            return;
        }

        final ZLTextRegion region = getOutlinedRegion();
        if (region != null) {
            final ZLTextRegion.Soul soul = region.getSoul();

            boolean doRunAction = false;
            if (soul instanceof ZLTextWordRegionSoul) {
                doRunAction =
                        myReader.MiscOptions.WordTappingAction.getValue() ==
                                MiscOptions.WordTappingActionEnum.openDictionary;
            }
//            else if (soul instanceof ZLTextImageRegionSoul) {
//                doRunAction =
//                        myReader.ImageOptions.TapAction.getValue() ==
//                                ImageOptions.TapActionEnum.openImageView;
//            }

            if (doRunAction) {
                myReader.runAction(ActionCode.PROCESS_HYPERLINK);
            }
        }
    }

    @Override
    public void onFingerEventCancelled() {
        final SelectionCursor.Which cursor = getSelectionCursorInMovement();
        if (cursor != null) {
            releaseSelectionCursor();
        }
    }

    public boolean onTrackballRotated(int diffX, int diffY) {
        if (diffX == 0 && diffY == 0) {
            return true;
        }

        final ZLViewEnums.Direction direction = (diffY != 0) ?
                (diffY > 0 ? ZLViewEnums.Direction.down : ZLViewEnums.Direction.up) :
                (diffX > 0 ? ZLViewEnums.Direction.leftToRight : ZLViewEnums.Direction.rightToLeft);

        new MoveCursorAction(myReader, direction).run();
        return true;
    }

    @Override
    public ZLTextStyleCollection getTextStyleCollection() {
        return myViewOptions.getTextStyleCollection();
    }

    @Override
    public ImageFitting getImageFitting() {
        return myReader.ImageOptions.FitToScreen.getValue();
    }

    @Override
    public int getLeftMargin() {
        return myViewOptions.LeftMargin.getValue();
    }

    @Override
    public int getRightMargin() {
        return myViewOptions.RightMargin.getValue();
    }

    @Override
    public int getTopMargin() { // 顶部距离
        return (int) (ZLAndroidLibrary.Instance().getDPI() * 27);
    }

    @Override
    public int getBottomMargin() {
        return (int) (ZLAndroidLibrary.Instance().getDPI() * 27);
    }

    @Override
    public int getSpaceBetweenColumns() {
        return myViewOptions.SpaceBetweenColumns.getValue();
    }

    @Override
    public boolean twoColumnView() {
        return getContextHeight() <= getContextWidth() && myViewOptions.TwoColumnView.getValue();
    }

    @Override
    public ZLFile getWallpaperFile() {
        final String filePath = myViewOptions.getColorProfile().WallpaperOption.getValue();
        if ("".equals(filePath)) {
            return null;
        }
        final ZLFile file = ZLFile.createFileByPath(filePath);
        if (file == null || !file.exists()) {
            return null;
        }
        return file;
    }

    @Override
    public ZLPaintContext.FillMode getFillMode() {
        return getWallpaperFile() instanceof ZLResourceFile
                ? ZLPaintContext.FillMode.tileMirror
                : myViewOptions.getColorProfile().FillModeOption.getValue();
    }

    @Override
    public ZLColor getBackgroundColor() {
        return myViewOptions.getColorProfile().BackgroundOption.getValue();
    }

    @Override
    public ZLColor getSelectionBackgroundColor() {

        return myViewOptions.getColorProfile().SelectionBackgroundOption.getValue();
    }

    @Override
    public ZLColor getSelectionForegroundColor() {
        return myViewOptions.getColorProfile().SelectionForegroundOption.getValue();
    }

    @Override
    public ZLColor getTextColor(ZLTextHyperlink hyperlink) {
//        LogUtil.i("Hyperlink" + ":" + hyperlink.Type);

        final ColorProfile profile = myViewOptions.getColorProfile();
        switch (hyperlink.Type) {
            default:
            case FBHyperlinkType.NONE:
                return profile.RegularTextOption.getValue();
            case FBHyperlinkType.INTERNAL: //y 这里新版本改了
                return myReader.Collection.isHyperlinkVisited(myReader.getCurrentBook(), hyperlink.Id)
                        ? profile.VisitedHyperlinkTextOption.getValue()
                        : profile.HyperlinkTextOption.getValue();
        }
    }

    @Override
    public ZLColor getHighlightingBackgroundColor() {
        return myViewOptions.getColorProfile().HighlightingBackgroundOption.getValue();
    }

    @Override
    public ZLColor getHighlightingForegroundColor() {
        return myViewOptions.getColorProfile().HighlightingForegroundOption.getValue();
    }

//    private abstract class Footer implements ZLView.FooterArea {
//        private Runnable UpdateTask = new Runnable() {
//            public void run() {
//                myReader.getViewWidget().repaint();
//            }
//        };
//
//        protected ArrayList<TOCTree> myTOCMarks;
//        private int myMaxTOCMarksNumber = -1;
//
//        public int getHeight() {
//            return myViewOptions.FooterHeight.getValue();
//        }
//
//        public synchronized void resetTOCMarks() {
//            myTOCMarks = null;
//        }
//
//        protected synchronized void updateTOCMarks(BookModel model, int maxNumber) {
//            if (myTOCMarks != null && myMaxTOCMarksNumber == maxNumber) {
//                return;
//            }
//
//            myTOCMarks = new ArrayList<TOCTree>();
//            myMaxTOCMarksNumber = maxNumber;
//
//            TOCTree toc = model.TOCTree;
//            if (toc == null) {
//                return;
//            }
//            int maxLevel = Integer.MAX_VALUE;
//            if (toc.getSize() >= maxNumber) {
//                final int[] sizes = new int[10];
//                for (TOCTree tocItem : toc) {
//                    if (tocItem.Level < 10) {
//                        ++sizes[tocItem.Level];
//                    }
//                }
//                for (int i = 1; i < sizes.length; ++i) {
//                    sizes[i] += sizes[i - 1];
//                }
//                for (maxLevel = sizes.length - 1; maxLevel >= 0; --maxLevel) {
//                    if (sizes[maxLevel] < maxNumber) {
//                        break;
//                    }
//                }
//            }
//            for (TOCTree tocItem : toc.allSubtrees(maxLevel)) {
//                myTOCMarks.add(tocItem);
//            }
//        }
//
//        protected String buildInfoString(PagePosition pagePosition, String separator) {
//            final StringBuilder info = new StringBuilder();
//            final FooterOptions footerOptions = myViewOptions.getFooterOptions();
//
//            if (footerOptions.showProgressAsPages()) {
//                maybeAddSeparator(info, separator);
//                info.append(pagePosition.Current);
//                info.append("/");
//                info.append(pagePosition.Total);
//            }
//            if (footerOptions.showProgressAsPercentage() && pagePosition.Total != 0) {
//                maybeAddSeparator(info, separator);
//                info.append(String.valueOf(100 * pagePosition.Current / pagePosition.Total));
//                info.append("%");
//            }
//
//            if (footerOptions.ShowClock.getValue()) {
//                maybeAddSeparator(info, separator);
//                info.append(ZLibrary.Instance().getCurrentTimeString());
//            }
//            if (footerOptions.ShowBattery.getValue()) {
//                maybeAddSeparator(info, separator);
//                info.append(myReader.getBatteryLevel());
//                info.append("%");
//            }
//            return info.toString();
//        }
//
//        private void maybeAddSeparator(StringBuilder info, String separator) {
//            if (info.length() > 0) {
//                info.append(separator);
//            }
//        }
//
//        private List<FontEntry> myFontEntry;
//        private Map<String,Integer> myHeightMap = new HashMap<String,Integer>();
//        private Map<String,Integer> myCharHeightMap = new HashMap<String,Integer>();
//        protected synchronized int setFont(ZLPaintContext context, int height, boolean bold) {
//            final String family = myViewOptions.getFooterOptions().Font.getValue();
//            if (myFontEntry == null || !family.equals(myFontEntry.get(0).Family)) {
//                myFontEntry = Collections.singletonList(FontEntry.systemEntry(family));
//            }
//            final String key = family + (bold ? "N" : "B") + height;
//            final Integer cached = myHeightMap.get(key);
//            if (cached != null) {
//                context.setFont(myFontEntry, cached, bold, false, false, false);
//                final Integer charHeight = myCharHeightMap.get(key);
//                return charHeight != null ? charHeight : height;
//            } else {
//                int h = height + 2;
//                int charHeight = height;
//                final int max = height < 9 ? height - 1 : height - 2;
//                for (; h > 5; --h) {
//                    context.setFont(myFontEntry, h, bold, false, false, false);
//                    charHeight = context.getCharHeight('H');
//                    if (charHeight <= max) {
//                        break;
//                    }
//                }
//                myHeightMap.put(key, h);
//                myCharHeightMap.put(key, charHeight);
//                return charHeight;
//            }
//        }
//    }
//
//    private class FooterOldStyle extends Footer {
//        public synchronized void paint(ZLPaintContext context) {
//            final ZLFile wallpaper = getWallpaperFile();
//            if (wallpaper != null) {
//                context.clear(wallpaper, getFillMode());
//            } else {
//                context.clear(getBackgroundColor());
//            }
//
//            final BookModel model = myReader.Model;
//            if (model == null) {
//                return;
//            }
//
//            //final ZLColor bgColor = getBackgroundColor();
//            // TODO: separate color option for footer color
//            final ZLColor fgColor = getTextColor(ZLTextHyperlink.NO_LINK);
//            final ZLColor fillColor = myViewOptions.getColorProfile().FooterFillOption.getValue();
//
//            final int left = getLeftMargin();
//            final int right = context.getWidth() - getRightMargin();
//            final int height = getHeight();
//            final int lineWidth = height <= 10 ? 1 : 2;
//            final int delta = height <= 10 ? 0 : 1;
//            setFont(context, height, height > 10);
//
//            final PagePosition pagePosition = KooView.this.pagePosition();
//
//            // draw info text
//            final String infoString = buildInfoString(pagePosition, " ");
//            final int infoWidth = context.getStringWidth(infoString);
//            context.setTextColor(fgColor);
//            context.drawString(right - infoWidth, height - delta, infoString);
//
//            // draw gauge
//            final int gaugeRight = right - (infoWidth == 0 ? 0 : infoWidth + 10);
//            final int gaugeWidth = gaugeRight - left - 2 * lineWidth;
//
//            context.setLineColor(fgColor);
//            context.setLineWidth(lineWidth);
//            context.drawLine(left, lineWidth, left, height - lineWidth);
//            context.drawLine(left, height - lineWidth, gaugeRight, height - lineWidth);
//            context.drawLine(gaugeRight, height - lineWidth, gaugeRight, lineWidth);
//            context.drawLine(gaugeRight, lineWidth, left, lineWidth);
//
//            final int gaugeInternalRight =
//                    left + lineWidth + (int)(1.0 * gaugeWidth * pagePosition.Current / pagePosition.Total);
//
//            context.setFillColor(fillColor);
//            context.fillRectangle(left + 1, height - 2 * lineWidth, gaugeInternalRight, lineWidth + 1);
//
//            final FooterOptions footerOptions = myViewOptions.getFooterOptions();
//            if (footerOptions.ShowTOCMarks.getValue()) {
//                updateTOCMarks(model, footerOptions.MaxTOCMarks.getValue());
//                final int fullLength = sizeOfFullText();
//                for (TOCTree tocItem : myTOCMarks) {
//                    TOCTree.Reference reference = tocItem.getReference();
//                    if (reference != null) {
//                        final int refCoord = sizeOfTextBeforeParagraph(reference.ParagraphIndex);
//                        final int xCoord =
//                                left + 2 * lineWidth + (int)(1.0 * gaugeWidth * refCoord / fullLength);
//                        context.drawLine(xCoord, height - lineWidth, xCoord, lineWidth);
//                    }
//                }
//            }
//        }
//    }
//
//    private class FooterNewStyle extends Footer {
//        public synchronized void paint(ZLPaintContext context) {
//            final ColorProfile cProfile = myViewOptions.getColorProfile();
//            context.clear(cProfile.FooterNGBackgroundOption.getValue());
//
//            final BookModel model = myReader.Model;
//            if (model == null) {
//                return;
//            }
//
//            final ZLColor textColor = cProfile.FooterNGForegroundOption.getValue();
//            final ZLColor readColor = cProfile.FooterNGForegroundOption.getValue();
//            final ZLColor unreadColor = cProfile.FooterNGForegroundUnreadOption.getValue();
//
//            final int left = getLeftMargin();
//            final int right = context.getWidth() - getRightMargin();
//            final int height = getHeight();
//            final int lineWidth = height <= 12 ? 1 : 2;
//            final int charHeight = setFont(context, height, height > 12);
//
//            final PagePosition pagePosition = KooView.this.pagePosition();
//
//            // draw info text
//            final String infoString = buildInfoString(pagePosition, "  ");
//            final int infoWidth = context.getStringWidth(infoString);
//            context.setTextColor(textColor);
//            context.drawString(right - infoWidth, (height + charHeight + 1) / 2, infoString);
//
//            // draw gauge
//            final int gaugeRight = right - (infoWidth == 0 ? 0 : infoWidth + 10);
//            final int gaugeInternalRight =
//                    left + (int)(1.0 * (gaugeRight - left) * pagePosition.Current / pagePosition.Total + 0.5);
//            final int v = height / 2;
//
//            context.setLineWidth(lineWidth);
//            context.setLineColor(readColor);
//            context.drawLine(left, v, gaugeInternalRight, v);
//            if (gaugeInternalRight < gaugeRight) {
//                context.setLineColor(unreadColor);
//                context.drawLine(gaugeInternalRight + 1, v, gaugeRight, v);
//            }
//
//            // draw labels
//            final FooterOptions footerOptions = myViewOptions.getFooterOptions();
//            if (footerOptions.ShowTOCMarks.getValue()) {
//                final TreeSet<Integer> labels = new TreeSet<Integer>();
//                labels.add(left);
//                labels.add(gaugeRight);
//                updateTOCMarks(model, footerOptions.MaxTOCMarks.getValue());
//                final int fullLength = sizeOfFullText();
//                for (TOCTree tocItem : myTOCMarks) {
//                    TOCTree.Reference reference = tocItem.getReference();
//                    if (reference != null) {
//                        final int refCoord = sizeOfTextBeforeParagraph(reference.ParagraphIndex);
//                        labels.add(left + (int)(1.0 * (gaugeRight - left) * refCoord / fullLength + 0.5));
//                    }
//                }
//                for (int l : labels) {
//                    context.setLineColor(l <= gaugeInternalRight ? readColor : unreadColor);
//                    context.drawLine(l, v + 3, l, v - lineWidth - 2);
//                }
//            }
//        }
//    }
//
//    private Footer myFooter;

//    @Override
//    public Footer getFooterArea() {
//        switch (myViewOptions.ScrollbarType.getValue()) {
//            case SCROLLBAR_SHOW_AS_FOOTER:
//                if (!(myFooter instanceof FooterNewStyle)) {
//                    if (myFooter != null) {
//                        myReader.removeTimerTask(myFooter.UpdateTask);
//                    }
//                    myFooter = new FooterNewStyle();
//                    myReader.addTimerTask(myFooter.UpdateTask, 15000);
//                }
//                break;
//            case SCROLLBAR_SHOW_AS_FOOTER_OLD_STYLE:
//                if (!(myFooter instanceof FooterOldStyle)) {
//                    if (myFooter != null) {
//                        myReader.removeTimerTask(myFooter.UpdateTask);
//                    }
//                    myFooter = new FooterOldStyle();
//                    myReader.addTimerTask(myFooter.UpdateTask, 15000);
//                }
//                break;
//            default:
//                if (myFooter != null) {
//                    myReader.removeTimerTask(myFooter.UpdateTask);
//                    myFooter = null;
//                }
//                break;
//        }
//        return myFooter;
//    }

    @Override
    protected void releaseSelectionCursor() {
        super.releaseSelectionCursor();
        if (getCountOfSelectedWords() > 0) {
            myReader.runAction(ActionCode.SELECTION_SHOW_PANEL);
        }
    }

    public TextSnippet getSelectedSnippet() {
        final ZLTextPosition start = getSelectionStartPosition();
        final ZLTextPosition end = getSelectionEndPosition();
        if (start == null || end == null) {
            return null;
        }
        final TextBuildTraverser traverser = new TextBuildTraverser(this);
        traverser.traverse(start, end);
        return new FixedTextSnippet(start, end, traverser.getText());
    }

    public int getCountOfSelectedWords() {
        final WordCountTraverser traverser = new WordCountTraverser(this);
        if (!isSelectionEmpty()) {
            traverser.traverse(getSelectionStartPosition(), getSelectionEndPosition());
        }
        return traverser.getCount();
    }

    public static final int SCROLLBAR_SHOW_AS_FOOTER = 3;
//    public static final int SCROLLBAR_SHOW_AS_FOOTER_OLD_STYLE = 4;

    @Override
    public int scrollbarType() {
        return myViewOptions.ScrollbarType.getValue();
    }

    @Override
    public ZLViewEnums.Animation getAnimationType() {
        return myReader.PageTurningOptions.Animation.getValue();
    }

    //y 颜色变换时，图片背景色的变化
    @Override
    protected ZLPaintContext.ColorAdjustingMode getAdjustingModeForImages() {
        if (myReader.ImageOptions.MatchBackground.getValue()) {
            if (ColorProfile.DAY.equals(myViewOptions.getColorProfile().Name)) {
                return ZLPaintContext.ColorAdjustingMode.DARKEN_TO_BACKGROUND;
            } else {
                return ZLPaintContext.ColorAdjustingMode.LIGHTEN_TO_BACKGROUND;
            }
        } else {
            return ZLPaintContext.ColorAdjustingMode.NONE;
        }
    }

    @Override
    public synchronized void onScrollingFinished(ZLViewEnums.PageIndex pageIndex) {
        super.onScrollingFinished(pageIndex);
        myReader.storePosition(); // 进度保存
    }

    @Override
    protected ExtensionElementManager getExtensionManager() {
        return myBookElementManager;
    }
}
