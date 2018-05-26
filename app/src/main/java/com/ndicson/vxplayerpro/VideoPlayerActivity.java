package com.ndicson.vxplayerpro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;


public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.MediaPlayerControlListener, MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnCompletionListener {

    private final static String TAG = "MainActivity";
    ResizeSurfaceView mVideoSurface;
    MediaPlayer mMediaPlayer;
    VideoControllerView controller;
    private int mVideoWidth;
    private int mVideoHeight;
    private View mContentView;
    private View mLoadingView;
    private boolean mIsComplete;
    private int currentVideoIndex = 0;
    private VideoControllerView.MediaPlayerControlListener mMediaPlayerControlListener;

    private ArrayList<HashMap<String, String>> videoList = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        mVideoSurface = (ResizeSurfaceView) findViewById(R.id.videoSurface);
        mContentView = findViewById(R.id.video_container);
        mLoadingView = findViewById(R.id.loading);
        SurfaceHolder videoHolder = mVideoSurface.getHolder();
        videoHolder.addCallback(this);

        intController("");
        controller.setMediaPlayerControlListener(mMediaPlayerControlListener);
        //Video list
        VideoManager videoManager = new VideoManager();
        videoList = videoManager.getPlayList();
        HashMap<String, String> video = videoList.get(currentVideoIndex);

        playVideo(controller.getChoice());
        //(FrameLayout) findViewById(R.id.videoSurfaceContainer)

//        try {
//            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mMediaPlayer.setDataSource(this, Uri.parse(vidpath));//Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"));
//            mMediaPlayer.setOnPreparedListener(this);
//            mMediaPlayer.setOnCompletionListener(this);
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

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

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        mVideoHeight = mp.getVideoHeight();
        mVideoWidth = mp.getVideoWidth();
        if (mVideoHeight > 0 && mVideoWidth > 0)
            mVideoSurface.adjustSize(mContentView.getWidth(), mContentView.getHeight(), mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mVideoWidth > 0 && mVideoHeight > 0)
            mVideoSurface.adjustSize(getDeviceWidth(this),getDeviceHeight(this),mVideoSurface.getWidth(), mVideoSurface.getHeight());
    }

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


    // Implement SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        resetPlayer();
    }
// End SurfaceHolder.Callback


    // Implement MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mp) {
        //setup video controller view
        mLoadingView.setVisibility(View.GONE);
        mVideoSurface.setVisibility(View.VISIBLE);
        mMediaPlayer.start();
        mIsComplete = false;
    }
// End MediaPlayer.OnPreparedListener

    /**
     * Implement VideoMediaController.MediaPlayerControl
     */

    @Override
    public int getBufferPercentage() {
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
    public int getDuration() {
        if(null != mMediaPlayer)
            return mMediaPlayer.getDuration();
        else
            return 0;
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
    public void pause() {
        if(null != mMediaPlayer) {
            mMediaPlayer.pause();
        }

    }

    @Override
    public void seekTo(int i) {
        if(null != mMediaPlayer) {
            mMediaPlayer.seekTo(i);
        }
    }

    @Override
    public void start() {
        if(null != mMediaPlayer) {
            mMediaPlayer.start();
            mIsComplete = false;
        }
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

    @Override
    public void showPlayList() {
        Intent intent = new Intent(VideoPlayerActivity.this,PlayListActivity.class);
        startActivityForResult(intent,100);
    }

//    @Override
//    public void setVideoLable(String videoLable) {
//        controller.setVideoLable(videoLable);
//    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        mIsComplete = true;
    }

    // End VideoMediaController.MediaPlayerControl



    /**
     * Receiving song index from playlist view
     * and play the song
     * */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
            currentVideoIndex = data.getExtras().getInt("videoIndex");
            // play selected video
            playVideo(currentVideoIndex);
        }

    }

    // initialise controller
    private void intController(String videoTitle){

        controller = new VideoControllerView.Builder(this, this)
                .withVideoTitle(videoTitle)
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
//        controller.showPlayList();
    }

    /**
     * Function to play a song
     * @param currentVideoIndex - index of song
     * */
    private void playVideo(int currentVideoIndex) {

        // Play video
        try {

            mMediaPlayer = new MediaPlayer();

            // Displaying Video title
            String videoTitle = videoList.get(currentVideoIndex).get("videoTitle");

            controller.setVideoLable(videoTitle);
            controller.getId();

            mMediaPlayer.setOnVideoSizeChangedListener(this);

            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(this, Uri.parse(videoList.get(currentVideoIndex).get("videoPath")));
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}