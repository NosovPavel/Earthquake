package example.earthquake;
import android.app.DialogFragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.app.Notification;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.Date;

/**
 * Created by nosovpavel on 13/10/14.
 */

public class EarthQuakeListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

//    ArrayAdapter<Quake> aa;
    SimpleCursorAdapter adapter;
    private static final String TAG = "EARTHQUAKE";
    private Handler handler = new Handler();



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        int LayoutId = android.R.layout.simple_list_item_2;
        adapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                new String[]{EarthQuakeContentProvider.KEY_SUMMARY},
                new int[]{android.R.id.text1},
                0);
        setListAdapter(adapter);
        getLoaderManager().initLoader(0,null,this);
        refreshEarthQuakes();
    }

    private void refreshEarthQuakes() {
        getLoaderManager().restartLoader(0,null,EarthQuakeListFragment.this);
        getActivity().startService(new Intent(getActivity(),EarthQuakeUpdateService.class));
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] projection = new String[]{EarthQuakeContentProvider.KEY_ID, EarthQuakeContentProvider.KEY_SUMMARY};
        MainActivity mainActivity = (MainActivity)getActivity();

        String where = EarthQuakeContentProvider.KEY_MAGNITUDE  + " >= " + mainActivity.minimumMagnitude;

        CursorLoader cr = new CursorLoader(getActivity(), EarthQuakeContentProvider.CONTENT_URI,projection,where,null,null);
        return cr;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ContentResolver cr = getActivity().getContentResolver();

        Cursor result = cr.query(ContentUris.withAppendedId(EarthQuakeContentProvider.CONTENT_URI, id), null, null, null, null);

        if (result.moveToFirst()) {
            Date date =
                    new Date(result.getLong(
                            result.getColumnIndex(EarthQuakeContentProvider.KEY_DATE)));
            String details =
                    result.getString(
                            result.getColumnIndex
                                    (EarthQuakeContentProvider.KEY_DETAILS));
            double magnitude =
                    result.getDouble(
                            result.getColumnIndex
                                    (EarthQuakeContentProvider.KEY_MAGNITUDE));
            String linkstring =
                    result.getString(
                            result.getColumnIndex
                                    (EarthQuakeContentProvider.KEY_LINK));
            double lat =
                    result.getDouble(
                            result.getColumnIndex
                                    (EarthQuakeContentProvider.KEY_LOCATION_LAT));
            double lng =
                    result.getDouble(
                            result.getColumnIndex
                                    (EarthQuakeContentProvider.KEY_LOCATION_LNG));
            Location location = new Location("db");
            location.setLatitude(lat);
            location.setLongitude(lng);


            Quake quake = new Quake(date, details, location,
                    magnitude, linkstring);
            DialogFragment newFragment =
                    EarthQuakeDialog.newInstance(getActivity(), quake);
            newFragment.show(getFragmentManager(), "dialog");

        }
    }
}
