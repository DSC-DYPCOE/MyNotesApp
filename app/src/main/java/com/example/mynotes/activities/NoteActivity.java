package com.example.mynotes.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mynotes.R;
import com.example.mynotes.db.DatabaseHelper;
import com.example.mynotes.pojo.Note;
import com.example.mynotes.util.Utility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class NoteActivity extends AppCompatActivity {

    private DatabaseHelper myDB;
    private EditText editNoteText;
    private FloatingActionButton btnSaveNote;
    int origin;
    Note mNote;

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

        boolean isInserted = false;
        
        if (origin == Utility.NEW_NOTE){
            isInserted = myDB.insertData(noteText ,date);
        } else if (origin == Utility.EDIT_NOTE){
            isInserted = myDB.updateData(mNote.getId(), noteText, date);
        }

        if (isInserted) {
            finish();
        }
        else Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show();

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
}
