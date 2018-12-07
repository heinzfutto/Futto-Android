package org.futto.app.ui.user;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.futto.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends BaseMapsActivity {

    @BindView(R.id.rootFrame)
    FrameLayout rootFrame;

    @BindView(R.id.rootll)
    LinearLayout rootll;

    @BindView(R.id.rlwhere)
    RelativeLayout rlWhere;

    @BindView(R.id.ivHome)
    ImageView ivHome;

    @BindView(R.id.tvWhereTo)
    TextView tvWhereto;

    @BindView(R.id.ivMenu)
    ImageView menu;

    public static LatLng from;
    public static LatLng to;
    ArgbEvaluator argbEvaluator;

    private LatLng destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        argbEvaluator = new ArgbEvaluator();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    }

    @OnClick(R.id.ivHome)
    void showViewPagerWithTransition() {
        Intent i = new Intent(this, DirectionsActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.ivMenu)
    void showMenu() {

        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.ivMenu));
        popupMenu.getMenu().add(1, R.id.menu_about,3, R.string.menu_about);
        popupMenu.getMenu().add(1, R.id.menu_change_password, 4, R.string.menu_change_password);
        popupMenu.getMenu().add(1, R.id.view_survey_answers,5, R.string.view_survey_answers);
        popupMenu.getMenu().add(1, R.id.menu_signout,6, R.string.menu_sign_out);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return MapsActivity.super.onOptionsItemSelected(item);
            }
        });

        popupMenu.show();

    }

    @OnClick(R.id.rlwhere)
    void openPlacesView() {
        openPlaceAutoCompleteView();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        rlWhere.setVisibility(View.VISIBLE);
    }

    @Override
    protected void setUpPolyLine(Place place) {
        if (place != null) {
            tvWhereto.setText(place.getName().toString());
        }
        else {
            tvWhereto.setText("Where to?");
        }
        ivHome.setVisibility(View.VISIBLE);
        LatLng source = new LatLng(getUserLocation().getLatitude(), getUserLocation().getLongitude());
        LatLng destination = getDestinationLatLong();
        addMarker(destination);
        addMarker(source);
        from = source;
        to = place.getLatLng();
    }

    @Override
    public void setSource(LatLng source) {
        addMarker(source);
        from = source;
    }

    @Override
    public void onBackPressed() {

        if (ivHome.getVisibility() == View.VISIBLE) {

            TransitionManager.beginDelayedTransition(rootFrame);
            mMap.setPadding(0, 0, 0, 0);
            ivHome.setVisibility(View.INVISIBLE);
            rlWhere.setVisibility(View.VISIBLE);
            tvWhereto.setText("Where to?");
            mMap.clear();
            return;
        }
        finish();
    }


}
