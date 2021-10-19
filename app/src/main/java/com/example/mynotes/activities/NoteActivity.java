package com.example.mynotes.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.mynotes.R;
import com.example.mynotes.db.DatabaseHelper;
import com.example.mynotes.models.ActivityResult;
import com.example.mynotes.pojo.Note;
import com.example.mynotes.util.NoteResult;
import com.example.mynotes.util.Utility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

public class NoteActivity extends AppCompatActivity {

    private DatabaseHelper myDB;
    private EditText editNoteText;
    private FloatingActionButton btnSaveNote;
    private Snackbar notificationSnackbar;

    int origin;
    Note mNote;
    ActivityResult activityResult = ActivityResult.NO_CHANGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        myDB = new DatabaseHelper(this);
        origin = getIntent().getIntExtra(Utility.ORIGIN,-1);

        initialize();
        getPreviousNote();

        btnSaveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
        notificationSnackbar = Snackbar.make(
                btnSaveNote,
                R.string.note_is_empty_message,
                Snackbar.LENGTH_SHORT
        ).setAnchorView(btnSaveNote);
    }

//    called from onCreate and Fetches data if note is already there
    private void getPreviousNote() {
        mNote = Objects.requireNonNull(getIntent().getExtras()).getParcelable("existingNote");
        if (origin == Utility.EDIT_NOTE){
            if (mNote != null){
                editNoteText.setText(mNote.getNoteText());
                Objects.requireNonNull(getSupportActionBar()).setTitle(mNote.getDate());
            }
        }
    }

//    onClick method of Save button
    private void saveNote() {
        String noteText = editNoteText.getText().toString().trim();
        String date = Utility.getCurrentDate();

        if (noteText.isEmpty()) {
            notifyCustomer(R.string.note_is_empty_message);
            return;
        }

        boolean isInserted = false;
        boolean isNewNote = origin == Utility.NEW_NOTE;
        boolean isEditNote = origin == Utility.EDIT_NOTE;
        if (isNewNote){
            isInserted = myDB.insertData(noteText ,date);
        } else if (isEditNote){
            isInserted = myDB.updateData(mNote.getId(), noteText, date);
        }



        if (isInserted) {
            activityResult = ActivityResult.SUCCESSFUL;
            finish();
            return;
        }
        notifyCustomer(isNewNote
                ? R.string.create_note_failure_message
                : R.string.update_note_failure_message
        );

    }

    // Displays the snackbar with the message stored within the messageId
    private void notifyCustomer(int messageId) {
        notificationSnackbar.setText(messageId).show();
    }

//    init views with findViewById() method
    private void initialize() {
        editNoteText = findViewById(R.id.edit_note_title);
        btnSaveNote = findViewById(R.id.btn_save_note);
    }

    //menu code
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_menu
                ,menu);
        return true;
    }

//    delete menu onClick code
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        
        if (item.getItemId() == R.id.menu_delete){
            if(mNote != null){
                myDB.deleteRecord(mNote.getId());
            }
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        NoteResult.storeNoteResult(getIntent(), activityResult);
        setResult(activityResult.ordinal(), getIntent());
        super.finish();
    }
}
