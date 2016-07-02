package com.loopbook.cuhk_loopbook;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/*
 * Store and get Cookies in SharedPreference
 * There is java.net.CookieManager, and android.webkit(sth.).CookieManager
 * But they are not compatible with Jsoup cookies().  Write one from raw also
 * has more customization freedom (forget RFC2109)
 */
public class CookieMonster {
    static final String FIELD_BEST_BEFORE = "best_before";
    static final String FIELD_COOKIE = "cookie";
    static final String FIELD_ADDITIONAL = "additional";
    private SharedPreferences prefs;

    public CookieMonster(Context ctx) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void put(int secLifeTime, String identifier, Map<String, String> cookies, String additional) {
        JSONObject j = new JSONObject();
        long best_before = Calendar.getInstance().getTimeInMillis() + secLifeTime*1000;
        try {
            j.put(FIELD_BEST_BEFORE, best_before);
            j.put(FIELD_COOKIE, new JSONObject(cookies));
            if (additional != null)
                j.put(FIELD_ADDITIONAL, additional);
            SharedPreferences.Editor editor = this.prefs.edit();
            editor.putString(identifier, j.toString());
            editor.commit();
        } catch (JSONException e) {
            Log.d("cookieMon","json put exception");
        }
    }

    /*
     * return null if cookies expired or not exist
     */
    public Pair<Map<String, String>,String> get(String identifier) {
        String rec = this.prefs.getString(identifier, "");
        if (rec.equals("")) {
            return null;
        }

        Map<String, String> outMap = new HashMap<>();
        String additional = "";
        try {
            JSONObject j = new JSONObject(rec);
            if (j.getLong(FIELD_BEST_BEFORE) < Calendar.getInstance().getTimeInMillis()) {
                Log.d("cookieMon" , j.getLong(FIELD_BEST_BEFORE)+ "expired cookie");
                return null;
            }

            JSONObject j_cookies = j.getJSONObject(FIELD_COOKIE);
            Iterator<String> keys = j_cookies.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                outMap.put(key, j_cookies.getString(key));
            }

            additional = j.optString(FIELD_ADDITIONAL);
        } catch (JSONException e) {
            Log.d("cookieMon","json get exception");
            return null;  /* not intended to reach here */
        }
        Log.d("cookieMon","get cached cookie");
        return new Pair<Map<String, String>,String>(outMap, additional);
    }
}
