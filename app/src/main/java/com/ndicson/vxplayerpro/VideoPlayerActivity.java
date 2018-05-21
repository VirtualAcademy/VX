package com.ndicson.vxplayerpro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class VideoPlayerActivity extends AppCompatActivity implements  SurfaceHolder.Callback, VideoControllerView.MediaPlayerControlListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnCompletionListener {

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
//    private MediaPlayer mMediaPlayer;
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
    private VideoControllerView.MediaPlayerControlListener mMediaControlListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // All player buttons
//        btnPlay = (ImageButton) findViewById(R.id.btn_play_pause);
//        btnForward = (ImageButton) findViewById(R.id.btn_forward);
//        btnBackward = (ImageButton) findViewById(R.id.btn_backward);
//        btnNext = (ImageButton) findViewById(R.id.btn_skipfwd);
//        btnPrevious = (ImageButton) findViewById(R.id.btn_skipback);
//        btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
//        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
//        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
//        videoProgressBar = (SeekBar) findViewById(R.id.bottom_seekbar);
//        videoTitleLabel = (TextView) findViewById(R.id.videoTitle);
//        videoCurrentDurationLabel = (TextView) findViewById(R.id.videoCurrentDurationLabel);
//        videoTotalDurationLabel = (TextView) findViewById(R.id.videoTotalDurationLabel);


        mVideoSurface = (ResizeSurfaceView) findViewById(R.id.videoSurface);
        mContentView = findViewById(R.id.video_container);
        mLoadingView = findViewById(R.id.loading);
        SurfaceHolder videoHolder = mVideoSurface.getHolder();
        videoHolder.addCallback(this);

        // Mediaplayer
        mMediaPlayer = new MediaPlayer();
        videoManager = new VideoManager();
        utils = new Vutils();

        // Getting all video list
        videoList = videoManager.getPlayList();

        // By default play first video
        playVideo(0);

        /**
         * Play button click event
         * plays a video and changes button to pause image
         * pauses a video and changes button to play image
         * */

//        /**
//         * Button Click event for Play list click event
//         * Launches list activity which displays list of video
//         * */
//        btnPlaylist.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
//                startActivityForResult(i, 100);
//            }
//        });
//
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
            // Listeners
//            videoProgressBar.setOnSeekBarChangeListener(this); // Important
            mMediaPlayer.setOnVideoSizeChangedListener(this);
            mMediaPlayer.setOnCompletionListener(this); // Important

            // Displaying Video title
            String videoTitle = videoList.get(videoIndex).get("videoTitle");
//            videoTitleLabel.setText(videoTitle);

            controller = new VideoControllerView.Builder(this, this)
                    .withVideoTitle(videoTitle)
                    .withVideoSurfaceView(mVideoSurface)//to enable toggle display controller view
                    .canControlBrightness(true)
                    .canControlVolume(true)
                    .canSeekVideo(true)
                    .exitIcon(R.drawable.video_top_back)
                    .pauseIcon(R.drawable.btn_pause)
                    .playIcon(R.drawable.btn_play)
                    .playListIcon(R.drawable.btn_playlist)
                    .shrinkIcon(R.drawable.ic_media_fullscreen_shrink)
                    .stretchIcon(R.drawable.ic_media_fullscreen_stretch)
                    .build((FrameLayout) findViewById(R.id.videoSurfaceContainer));//layout container that hold video play view

//            controller.setOnClickListener(mlister);

            mLoadingView.setVisibility(View.VISIBLE);


            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.reset();
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setDataSource(this, Uri.parse(videoList.get(videoIndex).get("videoPath")));
            mMediaPlayer.setOnCompletionListener(this);

//            controller.setMediaPlayerControlListener(mMediaControlListener);

//            mMediaPlayer.prepare();
//            mMediaPlayer.start();

//            // Changing Button Image to pause image
//            btnPlay.setImageResource(R.drawable.btn_pause);//need to verify****

//            // set Progress bar values
//            videoProgressBar.setProgress(0);
//            videoProgressBar.setMax(100);

//            // Updating progress bar
//            updateProgressBar();
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


    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        controller.show();
        return false;
    }

//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mMediaPlayer.release();
//    }

    /**
     * Implementing SurfaceHolder.Callback methonds
     */

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

    /**
     * On Video Playing completed
     * if repeat is ON play same video again
     * if shuffle is ON play random video
     */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mIsComplete = true;
    }

//================##### End VideoMediaController.MediaPlayerControl Implementation ####=============================//

    /**
     * Implement MediaPlayer.OnVideoSizeChangedListener method
     */

    @Override
    public void onVideoSizeChanged(MediaPlayer mMediaPlayer, int width, int height) {
        mVideoHeight = mMediaPlayer.getVideoHeight();
        mVideoWidth = mMediaPlayer.getVideoWidth();
        if (mVideoHeight > 0 && mVideoWidth > 0){
            mVideoSurface.adjustSize(mContentView.getWidth(), mContentView.getHeight(), mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
        }


}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mVideoWidth > 0 && mVideoHeight > 0)
            mVideoSurface.adjustSize(getDeviceWidth(this),getDeviceHeight(this),mVideoSurface.getWidth(), mVideoSurface.getHeight());
    }

//================##### End MediaPlayer.OnVideoSizeChangedListener method Implementation ####=============================//

    /**
     * Implement MediaPlayer.OnPreparedListener method
     */

    @Override
    public void onPrepared(MediaPlayer mMediaPlayer) {

        //setup video controller view
        mLoadingView.setVisibility(View.GONE);
        mVideoSurface.setVisibility(View.VISIBLE);
        mMediaPlayer.start();
        mIsComplete = false;
    }

//================##### End MediaPlayer.OnPreparedListener, method Implementation ####=============================//

    /**
//     * Implement Seek bar change listener method
//     */
//
//
//
//    /**
//     *
//     * */
//    @Override
//    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
//
//    }
//
//    /**
//     * When user starts moving the progress handler
//     */
//    @Override
//    public void onStartTrackingTouch(SeekBar seekBar) {
//        // remove message Handler from updating progress bar
//        mHandler.removeCallbacks(mUpdateTimeTask);
//    }
//
//    /**
//     * When user stops moving the progress hanlder
//     */
//    @Override
//    public void onStopTrackingTouch(SeekBar seekBar) {
//        mHandler.removeCallbacks(mUpdateTimeTask);
//        int totalDuration = mMediaPlayer.getDuration();
//        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
//
//        // forward or backward to certain seconds
//        mMediaPlayer.seekTo(currentPosition);
//
//        // update timer progress again
//        updateProgressBar();
//    }
//
//
////================##### End Seek bar change listener method Implementation ####=============================//

    /**
     *  Other method
     */

    private void resetPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public static int getDeviceWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(mDisplayMetrics);
        return mDisplayMetrics.widthPixels;
    }

    public static int getDeviceHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(mDisplayMetrics);
        return mDisplayMetrics.heightPixels;
    }

    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }


    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mMediaPlayer.getDuration();
            long currentDuration = mMediaPlayer.getCurrentPosition();

            // Displaying Total Duration time
            videoTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            videoCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            videoProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };
//
//    View.OnClickListener mlister = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            String b = String.valueOf(v.getId());
//            Toast.makeText(getApplicationContext(), b, Toast.LENGTH_LONG);
////            switch (v.getId()) {
////                case 1:
////            }
//        }
//    };
//================##### End Other method Implementation ####=============================//

}