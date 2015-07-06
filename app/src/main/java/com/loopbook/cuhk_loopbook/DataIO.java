package com.loopbook.cuhk_loopbook;

import java.util.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup; 

public class DataIO {
    static String filename = "books.html";

    public static ArrayList<LibConn.Book> getBooks(Context context)
                          throws IOException, java.text.ParseException {
        return LibConn.isConnectable(context) ?
            refreshStoredBooks(context) :
            getStoredBooks(context);
    }

    public static ArrayList<LibConn.Book> refreshStoredBooks(Context c) 
                          throws IOException, java.text.ParseException {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        String user_id = prefs.getString("user_id", "");
        String user_passwd = prefs.getString("user_passwd", "");
        LibConn myLib = new LibConn(user_id, user_passwd);
        Element elm;
        try {
            myLib.login();
            elm = myLib.getBooksElement();
        } catch (LibConn.NoBooksError e) {
            /* Need to make & save an fake element, so that no notification popup */
            elm = LibConn.newBooksElement();
        }

        saveData(c, elm);
        return LibConn.getBooksFromElement(elm);
    }

    public static void saveData(Context c, Element elm) {
        try (BufferedOutputStream out = new BufferedOutputStream(
                    c.openFileOutput(filename, Context.MODE_PRIVATE))) {
            out.write(elm.outerHtml().getBytes());
            out.close();
        } catch (IOException e) {
        }
    }

    public static ArrayList<LibConn.Book> getStoredBooks(Context c) {
        try (BufferedInputStream in = new BufferedInputStream(
                    c.openFileInput(filename))) {
            Element elm = Jsoup.parse(in, "UTF-8", "")
                               .select("table.patFunc").first();
            return LibConn.getBooksFromElement(elm);
        } catch (IOException e) {
            return new ArrayList<LibConn.Book>();
        }
    }
}
