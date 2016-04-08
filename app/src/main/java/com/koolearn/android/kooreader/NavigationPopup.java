package com.koolearn.android.kooreader;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.koolearn.android.kooreader.api.KooReaderIntents;
import com.koolearn.android.util.OrientationUtil;
import com.koolearn.klibrary.core.application.ZLApplication;
import com.koolearn.klibrary.text.view.ZLTextView;
import com.koolearn.klibrary.text.view.ZLTextWordCursor;
import com.koolearn.klibrary.ui.android.R;
import com.koolearn.kooreader.bookmodel.TOCTree;
import com.koolearn.kooreader.kooreader.KooReaderApp;
import com.koolearn.kooreader.kooreader.options.ColorProfile;

final class NavigationPopup extends ZLApplication.PopupPanel {
    final static String ID = "NavigationPopup";

    private volatile NavigationWindow myWindow;
    private volatile KooReader myActivity;
    private volatile RelativeLayout myRoot;
    private ZLTextWordCursor myStartPosition;
    private final KooReaderApp myKooReader;
    private volatile boolean myIsInProgress;
    private ZLTextView.PagePosition pagePosition;
    private TextView light;
    private TextView dark;

    NavigationPopup(KooReaderApp kooReader) {
        super(kooReader);
        myKooReader = kooReader;
    }

    public void setPanelInfo(KooReader activity, RelativeLayout root) {
        myActivity = activity;
        myRoot = root;
    }

    public void runNavigation() {
        if (myWindow == null || myWindow.getVisibility() == View.GONE) {
            myIsInProgress = false;
            Application.showPopup(ID);
        }
    }

    @Override
    protected void show_() {
        setStatusBarVisibility(true);
        if (myActivity != null) {
            createPanel(myActivity, myRoot);
        }
        if (myWindow != null) {
            myWindow.show();
            setupNavigation();
        }
    }

    @Override
    protected void hide_() {
        setStatusBarVisibility(false);
        if (myWindow != null) {
            myWindow.hide();
        }
    }

    private void setStatusBarVisibility(boolean visible) {
        if (visible) {
            myActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN); // 设置状态栏
        } else {
            myActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected void update() {
        if (!myIsInProgress && myWindow != null) {
            setupNavigation();
        }
    }

    private void gotoPage(int page) {
        final ZLTextView view = myKooReader.getTextView();
        if (page == 1) {
            view.gotoHome();
        } else {
            view.gotoPage(page);
        }
//        myKooReader.clearTextCaches();
        myKooReader.getViewWidget().reset();
        myKooReader.getViewWidget().repaint();
    }

    private void createPanel(KooReader activity, RelativeLayout root) {
        if (myWindow != null && activity == myWindow.getContext()) {
            return;
        }

        activity.getLayoutInflater().inflate(R.layout.navigation_panel, root);
        myWindow = (NavigationWindow) root.findViewById(R.id.navigation_panel);

        final SeekBar slider = (SeekBar) myWindow.findViewById(R.id.navigation_slider);
        final TextView text = (TextView) myWindow.findViewById(R.id.navigation_text);
        final TextView toc = (TextView) myWindow.findViewById(R.id.navigation_toc);
        final TextView fonts = (TextView) myWindow.findViewById(R.id.navigation_fonts);
        light = (TextView) myWindow.findViewById(R.id.navigation_light);
        dark = (TextView) myWindow.findViewById(R.id.navigation_dark);
        final TextView pre_character = (TextView) myWindow.findViewById(R.id.pre_character);
        final TextView next_character = (TextView) myWindow.findViewById(R.id.next_character);

        toc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Application.hideActivePopup();
                final Intent intent =
                        new Intent(myActivity.getApplicationContext(), TOCActivity.class);
                KooReaderIntents.putBookExtra(intent, myKooReader.getCurrentBook());
                KooReaderIntents.putBookmarkExtra(intent, myKooReader.createBookmark(80, true));
                OrientationUtil.startActivity(myActivity, intent);
            }
        });

        fonts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Application.hideActivePopup();
                ((SettingPopup) myKooReader.getPopupById(SettingPopup.ID)).runNavigation();
            }
        });

        dark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myKooReader.ViewOptions.ColorProfileName.setValue(ColorProfile.NIGHT);
                myKooReader.getViewWidget().reset();
                myKooReader.getViewWidget().repaint();
                light.setVisibility(View.VISIBLE);
                dark.setVisibility(View.INVISIBLE);
            }
        });

        light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dark.setVisibility(View.VISIBLE);
                light.setVisibility(View.INVISIBLE);
                myKooReader.ViewOptions.ColorProfileName.setValue(ColorProfile.DAY);
                myKooReader.getViewWidget().reset();
                myKooReader.getViewWidget().repaint();
            }
        });

        pre_character.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoPage(pagePosition.Current - 30);
            }
        });

        next_character.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                textView.getModel().getParagraphsNumber();
                gotoPage(pagePosition.Current + 30);
            }
        });


        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private void gotoPage(int page) {
                final ZLTextView view = myKooReader.getTextView();
                if (page == 1) {
                    view.gotoHome();
                } else {
                    view.gotoPage(page);
                }
            }

            private void gotoPagePer(int page) {
                final ZLTextView view = myKooReader.getTextView();
//                if (page == 0) {
//                    view.gotoHome();
//                } else {
                view.gotoPageByPec(page);
//                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                myIsInProgress = true;
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                myKooReader.getViewWidget().reset();
                myKooReader.getViewWidget().repaint();
                myIsInProgress = false;
                //y 松手直接进行跳转
//                final ZLTextWordCursor position = myStartPosition; // 返回到起始位置
                if (myStartPosition != null &&
                        !myStartPosition.equals(myKooReader.getTextView().getStartCursor())) {
                    myKooReader.addInvisibleBookmark(myStartPosition);
                    myKooReader.storePosition();
                }
                myStartPosition = null;
//                myKooReader.clearTextCaches();
//                myKooReader.getViewWidget().reset();
//                myKooReader.getViewWidget().repaint();
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
//                    final int page = progress + 1;
//                    final int pagesNumber = seekBar.getMax() + 1;
//                    gotoPage(page);
                    gotoPagePer(progress);
                    text.setText(makeProgressTextPer(myKooReader.getTextView().pagePositionPec()));
//                    text.setText(makeProgressText(page, pagesNumber));
                }
            }
        });
    }

    private void setupNavigation() {
        final SeekBar slider = (SeekBar) myWindow.findViewById(R.id.navigation_slider);
        final TextView text = (TextView) myWindow.findViewById(R.id.navigation_text);

        final ZLTextView textView = myKooReader.getTextView();
        pagePosition = textView.pagePosition();

        String progress = textView.pagePositionPec();

//        if (slider.getMax() != pagePosition.Total - 1 || slider.getProgress() != pagePosition.Current - 1) {
//            slider.setMax(pagePosition.Total - 1);
//            slider.setProgress(pagePosition.Current - 1);
        slider.setMax(textView.pagePosition2());
        slider.setProgress(textView.pagePosition1());
        text.setText(makeProgressTextPer(progress));
//            text.setText(makeProgressText(pagePosition.Current, pagePosition.Total));
//    }

    }

    private String makeProgressText(int page, int pagesNumber) {
        final StringBuilder builder = new StringBuilder();
        builder.append(page);
        builder.append("/");
        builder.append(pagesNumber);
        final TOCTree tocElement = myKooReader.getCurrentTOCElement();
        if (tocElement != null) {
            builder.append("  ");
            builder.append(tocElement.getText());
        }

        if (myKooReader.ViewOptions.ColorProfileName.getValue().equals(ColorProfile.DAY)) {
            dark.setVisibility(View.VISIBLE);
        } else {
            light.setVisibility(View.VISIBLE);
        }

        return builder.toString();
    }

    private String makeProgressTextPer(String progress) {
        final StringBuilder builder = new StringBuilder();
        builder.append(progress);
        final TOCTree tocElement = myKooReader.getCurrentTOCElement();
        if (tocElement != null) {
            builder.append("  ");
            builder.append(tocElement.getText());
        }

        if (myKooReader.ViewOptions.ColorProfileName.getValue().equals(ColorProfile.DAY)) {
            dark.setVisibility(View.VISIBLE);
        } else {
            light.setVisibility(View.VISIBLE);
        }

        return builder.toString();
    }

    final void removeWindow(Activity activity) {
        if (myWindow != null && activity == myWindow.getContext()) {
            final ViewGroup root = (ViewGroup) myWindow.getParent();
            myWindow.hide();
            root.removeView(myWindow);
            myWindow = null;
        }
    }
}