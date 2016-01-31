package ah.twrbtest;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

public class SnackbarHelper {
    public static void show(View view, String text, int duration) {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        snackbar.show();
    }

    public static void show(View view, String text, int duration, String actionText, View.OnClickListener onClickListener) {
        Snackbar snackbar = Snackbar.make(view, text, duration);
        snackbar.setAction(actionText, onClickListener);
        ((TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text)).setTextColor(Color.WHITE);
        snackbar.show();
    }
}
