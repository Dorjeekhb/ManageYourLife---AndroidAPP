package com.example.myapplication;

import androidx.room.TypeConverter;

public class Converters {
    @TypeConverter
    public static Priority fromString(String value) {
        return Priority.valueOf(value);
    }

    @TypeConverter
    public static String priorityToString(Priority priority) {
        return priority.name();
    }
}
