package com.loopbook.cuhk_loopbook.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.loopbook.cuhk_loopbook.R;

public class ConfirmGoButton extends RelativeLayout {
    public ConfirmGoButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.confirm_go, this);
    }
}
