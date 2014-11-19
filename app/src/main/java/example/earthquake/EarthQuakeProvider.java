package example.earthquake;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by nosovpavel on 18/11/14.
 *
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
    public static final String KEY_LINK = "link";

    private static final int QUAKES = 1;
    private static final int QUAKE_ID = 2;
    private static final int SEARCH = 3;

    EarthQuakeDataBaseHelper dataBaseHelper;

    private static final HashMap<String, String> SEARCH_PROJECTION_MAP;
    static {
        SEARCH_PROJECTION_MAP = new HashMap<String, String>();
        SEARCH_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1,KEY_SUMMARY + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        SEARCH_PROJECTION_MAP.put("_id",KEY_ID + " AS "+ "_id");
    }

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.paad.earthquakeprovider","earthquakes",QUAKES);
        uriMatcher.addURI("com.paad.earthquakeprovider","earthquakes/#",QUAKE_ID);
        uriMatcher.addURI("com.paad.earthquakeprovider",SearchManager.SUGGEST_URI_PATH_QUERY,SEARCH);
        uriMatcher.addURI("com.paad.earthquakeprovider",SearchManager.SUGGEST_URI_PATH_QUERY+"/*",SEARCH);
        uriMatcher.addURI("com.paad.earthquakeprovider",SearchManager.SUGGEST_URI_PATH_SHORTCUT,SEARCH);
        uriMatcher.addURI("com.paad.earthquakeprovider",SearchManager.SUGGEST_URI_PATH_SHORTCUT+"/*",SEARCH);
    }


    @Override
    public boolean onCreate() {
        Context context = getContext();
        dataBaseHelper = new EarthQuakeDataBaseHelper(context,EarthQuakeDataBaseHelper.DATABASE_NAME,null,EarthQuakeDataBaseHelper.DATABASE_VERSION);

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
        SQLiteDatabase database = dataBaseHelper.getReadableDatabase();
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        sqLiteQueryBuilder.setTables(EarthQuakeDataBaseHelper.EARTHQUAKE_TABLE);

        //Если это запрос строки то ограничиваем его одной строкой
        switch (uriMatcher.match(uri)){
            case QUAKE_ID: sqLiteQueryBuilder.appendWhere(KEY_ID+"="+uri.getPathSegments().get(1));
                break;
            case SEARCH: {
                sqLiteQueryBuilder.appendWhere(KEY_SUMMARY + " LIKE \"%" + uri.getPathSegments().get(1) + "%\"");
                sqLiteQueryBuilder.setProjectionMap(SEARCH_PROJECTION_MAP);
                break;
            }
            default:break;
        }

        String ordetBy;
        if(TextUtils.isEmpty(sort)){
            ordetBy = KEY_DATE;
        } else {
            ordetBy = sort;
        }

        //Выполняем запрос
        Cursor c =sqLiteQueryBuilder.query(database,projection,selection,selectionArgs,null,null,ordetBy);

        //Регистрируем контекстный объект ContentResolver он будет оповещен если возвращенные курсором данные изменятся
        c.setNotificationUri(getContext().getContentResolver(),uri);

        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case QUAKES: return "vnd.android.cursor.dir/vnd.paad.earthquake";
            case QUAKE_ID: return "vnd.android.cursor.item/vnd.paad.earthquake";
            case SEARCH: return SearchManager.SUGGEST_MIME_TYPE;
            default:throw new IllegalArgumentException("Unsuported URI: "+uri);
        }
    }

    @Override
    public Uri insert(Uri _uri, ContentValues contentValues) {
        SQLiteDatabase database = dataBaseHelper.getWritableDatabase();
        //Вставляем новую строку, в случае успеха database.insert вернет  номер этой строки
        long rowId =database.insert(EarthQuakeDataBaseHelper.EARTHQUAKE_TABLE,"quake",contentValues);

        //В случае успеха вернем путь Uri к только что вставленной строке
        if(rowId>0) {
            Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        }
        return null;
//        } else {
//            Log.w(EarthQuakeDataBaseHelper.TAG,"Failed to insert row into " + _uri);
//        }
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase database = dataBaseHelper.getWritableDatabase();

        int count;
        switch (uriMatcher.match(uri)){
            case QUAKES:{
                count =database.delete(EarthQuakeDataBaseHelper.EARTHQUAKE_TABLE,where,whereArgs);
                break;
            }
            case QUAKE_ID:{
                String segment = uri.getPathSegments().get(1);
                count = database.delete(EarthQuakeDataBaseHelper.EARTHQUAKE_TABLE,KEY_ID+"="+segment+(!TextUtils.isEmpty(where)?" AND ("+where+")":""),whereArgs);
                break;
            }
            default:throw new IllegalArgumentException("Unsupported uri "+uri);
        }

        getContext().getContentResolver().notifyChange(uri,null);
        return count;

    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] whereArgs) {
        SQLiteDatabase database = dataBaseHelper.getWritableDatabase();

        int count;
        switch (uriMatcher.match(uri)){
            case QUAKES:{
                count =database.update(EarthQuakeDataBaseHelper.EARTHQUAKE_TABLE,contentValues,where,whereArgs);
                break;
            }
            case QUAKE_ID:{
                String segment = uri.getPathSegments().get(1);
                count = database.update(EarthQuakeDataBaseHelper.EARTHQUAKE_TABLE,contentValues,KEY_ID+"="+segment+(!TextUtils.isEmpty(where)?" AND ("+where+")":""),whereArgs);
                break;
            }
            default:throw  new IllegalArgumentException("Unsupported uri "+uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return count;
    }
}
