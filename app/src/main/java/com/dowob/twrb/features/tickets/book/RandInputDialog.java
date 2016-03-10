package com.dowob.twrb.features.tickets.book;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.dowob.twrb.R;
import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class RandInputDialog extends Dialog {
    @Bind(R.id.editText_randInput)
    EditText randInput_editText;
    @Bind(R.id.button_submit)
    Button submit_button;
    @Bind(R.id.imageView_captcha)
    ImageView captcha_imageView;

    private Context context;
    private Bitmap captcha;

    public RandInputDialog(Context context, Bitmap captcha) {
        super(context);
        this.context = context;
        this.captcha = captcha;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.type_randinput);
        ButterKnife.bind(this);
        RxView.clicks(submit_button).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(v -> onSubmitButtonClick());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        captcha_imageView.setImageBitmap(captcha);
    }

    public void onSubmitButtonClick() {
        dismiss();
        EventBus.getDefault().post(new OnSubmitEvent(randInput_editText.getText().toString()));
        System.out.println(randInput_editText.getText().toString());
    }

    public static class OnSubmitEvent {
        String randInput;

        public OnSubmitEvent(String randInput) {
            this.randInput = randInput;
        }

        public String getRandInput() {
            return randInput;
        }
    }
}
