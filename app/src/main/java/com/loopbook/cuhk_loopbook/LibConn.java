package com.loopbook.cuhk_loopbook;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Pair;

import java.text.SimpleDateFormat;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.*;

public class LibConn {
    private final String name;
    private final String passwd;
    private CookieMonster cookieMonster;
    private String bookhref;
    private static final String COOKIE_IDENTIFIER = "cuhk_lib_cookie";
    private static final int COOKIE_LIFE = 180; /* 180sec == 3 minutes */

    public static class Book {
        public String name;
        public Calendar dueDate;
        /* following 2 are used when renew */
        public String html_form_name;  /* something like renew2 at the time of writing */
        public String html_form_value;  /* somthing like i4524617 */

        public Book(String name, Calendar dueDate,
                String html_form_name, String html_form_value) {
            this.name = name;
            this.dueDate = dueDate;
            this.html_form_name = html_form_name;
            this.html_form_value = html_form_value;
        }

        public int remainDays() {
            long diff = this.dueDate.getTimeInMillis()
                       - Calendar.getInstance().getTimeInMillis();
            return (int)(diff/(1000*60*60*24));
        }
    }

    public static class NoBooksError extends RuntimeException {
        public NoBooksError(String msg) { super(msg); }
    };

    public LibConn(String name, String passwd, CookieMonster cookieMonster) {
        this.name = name;
        this.passwd = passwd;
        this.cookieMonster = cookieMonster;
    }

    public static boolean isConnectable(Context context) {
        ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean connectable =  netInfo != null && netInfo.isConnectedOrConnecting();
        if (connectable) Log.i("Libconn", "connectable");
        else Log.i("Libconn", "unconnectable");
        return connectable;
    }

    /* ========================
     * Section: Sending Request
     * ======================== */
    public Connection.Response login()
            throws java.io.IOException, java.text.ParseException {
        Connection conn = Jsoup.connect("https://m.library.cuhk.edu.hk/patroninfo")
                               .data("code", this.name, "pin", this.passwd)
                               .method(Connection.Method.POST);

        Connection.Response resp = connectWithRetry(conn);
        Document doc = resp.parse();
        Log.i("Libconn", "HTTP and parse ok");

        Elements succElm = doc.getElementsByClass("loggedInMessage");
        if (succElm.size() == 0) {
            //Log.e("Libconn", "fail login\n" + doc.body().html());
            throw new java.text.ParseException("Failed login", 0);
        }

        //Element name = doc.select("strong").first();
        Element bookListLink = doc.select(".patroninfoList a").first();

        if (bookListLink == null)
            throw new NoBooksError("No Book");
        this.bookhref = bookListLink.attr("abs:href");
        if (this.bookhref == "")
            throw new java.text.ParseException("Cannnot get books after login", 0);
        return resp;
    }

    /*
     * There is a weird bug in Android2.2 (not exist in Android4.4):
     * If this function is invoked in alarmanager, conn.execute() will
     * success and fail alternately (tested in wifi connection). In main
     * thread, there is no such problem. So, try more than once.
     */
    private Connection.Response connectWithRetry(Connection conn)
            throws java.io.IOException {
        for (int trial=3;; trial--) {
            try {
                Connection.Response resp = conn.execute();
                return resp;
            } catch(java.io.IOException e) {
                Log.e("Libconn", "IOException "+e.getMessage());
                if (trial < 1)
                    throw new java.io.IOException("Failed connection");
            }
        }
    }

    public Map<String,String> loginAndSetCookie()
            throws java.io.IOException, java.text.ParseException {
        Connection.Response resp = login();
        Map<String,String> cookies = resp.cookies();
        /* Jsoup will strip path, date, etc, from the cookie, simplify the work */
        /* Refer to Jsoup source HttpConnection.java */
        cookieMonster.put(COOKIE_LIFE, COOKIE_IDENTIFIER, cookies, this.bookhref);
        return cookies;
    }

    /* get session cookie, if not available, login to get cookie, and store it
     * in CookieMonster */
    /* this function should set this.bookhref */
    public Map<String,String> getCookieFallBackLogin()
            throws java.io.IOException, java.text.ParseException {
        Pair<Map<String, String>,String> saved = cookieMonster.get(COOKIE_IDENTIFIER);
        if (saved != null) {
            Map<String,String> cookies = saved.first;
            this.bookhref = saved.second;
            return cookies;
        } else {
            return loginAndSetCookie();
        }
    }

    public void renewBooks(Iterable<Book> books)
            throws java.io.IOException, java.text.ParseException {
        /* A renew from command line looks like:
         * curl -b '[_cookies_here_]' \
         * -d 'renew2=i4524617&currentsortorder=current_checkout&renewsome=yes' \
         * 'https://m.library.cuhk.edu.hk/patroninfo~S15/1289019/items~'
         */
        Map<String,String> cookies = getCookieFallBackLogin();
        Connection conn = Jsoup.connect(this.bookhref)
                               .cookies(cookies)
                               .method(Connection.Method.POST);
        conn.data("currentsortorder", "current_checkout");
        conn.data("renewsome", "yes");
        for (Book book: books) {
           conn.data(book.html_form_name, book.html_form_value);
           Log.d("Libconn", "Adding " + book.html_form_name +" "+ book.html_form_value);
        }
        Log.e("Libconn", "renewing books");
        Connection.Response resp = connectWithRetry(conn);
        /* For debugging, log all html
        String xml = resp.body();
        for(int i=0;i<xml.length();i+=4000){
            if(i+4000<xml.length())
                Log.i("rescounter"+i,xml.substring(i, i+4000));
            else
                Log.i("rescounter"+i,xml.substring(i, xml.length()));
        }
        */
    }

    /* =====================
     * Section: html parsing
     * ===================== */
    public Element getBooksElement()
            throws java.io.IOException, java.text.ParseException {
        Document doc;
        try {
            Map<String,String> cookies = getCookieFallBackLogin();
            doc = Jsoup.connect(this.bookhref).cookies(cookies).get();
        } catch(java.io.IOException e) { 
            Log.e("Libconn", "fail getting books form " + this.bookhref);
            throw new java.io.IOException("Failed getting books"); 
        } catch(IllegalArgumentException e) {
            throw new java.text.ParseException("web link is bad. There may be server error", 0);
        }

        Element table = doc.select("table.patFunc").first();

        return table;
    }

    public static ArrayList<Book> getBooksFromElement(Element elm) {
        Elements bookrows = elm.select("table.patFunc > tbody > tr.patFuncEntry");
        ArrayList<Book> bookList = new ArrayList<>();
        SimpleDateFormat dateparser = new SimpleDateFormat("dd-MM-yy", java.util.Locale.UK);

        for (Element row : bookrows) {
            /* the patfunc field has book name and author seperated by "/" */
            String title = row.select(".patFuncTitle").text().split("/")[0];
            String dateStr = row.select(".patFuncStatus").text().substring(4, 12);

            Calendar dueDate = Calendar.getInstance();
            try {
                dueDate.setTime(dateparser.parse(dateStr));
            } catch (java.text.ParseException e) {
            }
            dueDate.set(Calendar.HOUR_OF_DAY, 23);

            Element input_elm = row.select("input").first();

            bookList.add(
                new Book(title, dueDate, input_elm.attr("name"), input_elm.attr("value")));
        }

        return bookList;
    }

    /* empty html element for fallback */
    public static Element newBooksElement() {
        return new Element(Tag.valueOf("table"), "").addClass("patFunc");
    }
}
