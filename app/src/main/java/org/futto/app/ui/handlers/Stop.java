package org.futto.app.ui.handlers;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by devsen on 3/25/18.
 */

public class Stop {

    private String bus;
    private int stopId;
    private String stopName;
    private LatLng stop;
    private boolean start;
    private Date arriveTime;

    public Stop(String bus, JSONObject stop) {
        try {
            double lat = stop.getDouble("lat");
            double lon = stop.getDouble("lon");
            this.bus = bus;
            this.stopId = stop.getInt("stpid");
            this.stopName = stop.getString("stpnm");
            this.stop = new LatLng(lat, lon);
        } catch (Exception e) {

        }
    }

    public Stop(String bus, LatLng stop, boolean start, Date arriveTime) {
        this.bus = bus;
        this.stop = stop;
        this.start = start;
        this.arriveTime = arriveTime;
    }

    @Override
    public String toString() {
        return "Stop{" +
                "bus='" + bus + '\'' +
                ", stopId='" + stopId + '\'' +
                ", stopName='" + stopName + '\'' +
                ", stop=" + stop +
                '}';
    }

    public String getBus() {
        return bus;
    }

    public int getStopId() {
        return stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public LatLng getStop() {
        return stop;
    }

    public Date getArriveTime() {
        return arriveTime;
    }

    public boolean getIfStartStop() {
        return start;
    }
}
