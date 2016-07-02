package com.loopbook.cuhk_loopbook.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.loopbook.cuhk_loopbook.R;

public class ConfirmGoButton extends RelativeLayout {
    public interface ConfirmGoListener {
        void onStarted();
        void onCanceled();
        void onGo();
    }

    private ConfirmGoListener listener;
    private View startButton;
    private View cancelButton;
    private View goButton;

    private enum State {READY, CONFIRMING, BUSY}

    public ConfirmGoButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.confirm_go, this);
        bindView(view);
        setupButons();
    }

    private void bindView(View view) {
        startButton = view.findViewById(R.id.fab_start);
        cancelButton = view.findViewById(R.id.fab_cancel);
        goButton = view.findViewById(R.id.fab_go);
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
                switchState(State.READY);
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
            cancelButton.setVisibility(View.GONE);
            goButton.setVisibility(View.GONE);
            startButton.setVisibility(View.VISIBLE);
            break;
        case CONFIRMING:
            cancelButton.setVisibility(View.VISIBLE);
            goButton.setVisibility(View.VISIBLE);
            startButton.setVisibility(View.GONE);
            break;
        case BUSY:
            break;
        }
    }
}
