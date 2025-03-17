package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class GamificationManager {

    private static final String PREFS_NAME = "GamePrefs";
    private static final String KEY_POINTS = "points";

    public static void addPoints(Context context, int amount) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int currentPoints = prefs.getInt(KEY_POINTS, 0);
        int newTotal = currentPoints + amount;
        prefs.edit().putInt(KEY_POINTS, newTotal).apply();
    }

    public static int getPoints(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_POINTS, 0);
    }


    public static int getLevel(Context context) {
        int points = getPoints(context);
        return (points / 100) + 1;
    }

    public static void resetPoints(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_POINTS, 0).apply();
    }
}
