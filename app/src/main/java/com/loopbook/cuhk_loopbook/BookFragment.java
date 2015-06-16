package com.loopbook.cuhk_loopbook;

import java.util.*;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.apache.http.util.EncodingUtils;

import android.content.Context;

import org.jsoup.nodes.Element;
import android.util.Log;

public class BookFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private ArrayAdapter arrayAdapter;
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
        data.refresh(arrayAdapter, getActivity());
    }

    private static class Data {

        private class AsyncBookLoader extends AsyncTask<Context, String, ArrayList<String>> {
            private Exception caughtException = null;
            private Context context;
            private ArrayAdapter arrayAdapter;

            @Override
            protected ArrayList<String> doInBackground(Context... context) {
                Element elm;
                this.context = context[0];
                String msg = LibConn.isConnectable(this.context) ? "connecting" : "No connection";
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
                    Toast.makeText(context,
                            caughtException.getMessage(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        }

        private static Data singleton = null;
        private ArrayList<String> books = new ArrayList<>();

        public ArrayList<String> getbooks() {
            return books;
        }

        public void refresh(ArrayAdapter arrayAdapter, Context context) {
            AsyncBookLoader bookLoader = new AsyncBookLoader();
            bookLoader.arrayAdapter = arrayAdapter;
            bookLoader.execute(context);
        }

        /*
        public Data getSingleton() {
            if (singleton == null)
                singleton = new Data();
            return singleton;
        }
        */
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e("BookFragment", "oncreate - retaining instance");
        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        data = new Data();

        arrayAdapter = new ArrayAdapter(
                getActivity(),
                R.layout.list_item,
                data.getbooks());
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
