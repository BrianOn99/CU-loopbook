package com.loopbook.cuhk_loopbook;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.Preference;
import android.view.View;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

public class Setting extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_bar);
        toolbar.setTitle(getString(R.string.activity_setting));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addPreferencesFromResource(R.layout.prefs);
        findPreference("alert_days").setOnPreferenceChangeListener(
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference pref, Object value) {
                    int days = Integer.parseInt((String)value);
                    if (days > 0 && days < (BuildConfig.DEBUG ? 15 : 5)) {
                        return true;
                    } else {
                        Toast.makeText(getApplicationContext(), "invalid day", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            }
        );
    }
}
