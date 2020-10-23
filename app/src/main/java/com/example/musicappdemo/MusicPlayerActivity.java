package com.example.musicappdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static java.nio.file.Paths.get;

/**
 * created by ketan 22-10-2020
 */
public class MusicPlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnPlaylist;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    // Media Player
    private MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();
    ;
    private Utilities utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = (long) MusicPlayerActivity.this.mp.getDuration();
            long currentDuration = (long) MusicPlayerActivity.this.mp.getCurrentPosition();
            MusicPlayerActivity.this.songTotalDurationLabel.setText(MusicPlayerActivity.this.utils.milliSecondsToTimer(totalDuration));
            MusicPlayerActivity.this.songCurrentDurationLabel.setText(MusicPlayerActivity.this.utils.milliSecondsToTimer(currentDuration));
            int progress = MusicPlayerActivity.this.utils.getProgressPercentage(currentDuration, totalDuration);
            MusicPlayerActivity.this.songProgressBar.setProgress(progress);
            MusicPlayerActivity.this.mHandler.postDelayed(this, 100L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        setupUI();
        setupEvents();
        getSongListFromStorage();
        mediaPlayerEvents();
    }

    private void mediaPlayerEvents() {
        // Mediaplayer
        mp = new MediaPlayer();
        utils = new Utilities();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this); // Important

        // By default play first song
        playSong(0);
    }

    /**
     * Receiving song index from playlist view
     * and play the song
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            currentSongIndex = data.getExtras().getInt("songIndex");
            // play selected song
            playSong(currentSongIndex);
        }

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
            do {
                int titleColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.TITLE);
                String data = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String thisTitle = musicCursor.getString(titleColumn);
                HashMap<String, String> song = new HashMap();
                song.put("songTitle", thisTitle);
                song.put("songPath", data);
                songsList.add(song);
            } while (musicCursor.moveToNext());
        }
    }

    private void setupUI() {
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
    }

    private void setupEvents() {
/**
 * Play button click event
 * plays a song and changes button to pause image
 * pauses a song and changes button to play image
 * */
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check for already playing
                if (mp.isPlaying()) {
                    if (mp != null) {
                        mp.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.btn_play);
                    }
                } else {
                    // Resume song
                    if (mp != null) {
                        mp.start();
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    }
                }

            }
        });

        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
        btnForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mp.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if (currentPosition + seekForwardTime <= mp.getDuration()) {
                    // forward song
                    mp.seekTo(currentPosition + seekForwardTime);
                } else {
                    // forward to end position
                    mp.seekTo(mp.getDuration());
                }
            }
        });

        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
        btnBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mp.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if (currentPosition - seekBackwardTime >= 0) {
                    // forward song
                    mp.seekTo(currentPosition - seekBackwardTime);
                } else {
                    // backward to starting position
                    mp.seekTo(0);
                }

            }
        });

        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check if next song is there or not
                if (currentSongIndex < (songsList.size() - 1)) {
                    playSong(currentSongIndex + 1);
                    currentSongIndex = currentSongIndex + 1;
                } else {
                    // play first song
                    playSong(0);
                    currentSongIndex = 0;
                }

            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (currentSongIndex > 0) {
                    playSong(currentSongIndex - 1);
                    currentSongIndex = currentSongIndex - 1;
                } else {
                    // play last song
                    playSong(songsList.size() - 1);
                    currentSongIndex = songsList.size() - 1;
                }

            }
        });

        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isRepeat) {
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                } else {
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                }
            }
        });

        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isShuffle) {
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
                } else {
                    // make repeat to true
                    isShuffle = true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                    btnRepeat.setImageResource(R.drawable.btn_repeat);
                }
            }
        });

        /**
         * Button Click event for Play list click event
         * Launches list activity which displays list of songs
         * */
        btnPlaylist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivityForResult(i, 100);
            }
        });
    }

    public void playSong(int songIndex) {
        try {
            this.mp.reset();
            this.mp.setDataSource(songsList.get(songIndex).get("songPath"));
            this.mp.prepare();
            this.mp.start();
            String songTitle = (String) ((HashMap) this.songsList.get(songIndex)).get("songTitle");
            this.songTitleLabel.setText(songTitle);
            this.btnPlay.setImageResource(R.drawable.btn_pause);
            this.songProgressBar.setProgress(0);
            this.songProgressBar.setMax(100);
            this.updateProgressBar();
        } catch (IllegalArgumentException var3) {
            var3.printStackTrace();
        } catch (IllegalStateException var4) {
            var4.printStackTrace();
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }

    public void updateProgressBar() {
        this.mHandler.postDelayed(this.mUpdateTimeTask, 100L);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        this.mHandler.removeCallbacks(this.mUpdateTimeTask);
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        this.mHandler.removeCallbacks(this.mUpdateTimeTask);
        int totalDuration = this.mp.getDuration();
        int currentPosition = this.utils.progressToTimer(seekBar.getProgress(), totalDuration);
        this.mp.seekTo(currentPosition);
        this.updateProgressBar();
    }

    public void onCompletion(MediaPlayer arg0) {
        if (this.isRepeat) {
            this.playSong(this.currentSongIndex);
        } else if (this.isShuffle) {
            Random rand = new Random();
            this.currentSongIndex = rand.nextInt(this.songsList.size() - 1 - 0 + 1) + 0;
            this.playSong(this.currentSongIndex);
        } else if (this.currentSongIndex < this.songsList.size() - 1) {
            this.playSong(this.currentSongIndex + 1);
            ++this.currentSongIndex;
        } else {
            this.playSong(0);
            this.currentSongIndex = 0;
        }

    }

    public void onDestroy() {
        super.onDestroy();
        this.mp.release();
    }

}