package example.earthquake;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by nosovpavel on 18/11/14.
 *  SQLiteOpenHelper  —  абстрактный  класс,  предназначенный  для  реализации  наибо
 * лее  эффективной  модели,  с  помощью  которой  можно  создавать,  открывать  и  обновлять
 * базы  данных.  При  реализации  этого  вспомогательного  класса  от  вас  скрывается  логика,
 * на  основе  которой  принимается  решение  о  создании  или  обновлении  базы  данных
 * перед  ее  открытием.  Кроме  того,  вы  получаете  гарантию,  что  каждая  операция  будет
 * выполнена  самым  рациональным  образом.
 * Рекомендуется  создавать  и  открывать  базы  данных  по  мере  их  необходимости.
 * SQLiteOpenHelper  кэширует  экземпляры  баз  данных  после  их  успешного  открытия,
 * поэтому  сразу  перед  выполнением  запросов  или  транзакций  вы  должны  открыть
 * базу  данных.  По  той  же  причине  нет  необходимости  закрывать  базу  данных  вруч
 * ную,  разве  что  вы  больше  никогда  не  будете  ее  снова  использовать.
 */
public class EarthQuakeDataBaseHelper extends SQLiteOpenHelper {

    public static final String TAG = "EarthQuakeProvider";

    public static final String DATABASE_NAME = "earthquakes.db";
    public static final int DATABASE_VERSION = 1;
    public static final String EARTHQUAKE_TABLE = "earthquakes";

    private static final String DATABASE_CREATE  =
                "create  table  "  +  EARTHQUAKE_TABLE  +  " ("
            +  EarthQuakeContentProvider.KEY_ID  +  "  integer  primary  key  autoincrement,  "
            +  EarthQuakeContentProvider.KEY_DATE  +  "  INTEGER,  "
            +  EarthQuakeContentProvider.KEY_DETAILS  +  "  TEXT,  "
            +  EarthQuakeContentProvider.KEY_SUMMARY  +  "  TEXT,  "
            +  EarthQuakeContentProvider.KEY_LOCATION_LAT  +  "  FLOAT,  "
            +  EarthQuakeContentProvider.KEY_LOCATION_LNG  +  "  FLOAT,  "
            +  EarthQuakeContentProvider.KEY_MAGNITUDE  +  "  FLOAT,  "
            +  EarthQuakeContentProvider.KEY_LINK  +  "  TEXT);";

    private SQLiteDatabase earthQuakeDB;

    public EarthQuakeDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public EarthQuakeDataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.w(TAG,"Updating database from version "+oldVersion +" to "+newVersion +", which will destroy all old data");

        earthQuakeDB.execSQL("DROP TABLE IF EXISTS "+ EARTHQUAKE_TABLE);
        onCreate(sqLiteDatabase);
    }
}
