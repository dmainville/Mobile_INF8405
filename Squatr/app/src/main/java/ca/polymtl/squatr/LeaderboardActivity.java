package ca.polymtl.squatr;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Objects;

public class LeaderboardActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private ListView mLeaderboardListView;
    private ArrayAdapter mLeaderboardListAdapter;
    private ArrayList<Flag> mLeaderboardArray;
    private String mUsername;
    private GoogleMap mMap;
    private Firebase mFirebaseRef;
    private TextView distanceToFlagTextView;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private TextView mTbBatterie;
    private int initialBatterieLevel = -1;

    @Override
    protected void onStop() {
        if(mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        try {
            this.unregisterReceiver(this.mBatInfoReceiver);
        } catch(Exception e){ }
        super.onStop();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        distanceToFlagTextView = (TextView) findViewById(R.id.distanceToFlagTextView);

        Bundle data = getIntent().getExtras();
        mUsername = data.getString("Username");
        initialBatterieLevel = data.getInt("Battery");

        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase("https://projetinf8405.firebaseio.com/");

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mGoogleApiClient.connect();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Écouté le changement de niveau de batterie
        mTbBatterie = (TextView) findViewById(R.id.lblBatterie);
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {

            //Récupérer le niveau actuel de la batterie
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);

            if(initialBatterieLevel == -1)
                initialBatterieLevel = level;

            int consommation  = initialBatterieLevel-level;
            //Consommation de batterie : 0%

            //Afficher la différence avec le niveau initial
            mTbBatterie.setText("Batterie : "+consommation+"%");
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        mLeaderboardArray = new ArrayList<>();

        mLeaderboardListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mLeaderboardArray);
        mLeaderboardListView = (ListView) findViewById(R.id.leaderboardListView);
        mLeaderboardListView.setAdapter(mLeaderboardListAdapter);
        mLeaderboardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Location flagLoc = new Location(mLeaderboardArray.get(position).title);
                flagLoc.setLatitude(mLeaderboardArray.get(position).latitude);
                flagLoc.setLongitude(mLeaderboardArray.get(position).longitude);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(flagLoc.getLatitude(), flagLoc.getLongitude())));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                distanceToFlagTextView.setText("Distance to " + flagLoc.getProvider() + " : " + String.format("%.2f",mLastLocation.distanceTo(flagLoc)) + " m");
            }
        });

        // request update from db for flag locations and add change listener
        mFirebaseRef.child("flags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                LoadAllFlag(snapshot);
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

    private void LoadAllFlag(DataSnapshot snapshot) {
        mLeaderboardArray.clear();

        // Add each child to our local array
        for (DataSnapshot flag : snapshot.getChildren())
        {
            String name = flag.getKey();
            String owner = flag.child("owner").getValue(String.class);
            Double latitude = flag.child("coords").child("latitude").getValue(Double.class);
            Double longitude = flag.child("coords").child("longitude").getValue(Double.class);
            Integer highscore = flag.child("highscore").getValue(Integer.class);
            String game = flag.child("game").getValue(String.class);

            if (highscore == null)
            {
                highscore = 0;
            }
            if (name != null && latitude != null && longitude != null)
            {
                Flag newFlag = new Flag(latitude, longitude, name, owner, highscore, game);
                mLeaderboardArray.add(newFlag);
                AddFlagToMap(newFlag);
            }
        }
        mLeaderboardListAdapter.notifyDataSetChanged();
    }

    private void AddFlagToMap(Flag flag){
        LatLng coords = new LatLng(flag.latitude, flag.longitude);
        MarkerOptions markerOptions = new MarkerOptions().position(coords).title(flag.title).draggable(false);
        if (flag.owner != null) {
            markerOptions.snippet(flag.owner);
        }
        // if we are the owner, we put the marker as green, else we put it red
        if (flag.owner != null && !flag.owner.equals("") && flag.owner.equals(mUsername)) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        mMap.addMarker(markerOptions);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
