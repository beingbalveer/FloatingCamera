package com.example.android.floatingcamera;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.example.android.floatingcamera.CameraPreview.mCamera;

public class CameraService extends Service {

        String TAG = "log";
        static String format_type = "jpg";
        static String video_size = "640x480";

        WindowManager windowManager;
        WindowManager.LayoutParams rootParams, buttonParams;
        RelativeLayout rootLayout;
        ImageView captureButton, closeButton;


        Camera camera = null;
        Camera.Parameters cameraParams;
        MediaRecorder mMediaRecorder;
        private CameraPreview mPreview;
        public static final int MEDIA_TYPE_IMAGE = 1;
        public static final int MEDIA_TYPE_VIDEO = 2;

        int xMargin = 0;
        int yMargin = 0;
        int statusBarHeight = 0;
        private boolean isRecording = false;
        boolean dragFlag = false;


        SharedPreferences preferences;

        @Override
        public IBinder onBind(Intent arg0) {
                return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {

                return Service.START_STICKY;
        }

        @Override
        public void onCreate() {

                windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                statusBarHeight = (int) (24 * getResources().getDisplayMetrics().density);

                rootLayout = new RelativeLayout(this);
                rootParams = new WindowManager.LayoutParams(300, 400, WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT);
                rootParams.gravity = Gravity.TOP | Gravity.START;

                camera = Camera.open();
                applyCameraSetting();

                mPreview = new CameraPreview(this, camera);
                rootLayout.addView(mPreview);
                addButtons();
                windowManager.addView(rootLayout, rootParams);
                addDragFunction();

        }


        void applyCameraSetting() {
                camera.setDisplayOrientation(90);
                preferences = this.getSharedPreferences("camera_preferences", Context.MODE_PRIVATE);
                cameraParams = camera.getParameters();

                //  camera flash setting
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                        String flashMode = preferences.getString(getString(R.string.flash_key), "auto");
                        if (flashMode.equals("auto"))
                                cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                        else if (flashMode.equals("on"))
                                cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        else if (flashMode.equals("off"))
                                cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                //  camera white balance setting
                String whiteBalance = preferences.getString(getString(R.string.white_balance_key), "Auto");
                switch (whiteBalance) {
                        case "Auto":
                                cameraParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
                                break;
                        case "Daylight":
                                cameraParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
                                break;
                        case "Twilight":
                                cameraParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_TWILIGHT);
                                break;
                        case "Cloudy daylight":
                                cameraParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
                                break;
                        case "Fluorescent":
                                cameraParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_FLUORESCENT);
                                break;
                        case "Incandescent":
                                cameraParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_INCANDESCENT);
                                break;
                        case "Shade":
                                cameraParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_SHADE);
                                break;
                        case "Warm flourescent":
                                cameraParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT);
                                break;
                }

                //  camera color filter setting
                String filter = preferences.getString(getString(R.string.color_filter_key), "none");
                switch (filter) {
                        case "None":
                                cameraParams.setColorEffect(Camera.Parameters.EFFECT_NONE);
                                break;
                        case "Aqua":
                                cameraParams.setColorEffect(Camera.Parameters.EFFECT_AQUA);
                                break;
                        case "Blackboard":
                                cameraParams.setColorEffect(Camera.Parameters.EFFECT_BLACKBOARD);
                                break;
                        case "Mono":
                                cameraParams.setColorEffect(Camera.Parameters.EFFECT_MONO);
                                break;
                        case "Negative":
                                cameraParams.setColorEffect(Camera.Parameters.EFFECT_NEGATIVE);
                                break;
                        case "Posterize":
                                cameraParams.setColorEffect(Camera.Parameters.EFFECT_POSTERIZE);
                                break;
                        case "Sepia":
                                cameraParams.setColorEffect(Camera.Parameters.EFFECT_SEPIA);
                                break;
                        case "Solarize":
                                cameraParams.setColorEffect(Camera.Parameters.EFFECT_SOLARIZE);
                                break;
                        case "Whiteboard":
                                cameraParams.setColorEffect(Camera.Parameters.EFFECT_WHITEBOARD);
                                break;

                }

                // camera picture quality setting
                String picture_quality = preferences.getString(getString(R.string.picture_quality_key), "High");
                switch (picture_quality) {
                        case "Low":
                                cameraParams.setJpegQuality(1);
                                break;
                        case "Medium":
                                cameraParams.setJpegQuality(50);
                                break;
                        case "High":
                                cameraParams.setJpegQuality(100);
                                break;

                }

                // camera picture format setting
                String picture_format = preferences.getString(getString(R.string.picture_format_key), "jpg");
                switch (picture_format) {
                        case "jpg":
                                format_type = ".jpg";
                                break;
                        case "png":
                                format_type = ".png";
                                break;
                }


                //  camera picture size setting
                String picture_size = preferences.getString(getString(R.string.picture_size_key), "640x480");
                String s[] = picture_size.split("x");
                cameraParams.setPictureSize(Integer.parseInt(s[0]), Integer.parseInt(s[1]));

                //  camera video size setting
                video_size = preferences.getString(getString(R.string.video_size_key), "640x480");

                camera.setParameters(cameraParams);

        }


        //=========     layout functions       =======//

        void addButtons() {
                captureButton = new ImageView(this);
                closeButton = new ImageView(this);
                captureButton.setImageResource(R.drawable.bg_capture);
                closeButton.setImageResource(android.R.drawable.ic_delete);

                buttonParams = new WindowManager.LayoutParams(dpToPx(40), dpToPx(40),
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT);
                buttonParams.verticalMargin = dpToPx(8);

                rootLayout.addView(captureButton, buttonParams);
                rootLayout.addView(closeButton, buttonParams);

                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) captureButton.getLayoutParams();
                params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params1.addRule(RelativeLayout.CENTER_HORIZONTAL);
                captureButton.setLayoutParams(params1);


                RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams) closeButton.getLayoutParams();
                params3.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params3.addRule(RelativeLayout.ALIGN_PARENT_END);
                closeButton.setLayoutParams(params3);

                captureButton.setOnClickListener(
                        new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                        if (!isRecording) {
                                                camera.takePicture(null, null, mPicture);
                                                captureButton.setImageResource(R.drawable.bg_capture_red);
                                                new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                mCamera.stopPreview();
                                                                mCamera.startPreview();
                                                                captureButton.setImageResource(R.drawable.bg_capture);

                                                        }
                                                }, 1000);
                                        } else {
                                                mMediaRecorder.stop();
                                                releaseMediaRecorder();
                                                mCamera.lock();
                                                captureButton.setImageResource(R.drawable.bg_capture);
                                                isRecording = false;
                                        }
                                }
                        }
                );

                closeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                if (isRecording) {
                                        mMediaRecorder.stop();
                                        releaseMediaRecorder();
                                        mCamera.lock();
                                        captureButton.setImageResource(R.drawable.bg_capture);
                                        isRecording = false;
                                }
                                stopSelf();
                        }
                });

                captureButton.setOnLongClickListener(
                        new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                        if (isRecording) {
                                                mMediaRecorder.stop();
                                                releaseMediaRecorder();
                                                mCamera.lock();
                                                captureButton.setImageResource(R.drawable.bg_capture);
                                                isRecording = false;
                                        } else {

                                                if (prepareVideoRecorder()) {
                                                        mMediaRecorder.start();
                                                        captureButton.setImageResource(R.drawable.bg_capture_red);
                                                        isRecording = true;
                                                } else {
                                                        releaseMediaRecorder();
                                                }
                                        }
                                        return true;
                                }
                        }
                );


        }

        void addDragFunction() {
                rootLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {

                                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                        xMargin = (int) motionEvent.getX();
                                        yMargin = (int) motionEvent.getY();
                                        dragFlag = rootParams.height - yMargin < dpToPx(20) && rootParams.width - xMargin < dpToPx(20);
                                }

                                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                                        int rawX = (int) motionEvent.getRawX();
                                        int rawY = (int) motionEvent.getRawY();
                                        int x = (int) motionEvent.getX();
                                        int y = (int) motionEvent.getY();
                                        WindowManager.LayoutParams rootParams = (WindowManager.LayoutParams) rootLayout.getLayoutParams();

                                        if (dragFlag && !isRecording) {
                                                rootParams.width = x;
                                                rootParams.height = y;
                                        } else {
                                                rootParams.x = rawX - xMargin;
                                                rootParams.y = rawY - yMargin - statusBarHeight;
                                        }
                                        windowManager.updateViewLayout(rootLayout, rootParams);

                                }

                                return true;
                        }
                });
        }

        @Override
        public void onDestroy() {
                super.onDestroy();
                releaseCamera();
                releaseMediaRecorder();
                if (rootLayout != null) {
                        windowManager.removeView(rootLayout);
                }

                if (isRecording) {
                        mMediaRecorder.stop();
                        releaseMediaRecorder();
                        mCamera.lock();
                        captureButton.setImageResource(R.drawable.bg_capture);
                        isRecording = false;
                }
        }


        //=======       camera functions    ==========//

        private void releaseCamera() {
                if (camera != null) {
                        camera.release();        // release the camera for other applications
                        camera = null;
                }
        }

        private void releaseMediaRecorder() {
                if (mMediaRecorder != null) {
                        mMediaRecorder.reset();
                        mMediaRecorder.release(); // release the recorder object
                        mMediaRecorder = null;
                        mCamera.lock();           // lock camera for later use
                }
        }


        private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                        Log.d(TAG, "picture callback run ");

                        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                        if (pictureFile == null) {
                                Log.d(TAG, "Error creating media file, check storage permissions: ");
                                return;
                        }

                        try {
                                FileOutputStream fos = new FileOutputStream(pictureFile);
                                fos.write(data);
                                fos.close();
                        } catch (FileNotFoundException e) {
                                Log.d(TAG, "File not found: " + e.getMessage());
                        } catch (IOException e) {
                                Log.d(TAG, "Error accessing file: " + e.getMessage());
                        }
                }
        };

        private static Uri getOutputMediaFileUri(int type) {
                return Uri.fromFile(getOutputMediaFile(type));
        }

        @Nullable
        private static File getOutputMediaFile(int type) {
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");

                if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                                Log.d("MyCameraApp", "failed to create directory");
                                return null;
                        }
                }

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                File mediaFile;
                if (type == MEDIA_TYPE_IMAGE) {
                        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                                "IMG_" + timeStamp + format_type);
                } else if (type == MEDIA_TYPE_VIDEO) {
                        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                                "VID_" + timeStamp + ".mp4");
                } else {
                        return null;
                }

                return mediaFile;
        }


        private boolean prepareVideoRecorder() {

                mMediaRecorder = new MediaRecorder();

                mCamera.unlock();
                mMediaRecorder.setCamera(mCamera);

                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                String s[] = video_size.split("x");
                Log.v("log", "size " + Integer.parseInt(s[0]) + " " + Integer.parseInt(s[1]));
                //   mMediaRecorder.setVideoSize(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
                mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
                mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
                //     mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
                // Step 6: Prepare configured MediaRecorder
                try {
                        mMediaRecorder.prepare();
                } catch (IllegalStateException e) {
                        Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
                        releaseMediaRecorder();
                        return false;
                } catch (IOException e) {
                        Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
                        releaseMediaRecorder();
                        return false;
                }
                return true;
        }


        public int dpToPx(int dp) {
                return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
        }
}