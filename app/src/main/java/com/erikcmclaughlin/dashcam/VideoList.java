package com.erikcmclaughlin.dashcam;

import android.R.layout;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class VideoList extends AppCompatActivity{

    private ArrayList<File> fileList = new ArrayList<>();
    private ArrayList<String> fileListStr = new ArrayList<String>();
    private ArrayAdapter<String> fileListAdapter;

    private File videosDir = new File(Environment.getExternalStorageDirectory(), "DashCamVideos");
    private File recordDir = new File(videosDir, "Record");
    private File savedDir = new File(videosDir, "Saved");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Create video directories if they do not already exist
        if(!videosDir.exists()){
            videosDir.mkdirs();
        }
        if(!recordDir.exists()){
            recordDir.mkdirs();
        }
        if(!savedDir.exists()){
            savedDir.mkdirs();
        }

        fileList = scanDir();
        fileListStr = simplifyFilenames(fileList);

        ListView videoList = (ListView) findViewById(R.id.file_list);
        fileListAdapter = new ArrayAdapter<>(this, layout.simple_list_item_1, fileListStr);
        videoList.setAdapter(fileListAdapter);

        // Set up the record button
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //fileList.add("List Item " + itemNum++);
                //fileListAdapter.notifyDataSetChanged();
                startRecord(view);
                Snackbar.make(view, "Recording has started", Snackbar.LENGTH_LONG)
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
        else if (id == R.id.action_about){
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setMessage(R.string.about_text).setTitle("About");
            alertBuilder.create();
            alertBuilder.show();
        }
        else if(id == R.id.action_preview){
            Intent intent = new Intent(this, Camera_Preview.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Starts recording by pressing the record button
     * @param v OnClickListener object from button
     */
    private void startRecord(View v){
        String fname = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss").format(new Date()) + ".mp4";
        try {
            File outputFile = new File(recordDir, fname);
            FileOutputStream outputFileStream = new FileOutputStream(outputFile);

            OutputStreamWriter osw = new OutputStreamWriter(outputFileStream);

            osw.write("Test File '" + fname + "'".getBytes());
            osw.flush();
            osw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        addItem(v, fname);
        refreshFileList();
        //addItem(v,fname);
    }

    /**
     * Adds an item to the file list
     * @param v
     * @param fname
     */
    public void addItem(View v, String fname){
        fileListAdapter.add(fname);
    }

    /**
     * Refreshes the list of files in the app directory.
     */
    private void refreshFileList(){
        fileList = scanDir();
        fileListStr = simplifyFilenames(fileList);
        fileListAdapter.notifyDataSetChanged();
    }

    /**
     * Scans the app's file directory for files
     * @return ArrayList of files
     */
    private ArrayList<File> scanDir(){
        ArrayList<File> files = new ArrayList<>();
        File[] subFiles = recordDir.listFiles();
        //System.out.println("DIR: " + videosDir.toString());
        if(subFiles != null){
            for(File file : subFiles){
                files.add(file);
                //System.out.println("FILE: " + file.getAbsolutePath());
            }
        }
        return files;
    }

    /**
     * Converts a list of File objects to their simplified filenames for display
     * @param files
     * @return
     */
    private ArrayList<String> simplifyFilenames(ArrayList<File> files){
        ArrayList<String> fnames = new ArrayList<>();
        for(int i = 0; i <files.size(); i++){
            fnames.add(files.get(i).getName());
        }
        return fnames;
    }
}
