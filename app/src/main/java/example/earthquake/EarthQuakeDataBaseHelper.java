package example.earthquake;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by nosovpavel on 18/11/14.
 */
public class EarthQuakeDataBaseHelper extends SQLiteOpenHelper {

    public static final String TAG = "EarthQuakeProvider";

    public static final String DATABASE_NAME = "earthquakes.db";
    public static final int DATABASE_VERSION = 1;
    public static final String EARTHQUAKE_TABLE = "earthquakes";

    private 	static 	final 	String 	DATABASE_CREATE  =
            "create  table  “  +  EARTHQUAKE_TABLE  +  ("
            +  EarthQuakeProvider.KEY_ID  +  "  integer  primary  key  autoincrement,  "
            +  EarthQuakeProvider.KEY_DATE  +  "  INTEGER,  "
            +  EarthQuakeProvider.KEY_DETAILS  +  "  TEXT,  "
            +  EarthQuakeProvider.KEY_SUMMARY  +  "  TEXT,  "
            +  EarthQuakeProvider.KEY_LOCATION_LAT  +  "  FLOAT,  "
            +  EarthQuakeProvider.KEY_LOCATION_LNG  +  "  FLOAT,  "
            +  EarthQuakeProvider.KEY_MAGNITUDE  +  "  FLOAT,  "
            +  EarthQuakeProvider.KEY_LINK  +  "  TEXT);";

    private SQLiteDatabase earthQuakeDB;

    public EarthQuakeDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public EarthQuakeDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        earthQuakeDB.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.w(TAG,"Updating database from version "+oldVersion +" to "+newVersion +", which will destroy all old data");

        earthQuakeDB.execSQL("DROP TABLE IF EXISTS "+ EARTHQUAKE_TABLE);
        onCreate(sqLiteDatabase);
    }
}
