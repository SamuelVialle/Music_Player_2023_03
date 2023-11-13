package com.samuelvialle.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    /**
     * Add globals vars
     **/
    private RecyclerView rvSongs;
    private ImageView btnPrevious, btnPlay, btnNext;
    private TextView tvSongTitle, tvCurrentPos, tvTotalDuration;
    private SeekBar sbPosition;
    private MediaPlayer mediaPlayer;
    private ArrayList<ModelSong> songArrayList;
    private AdapterSong adapterSong;
    private LinearLayoutManager linearLayoutManager;
    private double currentPosition, totalDuration;

    /**
     * Init method to initialize all the widgets
     **/
    private void init() {


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

        linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
    }


    /**
     * Check permissions
     **/
    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_DENIED) { // Manifest ANDROID
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
        if (requestCode == 100) {
            // Check if permissions are granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read music folder is granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Read music folder denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Method to get the audio files from the terminal
     **/
    private void getAudioFiles() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projections = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        // ==> if (is_music != 0) {}

        Cursor cursor = contentResolver.query(uri, projections, selection, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                long album_id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                Uri uriCoverFolder = Uri.parse("content://media/external/audio/albumart");
                Uri uriAlbumArt = ContentUris.withAppendedId(uriCoverFolder, album_id);

                // Associer les données ci-dessus au ArrayList en utilisant le modèle
                ModelSong modelSong = new ModelSong();
                modelSong.setSongTitle(title);
                modelSong.setSongAlbum(album);
                modelSong.setSongArtist(artist);
                modelSong.setSongDuration(duration);
                modelSong.setSongUri(Uri.parse(data));
                modelSong.setSongCover(uriAlbumArt);

                songArrayList.add(modelSong);
            } while (cursor.moveToNext());
        }
    }


    private void manageRv() {
        adapterSong = new AdapterSong(MainActivity.this, songArrayList);
        rvSongs.setLayoutManager(linearLayoutManager);
        rvSongs.setAdapter(adapterSong);

        adapterSong.setMyOnItemClickListener(new AdapterSong.MyOnItemClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                playSong(position);
                Log.i("TAG", "onItemClick: " + position);
            }
        });
    }

    private void playSong(int position) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, songArrayList.get(position).getSongUri());
            mediaPlayer.prepare();
            mediaPlayer.start();

            btnPlay.setImageResource(R.drawable.ic_pause_48_w);
            tvSongTitle.setText(songArrayList.get(position).getSongTitle());

        } catch (Exception e) {
            e.printStackTrace();
        }
        songProgress(); // setSong Method ;)
    }

    private void songProgress() {
        currentPosition = mediaPlayer.getCurrentPosition();
        totalDuration = mediaPlayer.getDuration();

        // Assignation aux text view des temps qui leurs sont appartis
        // Autre solution en utilisant TimeUnit pour convertir les millis en ce que l'on veut ;) Merci Nadia
        // tvCurrentPos.setText((int) TimeUnit.MICROSECONDS.toHours((long) currentPosition));
        tvCurrentPos.setText(timerConvertion((long) currentPosition));
        tvTotalDuration.setText(timerConvertion((long) totalDuration));

        sbPosition.setMax((int) totalDuration);

        // Gestion du thread qui va s'occuper du temps du "temps réel"
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    currentPosition = mediaPlayer.getCurrentPosition();
                    tvCurrentPos.setText(timerConvertion((long) currentPosition));
                    sbPosition.setProgress((int) currentPosition);
                    handler.postDelayed(this, 1000);
                } catch (IllegalThreadStateException ie) {
                    ie.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    public String timerConvertion(long value) {
        String songDuration;
        int dur = (int) value; // duration in millis
        int hrs = dur / 3600000;
        int mns = (dur / 60000);
        int scs = (dur / 1000) % 60;

        if (hrs > 0) {
            songDuration = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            songDuration = String.format("%02d:%02d", mns, scs);
        }
        return songDuration;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init(); // initialiser les compoments
        checkPermission();
        getAudioFiles();
        manageRv();
    }
}