package example.earthquake;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

        int LayoutId = android.R.layout.simple_list_item_1;
        adapter = new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_1,null,new String[]{EarthQuakeProvider.KEY_SUMMARY},new int[]{android.R.id.text1},0);
        setListAdapter(adapter);

        getLoaderManager().initLoader(0,null,this);

        refreshEarthQuakes();
    }

    private void refreshEarthQuakes() {
        getLoaderManager().restartLoader(0,null,EarthQuakeListFragment.this);

        getActivity().startService(new Intent(getActivity(),EarthQuakeService.class));
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] projection = new String[]{EarthQuakeProvider.KEY_ID, EarthQuakeProvider.KEY_SUMMARY};
        main earthquakeActivity = (main)getActivity();

        String where = EarthQuakeProvider.KEY_MAGNITUDE  + "> " + earthquakeActivity.minimumMagnitude;

        CursorLoader cr = new CursorLoader(getActivity(),EarthQuakeProvider.CONTENT_URI,projection,where,null,null);
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
