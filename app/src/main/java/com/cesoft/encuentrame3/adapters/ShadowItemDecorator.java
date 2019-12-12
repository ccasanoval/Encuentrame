package com.cesoft.encuentrame3.adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 21/06/2019
public class ShadowItemDecorator extends RecyclerView.ItemDecoration {

    private final Drawable divider;

    public ShadowItemDecorator(Context context, int resId) {
        this.divider = ContextCompat.getDrawable(context, resId);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        int left = parent.getPaddingLeft()-1;
        int right = parent.getWidth() - parent.getPaddingRight() -1;
        for(int i=0; i < parent.getChildCount(); i++) {
            View item = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) item.getLayoutParams();
            int top = item.getBottom() + params.bottomMargin -1;
            int bottom = top + divider.getIntrinsicHeight() - 1;
            this.divider.setBounds(left, top, right, bottom);
            this.divider.draw(c);
        }
    }
}
