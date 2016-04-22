package ca.polymtl.squatr;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private String provider;
    private LocationManager locationManager;
    private UserLocation locationListener;
    private Location lastKnownLocation;
    private TextView mTbBatterie;
    private int initialBatterieLevel = -1;

    // Our username, passed from previous activity
    private String mUsername = "";

    // All the flags on the map, fetched from the server
    private final ArrayList<Flag> mAllFlags = new ArrayList<>();

    // These attributes will deal with the list of flags that are within capturable distance.
    private ArrayAdapter mProximityListAdapter;
    private final ArrayList<Flag> mCloseFlags = new ArrayList<>();

    // The reference to the Firebase db root
    private Firebase mFirebaseRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // set this as the firebase context
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

        // et username from Intent
        Bundle data = getIntent().getExtras();
        mUsername = data.getString("Username");
        initialBatterieLevel = data.getInt("Battery");

        // Setup the proximity list view with the adapter
        //noinspection unchecked
        mProximityListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mCloseFlags);

        ListView mProximityListView = (ListView) findViewById(R.id.proximityListView);
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
                        // Do nothing
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        locationManager.removeUpdates(locationListener);

                        if (mCloseFlags.get(position).game.equals("maze")) {
                            // Start Maze Game
                            Intent intent = new Intent(MapsActivity.this, MazeGame.class);
                            intent.putExtra("flag", mCloseFlags.get(position).title);
                            intent.putExtra("highscore", mCloseFlags.get(position).highscore);
                            intent.putExtra("Battery", initialBatterieLevel);
                            startActivityForResult(intent, 1);
                        } else {
                            // Start light game
                            Intent intent = new Intent(MapsActivity.this, LightGame.class);
                            intent.putExtra("flag", mCloseFlags.get(position).title);
                            intent.putExtra("highscore", mCloseFlags.get(position).highscore);
                            intent.putExtra("Battery", initialBatterieLevel);
                            startActivityForResult(intent, 2);
                        }
                    }
                });
                alertDialog.show();
            }
        });

        //Écouté le changement de niveau de batterie
        mTbBatterie = (TextView) findViewById(R.id.lblBatterie);
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return;
        }
        StartLocationListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start location listener
        mGoogleApiClient.connect();
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop listener
        try {
            this.unregisterReceiver(this.mBatInfoReceiver);
            locationManager.removeUpdates(locationListener);
        } catch(Exception e){ }

        if(mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // To activate location service (if they are disabled)
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    Toast t = Toast.makeText(this, "Location Services must be active to use the app", Toast.LENGTH_LONG);
                    t.show();
                    StartLocationListener();
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

    // Call when games end
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            // Start location listener
            locationManager.requestLocationUpdates(provider, 0, 0, locationListener);

            // Retrieve highscore (0 if no highscore)
            final double highscore = data.getExtras().getDouble("highscore");
            // Retrieve flag name
            final String flag = data.getExtras().getString("flag");
            String game=null, message;

            if(requestCode == 1) {
                game = "Maze Game";
            }

            if(requestCode == 2) {
                game = "Light Game";
            }

            // Set highscore on database
            if(highscore != 0) {
                message = "Félicitations!! Vous avez battu le meilleur score de " + game + "! Vous avez maintenant ce drapeau : " + flag;

                if(flag != null) {
                    mFirebaseRef.child("flags").child(flag).child("owner").setValue(mUsername);
                    mFirebaseRef.child("flags").child(flag).child("highscore").setValue(highscore);
                }
            }
            else
                message = "Vous n'avez pas battu le meilleur score. Meilleur chance la prochaine fois!";

            // Show user message after game
            final AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
            alertDialog.setMessage(message);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Continuer", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    updateProximityList(locationManager.getLastKnownLocation(provider));
                }
            });
            alertDialog.show();
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


    public void StartLocationListener(){
        // Start location listener
        Criteria criteria = new Criteria();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(criteria, false);
        locationListener = new UserLocation();
        locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
    }

    // This method is called when there is a change in the flags on the database.
    // It should also be called during initialization, to fetch all the flags
    private void reloadAllFlags(DataSnapshot snapshot)
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
        updateProximityList(lastKnownLocation);
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

    private void updateProximityList(Location location)
    {
        if(location != null) {
            // Retrieve flag from list
            mCloseFlags.clear();
            for (Flag flag : mAllFlags) {
                Location flagLoc = new Location("flag");
                flagLoc.setLatitude(flag.latitude);
                flagLoc.setLongitude(flag.longitude);
                // Add flag to close flags list
                if (location.distanceTo(flagLoc) < 500 && !mCloseFlags.contains(flag)) {
                    mCloseFlags.add(flag);
                }
            }
            // Notify adapter
            mProximityListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(lastKnownLocation!= null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            firstUpdate = false;
            updateProximityList(lastKnownLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {   }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {   }

    // To move camera
    private boolean firstUpdate = true;

    private class UserLocation implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            if(firstUpdate){
                firstUpdate = false;
                // Move camera on first update
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }

            // Update list of close flag
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