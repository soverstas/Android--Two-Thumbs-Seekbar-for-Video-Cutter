package com.example.TwoThumbsSeekBarActivity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

public class MyActivity extends Activity {
    TextView textViewLeft, textViewRight;
    VideoSliceSeekBar videoSliceSeekBar;
    VideoView videoView;
    View videoControlBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        textViewLeft = (TextView) findViewById(R.id.left_pointer);
        textViewRight = (TextView) findViewById(R.id.right_pointer);

        videoSliceSeekBar = (VideoSliceSeekBar) findViewById(R.id.seek_bar);
        videoView = (VideoView) findViewById(R.id.video);
        videoControlBtn = findViewById(R.id.video_control_btn);

        initVideoView();
    }

    private void initVideoView() {
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoSliceSeekBar.setSeekBarChangeListener(new VideoSliceSeekBar.SeekBarChangeListener() {
                    @Override
                    public void SeekBarValueChanged(int leftThumb, int rightThumb) {
                        textViewLeft.setText(getTimeForTrackFormat(leftThumb, true));
                        textViewRight.setText(getTimeForTrackFormat(rightThumb, true));
                    }
                });

                videoSliceSeekBar.setMaxValue(mp.getDuration());
                videoSliceSeekBar.setLeftProgress(0);
                videoSliceSeekBar.setRightProgress(mp.getDuration());
                videoSliceSeekBar.setProgressMinDiff(mp.getDuration() / 10);

                videoControlBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        performVideoViewClick();
                    }
                });
            }
        });
        videoView.setVideoURI(Uri.parse("android.resource://com.example.TwoThumbsSeekBarActivity/" + R.raw.video));
    }


    private void performVideoViewClick() {
        if (videoView.isPlaying()) {
            videoView.pause();
            videoSliceSeekBar.setSliceBlocked(false);
            videoSliceSeekBar.removeVideoStatusThumb();
        } else {
            videoView.seekTo(videoSliceSeekBar.getLeftProgress());
            videoView.start();
            videoSliceSeekBar.setSliceBlocked(true);
            videoSliceSeekBar.videoPlayingProgress(videoSliceSeekBar.getLeftProgress());
            videoStateObserver.startVideoProgressObserving();
        }
    }

    public static String getTimeForTrackFormat(int timeInMills, boolean display2DigitsInMinsSection) {
        int minutes = (timeInMills / (60 * 1000));
        int seconds = (timeInMills - minutes * 60 * 1000) / 1000;
        String result = display2DigitsInMinsSection && minutes < 10 ? "0" : "";
        result += minutes + ":";
        if (seconds < 10) {
            result += "0" + seconds;
        } else {
            result += seconds;
        }
        return result;
    }


    private StateObserver videoStateObserver = new StateObserver();

    private class StateObserver extends Handler {

        private boolean alreadyStarted = false;

        private void startVideoProgressObserving() {
            if (!alreadyStarted) {
                alreadyStarted = true;
                sendEmptyMessage(0);
            }
        }

        private Runnable observerWork = new Runnable() {
            @Override
            public void run() {
                startVideoProgressObserving();
            }
        };

        @Override
        public void handleMessage(Message msg) {
            alreadyStarted = false;
            videoSliceSeekBar.videoPlayingProgress(videoView.getCurrentPosition());
            if (videoView.isPlaying() && videoView.getCurrentPosition() < videoSliceSeekBar.getRightProgress()) {
                postDelayed(observerWork, 50);
            } else {

                if (videoView.isPlaying()) videoView.pause();

                videoSliceSeekBar.setSliceBlocked(false);
                videoSliceSeekBar.removeVideoStatusThumb();
            }
        }
    }
}
