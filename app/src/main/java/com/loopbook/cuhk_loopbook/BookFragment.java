package com.loopbook.cuhk_loopbook;

import java.util.*;
import java.text.SimpleDateFormat;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.AsyncTask;

import android.content.Context;

import android.util.Log;

public class BookFragment extends Fragment {

    private BookAdapter bookAdapter;
    private static BookFragment instance;
    public Data data;

    public BookFragment() {
        super();
        instance = this;
    }

    public static BookFragment getInstance() {
        return instance;
    }

    public void refresh() {
        data.refresh(bookAdapter, getActivity());
    }

    /* Adapter for displaying books, inluding an icon showing the book status
     * and a text of book name and due date
     * some code is copied from <Android Cookbook>, Oreilly 2012, recipe 9.2
     */
    public static class BookAdapter extends BaseAdapter {
        private static SimpleDateFormat formater = new SimpleDateFormat("dd/MM", java.util.Locale.UK);
        private LayoutInflater mInflater;
        private ArrayList<LibConn.Book> books;
        private int viewResourceId;
        private Context ctx;

        public BookAdapter(Context ctx, int viewResourceId, ArrayList<LibConn.Book> books) {
            this.ctx = ctx;
            mInflater = (LayoutInflater)ctx.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
            this.books = books;
            this.viewResourceId = viewResourceId;
        }

        public LibConn.Book getItem(int position) {
            return books.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return books.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = mInflater.inflate(viewResourceId, null);
            LibConn.Book book = books.get(position);

            ImageView iv = (ImageView)view.findViewById(R.id.option_icon);
            iv.setImageResource(book.remainDays() >= DueChecker.getAlertDays(ctx) ?
                                R.drawable.green_circle :
                                R.drawable.red_circle);

            TextView tv = (TextView)view.findViewById(R.id.option_text);
            tv.setText(book.name + "\n" + formater.format(book.dueDate.getTime()));

            return view;
        }
    }

    private static class Data {

        private ArrayList<LibConn.Book> books = new ArrayList<>();

        private class AsyncBookLoader extends AsyncTask<Context, String, Void> {
            private Exception caughtException = null;
            private Context context;
            private BookAdapter bookAdapter;

            @Override
            protected Void doInBackground(Context... context) {
                this.context = context[0];
                String msg = LibConn.isConnectable(this.context) ? "connecting" : "No connection";
                publishProgress(msg);

                books.clear();
                ArrayList<LibConn.Book> booksGot;
                try {
                    booksGot = DataIO.getBooks(this.context);
                } catch (java.io.IOException | java.text.ParseException e) {
                    caughtException = e;
                    return null;
                }

                for (LibConn.Book book: booksGot) {  /* make deep copy */
                    books.add(book);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... msgs) {
                Toast.makeText(this.context, msgs[0], Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onPostExecute(Void nothing) {
                if (caughtException != null) {
                    Toast.makeText(context,
                            caughtException.getMessage(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (books.size() == 0) {
                        Toast.makeText(context,
                                context.getString(R.string.no_books),
                                Toast.LENGTH_SHORT).show();
                    }
                    bookAdapter.notifyDataSetChanged();
                }
            }
        }

        public ArrayList<LibConn.Book> getbooks() {
            return books;
        }

        public void refresh(BookAdapter adapter, Context context) {
            AsyncBookLoader bookLoader = new AsyncBookLoader();
            bookLoader.bookAdapter = adapter;
            bookLoader.execute(context);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        data = new Data();

        bookAdapter = new BookAdapter(
                getActivity(),
                R.layout.list_item,
                data.getbooks());
        if  (getArguments() != null ? getArguments().getBoolean("firstRun", false) : false) {
            getArguments().putBoolean("firstRun", false);
        } else {
            refresh();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView lv = (ListView) rootView.findViewById(R.id.book_list);
        lv.setAdapter(bookAdapter);

        return rootView;
    }
}
