package com.gregavola.plugins;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import android.provider.MediaStore;
import android.database.Cursor;

import java.io.File;
import java.io.IOException;

import com.aviary.android.feather.sdk.AviaryIntent;
import com.aviary.android.feather.sdk.IAviaryClientCredentials;
import com.aviary.android.feather.sdk.internal.headless.utils.MegaPixels;
import com.aviary.android.feather.sdk.internal.Constants;
import com.aviary.android.feather.sdk.internal.filters.ToolLoaderFactory.Tools;

import android.util.Log;

import android.app.Application;


public class aviary extends CordovaPlugin {


    /** Folder name on the sdcard where the images will be saved **/
    private static final int ACTION_REQUEST_FEATHER = 1;
    private static final String FOLDER_NAME = "untappd";
    private static final String LOG_TAG = "Aviary";
    public String isSavedPhoto = "yes";
    public String orgPath = "";
    public String outputPath;

    private File mGalleryFolder;
    public File outputFile;

    //private CallbackContext callbackContext;

    public CallbackContext callbackContext;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        mGalleryFolder = createFolders();

        Log.e(LOG_TAG, action);

        if (action.equals("show")) {
            try {
                Log.d(LOG_TAG, action);

                this.callbackContext = callbackContext;
                String source = args.getString(0);
                isSavedPhoto = args.getString(1);

                this.orgPath = source;

                Uri uri = Uri.parse(source);

                // first check the external storage availability
                if (!isExternalStorageAvilable()) {
                    callbackContext.error("Cannot start aviary, external storage unavailable.");
                    return true;
                }

                String mOutputFilePath;
                // create a temporary file where to store the resulting image
                this.outputFile = getNextFileName();
                if (null != this.outputFile) {
                    this.outputPath = this.outputFile.getAbsolutePath();
                } else {
                    callbackContext.error("Cannot start aviary, failed to create a temp file.");
                    return true;
                }

                Intent imageEditorIntent = new AviaryIntent.Builder(cordova.getActivity())
                        .setData(uri) // input image source
                        .withOutput(this.outputFile) // output file destination
                        .withOutputFormat(Bitmap.CompressFormat.JPEG) // output format
                        .withOutputSize(MegaPixels.Mp30) // output size
                        .withOutputQuality(100) // output quality
                        .build();

                cordova.setActivityResultCallback(this);
                cordova.getActivity().startActivityForResult(imageEditorIntent, 1);

                Log.e(LOG_TAG, "Were getting stgrted");

                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                pluginResult.setKeepCallback(true);

                this.callbackContext.sendPluginResult(pluginResult);
                return true;

            } catch (Exception ex) {
                Log.e(LOG_TAG, ex.toString());
                callbackContext.error("Unknown error occured showing aviary.");
                return true;
            }
        } else {
            callbackContext.error(action + " is not a valid action.");
        }

        return false;
    }

    /* 3) Handle the results */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( resultCode == Activity.RESULT_OK ) {
            switch( requestCode ) {

                case ACTION_REQUEST_FEATHER:

                    boolean changed = true;

                    if( null != data ) {
                        Bundle extra = data.getExtras();
                        if( null != extra ) {
                            // image was changed by the user?
                            changed = extra.getBoolean( Constants.EXTRA_OUT_BITMAP_CHANGED );
                        }
                    }

                    JSONObject returnVal = new JSONObject();

                    if (!changed) {
                        Uri uri = Uri.parse(this.orgPath);
                        String orgPath = convertMediaUriToPath(uri);
                        try {

                            // delete the new output
                            try {
                                this.outputFile.delete();
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                Log.e(LOG_TAG, "There was an error " + e.getMessage());
                            }

                            returnVal.put("status", "no_change");
                            returnVal.put("src", this.orgPath);
                            returnVal.put("name", this.orgPath);
                            returnVal.put("output_path", this.orgPath);

                            Log.e(LOG_TAG, "We have everything here ready to go");
                            //this.callbackContext.success(returnVal);

                            sendPluginOK(returnVal, true);

                        } catch(JSONException ex) {
                            sendPluginBad("JSON EXCEPTION => " + ex.toString(), true);
                        }
                    } else {

                        try {

                            if (isSavedPhoto == "0") {
                                returnVal.put("status", "delete-photo");
                            } else {
                                returnVal.put("status", "ok");
                            }

                            returnVal.put("name", this.orgPath);
                            returnVal.put("output_path", this.outputPath);

                            Log.e(LOG_TAG, "We have everything here ready to go");

                            sendPluginOK(returnVal, true);

                        } catch(JSONException ex) {
                            Log.e(LOG_TAG, ex.toString());
                            sendPluginBad(ex.toString(), true);
                        }


                    }

                    break;
            }
        }
    }

    private void sendPluginOK(JSONObject info, boolean keepCallback) {
        if (this.callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, info);
            result.setKeepCallback(keepCallback);
            this.callbackContext.sendPluginResult(result);
        }
    }

    private void sendPluginBad(String info, boolean keepCallback) {
        if (this.callbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, info);
            result.setKeepCallback(keepCallback);
            this.callbackContext.sendPluginResult(result);
        }
    }

    protected String convertMediaUriToPath(Uri uri) {
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = this.cordova.getActivity().getContentResolver().query(uri, proj,  null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    /**
     * Check the external storage status
     *
     * @return
     */
    private boolean isExternalStorageAvilable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }

    /**
     * Return a new image file. Name is based on the current time. Parent folder will be the one created with createFolders
     *
     * @return
     * @see #createFolders()
     */
    private File getNextFileName() {
        if ( mGalleryFolder != null ) {
            if ( mGalleryFolder.exists() ) {
                File file = new File( mGalleryFolder, "aviary_" + System.currentTimeMillis() + ".jpg" );
                return file;
            }
        }
        return null;
    }

    /**
     * Try to create the required folder on the sdcard where images will be saved to.
     *
     * @return
     */
    private File createFolders() {

        File baseDir;

        if ( android.os.Build.VERSION.SDK_INT < 8 ) {
            baseDir = Environment.getExternalStorageDirectory();
        } else {
            baseDir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES );
        }

        if ( baseDir == null ) return Environment.getExternalStorageDirectory();

        Log.d( LOG_TAG, "Pictures folder: " + baseDir.getAbsolutePath() );
        File aviaryFolder = new File( baseDir, FOLDER_NAME );

        if ( aviaryFolder.exists() ) return aviaryFolder;
        if ( aviaryFolder.mkdirs() ) return aviaryFolder;

        return Environment.getExternalStorageDirectory();
    }
}