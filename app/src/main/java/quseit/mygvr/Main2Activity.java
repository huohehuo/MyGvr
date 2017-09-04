package quseit.mygvr;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


//展示全景图片例子
public class Main2Activity extends AppCompatActivity {
    private String TAG="vr_test";
    private VrPanoramaView panoramaView;
    public boolean loadImageSuccessful;
    /** Tracks the file to be loaded across the lifetime of this app. **/
    private Uri fileUri;
    private String VR_PIC="timbbg.jpg";
    private boolean changepic=false;
    /** Configuration information for the panorama. **/
    private VrPanoramaView.Options panoOptions = new VrPanoramaView.Options();
    private ImageLoaderTask backgroundImageLoaderTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        panoramaView = (VrPanoramaView) findViewById(R.id.vr_pic);
        panoramaView.setEventListener(new ActivityEventListener());

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
        Log.e(TAG, this.hashCode() + ".onNewIntent()");
        // Save the intent. This allows the getIntent() call in onCreate() to use this new Intent during
        // future invocations.
        setIntent(intent);
        // Load the new image.
        handleIntent(intent);

    }
    public void changeVrPic(View view){
        if (changepic){
            VR_PIC="timbbg.jpg";
            changepic=false;
        }else{
            VR_PIC="tvimg.jpg";
            changepic=true;
        }
        handleIntent(getIntent());
    }
    private void handleIntent(Intent intent) {
        // Determine if the Intent contains a file to load.
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.e(TAG, "ACTION_VIEW Intent recieved");

            fileUri = intent.getData();
            if (fileUri == null) {
                Log.e(TAG, "No data uri specified. Use \"-d /path/filename\".");
            } else {
                Log.e(TAG, "Using file " + fileUri.toString());
            }

            panoOptions.inputType = intent.getIntExtra("inputType", VrPanoramaView.Options.TYPE_MONO);
            Log.e(TAG, "Options.inputType = " + panoOptions.inputType);
        } else {
            Log.e(TAG, "Intent is not ACTION_VIEW. Using default pano image.");
            fileUri = null;
            panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;
        }

        // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
        // take 100s of milliseconds.
        if (backgroundImageLoaderTask != null) {
            // Cancel any task from a previous intent sent to this activity.
            backgroundImageLoaderTask.cancel(true);
        }
        backgroundImageLoaderTask = new ImageLoaderTask();
        backgroundImageLoaderTask.execute(Pair.create(fileUri, panoOptions));
    }
    /**
     * Helper class to manage threading.
     */
    class ImageLoaderTask extends AsyncTask<Pair<Uri, VrPanoramaView.Options>, Void, Boolean> {

        /**
         * Reads the bitmap from disk in the background and waits until it's loaded by pano widget.
         */
        @Override
        protected Boolean doInBackground(Pair<Uri, VrPanoramaView.Options>... fileInformation) {
            VrPanoramaView.Options panoOptions = null;  // It's safe to use null VrPanoramaView.Options.
            InputStream istr = null;
            if (fileInformation == null || fileInformation.length < 1
                    || fileInformation[0] == null || fileInformation[0].first == null) {
                //获取asset文件夹里面的图片资源
                AssetManager assetManager = getAssets();
                try {
                    istr = assetManager.open(VR_PIC);
                    panoOptions = new VrPanoramaView.Options();
                    //TYPE_MONO模式下：图片资源是一副vr全景视图，
                    // 而TYPE_STEREO_OVER_UNDER模式则是上下两张拼接的相同VR全景视图
                    panoOptions.inputType = VrPanoramaView.Options.TYPE_MONO;
                } catch (IOException e) {
                    Log.e(TAG, "Could not decode default bitmap: " + e);
                    return false;
                }
            } else {
                try {
                    istr = new FileInputStream(new File(fileInformation[0].first.getPath()));
                    panoOptions = fileInformation[0].second;
                } catch (IOException e) {
                    Log.e(TAG, "Could not load file: " + e);
                    return false;
                }
            }

            panoramaView.loadImageFromBitmap(BitmapFactory.decodeStream(istr), panoOptions);
            try {
                istr.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close input stream: " + e);
            }

            return true;
        }
    }
    private class ActivityEventListener extends VrPanoramaEventListener {
        /**
         * Called by pano widget on the UI thread when it's done loading the image.
         */
        @Override
        public void onLoadSuccess() {
            loadImageSuccessful = true;
        }

        /**
         * Called by pano widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            loadImageSuccessful = false;
            Toast.makeText(
                    Main2Activity.this, "Error loading pano: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Error loading pano: " + errorMessage);
        }
    }



    @Override
    protected void onPause() {
        panoramaView.pauseRendering();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        panoramaView.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        panoramaView.shutdown();

        // The background task has a 5 second timeout so it can potentially stay alive for 5 seconds
        // after the activity is destroyed unless it is explicitly cancelled.
        if (backgroundImageLoaderTask != null) {
            backgroundImageLoaderTask.cancel(true);
        }
        super.onDestroy();
    }

}
