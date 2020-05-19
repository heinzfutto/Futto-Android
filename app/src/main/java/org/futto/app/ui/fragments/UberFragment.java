package org.futto.app.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;
import com.uber.sdk.android.rides.RideRequestButton;

import org.futto.app.R;
import org.futto.app.ui.handlers.Step;
import org.futto.app.ui.handlers.UberHandler;
import org.futto.app.ui.handlers.UberManager;
import org.futto.app.ui.user.MainMenuActivity;
import org.futto.app.ui.user.MapsActivity;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.maps.model.JointType.ROUND;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link UberFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UberFragment extends Fragment implements OnMapReadyCallback {

    private RideRequestButton rideRequestButton;
    GoogleMap mMap;
    private List<LatLng> listLatLng = new ArrayList<>();
    private Polyline blackPolyLine, greyPolyLine;
    public static LatLng source;
    public static LatLng destination;

    public UberFragment() {
        // Required empty public constructor
    }

    public static UberFragment newInstance(LatLng start, LatLng end) {
        UberFragment fragment = new UberFragment();
        source = start;
        destination = end;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_uber, container, false);
        //RelativeLayout rlView = (RelativeLayout) v.findViewById(R.id.rlView);
        rideRequestButton = (RideRequestButton) v.findViewById(R.id.bRideRequest);
        //SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapUber);
        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentByTag("mapFragment");
        if (mapFragment == null) {
            mapFragment = new SupportMapFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.mapFragmentContainer, mapFragment, "mapFragment");
            ft.commit();
            fm.executePendingTransactions();
        }
        mapFragment.getMapAsync(this);
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        UberHandler.showUber(rideRequestButton, MapsActivity.from, MapsActivity.to);
    }

    private void addMarker(LatLng destination) {

        MarkerOptions options = new MarkerOptions();
        options.position(destination);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMap.addMarker(options);

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

        Step step = new Step(0);
        step.setName("Uber to destination");
        UberManager.getInstance(this).getPolyline(source, destination, step, new UberManager.UberManagerListener() {
            @Override
            public void onResponse(Step step) {
                showPolyline(step);
            }
        });
    }

    private void showPolyline(Step step) {
        PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.width(10);
        lineOptions.color(Color.BLACK);
        lineOptions.startCap(new SquareCap());
        lineOptions.endCap(new SquareCap());
        lineOptions.jointType(ROUND);

        String polyline = step.getPolyline();
        if (polyline == null) {
            return;
        }
        List<LatLng> latLngs = PolyUtil.decode(polyline);
        lineOptions.addAll(latLngs);

        IconGenerator icg = new IconGenerator(getActivity());
        Bitmap bm = icg.makeIcon(step.getName());

        MarkerOptions options = new MarkerOptions();
        options.position(latLngs.get(0));
        options.icon(BitmapDescriptorFactory.fromBitmap(bm));
        mMap.addMarker(options);
        mMap.addPolyline(lineOptions);
    }

    public void onBackPressed() {
        startActivity(new Intent(getActivity(), MainMenuActivity.class));
        getActivity().finish();
    }
}
