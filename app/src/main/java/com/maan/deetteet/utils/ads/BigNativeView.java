package com.maan.deetteet.utils.ads;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.maan.deetteet.R;

public class BigNativeView extends LinearLayout {
    public BigNativeView(@NonNull Context context) {
        super(context);
        initNative();
    }

    public BigNativeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initNative();
    }

    public BigNativeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initNative();
    }

    public BigNativeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initNative();
    }

    private void initNative() {
        new CustomNativeAds(getContext(), this, R.layout.admob_big, R.layout.fb_big);
    }
}
