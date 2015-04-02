package example.earthquake;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by nosovpavel on 26/11/14.
 */
public class EarthQuakeMapFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    static final LatLng HAMBURG = new LatLng(53.558, 9.927);
    static final LatLng KIEL = new LatLng(53.551, 9.993);
    private GoogleMap map;

    EarthQuakeOverlay eo;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.map_fragment, container, false);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();

        Marker hamburg = map.addMarker(new MarkerOptions().position(HAMBURG)
                .title("Hamburg"));

        Marker kiel = map.addMarker(new MarkerOptions()
                .position(KIEL)
                .title("Kiel")
                .snippet("Kiel is cool")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_launcher)));

        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.circle_red))
                .position(HAMBURG, 60f, 60f);
        map.addGroundOverlay(newarkMap);

//        Move the camera instantly to hamburg with a zoom of 15.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(HAMBURG, 15));

        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        getLoaderManager().initLoader(0,null,this);

        eo = new EarthQuakeOverlay(null);
//        map.addGroundOverlay(eo.groundOverlayOptions);

        return v;
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (map != null) {
            getFragmentManager()
                    .beginTransaction()
                    .remove(getFragmentManager().findFragmentById(R.id.map))
                    .commit();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = new String[] {
                EarthQuakeContentProvider.KEY_ID,
                EarthQuakeContentProvider.KEY_LOCATION_LAT,
                EarthQuakeContentProvider.KEY_LOCATION_LNG,
        };

        MainActivity earthquakeActivity = (MainActivity)getActivity();
        String where = EarthQuakeContentProvider.KEY_MAGNITUDE + " > " +
                earthquakeActivity.minimumMagnitude;

        CursorLoader loader = new CursorLoader(getActivity(),
                EarthQuakeContentProvider.CONTENT_URI, projection, where, null, null);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        eo.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        eo.swapCursor(null);
    }
}
