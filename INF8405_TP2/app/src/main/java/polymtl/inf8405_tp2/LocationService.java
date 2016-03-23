package polymtl.inf8405_tp2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

public class LocationService implements LocationListener{
    Location currentLocation = null;
    LocationManager mLocationManager;

    public LocationService(MainActivity context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            //return;
        }
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
        mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0, this);
        //To get last known location without onLocationChange
        currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(currentLocation == null)
            currentLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onLocationChanged(Location location) {
        // on veut être précis en dedans de 50 m.
        if (location.getAccuracy() < 50)
        {
            currentLocation = location;
            try {
                mLocationManager.removeUpdates(this);
            }
            catch (SecurityException e)
            {
                // si l'usager a déjà révoqué la permission, on n'a pas besoin de le faire.
            }

            // TODO: afficher utilisation de la batterie?
        }
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
