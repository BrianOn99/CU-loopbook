package com.loopbook.cuhk_loopbook;

import java.util.*;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.apache.http.util.EncodingUtils;

import android.content.Intent; 
import android.content.Context;

import org.jsoup.nodes.Element;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    Menu myMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent myIntent = new Intent(this, Setting.class);
                startActivity(myIntent);
                break;
            case R.id.action_login:
                CatalogFragment.login(this);
                break;
            case R.id.action_refresh:
                BookFragment.getInstance().refresh();
                break;
        }
        return true;
    }

    private int iconIdOfTab(ActionBar.Tab tab) {
        int pos = tab.getPosition();
        return (pos == 0) ? R.id.action_refresh :
               (pos == 1) ? R.id.action_login : 0;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
        if (myMenu != null) myMenu.findItem(iconIdOfTab(tab)).setVisible(true);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if (myMenu != null) myMenu.findItem(iconIdOfTab(tab)).setVisible(false);
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a fragment (defined as a static inner class below).
            if (position == 0) { return new BookFragment(); }
            else if (position == 1) { return new CatalogFragment(); }
            else { throw new RuntimeException("Unknown tab number"); }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }

    public static class CatalogFragment extends Fragment {
        public static WebView currentView = null;
        public CatalogFragment() {};
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            WebView myWebView = (WebView) inflater.inflate(R.layout.fragment_catalog, container, false);
            myWebView.setWebViewClient(new WebViewClient());
            myWebView.getSettings().setJavaScriptEnabled(true);
            currentView = myWebView;

            return myWebView;
        }

        public static void login(Context c) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            String user_id = prefs.getString("user_id", "");
            String user_passwd = prefs.getString("user_passwd", "");
            String url = "https://m.library.cuhk.edu.hk/patroninfo";

            String postData = String.format("code=%s&pin=%s", user_id, user_passwd);
            currentView.postUrl(url, EncodingUtils.getBytes(postData, "BASE64"));
        }
    }

    public static class BookFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";
        private ArrayAdapter arrayAdapter;
        private ArrayList<String> books = new ArrayList<>();
        private static BookFragment instance;

        public BookFragment() {
            super();
            instance = this;
        }

        public static BookFragment getInstance() {
            return instance;
        }

        private class AsyncBookLoader extends AsyncTask<Context, String, ArrayList<String>> {
            private Exception caughtException = null;
            private Context context;

            @Override
            protected ArrayList<String> doInBackground(Context... context) {
                Element elm;
                this.context = context[0];
                String msg = LibConn.isConnectable() ? "connecting" : "No connection";
                publishProgress(msg);
                try {
                    elm = DataIO.getData(this.context);
                } catch (java.io.IOException | java.text.ParseException e) {
                    caughtException = e;
                    return null;
                }

                books.clear();
                for (Map<String, String> book: LibConn.getBooksFromElement(elm)) {
                    books.add(book.get("title") + "\n" + book.get("dueDate"));
                }

                return books;
            }

            @Override
            protected void onProgressUpdate(String... msgs) {
                Toast.makeText(this.context, msgs[0], Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onPostExecute(ArrayList<String> result) {
                if (caughtException != null) {
                    Toast.makeText(getActivity(),
                                   caughtException.getMessage(),
                                   Toast.LENGTH_SHORT).show();
                } else {
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        }
        
        public void refresh() {
            AsyncBookLoader bookLoader = new AsyncBookLoader();
            bookLoader.execute(getActivity());
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Retain this fragment across configuration changes.
            setRetainInstance(true);

            arrayAdapter = new ArrayAdapter(
                    getActivity(),
                    R.layout.list_item,
                    books);
            refresh();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            ListView lv = (ListView) rootView.findViewById(R.id.book_list);
            lv.setAdapter(arrayAdapter);

            return rootView;
        }
    }
}
