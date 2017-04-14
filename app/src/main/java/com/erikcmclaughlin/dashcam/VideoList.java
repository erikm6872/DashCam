package com.erikcmclaughlin.dashcam;

import android.R.layout;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class VideoList extends AppCompatActivity {
    ListView video_list;

    ArrayList<String> file_list = new ArrayList<String>();
    ArrayAdapter<String> file_list_adapter;

    int itemNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        video_list = (ListView) findViewById(R.id.file_list);
        file_list_adapter= new ArrayAdapter<String>(this, layout.simple_list_item_1, file_list);
        video_list.setAdapter(file_list_adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //file_list.add("List Item " + itemNum++);
                //file_list_adapter.notifyDataSetChanged();
                addItem(view);
                Snackbar.make(view, "Added Item " + itemNum, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addItem(View v){
        file_list_adapter.add("List Item " + itemNum++);
       // file_list.add("List Item " + itemNum++);
        //file_list_adapter.notifyDataSetChanged();
        //this.
    }
}
