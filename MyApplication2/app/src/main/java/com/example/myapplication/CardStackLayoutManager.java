package com.example.myapplication;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CardStackLayoutManager extends LinearLayoutManager {
    private static final float ROTATION_ANGLE = 2.5f;
    private static final int MAX_VISIBLE_ITEMS = 5;

    public CardStackLayoutManager(Context context) {
        super(context, VERTICAL, false);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        updateItemTransformations();
    }

    private void updateItemTransformations() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child == null) continue;

            float scale = 1 - (0.05f * (childCount - i - 1));
            child.setScaleX(scale);
            child.setScaleY(scale);
            child.setRotationX(ROTATION_ANGLE * (childCount - i - 1));
            child.setTranslationY(-(childCount - i - 1) * child.getHeight() / 8f);
        }
    }

    @Override
    public int getExtraLayoutSpace(RecyclerView.State state) {
        return (int) (getHeight() * 1.5);
    }
}