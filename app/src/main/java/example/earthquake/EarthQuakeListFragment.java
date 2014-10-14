package example.earthquake;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.ArrayAdapter;

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
//import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by nosovpavel on 13/10/14.
 */

public class EarthQuakeListFragment extends ListFragment {

    ArrayAdapter<Quake> aa;
    ArrayList<Quake> earhquakes = new ArrayList<Quake>();

    private static final String TAG = "EARTHQUAKE";
    private Handler handler = new Handler();




    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int LayoutId = android.R.layout.simple_list_item_1;
        aa = new ArrayAdapter<Quake>(getActivity(), LayoutId,earhquakes);
        setListAdapter(aa);


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                refreshEarthQuakes();
            }
        });
        t.start();
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

                earhquakes.clear();

                //Получаем список всех записей о землетрясениях

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-'T'hh:mm:ss'Z'");
                Date qdate = (Date) new GregorianCalendar(0,0,0).getTime();

                NodeList nl = docEle.getElementsByTagName("entry");
                if (nl != null && nl.getLength()>0){
                    for (int i = 0; i < nl.getLength(); i++) {
                        Element entry = (Element) nl.item(i);
                        Element title = (Element)entry.getElementsByTagName("title").item(0);
                        Element q =(Element)entry.getElementsByTagName("georss:point").item(0);
                        Element when = (Element)entry.getElementsByTagName("updated").item(0);
                        Element link = (Element)entry.getElementsByTagName("link").item(0);

                        String details = title.getFirstChild().getNodeValue();
                        String hostName = "http://earthquake.usgs.gov";
                        String linkString = hostName+link.getAttribute("href");
                        String point;                        if (q!=null) {
                            point = q.getFirstChild().getNodeValue();
                        } else {
                            point = "12.4670 -88.2750";
                        }
                        String dt = when.getFirstChild().getNodeValue();

                        try {
                            qdate = (Date) sdf.parse(dt);
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

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                addNewQuake(quake);
                            };
                        });
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
    }

    private void addNewQuake(Quake _quake) {
        earhquakes.add(_quake);
        aa.notifyDataSetChanged();
    }

}
