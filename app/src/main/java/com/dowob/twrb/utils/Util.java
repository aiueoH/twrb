package com.dowob.twrb.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.dowob.twrb.R;

public class Util {
    public static Drawable getDrawable(Context context, int id) {
        if (Build.VERSION.SDK_INT >= 21)
            return context.getDrawable(id);
        return context.getResources().getDrawable(id);
    }

    public static int getCityDrawableId(int cityNo) {
        switch (cityNo) {
            case 0:
                return R.drawable.t0;
            case 1:
                return R.drawable.t1;
            case 2:
                return R.drawable.t2;
            case 3:
                return R.drawable.t3;
            case 4:
                return R.drawable.t4;
            case 5:
                return R.drawable.t5;
            case 6:
                return R.drawable.t6;
            case 7:
                return R.drawable.t7;
            case 8:
                return R.drawable.t8;
            case 9:
                return R.drawable.t9;
            case 10:
                return R.drawable.t10;
            case 11:
                return R.drawable.t11;
            case 12:
                return R.drawable.t12;
            case 13:
                return R.drawable.t13;
            case 14:
                return R.drawable.t14;
            case 15:
                return R.drawable.t15;
            case 16:
                return R.drawable.t16;
            case 17:
                return R.drawable.t17;
            case 18:
                return R.drawable.t18;
            default:
                return R.drawable.t19;
        }
    }
}
