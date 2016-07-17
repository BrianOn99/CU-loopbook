package com.loopbook.cuhk_loopbook.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopbook.cuhk_loopbook.R;

public class ConfirmGoButton extends RelativeLayout {
    public interface ConfirmGoListener {
        void onStarted();
        void onCanceled();
        void onGo();
    }

    private ConfirmGoListener listener;
    private ProgressBar loadSpinner;
    private TextView startButton;
    private View cancelButton;
    private View goButton;

    private enum State {READY, CONFIRMING, BUSY}

    public ConfirmGoButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.confirm_go, this);
        bindView(view);
        startButton.setText("Renew");
        setupButons();
        switchState(State.READY);
    }

    private void bindView(View view) {
        startButton = (TextView) view.findViewById(R.id.fab_start);
        cancelButton = view.findViewById(R.id.fab_cancel);
        goButton = view.findViewById(R.id.fab_go);
        loadSpinner = (ProgressBar) view.findViewById(R.id.load_spinner);
    }

    private void setupButons() {
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchState(State.CONFIRMING);
                listener.onStarted();
            }
        });
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchState(State.READY);
                listener.onCanceled();
            }
        });
        goButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onGo();
            }
        });
    }

    public void regListener(ConfirmGoListener listener) {
        this.listener = listener;
    }

    private void switchState(State state) {
        switch (state) {
        case READY:
            cancelButton.setVisibility(View.INVISIBLE);
            goButton.setVisibility(View.INVISIBLE);
            startButton.setVisibility(View.VISIBLE);
            loadSpinner.setVisibility(View.INVISIBLE);
            break;
        case CONFIRMING:
            cancelButton.setVisibility(View.VISIBLE);
            goButton.setVisibility(View.VISIBLE);
            startButton.setVisibility(View.INVISIBLE);
            loadSpinner.setVisibility(View.INVISIBLE);
            break;
        case BUSY:
            cancelButton.setVisibility(View.INVISIBLE);
            goButton.setVisibility(View.INVISIBLE);
            startButton.setVisibility(View.INVISIBLE);
            loadSpinner.setVisibility(View.VISIBLE);
            break;
        }
    }

    public void doingWork() {
        switchState(State.BUSY);
    }
    public void finishedWork() {
        switchState(State.READY);
    }
}
