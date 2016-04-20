package com.dowob.twrb.features.shared;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.dowob.twrb.R;

public class SnackbarHelper {
    public static void show(View view, String text, int duration) {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_action)).setTextColor(view.getContext().getResources().getColor(R.color.jackYellow));
        snackbar.show();
    }

    public static void show(View view, String text, int duration, String actionText, View.OnClickListener onClickListener) {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        snackbar.setAction(actionText, onClickListener);
        ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_action)).setTextColor(view.getContext().getResources().getColor(R.color.jackYellow));
        snackbar.show();
    }
}
