package com.example.myapplication;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StackItemDecoration extends RecyclerView.ItemDecoration {
    private final int maxOffset;
    private final int baseOffset;

    public StackItemDecoration(int baseOffset, int maxOffset) {
        this.baseOffset = baseOffset;
        this.maxOffset = maxOffset;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position == 0) return;

        int offset = Math.min(position * baseOffset, maxOffset);
        outRect.set(offset, offset, 0, 0);
    }
}