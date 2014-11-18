package example.earthquake;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by nosovpavel on 18/11/14.
 */
public class EarthQuakeProvider extends ContentProvider {

    public static final Uri CONTENT_URI = Uri.parse("content://com.paad.earthquakeprovider/earthquakes");

    //Names of fields
    public static final String KEY_ID ="_id";
    public static final String KEY_DATE = "date";
    public static final String KEY_DETAILS = "details";
    public static final String KEY_SUMMARY = "summary";
    public static final String KEY_LOCATION_LAT = "latitude";
    public static final String KEY_LOCATION_LNG = "longitude";
    public static final String KEY_MAGNITUDE = "magnitude";
    public static final String KEY_LINK = "magnitude";

    EarthQuakeDataBaseHelper dataBaseHelper;


    @Override
    public boolean onCreate() {
        Context context = getContext();
        dataBaseHelper = new EarthQuakeDataBaseHelper(context,EarthQuakeDataBaseHelper.DATABASE_NAME,null,EarthQuakeDataBaseHelper.DATABASE_VERSION);

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
