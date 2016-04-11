package ca.polymtl.squatr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // Our username, passed from previous activity
    private String mUsername = "";

    // All the flags on the map, fetched from the server
    private ArrayList<Flag> mAllFlags = new ArrayList<>();

    // These attributes will deal with the list of flags that are within capturable distance.
    private ListView mProximityListView;
    private ArrayAdapter mProximityListAdapter;
    private ArrayList<Flag> mCloseFlags = new ArrayList<>();

    // The reference to the Firebase db root
    Firebase mFirebaseRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set this as the firebase context
        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase("https://projetinf8405.firebaseio.com/");

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // TODO: get the username from previous activity
        mUsername = "";

        // Setup the proximity list view with the adapter
        mProximityListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mCloseFlags);
        mProximityListView = (ListView) findViewById(R.id.proximityListView);
        mProximityListView.setAdapter(mProximityListAdapter);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        // TODO: move to current location
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(coords));


        // request update from db for flag locations and add change listener
        mFirebaseRef.child("flags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                reloadAllFlags(snapshot);
            }
            @Override public void onCancelled(FirebaseError error) { }
        });
    }

    // This method is called when there is a change in the flags on the database.
    // It should also be called during initialization, to fetch all the flags
    void reloadAllFlags(DataSnapshot snapshot)
    {
        // clear local array
        mAllFlags.clear();

        // clear all markers on map
        mMap.clear();

        // Add each child to our local array
        for (DataSnapshot flag : snapshot.getChildren())
        {
            String name = flag.getKey();
            String owner = flag.child("owner").getValue(String.class);
            Double latitude = flag.child("coords").child("latitude").getValue(Double.class);
            Double longitude = flag.child("coords").child("longitude").getValue(Double.class);
            Integer highscore = flag.child("highscore").child("highscore").getValue(Integer.class);
            if (highscore == null)
            {
                highscore = 0;
            }
            if (name != null && latitude != null && longitude != null)
            {
                Flag newFlag = new Flag(latitude, longitude, name, owner, highscore);
                mAllFlags.add(newFlag);
                addFlagToMap(newFlag);
            }
        }

        //TODO: check proximity and populate close flags list, update array
        // TODO: or setup alternate solution with clickable markers
    }

    private void addFlagToMap(Flag flag)
    {
        LatLng coords = new LatLng(flag.latitude, flag.longitude);
        MarkerOptions markerOptions = new MarkerOptions().position(coords).title(flag.title).draggable(false);
        if (flag.owner != null) {
            markerOptions.snippet(flag.owner);
        }
        // if we are the owner, we put the marker as green, else we put it red
        if (flag.owner != null && !flag.owner.equals("") && flag.owner.equals(mUsername)) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }
        else
        {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
        mMap.addMarker(markerOptions);

    }

    // TODO: function to detect which coords are in proximity
    // TODO: or function to check if coord in param is close enough to us


    private void updateProximityList()
    {
        // TODO: function to update List (maybe, unless we go for the clickable marker solution
    }

    // TODO: function to launch one of two games randomly if in proximity

    // TODO: function to treat return value from minigame (compare with high score and upload data if won)
}
