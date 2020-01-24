package org.futto.app.ui.handlers;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.android.PolyUtil;

import org.futto.app.networking.GetRequest;
import org.futto.app.networking.HTTPUIAsync;
import org.futto.app.ui.fragments.getPolyline;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by devsen on 4/2/18.
 */

public class UberManager {
    private static UberManager instance;
    private static Fragment fragment;
    private static final String UBER_PRODUCT_URL = "https://api.uber.com/v1.2/products?";
    private static final String UBER_URL_ETA = "https://api.uber.com/v1.2/estimates/time?";
    private static final String UBER_URL_PRICE = "https://api.uber.com/v1.2/estimates/price?";
    private static final String UBER_KEY = "Token GEej4_2O2nyzX-FHSaXAXCSrj-7S0QaVvOAMOg3F";
//    private static final String UBER_KEY = "Bearer JA.VUNmGAAAAAAAEgASAAAABwAIAAwAAAAAAAAAEgAAAAAAAAG8AAAAFAAAAAAADgAQAAQAAAAIAAwAAAAOAAAAkAAAABwAAAAEAAAAEAAAAMkkK9-nq3ml5E59TAcxdTBsAAAAYUwC1oqFH6v5L5lGu8AvikyLZPNQD-QJUhymSp3YQmwN0eDstPRqyQzvG4oAmKEqbQHSrlbXbSvRDcEHZL0oweFORhWeuZx53VOEmwa1Rlr-APmSvyQTZVItEDouLIAUNoZOtmonXesaRG86DAAAAN0bsPnbWSADKLFPOiQAAABiMGQ4NTgwMy0zOGEwLTQyYjMtODA2ZS03YTRjZjhlMTk2ZWU";
//    private static final String UBER_KEY = "JA.VUNmGAAAAAAAEgASAAAABwAIAAwAAAAAAAAAEgAAAAAAAAH4AAAAFAAAAAAADgAQAAQAAAAIAAwAAAAOAAAAzAAAABwAAAAEAAAAEAAAAEOexhplr6vpiC3-rJjYMr2nAAAAq-OQ3ZU5zxBudxqI6bxwTy8REaR2fH8KFjMVTfQ-As4VDlzzG3BuIA7mRtzy7vy4mPjGoLQ8meGpN_9AhEvHiggfVYx5nddtDrzCub7opbLw93roykx77JPqyV2r9Bcjrn-un8J2gXWBH3nUprIuJJgcY9Hi75CgVGphCegqbkd9-K72RLmBzzr-DV--un5auCmrxKd6eva-UQbXHIGrPc8H0D6uEVEADAAAABEU-PGJCEhXzsC-VCQAAABiMGQ4NTgwMy0zOGEwLTQyYjMtODA2ZS03YTRjZjhlMTk2ZWU";
    public static UberManager getInstance(Fragment f) {
        if (instance == null) {
            instance = new UberManager();
            fragment = f;
        }
        return instance;
    }

    public void getUberRoutes(LatLng start, LatLng end, UberManagerListener listener) {
        tryUberProductDetails(UBER_PRODUCT_URL, start, end, listener);
    }

    public interface UberManagerListener {
        // TODO: Update argument type and name
        void onResponse(Step step);
    }

    /**Implements the server request logic for user, device registration.
     * @param url the URL for device registration*/
    private void tryUberProductDetails(final String url, LatLng start, LatLng end, UberManagerListener listener) {

        new HTTPUIAsync(url, fragment.getActivity()) {
            private JSONObject response;

            @Override
            protected Void doInBackground(Void... arg0) {
                GetRequest.initializeSSLContext(fragment.getActivity());
                parameters= GetRequest.makeParameter("latitude", start.latitude + "") +
                        GetRequest.makeParameter("longitude", start.longitude + "");

                List<String> headers = new ArrayList<String>();
                headers.add("Authorization," + UBER_KEY);
                response = GetRequest.httpGetResponse(parameters, url, headers);
                return null;
            }

            @Override
            protected void onPostExecute(Void arg) {
                super.onPostExecute(arg);
                //Toast.makeText(getApplicationContext(), responseCode + "", Toast.LENGTH_SHORT).show();
                //parseStops(response, transit, listener);
                parseProducts(response, start, end, listener);
            }
        };
    }

    /**Implements the server request logic for user, device registration.
     * @param url the URL for device registration*/
    private void tryUberEtaDetails(final String url, String productId, LatLng start, LatLng end, UberManagerListener listener) {

        new HTTPUIAsync(url, fragment.getActivity()) {
            private JSONObject response;

            @Override
            protected Void doInBackground(Void... arg0) {
                GetRequest.initializeSSLContext(fragment.getActivity());
                try {
                    parameters= GetRequest.makeParameter("start_latitude", start.latitude + "") +
                            GetRequest.makeParameter("start_longitude", start.longitude + "");

                    List<String> headers = new ArrayList<String>();
                    headers.add("Authorization," + UBER_KEY);
                    response = GetRequest.httpGetResponse(parameters, url, headers);
                } catch (Exception e) {

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void arg) {
                super.onPostExecute(arg);
                parseEtaRoutes(response, productId, start, end, listener);
            }
        };
    }

    /**Implements the server request logic for user, device registration.
     * @param url the URL for device registration*/
    private void tryUberDetails(final String url, String productId, double estimate, LatLng start, LatLng end, UberManagerListener listener) {

        new HTTPUIAsync(url, fragment.getActivity()) {
            private JSONObject response;

            @Override
            protected Void doInBackground(Void... arg0) {
                GetRequest.initializeSSLContext(fragment.getActivity());
                try {
                    parameters= GetRequest.makeParameter("start_latitude", start.latitude + "") +
                            GetRequest.makeParameter("start_longitude", start.longitude + "") +
                            GetRequest.makeParameter("end_latitude", end.latitude + "") +
                            GetRequest.makeParameter("end_longitude", end.longitude + "");

                    List<String> headers = new ArrayList<String>();
                    headers.add("Authorization," + UBER_KEY);
                    response = GetRequest.httpGetResponse(parameters, url, headers);
                } catch (Exception e) {

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void arg) {
                super.onPostExecute(arg);
                //Toast.makeText(getApplicationContext(), responseCode + "", Toast.LENGTH_SHORT).show();
                //parseStops(response, transit, listener);
                parseUberRoutes(response, productId, start, end, estimate, listener);
            }
        };
    }

    private void parseProducts(JSONObject response, LatLng start, LatLng end, UberManagerListener listener) {
        try {
            JSONArray products = response.getJSONArray("products");
            JSONObject product = products.getJSONObject(0);
            String productId = product.getString("product_id");

            tryUberEtaDetails(UBER_URL_ETA, productId, start, end, listener);
        } catch (Exception e) {
//            tryUberDetails(UBER_URL_PRICE, "", 1.0, start, end, listener);
//            e.printStackTrace();
        }
    }

    private void parseEtaRoutes(JSONObject response, String productId, LatLng start, LatLng end, UberManagerListener listener) {
        try {
            double estimate = 0;
            JSONArray estimates = response.getJSONArray("times");
            for (int i = 0; i < estimates.length(); i++) {
                JSONObject time = estimates.getJSONObject(i);
                if (time.getString("product_id").contentEquals(productId)) {
                    estimate = time.getDouble("estimate");
                    break;
                }
            }
            tryUberDetails(UBER_URL_PRICE, productId, estimate, start, end, listener);
        } catch (Exception e) {
            Log.d("Error", e.toString());
        }
    }

    private void parseUberRoutes(JSONObject response, String productId, LatLng start, LatLng end, double estimate, UberManagerListener listener) {
        try {
            Double fareValue = 0d;
            Double duration = 0d;
            Double distance = 0d;
            JSONArray prices = response.getJSONArray("prices");
            for (int i = 0; i < prices.length(); i++) {
                JSONObject time = prices.getJSONObject(i);
                if (time.getString("product_id").contentEquals(productId)) {
                    fareValue = time.getDouble("high_estimate");
                    duration = time.getDouble("duration");
                    distance = time.getDouble("distance");
                    break;
                }
            }
            createStep(fareValue, start, end, estimate, duration, distance, listener);
        } catch (Exception e) {
            Log.d("Error", e.toString());
        }
    }

    private void createStep(Double fare, LatLng start, LatLng end, Double estimate, Double duration, Double distance, UberManagerListener listener) {
        Step s = new Step(estimate, duration);
        s.setType("UBER");
        s.setDuration(duration);
        s.setDistance(distance * 1609);
        s.setName("Uber for " + String.format("%.2f", duration / 60) + " mins");
        s.setFare(fare);
        getPolyline(start, end, s, listener);
    }

    public void getPolyline(LatLng start, LatLng end, Step step, UberManagerListener listener) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/directions/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        getPolyline polyline = retrofit.create(getPolyline.class);

        polyline.getPolylineData(start.latitude + "," + start.longitude, end.latitude + "," + end.longitude)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                        JsonObject gson = new JsonParser().parse(response.body().toString()).getAsJsonObject();

                        if (gson == null) {
                            return;
                        }

                        try {
                            List<LatLng> polyline = new ArrayList<LatLng>();
                            JSONObject jsonObject = new JSONObject(gson.toString());
                            JSONArray steps = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
                            for (int i = 0; i < steps.length(); i++) {
                                String line = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                                polyline.addAll(PolyUtil.decode(line));
                            }
                            step.setPolyline(PolyUtil.encode(polyline));
                            listener.onResponse(step);
                        } catch (Exception e) {
                            listener.onResponse(step);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, Throwable t) {

                    }
                });
    }
}