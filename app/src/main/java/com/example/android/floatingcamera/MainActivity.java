package com.example.android.floatingcamera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

        public final static int REQUEST_CODE_OVERLAY_PERMISSION = 100;
        public final static int REQUEST_CODE_PERMISSION = 200;

        RelativeLayout rootLayout;

        ImageButton flashButton;
        ImageButton whiteBalanceButton;
        ImageButton colorFiltertButton;
        ImageButton pictureQualityButton;
        ImageButton pictureSizeButton;
        ImageButton pictureFormatButton;
        ImageButton videoSizeButton;

        Button startButton;
        Button stopButton;

        LinearLayout moreContainer;
        RelativeLayout moreOptionLayout;
        Button moreButton;
        Button helpButton;
        Button rateButton;
        Button donateButton;


        SharedPreferences preferences;
        SharedPreferences.Editor editor;

        ShowcaseView showcaseView;

        boolean isOptionLayoutOpen = false;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        checkPermissions();
                else {
                        doWork();
                }
        }


        void createDialog(final ArrayList<String> list, final String key) {
                editor = preferences.edit();
                String whiteBalance = preferences.getString(key, "auto");
                TextView title = new TextView(this);
                title.setText(key);
                title.setPadding(8, 8, 8, 8);
                title.setTextSize(20);
                title.setTextColor(Color.parseColor("#aaaaaa"));
                title.setBackgroundColor(Color.parseColor("#555555"));

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCustomTitle(title);
                CustomAdapter adapter = new CustomAdapter(list, whiteBalance);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                                editor.putString(key, list.get(i));
                                editor.apply();
                        }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
        }

        void showFirstTimeDialog(View view, String title, String content) {
                ShowcaseView.Builder builder = new ShowcaseView.Builder(MainActivity.this)
                        .setTarget(new ViewTarget(view))
                        .setContentTitle(title)
                        .setContentText(content);

                showcaseView = builder.build();
        }


        void moreOptionFunction() {
                rootLayout = (RelativeLayout) findViewById(R.id.root_layout);
                moreContainer = (LinearLayout) findViewById(R.id.more_container);
                moreOptionLayout = (RelativeLayout) findViewById(R.id.more_option_layout);
                moreButton = (Button) findViewById(R.id.more_button);
                helpButton = (Button) findViewById(R.id.help);
                rateButton = (Button) findViewById(R.id.rate);
                donateButton = (Button) findViewById(R.id.donate);

                moreButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                moreOptionLayout.requestFocus();
                                if (!isOptionLayoutOpen) {
                                        moreContainer.animate().translationY(-moreContainer.getHeight() + moreButton.getHeight()).setDuration(200);
                                        isOptionLayoutOpen = true;
                                } else {
                                        moreContainer.animate().translationY(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                        super.onAnimationEnd(animation);
                                                        moreContainer.animate().setListener(null);
                                                }
                                        });
                                        isOptionLayoutOpen = false;

                                }
                        }
                });

                rootLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                                if (isOptionLayoutOpen) {

                                        moreContainer.animate().translationY(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                        super.onAnimationEnd(animation);
                                                        moreContainer.animate().setListener(null);
                                                }
                                        });
                                        isOptionLayoutOpen = false;
                                }
                                return true;
                        }
                });


                helpButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle)
                                        .setTitle("Help")
                                        .setMessage(getString(R.string.help))
                                        .setNeutralButton("ok", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                }
                                        })
                                        .create().show();

                        }
                });

                rateButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=camera")));
                                } catch (Exception e) {
                                        e.printStackTrace();
                                        new AlertDialog.Builder(MainActivity.this).setTitle("Error")
                                                .setMessage("We unable to find play store in your device")
                                                .setNeutralButton("ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                                dialogInterface.dismiss();
                                                        }
                                                })
                                                .create().show();

                                }
                        }
                });

                donateButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                Toast.makeText(MainActivity.this, "Thanks", Toast.LENGTH_LONG).show();
                        }
                });

        }


        private void doWork() {

                moreOptionFunction();
                showcaseView = new ShowcaseView.Builder(this).build();
                showcaseView.hide();

                final SharedPreferences firstTimePreferences = this.getSharedPreferences("first_time_preferences", Context.MODE_PRIVATE);
                preferences = this.getSharedPreferences("camera_preferences", Context.MODE_PRIVATE);

                startButton = (Button) findViewById(R.id.start);
                stopButton = (Button) findViewById(R.id.stop);

                flashButton = (ImageButton) findViewById(R.id.flash_button);
                whiteBalanceButton = (ImageButton) findViewById(R.id.white_balance);
                colorFiltertButton = (ImageButton) findViewById(R.id.color_filter);
                pictureQualityButton = (ImageButton) findViewById(R.id.picture_quality);
                pictureSizeButton = (ImageButton) findViewById(R.id.picture_size);
                pictureFormatButton = (ImageButton) findViewById(R.id.picture_format);
                videoSizeButton = (ImageButton) findViewById(R.id.video_size);


                //========      start  button   ========//
                startButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                boolean isStartFirstTime = firstTimePreferences.getBoolean(getString(R.string.startButton_key), true);
                                if (!isStartFirstTime) {
                                        showcaseView.hide();
                                        Intent intent = new Intent(MainActivity.this, CameraService.class);
                                        startService(intent);
                                } else {
                                        showFirstTimeDialog(startButton, "Start button", "Click to start camera in floating mode");
                                        firstTimePreferences.edit().putBoolean(getString(R.string.startButton_key), false).apply();
                                }
                        }
                });

                //========      stop  button   ========//
                stopButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                boolean isStopFirstTime = firstTimePreferences.getBoolean(getString(R.string.stopButton_key), true);
                                if (!isStopFirstTime) {
                                        showcaseView.hide();
                                        Intent intent = new Intent(MainActivity.this, CameraService.class);
                                        stopService(intent);
                                } else {
                                        showFirstTimeDialog(stopButton, "Stop button", "Click to close camera preview");
                                        firstTimePreferences.edit().putBoolean(getString(R.string.stopButton_key), false).apply();
                                }
                        }
                });

                //=========        flash   =======//
                String flash_key = getString(R.string.flash_key);
                String flashMode = preferences.getString(flash_key, "auto");
                if (flashMode.equals("auto"))
                        flashButton.setImageResource(R.drawable.ic_flash_auto_black_24dp);
                else if (flashMode.equals("on"))
                        flashButton.setImageResource(R.drawable.ic_flash_on_black_24dp);
                else if (flashMode.equals("off"))
                        flashButton.setImageResource(R.drawable.ic_flash_off_black_24dp);

                flashButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                boolean isFlashFirstTime = firstTimePreferences.getBoolean(getString(R.string.flashButton_key), true);
                                if (!isFlashFirstTime) {
                                        showcaseView.hide();
                                        editor = preferences.edit();
                                        String key = getString(R.string.flash_key);
                                        String flashMode = preferences.getString(key, "auto");
                                        switch (flashMode) {
                                                case "auto":
                                                        editor.putString(key, "on");
                                                        flashButton.setImageResource(R.drawable.ic_flash_on_black_24dp);
                                                        break;
                                                case "on":
                                                        editor.putString(key, "off");
                                                        flashButton.setImageResource(R.drawable.ic_flash_off_black_24dp);
                                                        break;
                                                case "off":
                                                        editor.putString(key, "auto");
                                                        flashButton.setImageResource(R.drawable.ic_flash_auto_black_24dp);
                                                        break;
                                        }
                                        editor.apply();
                                } else {
                                        showFirstTimeDialog(flashButton, "Flash button", "Click to change the flash mode");
                                        firstTimePreferences.edit().putBoolean(getString(R.string.flashButton_key), false).apply();
                                }
                        }
                });

                //=======   picture quality     ==========//
                String pictureQualityArray[] = {"Low", "Medium", "High"};
                ArrayList<String> pictureQualityList = new ArrayList<>();
                pictureQualityList.addAll(Arrays.asList(pictureQualityArray));
                final ArrayList<String> pictureQualityList1 = pictureQualityList;
                pictureQualityButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                boolean isPictureQualityFirstTime = firstTimePreferences.getBoolean(getString(R.string.pictureQualityButton_key), true);
                                if (!isPictureQualityFirstTime) {
                                        showcaseView.hide();
                                        createDialog(pictureQualityList1, getString(R.string.picture_quality_key));
                                } else {
                                        showFirstTimeDialog(pictureQualityButton, "Picture Quality", "Choose picture quality from the list");
                                        firstTimePreferences.edit().putBoolean(getString(R.string.pictureQualityButton_key), false).apply();
                                }
                        }
                });


                //=========       picture format     =======//
                String pf_key = getString(R.string.picture_format_key);
                String pf_type = preferences.getString(pf_key, "jpg");
                if (pf_type.equals("jpg"))
                        pictureFormatButton.setImageResource(R.drawable.ic_jpg);
                else if (pf_type.equals("png"))
                        pictureFormatButton.setImageResource(R.drawable.ic_png);

                pictureFormatButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                boolean isPictureFormatFirstTime = firstTimePreferences.getBoolean(getString(R.string.pictureFormatButton_key), true);
                                if (!isPictureFormatFirstTime) {
                                        showcaseView.hide();
                                        editor = preferences.edit();
                                        String key = getString(R.string.picture_format_key);
                                        String pf_type = preferences.getString(key, "jpg");
                                        switch (pf_type) {
                                                case "png":
                                                        editor.putString(key, "jpg");
                                                        pictureFormatButton.setImageResource(R.drawable.ic_jpg);
                                                        break;
                                                case "jpg":
                                                        editor.putString(key, "png");
                                                        pictureFormatButton.setImageResource(R.drawable.ic_png);
                                                        break;
                                        }
                                        editor.apply();
                                } else {
                                        showFirstTimeDialog(pictureFormatButton, "Picture format", "Click the button to change picture format");
                                        firstTimePreferences.edit().putBoolean(getString(R.string.pictureFormatButton_key), false).apply();
                                }
                        }
                });

                //=======   picture size and video size ==========//

                boolean isFirstTimeLaunch = firstTimePreferences.getBoolean(getString(R.string.first_time_launch_key), true);

                boolean isCameraAvailbale = false;
                Camera camera = null;

                if (isFirstTimeLaunch) {
                        firstTimePreferences.edit().putBoolean(getString(R.string.first_time_launch_key), false).apply();
                        try {
                                camera = Camera.open();
                                isCameraAvailbale = true;
                        } catch (Exception e) {
                                new AlertDialog.Builder(this)
                                        .setTitle("Camera error")
                                        .setMessage("Can't connect to the camera")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                        finish();
                                                }
                                        })
                                        .setCancelable(false)
                                        .create()
                                        .show();
                        }

                        if (isCameraAvailbale) {
                                Camera.Parameters cameraParams = camera.getParameters();
                                List<Camera.Size> supportedPictureSize = cameraParams.getSupportedPictureSizes();
                                List<Camera.Size> supportedVideoSizes = cameraParams.getSupportedVideoSizes();
                                camera.release();

                                ArrayList<String> pictureSizeList = new ArrayList<>();
                                ArrayList<String> videoSizeList = new ArrayList<>();

                                if (supportedPictureSize != null) {
                                        for (Camera.Size size : supportedPictureSize)
                                                pictureSizeList.add("" + size.width + "x" + size.height);
                                } else
                                        pictureSizeList.add("640x480");


                                if (supportedVideoSizes != null) {
                                        for (Camera.Size size : supportedVideoSizes)
                                                videoSizeList.add("" + size.width + "x" + size.height);
                                } else
                                        videoSizeList.add("640x480");

                                List<String> supportedColorFilter = cameraParams.getSupportedColorEffects();
                                List<String> supportedWhiteBalance = cameraParams.getSupportedWhiteBalance();

                                if (supportedColorFilter == null) {
                                        supportedColorFilter = new ArrayList<>(0);
                                        supportedColorFilter.add("none");
                                }
                                if (supportedWhiteBalance == null) {
                                        supportedWhiteBalance = new ArrayList<>(0);
                                        supportedWhiteBalance.add("none");
                                }

                                camera.release();

                                firstTimePreferences.edit().putStringSet(getString(R.string.old_picture_size_key), new HashSet<>(pictureSizeList)).apply();
                                firstTimePreferences.edit().putStringSet(getString(R.string.old_video_size_key), new HashSet<>(videoSizeList)).apply();
                                firstTimePreferences.edit().putStringSet(getString(R.string.old_color_filter_key), new HashSet<>(supportedColorFilter)).apply();
                                firstTimePreferences.edit().putStringSet(getString(R.string.old_white_balance_key), new HashSet<>(supportedWhiteBalance)).apply();
                        }
                }

                Set<String> defaultSize = new ArraySet<>(new ArrayList<>(Arrays.asList(new String[]{"640x480"})));
                Set<String> defaultFilter = new ArraySet<>(new ArrayList<>(Arrays.asList(new String[]{"none"})));
                Set<String> oldPictureSizeSet = firstTimePreferences.getStringSet(getString(R.string.old_picture_size_key), defaultSize);
                Set<String> oldVideoSizeSet = firstTimePreferences.getStringSet(getString(R.string.old_video_size_key), defaultSize);
                Set<String> oldColorFilter = firstTimePreferences.getStringSet(getString(R.string.old_color_filter_key), defaultFilter);
                Set<String> oldWhiteBalance = firstTimePreferences.getStringSet(getString(R.string.old_white_balance_key), defaultFilter);

                final ArrayList<String> pictureSizeList = new ArrayList<>(oldPictureSizeSet);
                pictureSizeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                boolean isPictureSizeFirstTime = firstTimePreferences.getBoolean(getString(R.string.pictureSizeButton_key), true);
                                if (!isPictureSizeFirstTime) {
                                        showcaseView.hide();
                                        createDialog(pictureSizeList, getString(R.string.picture_size_key));
                                } else {
                                        showFirstTimeDialog(pictureSizeButton, "Picture size", "Click to choose picture size from list");
                                        firstTimePreferences.edit().putBoolean(getString(R.string.pictureSizeButton_key), false).apply();
                                }
                        }
                });

                final ArrayList<String> videoSizeList = new ArrayList<>(oldVideoSizeSet);
                videoSizeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                boolean isVideoSizeFirstTime = firstTimePreferences.getBoolean(getString(R.string.videoSizeButton_key), true);
                                if (!isVideoSizeFirstTime) {
                                        showcaseView.hide();
                                        createDialog(videoSizeList, getString(R.string.video_size_key));
                                } else {
                                        showFirstTimeDialog(videoSizeButton, "Video size", "Click to choose video size from list");
                                        firstTimePreferences.edit().putBoolean(getString(R.string.videoSizeButton_key), false).apply();
                                }
                        }
                });

                final ArrayList<String> colorFilterList = new ArrayList<>(oldColorFilter);
                colorFiltertButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                boolean isColorFilterFirstTime = firstTimePreferences.getBoolean(getString(R.string.colorFiltertButton_key), true);
                                if (!isColorFilterFirstTime) {
                                        showcaseView.hide();
                                        createDialog(colorFilterList, getString(R.string.color_filter_key));
                                } else {
                                        showFirstTimeDialog(colorFiltertButton, "Color Filter", "Click to choose supported color filters");
                                        firstTimePreferences.edit().putBoolean(getString(R.string.colorFiltertButton_key), false).apply();
                                }
                        }
                });

                final ArrayList<String> whiteBalanceList = new ArrayList<>(oldWhiteBalance);
                whiteBalanceButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                                boolean isWhiteBalanceFirstTime = firstTimePreferences.getBoolean(getString(R.string.whiteBalanceButton_key), true);
                                if (!isWhiteBalanceFirstTime) {
                                        showcaseView.hide();
                                        createDialog(whiteBalanceList, getString(R.string.white_balance_key));
                                } else {
                                        showFirstTimeDialog(whiteBalanceButton, "White balance", "Click to choose supported white balance");
                                        firstTimePreferences.edit().putBoolean(getString(R.string.whiteBalanceButton_key), false).apply();
                                }
                        }
                });

        }


        //////      permissions   (only for API>23)  //////////


        @RequiresApi(api = Build.VERSION_CODES.M)
        void checkPermissions() {
                boolean a = ActivityCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED;
                boolean b = ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                boolean c = ActivityCompat.checkSelfPermission(this, RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

                if (a && b && c)
                        checkDrawOverlayPermission();
                else
                        ActivityCompat.requestPermissions(this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, REQUEST_CODE_PERMISSION);
        }


        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                if (requestCode == REQUEST_CODE_PERMISSION) {
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                                grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                                checkDrawOverlayPermission();
                        } else
                                finish();
                }
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        public void checkDrawOverlayPermission() {
                if (!Settings.canDrawOverlays(this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION);
                } else
                        doWork();
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
                        if (Settings.canDrawOverlays(this)) {
                                doWork();
                        } else
                                finish();
                }
        }
}
