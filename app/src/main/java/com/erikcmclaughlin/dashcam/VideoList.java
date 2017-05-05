package com.erikcmclaughlin.dashcam;

import android.R.layout;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.*;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VideoList extends AppCompatActivity{

    private ArrayList<File> fileList = new ArrayList<>();
    private ArrayList<String> fileListStr = new ArrayList<String>();
    private ArrayAdapter<String> fileListAdapter;

    // File output directories
    private File videosDir;
    private File recordDir;
    private File savedDir;

    private boolean recording = false;
    private Date recordTime;

    private boolean enable_auto_rec;
    private boolean enable_maps;
    private int auto_delete_days;

    private CameraManager cameraManager;
    private CameraDevice camera;
    private MediaRecorder mediaRecorder = new MediaRecorder();
    private CaptureRequest captureRequest;
    private CameraCaptureSession captureSession;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        enable_auto_rec = pref.getBoolean("auto_detect_driving",true);
        enable_maps = pref.getBoolean("enable_maps_recording", true);
        auto_delete_days = Integer.parseInt(pref.getString("auto_delete", ""));

        System.out.println("Auto-record: " + enable_auto_rec);
        System.out.println("Enable maps: " + enable_maps);
        System.out.println("Auto-delete: " + auto_delete_days);



        // Set up directory structure
        videosDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.root_dir_name));
        recordDir = new File(videosDir, getString(R.string.record_dir_name));
        savedDir = new File(videosDir, getString(R.string.saved_dir_name));

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

                if(!recording) {
                    startRecord();
                    Snackbar.make(view, R.string.snackbar_start_text, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    fab.setImageResource(R.drawable.ic_stop_black_24dp);
                    recording = true;
                }
                else{
                    stopRecord();
                    Snackbar.make(view, R.string.snackbar_stop_text, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    fab.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    recording = false;
                }
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
            alertBuilder.setMessage(R.string.about_text).setTitle(R.string.about_title);
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
     */
    private void startRecord(){
        recordTime = new Date();
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        String fname = new SimpleDateFormat(getString(R.string.date_code_format)).format(recordTime) + getString(R.string.output_file_ext);
        try{
            final File outputFile = new File(recordDir, fname);
            outputFile.createNewFile();


            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            mediaRecorder.setMaxFileSize(0);

            mediaRecorder.setOrientationHint(0);
            mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
            mediaRecorder.prepare();

            String[] cameras = cameraManager.getCameraIdList();
            cameraManager.openCamera(cameras[0], new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice cameraDevice) {
                    camera = cameraDevice;
                    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
                    mediaRecorder.setMaxFileSize(0);
                    mediaRecorder.setOrientationHint(0);

                    try{
                        mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
                        mediaRecorder.prepare();

                        List<Surface> surfaceList = new ArrayList<>();
                        surfaceList.add(mediaRecorder.getSurface());
                        final CaptureRequest.Builder captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
                        captureRequestBuilder.addTarget(mediaRecorder.getSurface());
                        captureRequest = captureRequestBuilder.build();
                        camera.createCaptureSession(surfaceList, new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                                captureSession = cameraCaptureSession;
                            }

                            @Override
                            public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                                captureSession = cameraCaptureSession;
                            }
                        }, null);



                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDisconnected(CameraDevice cameraDevice) {

                }

                @Override
                public void onError(CameraDevice cameraDevice, int i) {

                }
            }, null);

            // Start capture session
            mediaRecorder.start();
            captureSession.setRepeatingRequest(captureRequest, new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                }
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                }
            }, null);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
/*
        try{
            mediaRecorder.start();
            captureSession.setRepeatingRequest(captureRequest, new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                }
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
*/
        /*
        String fname = new SimpleDateFormat(getString(R.string.date_code_format)).format(new Date()) + getString(R.string.output_file_ext);
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
        addItem(fname);
        refreshFileList();
        */
        //addItem(v,fname);
    }

    /**
     * Stops recording by pressing the stop button
     */
    private void stopRecord(){

        try {
            captureSession.stopRepeating();
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            //camera.close();
            mediaRecorder = null;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        /*String fname = new SimpleDateFormat(getString(R.string.date_code_format)).format(recordTime) + getString(R.string.output_file_ext);
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
        addItem(fname);*/
        refreshFileList();
    }
    /**
     * Adds an item to the file list
     * @param fname
     */
    public void addItem(String fname){
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
