package com.samuelvialle.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    /** Add globals vars **/
    private RecyclerView rvSongs;
    private ImageView btnPrevious, btnPlay, btnNext;
    private TextView tvSongTitle, tvCurrentPos, tvTotalDuration;
    private SeekBar sbPosition;
    private MediaPlayer mediaPlayer;
    private ArrayList<ModelSong> songArrayList;

    /** Init method to initialize all the widgets **/
    private void init(){
        rvSongs = findViewById(R.id.rv_songs);
        btnPrevious = findViewById(R.id.iv_btn_previous);
        btnPlay = findViewById(R.id.iv_btn_play);
        btnNext = findViewById(R.id.iv_btn_next);
        tvSongTitle = findViewById(R.id.tv_song_title);
        tvCurrentPos = findViewById(R.id.tv_current_pos);
        tvTotalDuration = findViewById(R.id.tv_total_duration);
        sbPosition = findViewById(R.id.sb_position);

        mediaPlayer = new MediaPlayer();
        songArrayList = new ArrayList<>();
    }


    /** Check permissions **/
    public void checkPermission(){
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_DENIED){ // Manifest ANDROID
            // If permission is not granted then we are requesting for the permission on below line.
            ActivityCompat.requestPermissions
                    (MainActivity.this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 100);
        } else {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            // Here we gonna put the method to play songs
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // on below line we are checking for the request code
        if(requestCode == 100) {
            // Check if permissions are granted
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Read music folder is granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Read music folder denied", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();
    }
}