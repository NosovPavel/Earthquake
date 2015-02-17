package example.earthquake;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
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
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by nosovpavel on 20/11/14.
 */
public class EarthQuakeService extends Service {
    public static final String TAG = "EARTHQUAKE_UPDATE_SERVICE";
    public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
    public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
    public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void addNewQuake(Quake _quake) {

        ContentResolver cr = getContentResolver();
        String where = EarthQuakeContentProvider.KEY_DATE + " = " + _quake.getDate().getTime();

        Cursor query = cr.query(EarthQuakeContentProvider.CONTENT_URI,null,where,null,null);

        if(query.getCount()==0){
            ContentValues values = new ContentValues();
            values.put(EarthQuakeContentProvider.KEY_DATE,_quake.getDate().getTime());
            values.put(EarthQuakeContentProvider.KEY_DETAILS,_quake.getDetails());
            values.put(EarthQuakeContentProvider.KEY_SUMMARY,_quake.toString());

            double lat = _quake.getLocation().getLatitude();
            double lng = _quake.getLocation().getLongitude();

            values.put(EarthQuakeContentProvider.KEY_LOCATION_LAT,lat);
            values.put(EarthQuakeContentProvider.KEY_LOCATION_LNG,lng);

            values.put(EarthQuakeContentProvider.KEY_LINK,_quake.getLink());
            values.put(EarthQuakeContentProvider.KEY_MAGNITUDE,_quake.getMagnitude());

            cr.insert(EarthQuakeContentProvider.CONTENT_URI,values);

        }
        query.close();
    }

    public void refreshEarthQuakes(){

        URL url;

        try{
            String quakesFeed = getString(R.string.earth_Quake_Feed);
            url = new URL(quakesFeed);

            URLConnection connection;
            connection = url.openConnection();

            HttpURLConnection httpURLConnection = (HttpURLConnection)connection;
            int responseCode = httpURLConnection.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream in = httpURLConnection.getInputStream();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                Document dom = db.parse(in);
                Element docEle = dom.getDocumentElement();

                //Получаем список всех записей о землетрясениях

                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                Date qdate = new GregorianCalendar(0,0,0).getTime();

                NodeList nl = docEle.getElementsByTagName("entry");
                if (nl != null && nl.getLength()>0){


                    for (int i = 0; i < nl.getLength(); i++) {
//                        Log.w(TAG, "i =" + i + "Length =" + nl.getLength());

                        Element entry = (Element) nl.item(i);
                        Element title = (Element)entry.getElementsByTagName("title").item(0);
                        Element q =(Element)entry.getElementsByTagName("georss:point").item(0);
                        Element when = (Element)entry.getElementsByTagName("updated").item(0);
                        Element link = (Element)entry.getElementsByTagName("link").item(0);

                        String details = title.getFirstChild().getNodeValue();
                        String hostName = "http://earthquake.usgs.gov";
                        String linkString = hostName+link.getAttribute("href");
                        String point;
                        if (q!=null) {
                            point = q.getFirstChild().getNodeValue();
                        } else {
                            point = "12.4670 -88.2750";
                        }
                        String dt = when.getFirstChild().getNodeValue();

                        try {
                            qdate =  (Date) formatter.parse(dt);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Log.d(TAG,"Date Parsing Exception");
                        }

                        String[] location = point.split(" ");
                        Location l = new Location("dummyGPS");
                        l.setLatitude(Double.parseDouble(location[0]));
                        l.setLongitude(Double.parseDouble(location[1]));

                        String magnitudeString = details.split(" ")[1];
                        double magnitude;

                        int end = magnitudeString.length()-1;
                        try{
                            magnitude = Double.parseDouble(magnitudeString.substring(0,end));
                            details = details.split(",")[1].trim();
                        } catch (Exception e){
                            magnitude = 0;
                        }

                        final Quake quake = new Quake(qdate,details,l,magnitude,linkString);

                        addNewQuake(quake);
                    }
                }
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG,"MalformedURLException");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"IOException");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            Log.d(TAG,"ParserConfigurationException");
        } catch (SAXException e) {
            e.printStackTrace();
            Log.d(TAG,"SAXException");
        }
        finally {
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
            //  Получите  Общие  настройки.
            Context context  =  getApplicationContext();
            SharedPreferences prefs  =  PreferenceManager.getDefaultSharedPreferences(context);
            int  updateFreq  =  Integer.parseInt(prefs.getString(PREF_UPDATE_FREQ, "60"));
            boolean  autoUpdateChecked  =  prefs.getBoolean(PREF_AUTO_UPDATE,  false);

            if  (autoUpdateChecked)  {

                int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
                long timeToRefresh = SystemClock.elapsedRealtime()+updateFreq*60*1000;

                alarmManager.setInexactRepeating(alarmType,timeToRefresh,updateFreq*60*1000,alarmIntent);
            }
            else  {

                alarmManager.cancel(alarmIntent);

                Thread  t  =  new  Thread(new  Runnable() 	{
                    public  void  run()  {
                        refreshEarthQuakes();
                    }
                });
                t.start();
            }
            return  Service.START_NOT_STICKY;
        };

    private TimerTask doRefresh  =  new  TimerTask()  {
        public  void  run(){
            refreshEarthQuakes();
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        String alarmAction = EarthQuakeAlarmReciever.ACTION_REFRESH_EARTHQUAKE_ALARM;

        Intent intentToFire = new Intent(alarmAction);

        alarmIntent = PendingIntent.getBroadcast(this,0,intentToFire,0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
