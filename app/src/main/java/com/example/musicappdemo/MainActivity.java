package com.example.musicappdemo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * created by ketan 22-10-2020
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnGetList;
    private ListView songlist;
    private ArrayList<String> filenames;
    private ArrayAdapter<String> adapter;

    private View view;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();
        setupEvents();
        showSongsList();
    }

    /**
     * set up all the ui elements of the screen here
     */
    private void setupUI() {
        filenames = new ArrayList<>(); // initialization of the arraylist for songs
        songlist = findViewById(R.id.songlist);//listview widget
        btnGetList = findViewById(R.id.btn_get_songs);
    }

    /**
     * set up all the click events here
     */
    private void setupEvents() {
        btnGetList.setOnClickListener(this);
        btnGetList.setVisibility(View.GONE);

        songlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,MusicPlayerActivity.class);
                intent.putExtra("songIndex",position);
                MainActivity.this.setResult(100, intent);
                MainActivity.this.finish();
            }
        });
    }

    /**
     * get the song list from storage here
     */
    public void getSongListFromStorage() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                filenames.add(thisTitle);
                Log.e("thisId", ":" + thisId);
                Log.e("thisTitle", ":" + thisTitle);
                Log.e("thisArtist", ":" + thisArtist);
            } while (musicCursor.moveToNext());
        }
    }

    /**
     * handle the click events of the views here
     * @param v
     */
    @Override
    public void onClick(View v) {
        view = v;
        int id = v.getId();
        switch (id) {
            case R.id.btn_get_songs:
                if (checkPermission()) {
                    Snackbar.make(view, "Permission already granted.", Snackbar.LENGTH_LONG).show();
                    showSongsList();
                } else {
                    requestPermission();
                }
                break;
        }
    }

    /**
     * set songs list to the list view
     */
    private void showSongsList() {
        getSongListFromStorage();
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, filenames);
        songlist.setAdapter(adapter);
    }

    /**
     * check for the permissions read and write external storage at run time
     * @return
     */
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * request permission if the permissions are not granted previously or
     * the application is installed first time on the device
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    /**
     * onRequest permissions
     * check for the permission request code
     * if the permissions are granted show the message on snackbar like permission granted now you can access storage data
     * else show the message permission denied you cannot access storage data.
     * and it will show alert dialog box with message you need to allow access to both the permissions
     * if you press ok the it will again request the permission
     * else it will close
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted)
                        Snackbar.make(view, "Permission Granted, Now you can access storage data.", Snackbar.LENGTH_LONG).show();
                    else {
                        Snackbar.make(view, "Permission Denied, You cannot access storage data.", Snackbar.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }


    /**
     * show message ok cancel
     * shows the alertdialog box with ok and cancel button
     * on ok click it will request permissions
     * on cancel click it will be closed
     */
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}