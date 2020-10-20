package com.example.trippy.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.trippy.R;

public class CountryImageDialog extends Dialog {
    private Context context;
    private String photoUrl;
    private RelativeLayout mainLayout;
    private ImageView photo;

    public CountryImageDialog(@NonNull Context context, String photoUrl) {
        super(context);
        this.context = context;
        this.photoUrl = photoUrl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_countryphoto);
        photo = findViewById(R.id.countryphoto_img_photo);

        Glide.with(photo).load(this.photoUrl).into(photo);
        photo.setOnTouchListener(new ImageMatrixTouchHandler(context));
    }
}
