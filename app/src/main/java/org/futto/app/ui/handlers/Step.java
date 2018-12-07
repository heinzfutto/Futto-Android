package org.futto.app.ui.handlers;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by devsen on 3/25/18.
 */

public class Step {

    private String type;
    private double duration;
    private String name;
    private String polyline;
    private double distance;
    private Date arrivalTime;
    private Date departureTime;
    private String arrivalStop;
    private String departureStop;
    private LatLng start;
    private LatLng end;
    private Double fare;
    private String bus;
    private String busType;
    private int numStops;

    public Step(double duration) {
        try {
            DateFormat formatter = new SimpleDateFormat("dd:MM:yyyy hh:mm a");
            String departure = formatter.format(new Date());
            departureTime = formatter.parse(departure);
            arrivalTime = new Date(departureTime.getTime() + ((long) duration * 1000));
        } catch (Exception e) {

        }
    }

    public Step(double estimate, double duration) {
        try {
            DateFormat formatter = new SimpleDateFormat("dd:MM:yyyy hh:mm a");
            String departure = formatter.format(new Date().getTime() + (long) estimate * 1000);
            departureTime = formatter.parse(departure);
            arrivalTime = new Date(departureTime.getTime() + ((long) duration * 1000));
        } catch (Exception e) {

        }
    }

    public Step(Date departureTime, JSONObject step) {
        try {
            DateFormat formatter = new SimpleDateFormat("hh:mm a");
            DateFormat formatter1 = new SimpleDateFormat("dd:MM:yyyy");
            DateFormat formatter2 = new SimpleDateFormat("dd:MM:yyyy hh:mm a", Locale.US);

            type = step.getString("travel_mode");
            duration = step.getJSONObject("duration").getDouble("value");
            distance = step.getJSONObject("distance").getDouble("value");
            if (type.equals("DRIVING")) {
                name = "Take Uber for " + String.format("%.2f", duration / 60) + " mins";
            } else {
                name = "Walk for " + String.format("%.2f", duration / 60) + " mins";
            }
            this.departureTime = departureTime;
            arrivalTime = new Date(departureTime.getTime() + ((long) duration * 1000));
            start = new LatLng(step.getJSONObject("start_location").getDouble("lat"), step.getJSONObject("start_location").getDouble("lng"));
            end = new LatLng(step.getJSONObject("end_location").getDouble("lat"), step.getJSONObject("end_location").getDouble("lng"));
            fare = 0d;
            polyline = step.getJSONObject("polyline").getString("points");

            Log.d("json transit",step.toString());
            Log.d("type transit",type + " ");

            if (type.equals("TRANSIT")) {
                departureStop = step.getJSONObject("transit_details").getJSONObject("departure_stop").getString("name");
                arrivalStop = step.getJSONObject("transit_details").getJSONObject("arrival_stop").getString("name");
                String time = step.getJSONObject("transit_details").getJSONObject("departure_time").getString("text");
                time = time.substring(0, time.length() - 2) + " " + time.substring(time.length() - 2, time.length());
                String d = formatter1.format(departureTime) + " " + time;
                departureTime = formatter2.parse(d);
                arrivalTime = new Date(departureTime.getTime() + ((long) duration * 1000));
                if (step.getJSONObject("transit_details").getString("headsign").toLowerCase().contains("inbound")) {
                    busType = "INBOUND";
                }
                else {
                    busType = "OUTBOUND";
                }
                name = step.getJSONObject("transit_details").getJSONObject("line").getString("short_name") + " from " + departureStop;
                bus = step.getJSONObject("transit_details").getJSONObject("line").getString("short_name");
                Log.d("type transit",bus + " ");
                Log.d("transitend",end + " ");
            }
            if (type.equals("DRIVING")) {
                this.departureTime = departureTime;
                arrivalTime = new Date(departureTime.getTime() + ((long) duration * 1000));
            }
        } catch (Exception e) {
            Log.d("type transit",e.getMessage().toString() + " ");
        }
    }

    @Override
    public String toString() {
        return "Step{" +
                "type='" + type + '\'' +
                ", duration=" + duration +
                ", name='" + name + '\'' +
                ", polyline='" + polyline + '\'' +
                ", distance=" + distance +
                ", arrivalTime=" + arrivalTime +
                ", departureTime=" + departureTime +
                ", arrivalStop='" + arrivalStop + '\'' +
                ", departureStop='" + departureStop + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", fare=" + fare +
                ", bus='" + bus + '\'' +
                ", busType='" + busType + '\'' +
                ", numStops=" + numStops +
                '}';
    }

    public String getType() {
        return type;
    }

    public Double getDuration() {
        return duration;
    }

    public String getName() {
        return name;
    }

    public String getBus() {
        return bus;
    }

    public String getBusType() {
        return busType;
    }

    public String getPolyline() {
        return polyline;
    }

    public Double getDistance() {
        return distance;
    }

    public Double getFare() {
        return fare;
    }

    public LatLng getStart() {
        return start;
    }

    public LatLng getEnd() {
        return end;
    }

    public String getArrivalStop() {
        return arrivalStop;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public String getDepartureStop() {
        return departureStop;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public int getNumStops() {
        return numStops;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBus(String bus) {
        this.bus = bus;
    }

    public void setBusType(String busType) {
        this.busType = busType;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public void setFare(Double fare) {
        this.fare = fare;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setStart(LatLng start) {
        this.start = start;
    }

    public void setEnd(LatLng end) {
        this.end = end;
    }

    public void setArrivalStop(String arrivalStop) {
        this.arrivalStop = arrivalStop;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setDepartureStop(String departureStop) {
        this.departureStop = departureStop;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    public void setNumStops(int numStops) {
        this.numStops = numStops;
    }
}
