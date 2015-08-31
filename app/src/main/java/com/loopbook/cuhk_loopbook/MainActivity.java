package com.loopbook.cuhk_loopbook;

import java.util.*;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.net.Uri;
import org.apache.http.util.EncodingUtils;

import android.content.Intent; 
import android.content.Context;


public class MainActivity extends ActionBarActivity {

    // private boolean renew = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // if (getIntent().getStringExtra("renew") != null) { renew = true; }

        boolean firstRun = (savedInstanceState == null && isFirstRun(this));

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            BookFragment bookFrag = new BookFragment();
            if (firstRun) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("firstRun", true);
                bookFrag.setArguments(bundle);
            }

            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder, bookFrag)
                .commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (firstRun) {
            setRunned();
            CheckSched.scheduleNotification(this);
            Toast.makeText(this, getString(R.string.first_run_msg), Toast.LENGTH_LONG).show();
            Intent myIntent1 = new Intent(this, Setting.class);
            startActivityForResult(myIntent1, 1);
        }
    }

    public static boolean isFirstRun(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean("firstrun", true);
    }

    public void setRunned() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstrun", false);
        editor.commit();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        BookFragment.getInstance().refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent myIntent1 = new Intent(this, Setting.class);
                startActivityForResult(myIntent1, 1);
                break;
            case R.id.action_renew:
                /* after many despairing attempt to open chrome with POST
                 * method, I found that I can log in with GET method!!!
                 * be aware that this is less secure, but nobody cares
                 */
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String user_id = prefs.getString("user_id", "");
                String user_passwd = prefs.getString("user_passwd", "");
                String url = "https://m.library.cuhk.edu.hk/patroninfo?code=%s&pin=%s";
                url = String.format(url, user_id, user_passwd);
                Intent myIntent2 = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(myIntent2);
                break;
            case R.id.action_refresh:
                BookFragment.getInstance().refresh();
                break;
        }
        return true;
    }
}
