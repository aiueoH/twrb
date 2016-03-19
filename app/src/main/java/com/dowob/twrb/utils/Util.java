package com.dowob.twrb.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

public class Util {
    public static Drawable getDrawable(Context context, int id) {
        if (Build.VERSION.SDK_INT >= 21)
            return context.getDrawable(id);
        return context.getResources().getDrawable(id);
    }
}
