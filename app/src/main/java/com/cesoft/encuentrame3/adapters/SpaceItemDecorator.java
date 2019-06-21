package com.cesoft.encuentrame3.adapters;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

////////////////////////////////////////////////////////////////////////////////////////////////////
// Created by Cesar_Casanova on 21/06/2019
public class SpaceItemDecorator extends RecyclerView.ItemDecoration {

    private final int verticalSpaceHeight;

    public SpaceItemDecorator(int verticalSpaceHeight) {
        this.verticalSpaceHeight = verticalSpaceHeight;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect,
                               @NonNull View view,
                               @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {

        if (parent.getAdapter() != null
                && parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = verticalSpaceHeight;
        }
    }
}