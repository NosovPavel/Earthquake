package example.earthquake;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;


public class main extends ActionBarActivity {

    static final private int SHOW_PREFERENCES = 1;

    public int minimumMagnitude = 0;
    public boolean autoUpdateChecked = false;
    public int updateFreq = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateFromPreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.add(0,1,0, R.string.menu_preferences);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        super.onOptionsItemSelected(item);

                Class c = Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB? PreferenceActivity.class:FragmentPreferences.class;
                Intent i = new Intent(this,c);
                startActivityForResult(i,SHOW_PREFERENCES);
                return true;
    }

    public void updateFromPreferences(){
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        minimumMagnitude = Integer.parseInt(prefs.getString(preferences_Activity.PREF_MIN_MAG,"3"));
        updateFreq = Integer.parseInt(prefs.getString(preferences_Activity.PREF_UPDATE_FREQ, "60"));

        autoUpdateChecked = prefs.getBoolean(preferences_Activity.PREF_AUTO_UPDATE,false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SHOW_PREFERENCES){
            updateFromPreferences();
        }

        FragmentManager fm = getFragmentManager();
        final EarthQuakeListFragment elf= (EarthQuakeListFragment) fm.findFragmentById(R.id.EarthQuakeListFragment);

//                Thread t = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        elf.refreshEarthQuakes();
//                    }
//                });
//                t.start();
    }
}
