package com.loopbook.cuhk_loopbook;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import android.support.v4.app.Fragment;

import java.text.SimpleDateFormat;

import com.loopbook.cuhk_loopbook.view.ConfirmGoButton;
import java.util.*;


public class BookFragment extends Fragment {

    private BookAdapter bookAdapter;
    public Data data;

    /*
     * Adapter for displaying books, inluding an icon showing the book status
     * and a text of book name and due date
     */
    public static class BookAdapter extends ArrayAdapter
            implements CompoundButton.OnCheckedChangeListener {
        private static SimpleDateFormat formater = new SimpleDateFormat("dd/MM", java.util.Locale.UK);
        private boolean isShowingCheckbox = false;
        private ArrayList<LibConn.Book> books;
        private boolean[] selectedStates;
        private Context ctx;

        public BookAdapter(Context ctx, ArrayList<LibConn.Book> list) {
            super(ctx, R.layout.list_item, R.id.title_text, list);
            this.books = list;
            this.ctx = ctx;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            LibConn.Book book = books.get(position);

            ViewSwitcher switcher = (ViewSwitcher)view.findViewById(R.id.switcher);
            if (isShowingCheckbox) {
                CheckBox cb = (CheckBox)view.findViewById(R.id.book_checkbox);
                /* The order of following 2 lines is important, otherwise when
                 * view is switched, box will be randomly checked, because
                 * android will reuse view randomly (convertview) */
                cb.setTag(position);  /* For use in onCheckCHanged */
                cb.setChecked(selectedStates[position]);
                cb.setOnCheckedChangeListener(this);
                switcher.setDisplayedChild(1);
            } else {
                ImageView iv = (ImageView)view.findViewById(R.id.option_icon);
                iv.setImageResource(book.remainDays() >= DueChecker.getAlertDays(ctx) ?
                                    R.drawable.green_circle :
                                    R.drawable.red_circle);
                switcher.setDisplayedChild(0);
            }

            ((TextView)view.findViewById(R.id.title_text))
                    .setText(book.name);
            ((TextView)view.findViewById(R.id.sub_text))
                    .setText(formater.format(book.dueDate.getTime()));

            return view;
        }

        public void setBooks(ArrayList<LibConn.Book> booksGot) {
            /* I am afraid race condition may happen in these line (issue #2),
             * but probability is very low */
            books.clear();
            books.addAll(booksGot);
            selectedStates = new boolean[books.size()];
            notifyDataSetChanged();
        }

        public void showCheckBoxes(boolean yesno) {
            isShowingCheckbox = yesno;
            /* TODO: update view instead of notifyDataSetChanged, using
             * View#getChildAt(int index), ListView#getFirstVisiblePosition().
             * See: http://stackoverflow.com/questions/3724874/how-can-i-update-a-single-row-in-a-listview */
            notifyDataSetChanged();
        }

        public ArrayList<LibConn.Book> getSelected() {
            ArrayList<LibConn.Book> selectedList = new ArrayList<>();
            for (int i=0; i < selectedStates.length; i++) {
                if (selectedStates[i])
                    selectedList.add(books.get(i));
            }
            return selectedList;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            selectedStates[(Integer) buttonView.getTag()] = isChecked;
        }
    }

    private static class Data {

        private class AsyncBookLoader extends AsyncTask<Context, String, ArrayList<LibConn.Book>> {
            private Exception caughtException = null;
            private Context context;
            public BookAdapter bookAdapter;

            public AsyncBookLoader(BookAdapter bookAdapter) {
                    this.bookAdapter = bookAdapter;
            }

            @Override
            protected ArrayList<LibConn.Book> doInBackground(Context... context) {
                this.context = context[0];
                String msg = LibConn.isConnectable(this.context) ? "connecting" : "No connection";
                publishProgress(msg);

                ArrayList<LibConn.Book> booksGot;
                try {
                    booksGot = DataIO.getBooks(this.context);
                } catch (java.io.IOException | java.text.ParseException e) {
                    caughtException = e;
                    return null;
                }

                return booksGot;
            }

            @Override
            protected void onProgressUpdate(String... msgs) {
                Toast.makeText(this.context, msgs[0], Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onPostExecute(ArrayList<LibConn.Book> booksGot) {
                if (caughtException != null) {
                    Toast.makeText(context,
                            caughtException.getMessage(),
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (booksGot.size() == 0) {
                        Toast.makeText(context,
                                context.getString(R.string.no_books),
                                Toast.LENGTH_SHORT).show();
                    }
                    bookAdapter.setBooks(booksGot);
                }
            }
        }

        public void refresh(BookAdapter adapter, Context context) {
            AsyncBookLoader bookLoader = new AsyncBookLoader(adapter);
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
                new ArrayList<LibConn.Book>());
        if  (getArguments() != null ? getArguments().getBoolean("firstRun", false) : false) {
            getArguments().putBoolean("firstRun", false);
        } else {
            refresh();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_book_list, container, false);
        ListView lv = (ListView) rootView.findViewById(R.id.book_list);
        lv.setAdapter(bookAdapter);
        listenToConfirmGo(((ConfirmGoButton) rootView.findViewById(R.id.fab)));

        return rootView;
    }

    public void refresh() {
        data.refresh(bookAdapter, getActivity());
    }

    public void listenToConfirmGo(ConfirmGoButton cgButton) {
        cgButton.regListener(new ConfirmGoButton.ConfirmGoListener() {
            @Override
            public void onStarted() { bookAdapter.showCheckBoxes(true); }
            @Override
            public void onCanceled() { bookAdapter.showCheckBoxes(false); }
        });
    }
}
