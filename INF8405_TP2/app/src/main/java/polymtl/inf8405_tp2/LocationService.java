package polymtl.inf8405_tp2;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by Thomas on 22/03/2016.
 */
public class LocationService{
    Location currentLocation;
    Location[] membersLocation;

    public LocationService(){
        LocationManager lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        currentLocation = new Location("Test");
        currentLocation.setLatitude(50);
        currentLocation.setLongitude(75);

        membersLocation = new Location[0];
    }

    public Location triangulateLocation(){
        Location locTemp = new Location("Meeting Location");
        double longitude = currentLocation.getLongitude();
        double latitude = currentLocation.getLatitude();

        for(int i = 0; i<membersLocation.length; i++){
            longitude += membersLocation[i].getLongitude();
            latitude += membersLocation[i].getLatitude();
        }
        longitude = longitude/(membersLocation.length+1);
        latitude = latitude/(membersLocation.length+1);

        locTemp.setLatitude(latitude);
        locTemp.setLongitude(longitude);

        return locTemp;
    }

    public void getOthersLocation(Location[] loc){
        //TODO: get location from server
        membersLocation = new Location[loc.length];
        for(int i = 0; i<loc.length; i++)
            membersLocation[i] = loc[i];
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
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
