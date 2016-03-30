package com.koolearn.android.kooreader.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.koolearn.klibrary.ui.android.R;

/**
 * ******************************************
 * 作    者 ：  杨越
 * 版    本 ：  1.0
 * 创建日期 ：  2016/3/30
 * 描    述 ：
 * 修订历史 ：
 * ******************************************
 */

public class BookNoteFragment extends Fragment {
    private RelativeLayout rlNote;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_note, null);
        rlNote = (RelativeLayout) view.findViewById(R.id.rl_bookmark);
        return view;
    }
}