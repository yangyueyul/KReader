package com.koolearn.android.kooreader.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.koolearn.klibrary.text.view.style.ZLTextBaseStyle;
import com.koolearn.klibrary.text.view.style.ZLTextNGStyleDescription;
import com.koolearn.klibrary.text.view.style.ZLTextStyleCollection;
import com.koolearn.klibrary.ui.android.R;
import com.koolearn.kooreader.kooreader.options.ViewOptions;

public class PreferenceActivity extends ZLPreferenceActivity {
    public PreferenceActivity() {
        super("Preferences");
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getListView().setPadding(0, 0, 0, 0); // 666
        View view = View.inflate(this, R.layout.setting_head, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        getListView().addHeaderView(view);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
    }

    @Override
    protected void init(Intent intent) {
        final ViewOptions viewOptions = new ViewOptions();
        final ZLTextStyleCollection collection = viewOptions.getTextStyleCollection();
//		 TODO: use user-defined locale, not the default one,
        final ZLTextBaseStyle baseStyle = collection.getBaseStyle();

        addPreference(new FontPreference( //y 字体
                this, Resource.getResource("text"),
                baseStyle.FontFamilyOption, false
        ));
        addPreference(new FontStylePreference( //y 字型
                this, Resource.getResource("fontStyle"),
                baseStyle.BoldOption, baseStyle.ItalicOption
        ));

        ZLTextNGStyleDescription description = collection.getDescriptionList().get(1);
        addPreference(new StringPreference(
                this, description.TextIndentOption, // 首行缩进
                StringPreference.Constraint.LENGTH,
                Resource, "firstLineIndent"
        ));
        addPreference(new StringPreference(
                this, description.MarginLeftOption,
                StringPreference.Constraint.LENGTH,
                Resource, "leftIndent"
        ));
        addPreference(new StringPreference(
                this, description.MarginRightOption,
                StringPreference.Constraint.LENGTH,
                Resource, "rightIndent"
        ));
        addPreference(new StringPreference(
                this, description.MarginTopOption, // 段前距
                StringPreference.Constraint.LENGTH,
                Resource, "spaceBefore"
        ));
        addPreference(new StringPreference( // 段后距
                this, description.MarginBottomOption,
                StringPreference.Constraint.LENGTH,
                Resource, "spaceAfter"
        ));

        final Screen cssScreen = createPreferenceScreen("css"); // 使用CSS样式
        cssScreen.addOption(baseStyle.UseCSSFontFamilyOption, "fontFamily");
        cssScreen.addOption(baseStyle.UseCSSFontSizeOption, "fontSize");
        cssScreen.addOption(baseStyle.UseCSSTextAlignmentOption, "textAlignment");
        cssScreen.addOption(baseStyle.UseCSSMarginsOption, "margins");
    }
}