package com.example.mynotes.util;

import android.content.Intent;

import androidx.annotation.Nullable;

import com.example.mynotes.R;
import com.example.mynotes.models.ActivityResult;
import com.google.android.material.snackbar.Snackbar;

public class NoteResult {
    private static final String NOTE_RESULT_MESSAGE_ID = "NOTE_RESULT_MESSAGE_ID";
    private static final int INVALID_MESSAGE_ID = -1;

    public static void notify(@Nullable Intent data, Snackbar snackbar) {
        if (data == null || !data.hasExtra(NOTE_RESULT_MESSAGE_ID)) {
            return;
        }
        int messageId = data.getIntExtra(NOTE_RESULT_MESSAGE_ID, INVALID_MESSAGE_ID);
        if (INVALID_MESSAGE_ID == messageId) {
            return;
        }
        snackbar.setText(messageId).show();
    }

    public static void storeNoteResult(
            Intent activityIntent,
            ActivityResult activityResult
    ) {
        if (ActivityResult.NO_CHANGE.equals(activityResult)) {
            return;
        }
        boolean isSuccessful = ActivityResult.SUCCESSFUL.equals(activityResult);
        int noteOrigin = Utility.extractNoteOrigin(activityIntent);

        int messageId;
        switch (noteOrigin) {
            case Utility.NEW_NOTE:
                messageId = isSuccessful
                        ? R.string.create_note_successful_message
                        : R.string.create_note_failure_message;
                break;
            case Utility.EDIT_NOTE:
                messageId = isSuccessful
                        ? R.string.update_note_successful_message
                        : R.string.update_note_failure_message;
                break;
            default: return;
        }

        activityIntent.putExtra(NOTE_RESULT_MESSAGE_ID, messageId);
    }
}
