package com.example.mynotes.util;

import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utility {
    private Utility(){}

    public static final int INVALID_NOTE_ORIGIN = -1;
    public static final int NEW_NOTE = 1;
    public static final int EDIT_NOTE = 2;
    public static final String ORIGIN = "origin";

    public static void storeNoteOrigin(Intent intent, int origin) {
        intent.putExtra(Utility.ORIGIN, origin);
    }

    public static int extractNoteOrigin(Intent intent) {
        return intent.getIntExtra(ORIGIN, INVALID_NOTE_ORIGIN);
    }

    public static String cutTheString(String text, int n){
        String trimText, removeNewLines;
        removeNewLines = text.replaceAll("\n", " ");
        trimText = removeNewLines.substring( 0, Math.min(text.length(), n));
        return trimText;
    }

    public static String getCurrentDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
        Date currentDate = new Date();
        return dateFormat.format(currentDate);
    }
}
