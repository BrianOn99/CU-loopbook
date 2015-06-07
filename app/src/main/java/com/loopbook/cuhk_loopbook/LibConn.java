package com.loopbook.cuhk_loopbook;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Connection;
import java.util.*;
import java.net.InetAddress;

import android.util.Log;

public class LibConn {
    private final String name;
    private final String passwd;
    private String bookhref;
    private Map<String, String> cookies;

    public LibConn(String name, String passwd) {
        this.name = name;
        this.passwd = passwd;
    }

    public static boolean isConnectable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            Log.e("Libconn", "connectable");
            return !ipAddr.equals("");
        } catch (Exception e) {
            Log.e("Libconn", "unconnectable");
            return false;
        }

    }

    public void login() {
        Connection conn = Jsoup.connect("https://m.library.cuhk.edu.hk/patroninfo")
                               .data("code", this.name, "pin", this.passwd)
                               .method(Connection.Method.POST);
        Connection.Response resp;
        Document doc;

        /*
         * There is a weird bug in Android2.2 (not exist in Android4.4):
         * If this function is invoked in alarmanager, conn.execute() will
         * success and fail alternately (tested in wifi connection). In main
         * thread, there is no such problem. So, try more than once.
         */
        int trial = 3;
        while (true) {
            try {
                resp = conn.execute();
                doc = resp.parse();
                break;
            } catch(java.io.IOException e) {
                Log.e("Libconn", "IOException "+e.getMessage());
                if (--trial < 1)
                    throw new RuntimeException("Failed connection", e);
            }
        }
        Log.e("Libconn", "HTTP and parse ok");

        Elements succElm = doc.getElementsByClass("loggedInMessage");
        if (succElm.size() == 0) {
            throw new RuntimeException("Failed login");
        }

        //Element name = doc.select("strong").first();
        Element bookListLink = doc.select(".patroninfoList a").first();
        if (bookListLink == null)
            throw new RuntimeException("Cannnot get books after login");
        this.bookhref = bookListLink.attr("abs:href");
        if (this.bookhref == "")
            throw new RuntimeException("Cannnot get books after login");
        this.cookies = resp.cookies();
    }

    public Element getBooksElement() {
        Document doc;
        try {
            doc = Jsoup.connect(this.bookhref).cookies(this.cookies).get();
        } catch(Exception e) { 
            throw new RuntimeException("Failed connection", e); 
        }

        Element table = doc.select("table.patFunc").first();

        return table;
    }

    public static ArrayList<Map<String, String>> getBooksFromElement(Element elm) {
        Elements bookrows = elm.select("table.patFunc > tbody > tr.patFuncEntry");
        ArrayList<Map<String, String>> bookList = new ArrayList<>();

        for (Element row : bookrows) {
            Map<String, String> info = new HashMap<>();
            /* the patfunc field has book name and author seperated by "/" */
            info.put("title", row.select(".patFuncTitle").text().split("/")[0]);
            info.put("dueDate", row.select(".patFuncStatus").text().substring(4, 12));
            bookList.add(info);
        }

        return bookList;
    }

    public ArrayList<Map<String, String>> getBooks() {
        return getBooksFromElement(getBooksElement());
    }

    public String getBooksHtml() {
        return getBooksElement().outerHtml();
    }
}
