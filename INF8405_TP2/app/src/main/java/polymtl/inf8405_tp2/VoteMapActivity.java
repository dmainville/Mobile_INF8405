package polymtl.inf8405_tp2;

import android.location.Location;
import android.location.LocationListener;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class VoteMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationService locService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_map);

        locService = new LocationService();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        System.out.println("ON_MAP_READY");
        mMap = googleMap;

        Location loc = locService.triangulateLocation();
        // Add a marker in Sydney and move the camera
        LatLng latlng = new LatLng(loc.getLatitude(), loc.getLatitude());
        mMap.addMarker(new MarkerOptions().position(latlng).title(loc.getProvider()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));

    }
}
