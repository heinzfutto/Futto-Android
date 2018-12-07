package org.futto.app.networking;

import android.content.Context;
import android.util.Log;

import org.futto.app.CrashHandler;
import org.futto.app.JSONUtils;
import org.futto.app.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static org.futto.app.networking.PostRequest.addWebsitePrefix;

public class SettingUpdate {
    public static String getGps() {
        return gps;
    }

    public static String getGps_on_duration_seconds() {
        return gps_on_duration_seconds;
    }

    public static String getGps_off_duration_seconds() {
        return gps_off_duration_seconds;
    }

    public static String getUpload_data_files_frequency_seconds() {
        return upload_data_files_frequency_seconds;
    }

    private static String gps;
    private static String gps_on_duration_seconds;
    private static String gps_off_duration_seconds;
    private static String upload_data_files_frequency_seconds;

    public static void downloadSetting( Context appContext ) {
        // Log.d("QuestionsDownloader", "downloadJSONQuestions() called");
        doDownload( addWebsitePrefix(appContext.getResources().getString(R.string.download_settings_url)), appContext );
    }

    private static void doDownload(final String url, final Context appContext) { new HTTPAsync(url) {
        String jsonResponseString;
        @Override
        protected Void doInBackground(Void... arg0) {
            String parameters = "";
            try { jsonResponseString = PostRequest.httpRequestString( parameters, url); }
            catch (NullPointerException e) {  }  //We do not care.
            return null; //hate
        }
        @Override
        protected void onPostExecute(Void arg) {
            responseCode = updateSettings( appContext, jsonResponseString);
            super.onPostExecute(arg);
        } }.execute();
    }
    //Returns an appropriate return code for the httpAsync error parsing.  -1 if something goes wrong, 200 if it works.
    private static int updateSettings( Context appContext, String jsonString){
        if (jsonString == null) {
            Log.e("Setting Downloader", "jsonString is null, probably have no network connection. squashing.");
            return -1; }

        JSONObject settings;

        try { settings = new JSONObject(jsonString);}
        catch (JSONException e) {
            Log.e("Setting Update", "JSON PARSING FAIL FAIL FAIL");
            return -1; }

        try { gps = settings.getString("gps");}
        catch (JSONException e) {
            CrashHandler.writeCrashlog(e, appContext);
            Log.e("Survey Downloader", "JSON fail 2"); return -1;
        }

        try { gps_on_duration_seconds = settings.getString("gps_on_duration_seconds");}
        catch (JSONException e) {
            CrashHandler.writeCrashlog(e, appContext);
            Log.e("Survey Downloader", "JSON fail 3"); return -1;
        }

        try { gps_off_duration_seconds = settings.getString("gps_off_duration_seconds");}
        catch (JSONException e) {
            CrashHandler.writeCrashlog(e, appContext);
            Log.e("Survey Downloader", "JSON fail 2"); return -1;
        }

        try { upload_data_files_frequency_seconds = settings.getString("upload_data_files_frequency_seconds");}
        catch (JSONException e) {
            CrashHandler.writeCrashlog(e, appContext);
            Log.e("Survey Downloader", "JSON fail 2"); return -1;
        }



        return 200;

    }
}
