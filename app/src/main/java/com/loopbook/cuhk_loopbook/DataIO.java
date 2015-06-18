package com.loopbook.cuhk_loopbook;

import java.util.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.Jsoup; 

public class DataIO {
    static String filename = "books.html";

    public static Element getData(Context context)
                          throws IOException, java.text.ParseException {
        return LibConn.isConnectable(context) ?
            refreshStoredData(context) :
            getStoredData(context);
    }

    public static Element refreshStoredData(Context c) 
                          throws IOException, java.text.ParseException {
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
        try (BufferedOutputStream out = new BufferedOutputStream(
                    c.openFileOutput(filename, Context.MODE_PRIVATE))) {
            out.write(elm.outerHtml().getBytes());
            out.close();
        } catch (IOException e) {
        }
    }

    public static Element getStoredData(Context c) {
        try (BufferedInputStream in = new BufferedInputStream(
                    c.openFileInput(filename))) {
            Element elm = Jsoup.parse(in, "UTF-8", "")
                               .select("table.patFunc").first();
            return elm;
        } catch (IOException e) {
            return new Element(Tag.valueOf("table"), "").classNames(
                           new HashSet(Arrays.asList("patFunc"))
                       );
        }
    }
}
