package example.earthquake;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by nosovpavel on 13/10/14.
 */
public class Quake {

    public Quake(Date date, String details, Location location, double magnitude, String link) {
        this.date = date;
        this.details = details;
        this.location = location;
        this.magnitude = magnitude;
        this.link = link;
    }

    public Date getDate() {
        return date;
    }

    public String getDetails() {
        return details;
    }

    public Location getLocation() {
        return location;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public String getLink() {
        return link;
    }

    private Date date;
    private String details;
    private Location location;
    private double magnitude;
    private String link;

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yy.MM.dd HH:mm");
        String dateString = sdf.format(date);

        return dateString+", Magnitude:"+magnitude+ " " +details;
    }
}
