package example.earthquake;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

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
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by nosovpavel on 20/11/14.
 */
public class EarthQuakeUpdateService extends IntentService {
    public static final String TAG = "EARTHQUAKE_UPDATE_SERVICE";
    public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
    public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
    public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";

    public static final String QUAKES_REFRESHED = "com.paad.earthquake.QUAKES_REFRESHED";

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    private Context context;
    private SharedPreferences prefs;
    private int minimumMagnitude;

    private Notification.Builder earthQuakeNotificationBuilder;
    public static final  int NOTIFICATION_ID= 1;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public EarthQuakeUpdateService(String name) {
        super(name);
    }

    public EarthQuakeUpdateService() {
        super("EarthQuakeUpdateService");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //  Получите  Общие  настройки.
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int updateFreq = Integer.parseInt(prefs.getString(PREF_UPDATE_FREQ, "60"));
        boolean autoUpdateChecked = prefs.getBoolean(PREF_AUTO_UPDATE, false);

        if (autoUpdateChecked) {
            int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
            long timeToRefresh = SystemClock.elapsedRealtime() + updateFreq * 60 * 1000;
            alarmManager.setInexactRepeating(alarmType, timeToRefresh, updateFreq * 60 * 1000, alarmIntent);
        } else {
            alarmManager.cancel(alarmIntent);
        }
        refreshEarthQuakes();
        sendBroadcast(new Intent(QUAKES_REFRESHED));
    }

    private void addNewQuake(Quake _quake) {

        ContentResolver cr = getContentResolver();
        String where = EarthQuakeContentProvider.KEY_DATE + " = " + _quake.getDate().getTime();

        Cursor query = cr.query(EarthQuakeContentProvider.CONTENT_URI, null, where, null, null);

        if (query.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(EarthQuakeContentProvider.KEY_DATE, _quake.getDate().getTime());
            values.put(EarthQuakeContentProvider.KEY_DETAILS, _quake.getDetails());
            values.put(EarthQuakeContentProvider.KEY_SUMMARY, _quake.toString());

            double lat = _quake.getLocation().getLatitude();
            double lng = _quake.getLocation().getLongitude();

            values.put(EarthQuakeContentProvider.KEY_LOCATION_LAT, lat);
            values.put(EarthQuakeContentProvider.KEY_LOCATION_LNG, lng);

            values.put(EarthQuakeContentProvider.KEY_LINK, _quake.getLink());
            values.put(EarthQuakeContentProvider.KEY_MAGNITUDE, _quake.getMagnitude());

            broadcastNotification(_quake);

            cr.insert(EarthQuakeContentProvider.CONTENT_URI, values);

        }
        query.close();
    }

    private void broadcastNotification(Quake quake){

        //Если выбранная в настройках минимальная магнитуда меньше чем у текущего землетрясения, то показываем его
        if(quake.getMagnitude() >= minimumMagnitude) {

            double vibrateLength = 100 * Math.exp(0.53 * quake.getMagnitude());
            long[] vibrate = new long[]{100, 100, (long) vibrateLength};
            earthQuakeNotificationBuilder.setVibrate(vibrate);

            if (quake.getMagnitude() > 6) {
                Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                earthQuakeNotificationBuilder.setSound(ringUri);
            }

            int color;
            if (quake.getMagnitude() < 5.4) {
                color = Color.GREEN;
            } else if (quake.getMagnitude() < 6) {
                color = Color.YELLOW;
            } else {
                color = Color.RED;
            }

            Intent startActivitylntent = new Intent(this, Quake.class);
            PendingIntent launchlntent =
                    PendingIntent.getActivity(this, 0, startActivitylntent, 0);
            earthQuakeNotificationBuilder
                    .setContentIntent(launchlntent)
                    .setWhen(quake.getDate().getTime())
                    .setContentTitle("M:" + quake.getMagnitude()).setContentText(quake.getDetails());
            NotificationManager notificationManager = (NotificationManager) getSystemService
                    (Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID,
                    earthQuakeNotificationBuilder.getNotification());
        } else {
            return;
        }
    }

    public void refreshEarthQuakes() {

        URL url;

        try {
            String quakesFeed = getString(R.string.earth_Quake_Feed);
            url = new URL(quakesFeed);

            URLConnection connection;
            connection = url.openConnection();

            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
            int responseCode = httpURLConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = httpURLConnection.getInputStream();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                Document dom = db.parse(in);
                Element docEle = dom.getDocumentElement();

                //Получаем список всех записей о землетрясениях

                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                Date qdate = new GregorianCalendar(0, 0, 0).getTime();

                NodeList nl = docEle.getElementsByTagName("entry");
                if (nl != null && nl.getLength() > 0) {


                    for (int i = 0; i < nl.getLength(); i++) {
//                        Log.w(TAG, "i =" + i + "Length =" + nl.getLength());

                        Element entry = (Element) nl.item(i);
                        Element title = (Element) entry.getElementsByTagName("title").item(0);
                        Element q = (Element) entry.getElementsByTagName("georss:point").item(0);
                        Element when = (Element) entry.getElementsByTagName("updated").item(0);
                        Element link = (Element) entry.getElementsByTagName("link").item(0);

                        String details = title.getFirstChild().getNodeValue();
                        String hostName = "http://earthquake.usgs.gov";
                        String linkString = hostName + link.getAttribute("href");
                        String point;
                        if (q != null) {
                            point = q.getFirstChild().getNodeValue();
                        } else {
                            point = "12.4670 -88.2750";
                        }
                        String dt = when.getFirstChild().getNodeValue();

                        try {
                            qdate = (Date) formatter.parse(dt);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Date Parsing Exception");
                        }

                        String[] location = point.split(" ");
                        Location l = new Location("dummyGPS");
                        l.setLatitude(Double.parseDouble(location[0]));
                        l.setLongitude(Double.parseDouble(location[1]));

                        String magnitudeString = details.split(" ")[1];
                        double magnitude;

                        int end = magnitudeString.length() - 1;
                        try {
                            magnitude = Double.parseDouble(magnitudeString.substring(0, end));
                            details = details.split(",")[1].trim();
                        } catch (Exception e) {
                            magnitude = 0;
                        }

                        final Quake quake = new Quake(qdate, details, l, magnitude, linkString);

                        addNewQuake(quake);
                    }
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG, "MalformedURLException");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "IOException");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            Log.d(TAG, "ParserConfigurationException");
        } catch (SAXException e) {
            e.printStackTrace();
            Log.d(TAG, "SAXException");
        } finally {
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        String alarmAction = EarthQuakeAlarmReceiver.ACTION_REFRESH_EARTHQUAKE_ALARM;

        Intent intentToFire = new Intent(alarmAction);

        alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);

        earthQuakeNotificationBuilder = new Notification.Builder(this);
        earthQuakeNotificationBuilder.setAutoCancel(true).setTicker("Earthquake detected").setSmallIcon(R.drawable.ic_launcher);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }




    public Context getContext() {
        if(context == null) {
            context = getApplicationContext();
        }
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public SharedPreferences getPrefs() {
        if(prefs == null)
            prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs;
    }

    public void setPrefs(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public int getMinimumMagnitude() {
        return Integer.parseInt(getPrefs().getString(EarthQuakeUpdateService.PREF_MIN_MAG, "1"));
    }

    public void setMinimumMagnitude(int minimumMagnitude) {
        this.minimumMagnitude = minimumMagnitude;
    }
}
