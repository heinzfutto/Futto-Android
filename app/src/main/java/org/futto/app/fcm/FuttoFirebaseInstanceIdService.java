/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.futto.app.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.futto.app.storage.PersistentData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Listens for changes in the InstanceID
 */
public class FuttoFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static String LOG_TAG = FuttoFirebaseInstanceIdService.class.getSimpleName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String username = null;
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        while (!PersistentData.isLoggedIn()) username = PersistentData.getPatientID();
        Log.d(LOG_TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken, username);
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token, String username) {
        // Add custom implementation, as needed.
        //Enviar para servidor
        Log.d(LOG_TAG, token); //NOT PRINT

        HttpURLConnection connection;

        URL url = null;
        if (token == null || token.length() == 0) {
            return;
        }
        //Send the FCM token to the server
        try {
            String adress = "http://futtonotification.us-east-1.elasticbeanstalk.com/FCM_RECIEVER/register&" + token + "&" + username;
            url = new URL(adress);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            while (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                String output = "";
                StringBuilder xmlResponse = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                while ((output = br.readLine()) != null) {
                    xmlResponse.append(output).append("\n");
                }
            }
            connection.disconnect();
        } catch (IOException e) {
        }
    }
}