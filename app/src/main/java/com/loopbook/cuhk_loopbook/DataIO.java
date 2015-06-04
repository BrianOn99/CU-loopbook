package com.loopbook.cuhk_loopbook;

import java.util.*;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.Jsoup; 

import com.loopbook.cuhk_loopbook.LibConn;

public class DataIO {
    static String filename = "books.html";

    public static Element refreshStoredData(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String user_id = prefs.getString("user_id", "");
        String user_passwd = prefs.getString("user_passwd", "");
        LibConn myLib = new LibConn(user_id, user_passwd);
        myLib.login();
        Element elm = myLib.getBooksElement();

        saveData(c, elm);
        return elm;
    }

    public static void saveData(Context c, Element elm) {
        FileOutputStream out;

        try {
            out = c.openFileOutput(filename, Context.MODE_PRIVATE);
            out.write(elm.outerHtml().getBytes());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Element getStoredData(Context c) {
        InputStream in;

        try {
            in = new BufferedInputStream(c.openFileInput(filename));
            Element elm = Jsoup.parse(in, "UTF-8", "")
                               .select("table.patFunc").first();
            in.close();
            return elm;
        } catch (Exception e) {
            return new Element(Tag.valueOf("table"), "").classNames(
                           new HashSet(Arrays.asList("patFunc"))
                       );
        }
    }
}
