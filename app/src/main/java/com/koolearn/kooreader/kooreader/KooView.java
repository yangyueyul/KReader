package com.koolearn.kooreader.kooreader;

import com.koolearn.android.kooreader.OpenPhotoAction;
import com.koolearn.android.util.LogUtil;
import com.koolearn.klibrary.core.filesystem.ZLFile;
import com.koolearn.klibrary.core.filesystem.ZLResourceFile;
import com.koolearn.klibrary.core.util.ZLColor;
import com.koolearn.klibrary.core.view.ZLPaintContext;
import com.koolearn.klibrary.core.view.ZLViewEnums;
import com.koolearn.klibrary.text.model.ZLTextModel;
import com.koolearn.klibrary.text.view.ExtensionElementManager;
import com.koolearn.klibrary.text.view.ZLTextHyperlink;
import com.koolearn.klibrary.text.view.ZLTextImageRegionSoul;
import com.koolearn.klibrary.text.view.ZLTextRegion;
import com.koolearn.klibrary.text.view.ZLTextView;
import com.koolearn.klibrary.text.view.style.ZLTextStyleCollection;
import com.koolearn.klibrary.ui.android.library.ZLAndroidLibrary;
import com.koolearn.kooreader.bookmodel.FBHyperlinkType;
import com.koolearn.kooreader.kooreader.options.ColorProfile;
import com.koolearn.kooreader.kooreader.options.PageTurningOptions;
import com.koolearn.kooreader.kooreader.options.ViewOptions;

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
        LogUtil.i20("SingleTap:" + getZoneMap().getActionByCoordinates(x, y, getContextWidth(), getContextHeight(), isDoubleTapSupported() ? TapZoneMap.Tap.singleNotDoubleTap : TapZoneMap.Tap.singleTap));
        myReader.runAction(getZoneMap().getActionByCoordinates( //y 运行功能   根据x,y,w,h,tap判断
                x, y, getContextWidth(), getContextHeight(),
                isDoubleTapSupported() ? TapZoneMap.Tap.singleNotDoubleTap : TapZoneMap.Tap.singleTap
        ), x, y); //y 传入actionId,x,y的值 进行功能显示
    }

    @Override
    public void onFingerSingleTap(int x, int y) {
        Application.hideActivePopup(); // 隐藏popup
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
    public void onFingerDoubleTap(int x, int y) {

    }

    @Override
    public void onFingerPress(int x, int y) {
        Application.hideActivePopup(); //隐藏进度选择
        if (x < getContextWidth() / 10) { // 在屏幕左边1/10区域内
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
        LogUtil.i5("onFingerRelease");
        if (myIsBrightnessAdjustmentInProgress) {
            myIsBrightnessAdjustmentInProgress = false;
        } else if (isFlickScrollingEnabled()) {
            myReader.getViewWidget().startAnimatedScrolling(
                    x, y, myReader.PageTurningOptions.AnimationSpeed.getValue()
            );
        }
    }


    @Override
    public boolean onFingerLongPress(int x, int y) {
        return false;
    }

    @Override
    public void onFingerMoveAfterLongPress(int x, int y) {

    }

    @Override
    public void onFingerReleaseAfterLongPress(int x, int y) {
    }

    @Override
    public void onFingerEventCancelled() {
    }

    public boolean onTrackballRotated(int diffX, int diffY) {
        if (diffX == 0 && diffY == 0) {
            return true;
        }
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

    public static final int SCROLLBAR_SHOW_AS_FOOTER = 3;

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
