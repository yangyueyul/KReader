package com.koolearn.android.kooreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koolearn.android.util.OrientationUtil;
import com.koolearn.android.util.ViewUtil;
import com.koolearn.klibrary.core.application.ZLApplication;
import com.koolearn.klibrary.core.tree.ZLTree;
import com.koolearn.klibrary.ui.android.R;
import com.koolearn.kooreader.bookmodel.TOCTree;
import com.koolearn.kooreader.kooreader.KooReaderApp;

import java.util.ArrayList;
import java.util.List;

public class TOCActivity extends Activity {
    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private LayoutInflater mInflater;
    private List<String> mTitleList = new ArrayList<>(); // 页卡标题集合
    private View view1, view2, view3; // 页卡视图
    private List<View> mViewList = new ArrayList<>(); // 页卡视图集合

    private TOCAdapter myAdapter;
    private ZLTree<?> mySelectedItem;
    private RelativeLayout rlLayout;
    private TextView tvBook;
    private RelativeLayout rlBookmark;
    private RelativeLayout rlNote;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.listview_toc);
        rlLayout = (RelativeLayout) findViewById(R.id.rl_shelf);
        tvBook = (TextView) findViewById(R.id.tv_book);
        mViewPager = (ViewPager) findViewById(R.id.vp_view);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mInflater = LayoutInflater.from(this);

        view1 = mInflater.inflate(R.layout.item_listview, null);
        ListView listView = (ListView) view1.findViewById(R.id.listview);
        final KooReaderApp kooreader = (KooReaderApp) ZLApplication.Instance();
        final TOCTree root = kooreader.Model.TOCTree;
        tvBook.setText(kooreader.getCurrentBook().getTitle());

        myAdapter = new TOCAdapter(listView, root);
        TOCTree treeToSelect = kooreader.getCurrentTOCElement();
        myAdapter.selectItem(treeToSelect); // 设置当前位置
        mySelectedItem = treeToSelect;

        view2 = mInflater.inflate(R.layout.list_bookmark, null);
        rlBookmark = (RelativeLayout) view2.findViewById(R.id.rl_bookmark);
        view3 = mInflater.inflate(R.layout.list_note, null);
        rlNote = (RelativeLayout) view3.findViewById(R.id.rl_note);

        /**
         * 设置背景与阅读背景一致
         */
        String bgValue = kooreader.ViewOptions.getColorProfile().WallpaperOption.getValue();
        switch (bgValue) {
            case "wallpapers/bg_green.png":
                listView.setBackgroundResource(R.drawable.bg_green);
                rlLayout.setBackgroundResource(R.drawable.bg_green);
                mTabLayout.setBackgroundResource(R.drawable.bg_green);
                rlBookmark.setBackgroundResource(R.drawable.bg_green);
                rlNote.setBackgroundResource(R.drawable.bg_green);
                break;
            case "wallpapers/bg_grey.png":
                listView.setBackgroundResource(R.drawable.bg_grey);
                rlLayout.setBackgroundResource(R.drawable.bg_grey);
                mTabLayout.setBackgroundResource(R.drawable.bg_grey);
                rlBookmark.setBackgroundResource(R.drawable.bg_grey);
                rlNote.setBackgroundResource(R.drawable.bg_grey);
                break;
            case "wallpapers/bg_night.png":
                listView.setBackgroundResource(R.drawable.bg_white);
                rlLayout.setBackgroundResource(R.drawable.bg_white);
                mTabLayout.setBackgroundResource(R.drawable.bg_white);
                rlBookmark.setBackgroundResource(R.drawable.bg_white);
                rlNote.setBackgroundResource(R.drawable.bg_white);
                break;
            case "wallpapers/bg_vine_grey.png":
                listView.setBackgroundResource(R.drawable.bg_vine_grey);
                rlLayout.setBackgroundResource(R.drawable.bg_vine_grey);
                mTabLayout.setBackgroundResource(R.drawable.bg_vine_grey);
                rlBookmark.setBackgroundResource(R.drawable.bg_vine_grey);
                rlNote.setBackgroundResource(R.drawable.bg_vine_grey);
                break;
            case "wallpapers/bg_vine_white.png":
                listView.setBackgroundResource(R.drawable.bg_vine_white);
                rlLayout.setBackgroundResource(R.drawable.bg_vine_white);
                mTabLayout.setBackgroundResource(R.drawable.bg_vine_white);
                rlBookmark.setBackgroundResource(R.drawable.bg_vine_white);
                rlNote.setBackgroundResource(R.drawable.bg_vine_white);
                break;
            case "wallpapers/bg_white.png":
                listView.setBackgroundResource(R.drawable.bg_white);
                rlLayout.setBackgroundResource(R.drawable.bg_white);
                mTabLayout.setBackgroundResource(R.drawable.bg_white);
                rlBookmark.setBackgroundResource(R.drawable.bg_white);
                rlNote.setBackgroundResource(R.drawable.bg_white);
                break;
        }

        //添加页卡视图
        mViewList.add(view1);
        mViewList.add(view2);
        mViewList.add(view3);
        //添加页卡标题
        mTitleList.add("目录");
        mTitleList.add("笔记");
        mTitleList.add("书签");

        mTabLayout.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
        mTabLayout.addTab(mTabLayout.newTab().setText(mTitleList.get(0)));//添加tab选项卡
        mTabLayout.addTab(mTabLayout.newTab().setText(mTitleList.get(1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mTitleList.get(2)));

        MyPagerAdapter mAdapter = new MyPagerAdapter(mViewList);
        mViewPager.setAdapter(mAdapter); // 给ViewPager设置适配器
        mTabLayout.setupWithViewPager(mViewPager); // 将TabLayout和ViewPager关联起来。
        mTabLayout.setTabsFromPagerAdapter(mAdapter); // 给Tabs设置适配器
    }

    @Override
    protected void onStart() {
        super.onStart();
        OrientationUtil.setOrientation(this, getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        OrientationUtil.setOrientation(this, intent);
    }

    private final class TOCAdapter extends ZLTreeAdapter {
        TOCAdapter(ListView listView, TOCTree root) {
            super(listView, root);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = (convertView != null) ? convertView : LayoutInflater.from(parent.getContext()).inflate(R.layout.toc_tree_item, parent, false);
            final TOCTree tree = (TOCTree) getItem(position);
            ViewUtil.findTextView(view, R.id.toc_tree_item_text).setText(tree.getText());
            return view;
        }

        void openBookText(TOCTree tree) {
            final TOCTree.Reference reference = tree.getReference();
            if (reference != null) {
                finish();
                final KooReaderApp kooreader = (KooReaderApp) ZLApplication.Instance();
                kooreader.addInvisibleBookmark();
                kooreader.BookTextView.gotoPosition(reference.ParagraphIndex, 0, 0);
                kooreader.showBookTextView();
                kooreader.storePosition();
            }
        }

        @Override
        protected boolean runTreeItem(ZLTree<?> tree) {
            if (super.runTreeItem(tree)) {
                return true;
            }
            openBookText((TOCTree) tree);
            return true;
        }
    }


    //ViewPager适配器
    class MyPagerAdapter extends PagerAdapter {
        private List<View> mViewList;

        public MyPagerAdapter(List<View> mViewList) {
            this.mViewList = mViewList;
        }

        @Override
        public int getCount() {
            return mViewList.size();//页卡数
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;//官方推荐写法
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewList.get(position));//添加页卡
            return mViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position));//删除页卡
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);//页卡标题
        }

    }
}
