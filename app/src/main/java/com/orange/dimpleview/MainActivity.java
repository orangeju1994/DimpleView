package com.orange.dimpleview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {
    private ObjectAnimator mAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CircleImageView civ = findViewById(R.id.civ);
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) civ.getLayoutParams();
        layoutParams.width = DimpleView.getScreenWidth(this) / 2 - 20;
        layoutParams.height = layoutParams.width;
        civ.setLayoutParams(layoutParams);
        civ.setImageResource(R.mipmap.bg);

        mAnimator = ObjectAnimator.ofFloat(civ, "rotation", 0, 3600)
                .setDuration(80000);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
    }
}