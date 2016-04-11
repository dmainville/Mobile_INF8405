package ca.polymtl.squatr;

import java.io.Serializable;

/// Describes a flag, i.e. a capturable location on the map
public class Flag implements Serializable{
    public double latitude;
    public double longitude;
    public String title;
    public String owner;

    public Flag(double latitude, double longitude, String title, String owner)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.owner = owner;
        this.title = title;
    }

    public String toString()
    {
        return title + "\n(" + latitude + ", " + longitude + ") " + "\nPropriétaire: " + owner;
    }
}
