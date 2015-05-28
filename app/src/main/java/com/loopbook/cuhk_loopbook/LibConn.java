package com.loopbook.cuhk_loopbook;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Connection;
import java.util.*;
import java.net.InetAddress;


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
            return !ipAddr.equals("");
        } catch (Exception e) {
            return false;
        }

    }

    public void login() {
        Connection conn = Jsoup.connect("https://m.library.cuhk.edu.hk/patroninfo")
                               .data("code", this.name, "pin", this.passwd)
                               .method(Connection.Method.POST);
        Connection.Response resp;
        Document doc;

        try {
            resp = conn.execute();
            doc = resp.parse();
        } catch(Exception e) { 
            throw new RuntimeException("Failed connection", e); 
        }

        //Element name = doc.select("strong").first();
        Element bookListLink = doc.select(".patroninfoList a").first();
        this.bookhref = bookListLink.attr("abs:href");
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
