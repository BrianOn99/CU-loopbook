package com.loopbook.cuhk_loopbook;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Setting extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.prefs);
    }
}
