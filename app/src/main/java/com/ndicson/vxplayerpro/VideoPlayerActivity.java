package com.ndicson.vxplayerpro;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class VideoPlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.MediaPlayerControlListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnCompletionListener , SeekBar.OnSeekBarChangeListener {

    private final static String TAG = "MainActivity";
    ResizeSurfaceView mVideoSurface;
    MediaPlayer mMediaPlayer;
    VideoControllerView controller;

    private String LOG_TAG;
    private String media;
    private int mVideoWidth;
    private int mVideoHeight;
    private View mContentView;
    private View mLoadingView;
    private boolean mIsComplete;
    
    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnPlaylist;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar videoProgressBar;
    private TextView videoTitleLabel;
    private TextView videoCurrentDurationLabel;
    private TextView videoTotalDurationLabel;
    // Media Player
    private MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();
    ;
    private VideoManager videoManager;
    private Vutils utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentVideoIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ArrayList<HashMap<String, String>> videoList = new ArrayList<HashMap<String, String>>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
/*
        // All player buttons
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        videoProgressBar = (SeekBar) findViewById(R.id.videoProgressBar);
        videoTitleLabel = (TextView) findViewById(R.id.videoTitle);
        videoCurrentDurationLabel = (TextView) findViewById(R.id.videoCurrentDurationLabel);
        videoTotalDurationLabel = (TextView) findViewById(R.id.videoTotalDurationLabel);
*/

        mVideoSurface = (ResizeSurfaceView) findViewById(R.id.videoSurface);
        mContentView = findViewById(R.id.video_container);
        mLoadingView = findViewById(R.id.loading);
        SurfaceHolder videoHolder = mVideoSurface.getHolder();
        videoHolder.addCallback(this);

        // Mediaplayer
        mp = new MediaPlayer();
        videoManager = new VideoManager();
        utils = new Vutils();
        //mMediaPlayer.setOnVideoSizeChangedListener(this);
        //(FrameLayout) findViewById(R.id.videoSurfaceContainer)
        controller = new VideoControllerView.Builder(this, this)
                .withVideoTitle("Buck Bunny")
                .withVideoSurfaceView(mVideoSurface)//to enable toggle display controller view
                .canControlBrightness(true)
                .canControlVolume(true)
                .canSeekVideo(true)
                .exitIcon(R.drawable.video_top_back)
                .pauseIcon(R.drawable.btn_pause)
                .playIcon(R.drawable.btn_play)
                .shrinkIcon(R.drawable.ic_media_fullscreen_shrink)
                .stretchIcon(R.drawable.ic_media_fullscreen_stretch)
                .build((FrameLayout) findViewById(R.id.videoSurfaceContainer));//layout container that hold video play view

        mLoadingView.setVisibility(View.VISIBLE);

        // Listeners
        videoProgressBar.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this); // Important

        // Getting all video list
        videoList = videoManager.getPlayList();

        // By default play first video
        playVideo(0);

        /**
         * Play button click event
         * plays a video and changes button to pause image
         * pauses a video and changes button to play image
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
                    // Resume video
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
         * Forwards video specified seconds
         * */
        btnForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current video position				
                int currentPosition = mp.getCurrentPosition();
                // check if seekForward time is lesser than video duration
                if (currentPosition + seekForwardTime <= mp.getDuration()) {
                    // forward video
                    mp.seekTo(currentPosition + seekForwardTime);
                } else {
                    // forward to end position
                    mp.seekTo(mp.getDuration());
                }
            }
        });

        /**
         * Backward button click event
         * Backward video to specified seconds
         * */
        btnBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current video position				
                int currentPosition = mp.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if (currentPosition - seekBackwardTime >= 0) {
                    // forward video
                    mp.seekTo(currentPosition - seekBackwardTime);
                } else {
                    // backward to starting position
                    mp.seekTo(0);
                }

            }
        });

        /**
         * Next button click event
         * Plays next video by taking currentVideoIndex + 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check if next video is there or not
                if (currentVideoIndex < (videoList.size() - 1)) {
                    playVideo(currentVideoIndex + 1);
                    currentVideoIndex = currentVideoIndex + 1;
                } else {
                    // play first video
                    playVideo(0);
                    currentVideoIndex = 0;
                }

            }
        });

        /**
         * Back button click event
         * Plays previous video by currentVideoIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (currentVideoIndex > 0) {
                    playVideo(currentVideoIndex - 1);
                    currentVideoIndex = currentVideoIndex - 1;
                } else {
                    // play last video
                    playVideo(videoList.size() - 1);
                    currentVideoIndex = videoList.size() - 1;
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
         * Launches list activity which displays list of video
         * */
        btnPlaylist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
                startActivityForResult(i, 100);
            }
        });

    }

    /**
     * Receiving video index from playlist view
     * and play the video
     */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            currentVideoIndex = data.getExtras().getInt("videoIndex");
            // play selected video
            playVideo(currentVideoIndex);
        }

    }

    /**
     * Function to play a video
     *
     * @param videoIndex - index of video
     */
    public void playVideo(int videoIndex) {
        // Play video
        try {
            mp.reset();
            mp.setDataSource(videoList.get(videoIndex).get("videoPath"));
            mp.prepare();
            mp.start();
            // Displaying Video title
            String videoTitle = videoList.get(videoIndex).get("videoTitle");
            videoTitleLabel.setText(videoTitle);

            // Changing Button Image to pause image
            btnPlay.setImageResource(R.drawable.btn_pause);

            // set Progress bar values
            videoProgressBar.setProgress(0);
            videoProgressBar.setMax(100);

            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mVideoSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                controller.toggleControllerView();
                return false;
            }
        });
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            // Displaying Total Duration time
            videoTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            videoCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            videoProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mp.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    /**
     * On Video Playing completed
     * if repeat is ON play same video again
     * if shuffle is ON play random video
     */
    @Override
    public void onCompletion(MediaPlayer arg0) {

        mIsComplete = true;

        // check for repeat is ON or OFF
        if (isRepeat) {
            // repeat is on play same video again
            playVideo(currentVideoIndex);
        } else if (isShuffle) {
            // shuffle is on - play a random video
            Random rand = new Random();
            currentVideoIndex = rand.nextInt((videoList.size() - 1) - 0 + 1) + 0;
            playVideo(currentVideoIndex);
        } else {
            // no repeat or shuffle ON - play next video
            if (currentVideoIndex < (videoList.size() - 1)) {
                playVideo(currentVideoIndex + 1);
                currentVideoIndex = currentVideoIndex + 1;
            } else {
                // play first video
                playVideo(0);
                currentVideoIndex = 0;
            }
        }
    }


    /**
     * Implementing SurfaceHolder.Callback methonds
     */

    @Override
    public void onDestroy() {
        super.onDestroy();
        mp.release();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
        mMediaPlayer.prepareAsync();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        resetPlayer();

    }
//================##### End SurfaceHolder.Callback Implementation ####=============================//

    /**
     * Implement VideoMediaController.MediaPlayerControl
     */

    @Override
    public void start() {
        if(null != mMediaPlayer) {
            mMediaPlayer.start();
            mIsComplete = false;
        }

    }

    @Override
    public void pause() {
        if(null != mMediaPlayer) {
            mMediaPlayer.pause();
        }

    }

    @Override
    public int getDuration() {
        if(null != mMediaPlayer)
            return mMediaPlayer.getDuration();
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(null != mMediaPlayer)
            return mMediaPlayer.getCurrentPosition();
        else
            return 0;
    }

    @Override
    public void seekTo(int position) {
        if(null != mMediaPlayer) {
            mMediaPlayer.seekTo(position);
        }

    }

    @Override
    public boolean isPlaying() {
        if(null != mMediaPlayer)
            return mMediaPlayer.isPlaying();
        else
            return false;
    }

    @Override
    public boolean isComplete() {
        return mIsComplete;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean isFullScreen() {
        return getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ? true : false;
    }

    @Override
    public void toggleFullScreen() {
        if(isFullScreen()){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

    }

    @Override
    public void exit() {
        resetPlayer();
        finish();

    }

//================##### End VideoMediaController.MediaPlayerControl Implementation ####=============================//

    /**
     * Implement MediaPlayer.OnVideoSizeChangedListener method
     */

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        mVideoHeight = mp.getVideoHeight();
        mVideoWidth = mp.getVideoWidth();
        if (mVideoHeight > 0 && mVideoWidth > 0){
            mVideoSurface.adjustSize(mContentView.getWidth(), mContentView.getHeight(), mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
        }


}

//================##### End MediaPlayer.OnVideoSizeChangedListener method Implementation ####=============================//

    /**
     * Implement MediaPlayer.OnPreparedListener method
     */

    @Override
    public void onPrepared(MediaPlayer mp) {

    }


//================##### End MediaPlayer.OnPreparedListener, method Implementation ####=============================//

    /**
     * Implement Other method
     */

    private void resetPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }


//================##### End Other method Implementation ####=============================//

}