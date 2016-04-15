package ca.polymtl.squatr;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String provider;
    private LocationManager locationManager;
    private UserLocation locationListener;

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
        Bundle data = getIntent().getExtras();
        mUsername = data.getString("Username");

        // Setup the proximity list view with the adapter
        mProximityListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mCloseFlags);
        mProximityListView = (ListView) findViewById(R.id.proximityListView);
        mProximityListView.setAdapter(mProximityListAdapter);
        mProximityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
                alertDialog.setTitle("Start Game");
                alertDialog.setMessage("You are about to start the " + mCloseFlags.get(position).game + " are you ready ? ");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mCloseFlags.get(position).game == "maze") {
                            Intent intent = new Intent(MapsActivity.this, MazeGame.class);
                            intent.putExtra("flag", mCloseFlags.get(position).title);
                            intent.putExtra("highscore", mCloseFlags.get(position).highscore);
                            startActivityForResult(intent, 1);
                        } else {
                            Intent intent = new Intent(MapsActivity.this, LightGame.class);
                            intent.putExtra("flag", mCloseFlags.get(position).title);
                            intent.putExtra("highscore", mCloseFlags.get(position).highscore);
                            startActivityForResult(intent, 2);
                        }
                    }
                });
                alertDialog.show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    Toast t = Toast.makeText(this, "Location Services must be active to use the app", Toast.LENGTH_LONG);
                    t.show();
                    return;
                }
                Toast t = Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT);
                t.show();
            } else {
                Toast t = Toast.makeText(this, "Location Services must be active to use the app", Toast.LENGTH_LONG);
                t.show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            int highscore = extras.getInt("highscore");
            String flag = extras.getString("flag");

            System.out.println("New Maze highscore = " + highscore + " on flag " + flag);
            //if(highscore != 0)
              //TODO:Load new highscore on DB (recoit un chiffre autre que 0 si cest le highscore)
        }

        if(requestCode == 2 && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            double highscore = extras.getDouble("highscore");
            String flag = extras.getString("flag");

            System.out.println("New Light highscore = " + highscore + " on flag " + flag);
        }
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        // TODO: move to current location
        //mMap.moveCamera(CameraUpdateFactory.newLatLng();

        // request update from db for flag locations and add change listener
        mFirebaseRef.child("flags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                reloadAllFlags(snapshot);
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });

        Criteria criteria = new Criteria();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(criteria, false);
        locationListener = new UserLocation();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
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
            Integer highscore = flag.child("highscore").getValue(Integer.class);
            String game = flag.child("game").getValue(String.class);

            if (highscore == null)
            {
                highscore = 0;
            }
            if (name != null && latitude != null && longitude != null)
            {
                Flag newFlag = new Flag(latitude, longitude, name, owner, highscore, game);
                mAllFlags.add(newFlag);
                addFlagToMap(newFlag);
            }
        }
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

    private void updateProximityList(Location location)
    {
        for(Flag flag : mAllFlags){
            Location flagLoc = new Location("flag");
            flagLoc.setLatitude(flag.latitude);
            flagLoc.setLongitude(flag.longitude);
            if(location.distanceTo(flagLoc) < 100 && !mCloseFlags.contains(flag)) {
                mCloseFlags.add(flag);
            }
            if(location.distanceTo(flagLoc) > 100 && mCloseFlags.contains(flag)){
                mCloseFlags.remove(flag);
            }
        }
        // TODO: function to update List (maybe, unless we go for the clickable marker solution
    }

    // TODO: function to launch one of two games randomly if in proximity

    // TODO: function to treat return value from minigame (compare with high score and upload data if won)

    private class UserLocation implements LocationListener{
        private boolean firstUpdate = true;

        @Override
        public void onLocationChanged(Location location) {
            if(firstUpdate){
                firstUpdate = false;
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }

            updateProximityList(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
