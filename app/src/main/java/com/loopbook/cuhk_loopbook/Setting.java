package com.loopbook.cuhk_loopbook;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.support.v7.widget.Toolbar;

public class Setting extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_bar);
        toolbar.setTitle("Setting");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        addPreferencesFromResource(R.layout.prefs);
    }
}
