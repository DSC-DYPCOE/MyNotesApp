package com.example.mynotes.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mynotes.R;
import com.example.mynotes.adapters.NoteAdapter;
import com.example.mynotes.db.DatabaseHelper;
import com.example.mynotes.pojo.Note;
import com.example.mynotes.util.NoteResult;
import com.example.mynotes.util.Utility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteListener{

    private RecyclerView rvNotes;
    private FloatingActionButton fab;
    LinearLayout noDataLayout;
    private Snackbar notificationSnackbar;
    
    private DatabaseHelper myDB;
    public NoteAdapter adapter;
    ArrayList<Note> myNotes = new ArrayList<>();

    @Override
    protected void onStart() {
        super.onStart();

        fetchData();
        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        myDB = new DatabaseHelper(this);
        
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                Utility.storeNoteOrigin(intent, Utility.NEW_NOTE);
                startActivityForResult(intent, Utility.NEW_NOTE);
            }
        });

        notificationSnackbar = Snackbar.make(
                fab,
                R.string.create_note_successful_message,
                Snackbar.LENGTH_SHORT
        ).setAnchorView(fab);

        prepareRecyclerView(myNotes);

    }

//    called in onStart method and fetches all data from DB
    private void fetchData() {
        Cursor c = myDB.getAllData();
        myNotes.clear();
//        check if DB has some data
        if (c.getCount() >=1){
//            setting visibility for RV & noDataLayout
            noDataLayout.setVisibility(View.GONE);
            rvNotes.setVisibility(View.VISIBLE);
//            looping through DB records
            while (c.moveToNext()){
                Note note = new Note();
                note.setId(c.getString(0));
                note.setNoteText(c.getString(1));
                note.setDate(c.getString(2));
                myNotes.add(note);
            }

        }else {
//            if DB has NO data
//            setting visibility for RV & noDataLayout
            noDataLayout.setVisibility(View.VISIBLE);
            rvNotes.setVisibility(View.GONE);
        }
        c.close();
    }

//    called in onCreate method and manages Recycler View
    public void prepareRecyclerView(ArrayList<Note> myNotes){
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(myNotes, this);
        rvNotes.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvNotes);
    }

    private void initialize() {
//        init views
        rvNotes = findViewById(R.id.rv_notes);
        fab = findViewById(R.id.add_note_fab);
        noDataLayout = findViewById(R.id.no_data_view);
    }

//    on click for recycler view item
    @Override
    public void onNoteClick(int position) {
        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        Utility.storeNoteOrigin(intent, Utility.EDIT_NOTE);
        intent.putExtra("existingNote", myNotes.get(position));
        startActivityForResult(intent, Utility.EDIT_NOTE);
    }

//    handling swipe-to-delete
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT| ItemTouchHelper.LEFT) {
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();

//        1. remove from DB, 2. remove from RV, 3. notify data set changed
        final Note deletedNote = myNotes.get(position);
        myDB.deleteRecord(deletedNote.getId());
        myNotes.remove(position);
        adapter.notifyDataSetChanged();

//        option for UNDO
        Snackbar.make(rvNotes,"Deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isRestored = myDB.insertDeletedNote(deletedNote);
                        if (!isRestored){
                            Toast.makeText(MainActivity.this, "Sorry, Could not Restore", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        myNotes.add(deletedNote);
                        adapter.notifyDataSetChanged();
                    }
                }).show();
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        new RecyclerViewSwipeDecorator.Builder(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive)
                .addBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorRed))
                .addActionIcon(R.drawable.ic_baseline_delete_24)
                .create()
                .decorate();

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
};

    //    3 dot menu code
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.menu_about){
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        NoteResult.notify(data, notificationSnackbar);
    }
}
