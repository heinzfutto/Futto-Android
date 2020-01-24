package org.futto.app.ui.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;

import org.futto.app.R;
import org.futto.app.ui.adapters.TransitAdapter;
import org.futto.app.ui.handlers.Routes;
import org.futto.app.ui.handlers.Step;
import org.futto.app.ui.handlers.Stop;
import org.futto.app.ui.handlers.TransitManager;
import org.futto.app.ui.handlers.UberManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import okhttp3.Route;

import static com.google.android.gms.maps.model.JointType.ROUND;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TransitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HybridFragment extends Fragment implements OnMapReadyCallback {

    private List<Routes> routes;
    private TransitAdapter adapter;
    public static LatLng source;
    public static LatLng destination;
    GoogleMap mMap;
    private RelativeLayout mapHolder;
    private Button backToResults;
    private ListView lvTransits;

    public HybridFragment() {
        // Required empty public constructor
    }

    public static HybridFragment newInstance(LatLng from, LatLng to) {
        HybridFragment fragment = new HybridFragment();
        source = from;
        destination = to;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_hybrid, container, false);
        if (routes == null || routes.isEmpty()) {
            routes = new ArrayList<Routes>();
        }

        lvTransits = (ListView) v.findViewById(R.id.lvRoutes);
        TextView tvNone = (TextView) v.findViewById(R.id.tvNone);
        adapter = new TransitAdapter(getActivity(), routes);
        lvTransits.setEmptyView(tvNone);
        lvTransits.setAdapter(adapter);

        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentByTag("mapFragment");
        mapHolder = (RelativeLayout) v.findViewById(R.id.mapHolder1);
        mapHolder.setVisibility(View.GONE);
        if (mapFragment == null) {
            mapFragment = new SupportMapFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.mapFragmentContainer1, mapFragment, "mapFragment");
            ft.commit();
            fm.executePendingTransactions();
        }
        mapFragment.getMapAsync(this);

        if (routes.isEmpty()) {

            TransitManager.getInstance(this).getTransitData(source, destination, new TransitManager.TransitManagerListener() {
                @Override
                public void onResponseRoutes(List<Routes> r) {
                    routes.addAll(r);
                    adapter.notifyDataSetChanged();
                    List<Step> transits = new ArrayList<Step>();
                    for(int i=0;i<r.size();i++){
                        for (Step s : r.get(i).getSteps()) {
                            if (s.getType().contentEquals("TRANSIT") || s.getType().contentEquals("DRIVING")) {
                                transits.add(s);
                            }
                        }
                    }


                    if (!transits.isEmpty()) {
                            getStops(transits);
                        }

                }

                @Override
                public void onResponseStops(List<Stop> s) {

                }
            });
        }

        lvTransits.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mapHolder.setVisibility(View.VISIBLE);
                lvTransits.setVisibility(View.GONE);
                Routes route = routes.get(position);
                List<Step> steps = route.getSteps();

                PolylineOptions lineOptions = new PolylineOptions();
                lineOptions.width(10);
                lineOptions.color(Color.BLACK);
                lineOptions.startCap(new SquareCap());
                lineOptions.endCap(new SquareCap());
                lineOptions.jointType(ROUND);

                onMapReady(mMap);
                boolean Uber = false;
                for (Step s : steps) {
                    String polyline = s.getPolyline();
                    if (polyline == null) {
                        continue;
                    }
                    List<LatLng> latLngs = PolyUtil.decode(polyline);
                    lineOptions.addAll(latLngs);

                    IconGenerator icg = new IconGenerator(getActivity());
                    Bitmap bm = icg.makeIcon(s.getName());

                    MarkerOptions options = new MarkerOptions();
                    options.position(latLngs.get(0));
                    Date uberstart = new Date();
                    Date uberend = new Date();
                    if (s.getType().equals("DRIVING") && !Uber) {
                        bm = icg.makeIcon("Take the Uber");
                        options.icon(BitmapDescriptorFactory.fromBitmap(bm));
                        mMap.addMarker(options);
                        Uber = true;
                        uberstart.setTime(s.getDepartureTime().getTime());
                        continue;
                    }

                    if (s.getType().equals("DRIVING") && Uber) {
                        uberend.setTime(s.getDepartureTime().getTime());
                    } else {
                        options.icon(BitmapDescriptorFactory.fromBitmap(bm));
                        mMap.addMarker(options);
                    }
                }
                mMap.addPolyline(lineOptions);
            }
        });

        backToResults = (Button) v.findViewById(R.id.bBackToResults1);
        backToResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void getStops(List<Step> transits) {
        Log.d("transit size", String.valueOf(transits.size()));
        Log.d("transit type", String.valueOf(transits.get(0).getName()));
        List<Stop> stops = new ArrayList<>();
        int count = 0;
        for (Step step : transits) {
            if (step.getType().equals("TRANSIT")){
                if (count % 2 == 0) {
                    Stop stop = new Stop(step.getName(), step.getEnd(), false, step.getArrivalTime());
                    stops.add(stop);
                } else if (count % 2 == 1) {
                    Stop stop = new Stop(step.getName(), step.getStart(), true, step.getDepartureTime());
                    stops.add(stop);
                }
                count++;
            }
        }
        parseStops(stops);

    }

    private void parseStops(List<Stop> stops) {
        if (stops.isEmpty()) {
            return;
        }
        Log.d("stopsize", String.valueOf(stops.size()));
        Stop stop = stops.get(stops.size() - 1);
        Log.d("stopbus", stop.getBus());
        stops.remove(stop);
        Log.d("stopbus", String.valueOf(stop.getIfStartStop()));
        if (stop.getIfStartStop()) {
            UberManager.getInstance(this).getUberRoutes(source, stop.getStop(), new UberManager.UberManagerListener() {
                @Override
                public void onResponse(Step step) {
                    Log.d("EnterUber", step.getName());
                    List<Step> steps = new ArrayList<>();
                    TransitManager.getInstance(HybridFragment.this).getDrivingData(source, stop.getStop(), new Date(System.currentTimeMillis()+ 2 * 60000),new TransitManager.TransitManagerListener() {
                        @Override
                        public void onResponseRoutes(List<Routes> r) {
                            List<Step> transits = new ArrayList<>();

                            for (Step s : r.get(0).getSteps()) {
                                if (s.getType().contentEquals("DRIVING")) {
                                    transits.add(s);
                                }
                            }
                            steps.addAll(0,transits);
                        }

                        @Override
                        public void onResponseStops(List<Stop> s) {

                        }
                    });

                    step.setArrivalStop(stop.getStopName());
                    step.setStart(source);
                    step.setEnd(stop.getStop());
                    steps.add(step);
                    createRemainingSteps(stops, stop, steps, true);
                }
            });
        } else {
            UberManager.getInstance(this).getUberRoutes(stop.getStop(), destination, new UberManager.UberManagerListener() {
                @Override
                public void onResponse(Step step) {
                    Log.d("EnterUber", step.getName());
                    List<Step> steps = new ArrayList<>();
                    TransitManager.getInstance(HybridFragment.this).getDrivingData(stop.getStop(), destination, stop.getArriveTime(),new TransitManager.TransitManagerListener() {
                        @Override
                        public void onResponseRoutes(List<Routes> r) {
                            List<Step> transits = new ArrayList<>();

                            for (Step s : r.get(0).getSteps()) {
                                if (s.getType().contentEquals("DRIVING")) {
                                    transits.add(s);
                                }
                            }
                            steps.addAll(0,transits);

                        }

                        @Override
                        public void onResponseStops(List<Stop> s) {

                        }
                    });

                    step.setArrivalStop(stop.getStopName());
                    step.setStart(stop.getStop());
                    step.setEnd(destination);
                    steps.add(0, step);
                    createRemainingSteps(stops, stop, steps, false);
                }
            });

        }
    }

    private void createRemainingSteps(List<Stop> stops, Stop start, List<Step> steps, boolean firstUber) {
        Date departureTime = new Date(steps.get(0).getArrivalTime().getTime() + 2 * 60000);
        Log.d("time", departureTime.toString());
        if (firstUber) {
            TransitManager.getInstance(this).getTransitData(start.getStop(), destination, departureTime, new TransitManager.TransitManagerListener() {
                @Override
                public void onResponseRoutes(List<Routes> routes) {
                    Routes r = routes.get(0);
                    List<Step> s = r.getSteps();
                    Step transit = null;
                    for (Step step: s) {
                        if (step.getType().contentEquals("TRANSIT")) {
                            transit = step;
                            Log.d("stopbus", step.getName());
                            break;
                        }
                    }
                    /*Step transit = s.stream()
                            .filter(t -> t.getType().contentEquals("TRANSIT"))
                            .findFirst()
                            .orElse(null);*/
                    if (transit != null) {
                        steps.addAll(r.getSteps());
                        createRoute(stops, steps);
                    }
                    else {
                        parseStops(stops);
                    }
                }

                @Override
                public void onResponseStops(List<Stop> stops) {

                }
            });
        } else {
            TransitManager.getInstance(this).getTransitData(source, start.getStop(), new Date(System.currentTimeMillis()+ 2 * 60000), new TransitManager.TransitManagerListener() {
                @Override
                public void onResponseRoutes(List<Routes> routes) {
                    Routes r = routes.get(0);
                    List<Step> s = r.getSteps();
                    Step transit = null;
                    for (Step step: s) {
                        if (step.getType().contentEquals("TRANSIT")) {
                            transit = step;
                            Log.d("stopbus", step.getName());
                            break;
                        }
                    }
                    Date arrivalTime = transit.getArrivalTime();
//                    Step transit = s.stream()
//                            .filter(t -> t.getType().contentEquals("TRANSIT"))
//                            .findFirst()
//                            .orElse(null);
                    for (int i = 0; i < steps.size(); i++) {
                        Step curr = steps.get(i);
                        curr.setDepartureTime(arrivalTime);
                        arrivalTime = new Date(arrivalTime.getTime() + (curr.getDuration().longValue() * 1000));
                        curr.setArrivalTime(arrivalTime);
                    }
                    if (transit != null) {
                        steps.addAll(0, r.getSteps());
                        createRoute(stops, steps);
                    }
                    else {
                        parseStops(stops);
                    }
                }

                @Override
                public void onResponseStops(List<Stop> stops) {

                }
            });
        }
    }

    private void createRoute(List<Stop> stops, List<Step> steps) {
        //Routes main = routes.get(0);
        Log.d("ubersecond", steps.toString());
        Routes r = new Routes(steps);
        routes.add(r);
        Collections.sort(routes, new Comparator<Routes>() {
            @Override
            public int compare(Routes o1, Routes o2) {
                return o1.compareTo(o2);
            }
        });
//        routes.sort(new Comparator<Routes>() {
//            @Override
//            public int compare(Routes o1, Routes o2) {
//                return o1.compareTo(o2);
//            }
//        });
        if (mapHolder.getVisibility() != View.VISIBLE) {
            adapter.notifyDataSetChanged();
        }
        Log.d("Route", source + " " + destination);
        //Log.d("Route", "Main : " + main);
        Log.d("Route", "Uber : " + r);
        parseStops(stops);
    }

    public void onBackPressed() {
        if (mapHolder.getVisibility() == View.VISIBLE) {
            lvTransits.setVisibility(View.VISIBLE);
            mapHolder.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            mMap.clear();
            return;
        }
        getActivity().finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(20);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(source.latitude, source.longitude))
                .zoom(17)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
