package org.futto.app.ui.handlers;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by devsen on 3/25/18.
 */

public class Routes implements Comparable<Routes> {

    private double fare;
    private double duration;
    private double distance;
    private Date arrivalTime;
    private Date departureTime;
    private String title;
    private String name;
    private String mode;
    private List<Step> steps;

    public Routes(List<Step> steps) {
        fare = 0;
        this.steps = steps;
        processSteps();;
    }

    public Routes(JSONObject route) {
        try {
            fare = 0;
            if (route.has("fare")) {
                fare = route.optJSONObject("fare").optDouble("value");
            }
            JSONObject leg = route.getJSONArray("legs").getJSONObject(0);

            JSONArray stepsList = leg.getJSONArray("steps");
            steps = new ArrayList<Step>();
            for (int i = 0; i < stepsList.length(); i++) {
                DateFormat formatter = new SimpleDateFormat("dd:MM:yyyy hh:mm a");
                if (!steps.isEmpty()) {
                    departureTime = steps.get(steps.size() - 1).getArrivalTime();
                    Log.d("departureTime", departureTime.toString());
                }
                else if (leg.has("departure_time")){
                    departureTime = new Date(leg.getJSONObject("departure_time").getLong("value") * 1000);
//                }
//                else if (i != 0){
//                    departureTime = new Date((long) (steps.get(i - 1).getArrivalTime().getTime()+
//                                        steps.get(i).getDuration()));
                } else {
                    departureTime = new Date();
                }
                steps.add(new Step(departureTime, stepsList.getJSONObject(i)));
            }
            processSteps();
        } catch (Exception e) {
            Log.d("Error", e.toString());
        }
    }

    @Override
    public String toString() {
        return "Routes{" +
                "fare=" + fare +
                ", duration=" + duration +
                ", distance=" + distance +
                ", arrivalTime=" + arrivalTime +
                ", departureTime=" + departureTime +
                ", title='" + title + '\'' +
                ", name='" + name + '\'' +
                ", mode='" + mode + '\'' +
                ", steps=" + steps +
                '}';
    }

    private void extractSteps(JSONArray steps, JSONArray stepsList) {
        try {
            for (int i = 0; i < stepsList.length(); i++) {
                JSONObject step = stepsList.getJSONObject(i);
                if (step.has("steps")) {
                    extractSteps(steps, step.getJSONArray("steps"));
                }
                else {
                    steps.put(step);
                }
            }
        } catch (Exception e) {
            Log.d("Error", e.toString());
        }
    }

    public void processSteps() {
        Date start = steps.get(0).getDepartureTime();
        Log.d("start",steps.get(0).getName());
        Log.d("start",start.toString());
        Date end = steps.get(steps.size() - 1).getArrivalTime();
        Log.d("end",steps.get(steps.size() - 1).getName());
        Log.d("end",steps.get(steps.size() - 2).getArrivalTime().toString());
        Log.d("end",end.toString());
        duration = (end.getTime() - start.getTime()) / 1000;
        Log.d("duration", String.valueOf(duration));
        for (Step s : steps) {
            fare += s.getFare();
            distance += s.getDistance();
        }

        departureTime = start;
        arrivalTime = end;

        DateFormat formatter = new SimpleDateFormat("hh:mm a");
        title = formatter.format(departureTime) + " - " + formatter.format(arrivalTime);

        List<Step> transits = new ArrayList<Step>();

        for (Step s : steps) {
            if (s.getType().contentEquals("TRANSIT")) {
                transits.add(s);
            }
        }

        StringBuilder s = new StringBuilder();
        String lastType = "";
        for (Step step : steps) {
            if (!step.getType().contentEquals(lastType)) {
                s.append(step.getType());
                lastType = step.getType();
            }
        }

        mode = s.toString();

        if (!transits.isEmpty()) {
            name = transits.get(0).getName();
        }
    }



    public Double getFare() {
        return fare;
    }

    public Double getDuration() {
        return duration;
    }

    public Double getDistance() {
        return distance;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setArrivalTime(Date arrivalTime) {
        DateFormat formatter = new SimpleDateFormat("hh:mm a");
        this.arrivalTime = arrivalTime;
        title = formatter.format(departureTime) + " - " + formatter.format(arrivalTime);
        duration = (arrivalTime.getTime() - departureTime.getTime()) / 1000;
    }

    public void setDepartureTime(Date departureTime) {
        DateFormat formatter = new SimpleDateFormat("hh:mm a");
        this.departureTime = departureTime;
        title = formatter.format(departureTime) + " - " + formatter.format(arrivalTime);
        duration = (arrivalTime.getTime() - departureTime.getTime()) / 1000;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void addSteps(List<Step> steps) {
        this.steps.addAll(steps);
    }

    public void addUberStep(List<Step> steps) {
//        List<Step> tmp = new ArrayList<>();
//        tmp.addAll(steps);
//        tmp.addAll(this.steps);
        this.steps.addAll(0, steps);
    }
    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    @Override
    public int compareTo(@NonNull Routes o) {
        return arrivalTime.compareTo(o.getArrivalTime()) * (int) (duration - o.getDuration());
    }
}
