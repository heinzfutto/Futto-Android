package org.futto.app.ui.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.maps.model.JointType.ROUND;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link TransitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransitFragment extends Fragment implements OnMapReadyCallback {

//    private static String KEY = "AIzaSyDRJSnmycmwl9emHWdzBErbzUdT7rRGJa0";
    private static String KEY = "AIzaSyCPGkT_1i3FY3BZmTVBvbzjbptKVFTqFY8";
    private static String URL = "https://maps.googleapis.com/maps/api/directions/json?";
    private List<Routes> routes;
    private TransitAdapter adapter;
    GoogleMap mMap;
    private RelativeLayout mapHolder;
    private Button backToResults;
    private ListView lvTransits;
    public LatLng source;
    public LatLng destination;

    public TransitFragment() {
        // Required empty public constructor
    }

    public static TransitFragment newInstance(LatLng from, LatLng to) {
        TransitFragment fragment = new TransitFragment();
        fragment.source = from;
        fragment.destination = to;
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
        View v = inflater.inflate(R.layout.fragment_transit, container, false);
        routes = new ArrayList<Routes>();
        lvTransits = (ListView) v.findViewById(R.id.lvTransits);
        TextView tvNone = (TextView) v.findViewById(R.id.tvNone);
        adapter = new TransitAdapter(v.getContext(), routes);
        lvTransits.setEmptyView(tvNone);
        lvTransits.setAdapter(adapter);

        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentByTag("mapFragment");
        mapHolder = (RelativeLayout) v.findViewById(R.id.mapHolder);
        mapHolder.setVisibility(View.GONE);
        if (mapFragment == null) {
            mapFragment = new SupportMapFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.mapFragmentContainer, mapFragment, "mapFragment");
            ft.commit();
            fm.executePendingTransactions();
        }
        mapFragment.getMapAsync(this);

        TransitManager.getInstance(this).getTransitData(source, destination, new TransitManager.TransitManagerListener() {
            @Override
            public void onResponseRoutes(List<Routes> r) {
                Log.d("gggg",r.get(0).toString());
                routes.clear();
                routes.addAll(r);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onResponseStops(List<Stop> stops) {

            }
        });

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
                    options.icon(BitmapDescriptorFactory.fromBitmap(bm));
                    mMap.addMarker(options);
                }
                mMap.addPolyline(lineOptions);
            }
        });

        backToResults = (Button) v.findViewById(R.id.bBackToResults);
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

    private void parseResponse(JSONObject response) throws JSONException {
        JSONArray routesList = response.getJSONArray("routes");
        for (int i = 0; i < routesList.length(); i++) {
           routes.add(new Routes(routesList.getJSONObject(i)));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(20);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(source.latitude, source.longitude))
                .zoom(12)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void onBackPressed() {

        if (mapHolder.getVisibility() == View.VISIBLE) {
            lvTransits.setVisibility(View.VISIBLE);
            mapHolder.setVisibility(View.GONE);
            mMap.clear();
            return;
        }
        getActivity().finish();
    }
}
