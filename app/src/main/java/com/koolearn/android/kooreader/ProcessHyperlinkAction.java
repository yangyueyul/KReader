package com.koolearn.android.kooreader;

import com.koolearn.android.util.LogUtil;
import com.koolearn.klibrary.text.view.ZLTextHyperlink;
import com.koolearn.klibrary.text.view.ZLTextHyperlinkRegionSoul;
import com.koolearn.klibrary.text.view.ZLTextRegion;
import com.koolearn.kooreader.bookmodel.FBHyperlinkType;
import com.koolearn.kooreader.kooreader.KooReaderApp;
import com.koolearn.kooreader.util.AutoTextSnippet;

class ProcessHyperlinkAction extends KooAndroidAction {
    ProcessHyperlinkAction(KooReader baseActivity, KooReaderApp kooreader) {
        super(baseActivity, kooreader);
    }

    @Override
    public boolean isEnabled() {
        return Reader.getTextView().getOutlinedRegion() != null;
    }

    @Override
    protected void run(Object... params) {
        final ZLTextRegion region = Reader.getTextView().getOutlinedRegion();
        if (region == null) {
            return;
        }

        final ZLTextRegion.Soul soul = region.getSoul();
        if (soul instanceof ZLTextHyperlinkRegionSoul) {
            Reader.getTextView().hideOutline();
            Reader.getViewWidget().repaint();
            final ZLTextHyperlink hyperlink = ((ZLTextHyperlinkRegionSoul) soul).Hyperlink;
            switch (hyperlink.Type) {
                case FBHyperlinkType.INTERNAL: {
                    LogUtil.i("FOOTNOTE");
                    final AutoTextSnippet snippet = Reader.getFootnoteData(hyperlink.Id);
                    if (snippet == null) {
                        break;
                    }
                    LogUtil.i("FOOTNOTE");
                    Reader.Collection.markHyperlinkAsVisited(Reader.getCurrentBook(), hyperlink.Id);
                    Reader.tryOpenFootnote(hyperlink.Id);
                    break;
                }
            }
        }
//        else if (soul instanceof ZLTextImageRegionSoul) {
//
//            Reader.getTextView().hideOutline();
//            Reader.getViewWidget().repaint();
//            final String url = ((ZLTextImageRegionSoul) soul).ImageElement.URL;
//            if (url != null) {
//                try {
//                    final Intent intent = new Intent();
//                    intent.setClass(BaseActivity, PhotoViewActivity.class);
//                    LogUtil.i5("url:" + url);
//                    intent.putExtra(PhotoViewActivity.URL_KEY, url);
////                    intent.putExtra(
////                            PhotoViewActivity.BACKGROUND_COLOR_KEY,
////                            Reader.ImageOptions.ImageViewBackground.getValue().intValue()
////                    );
//                    BaseActivity.startActivity(intent);
//                    BaseActivity.overridePendingTransition(R.anim.tran_fade_in_long, R.anim.tran_fade_out_long);
////                    BaseActivity.overridePendingTransition(R.anim.tran_fade_in_long, R.anim.tran_fade_out_long);
////                    BaseActivity.overridePendingTransition(R.anim.tran_fade_in, R.anim.tran_fade_out);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }
}
