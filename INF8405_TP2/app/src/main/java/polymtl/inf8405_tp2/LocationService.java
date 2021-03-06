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
    Location currentLocation = null; //< doit rester null jusqu'à ce qu'on ait la valeur désirée
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
        //mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1000, 0, this);
    }


    @Override
    public void onLocationChanged(Location location) {
        // on veut être précis en dedans de 100 m.
        if (location.getAccuracy() < 100)
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
