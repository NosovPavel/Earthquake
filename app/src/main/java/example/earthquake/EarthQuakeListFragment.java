package example.earthquake;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SimpleCursorAdapter;

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
}
