package example.earthquake;

import android.database.Cursor;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Pavel on 02/04/15.
 */
public class EarthQuakeOverlay {
    Cursor earthquakes;

    GroundOverlayOptions groundOverlayOptions =  new GroundOverlayOptions();

    ArrayList<LatLng> quakeLocations;

    public EarthQuakeOverlay(Cursor cursor){
        super();
        earthquakes = cursor;

        quakeLocations = new ArrayList<LatLng>();
        refreshQuakeLocations();
    }

    public void swapCursor(Cursor cursor) {
        earthquakes = cursor;
        refreshQuakeLocations();
    }


    private void refreshQuakeLocations() {
        quakeLocations.clear();

        if (earthquakes != null && earthquakes.moveToFirst())
            do {
                int latIndex = earthquakes.getColumnIndexOrThrow(EarthQuakeContentProvider.KEY_LOCATION_LAT);
                int lngIndex = earthquakes.getColumnIndexOrThrow(EarthQuakeContentProvider.KEY_LOCATION_LNG);

                Double lat = earthquakes.getFloat(latIndex) * 1E6;
                Double lng = earthquakes.getFloat(lngIndex) * 1E6;

                LatLng geoPoint = new LatLng(lat.intValue(),lng.intValue());
                quakeLocations.add(geoPoint);

            } while(earthquakes.moveToNext());
    }

//    @Override
//    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
//        Projection projection = mapView.getProjection();
//
//        // Create and setup your paint brush
//        Paint paint = new Paint();
//        paint.setARGB(250, 255, 0, 0);
//        paint.setAntiAlias(true);
//        paint.setFakeBoldText(true);
//        if (shadow == false) {
//            for (GeoPoint point : quakeLocations) {
//                Point myPoint = new Point();
//                projection.toPixels(point, myPoint);
//
//                RectF oval = new RectF(myPoint.x-rad, myPoint.y-rad,
//                        myPoint.x+rad, myPoint.y+rad);
//
//                canvas.drawOval(oval, paint);
//            }
//        }
//    }

}
