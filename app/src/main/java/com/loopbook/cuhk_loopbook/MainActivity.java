package com.loopbook.cuhk_loopbook;

import java.util.*;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.net.Uri;
import org.apache.http.util.EncodingUtils;

import android.content.Intent; 
import android.content.Context;

import org.jsoup.nodes.Element;


public class MainActivity extends ActionBarActivity {

    private Menu myMenu;
    private boolean renew = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getStringExtra("renew") != null) { renew = true; }

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_placeholder, new BookFragment())
                .commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null && isFirstRun()) {
            if (BuildInfo.DEBUG)
                Toast.makeText(this, "firstrun", Toast.LENGTH_SHORT).show();
            setRunned();
            CheckSched.scheduleNotification(this);
        }
    }

    public boolean isFirstRun() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("firstrun", true);
    }

    public void setRunned() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstrun", false);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        myMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent myIntent1 = new Intent(this, Setting.class);
                startActivity(myIntent1);
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
