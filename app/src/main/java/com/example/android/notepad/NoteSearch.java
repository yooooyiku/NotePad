package com.example.android.notepad;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

public class NoteSearch extends Activity implements SearchView.OnQueryTextListener{
    private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,//2
    };
    ListView listView;
    SQLiteDatabase sqLiteDatabase;

    public boolean onQueryTextSubmit(String query){
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_search);
        SearchView searchView=findViewById(R.id.search_view);
        Intent intent=getIntent();
        if (intent.getData()==null){
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        listView=findViewById(R.id.list_view);
        sqLiteDatabase=new NotePadProvider.DatabaseHelper(this).getReadableDatabase();
        //显示搜索按钮
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(NoteSearch.this);
    }
    public boolean onQueryTextChange(String string){
        String selection1 = NotePad.Notes.COLUMN_NAME_TITLE+" like ? or "+NotePad.Notes.COLUMN_NAME_NOTE+" like ?";
        String[] selection2 = {"%"+string+"%","%"+string+"%"};
        Cursor cursor=sqLiteDatabase.query(
                NotePad.Notes.TABLE_NAME,
                PROJECTION,
                selection1,
                selection2,
                null,
                null,
                NotePad.Notes.DEFAULT_SORT_ORDER
        );
        String[] dataColumns = {
                NotePad.Notes.COLUMN_NAME_TITLE,//标题
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE //时间
        } ;
        // The view IDs that will display the cursor columns, initialized to the TextView in
        // noteslist_item.xml
        int[] viewIDs = {
                android.R.id.text1,
                android.R.id.text2
        };
        // Creates the backing adapter for the ListView.
        SimpleCursorAdapter adapter
                = new SimpleCursorAdapter(
                this,                             // The Context for the ListView
                R.layout.noteslist_item,         // Points to the XML for a list item
                cursor,                           // The cursor to get items from
                dataColumns,
                viewIDs
        );
        // Sets the ListView's adapter to be the cursor adapter that was just created.
        listView.setAdapter(adapter);
        return true;
    }
}
