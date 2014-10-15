package example.earthquake;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;


public class main extends ActionBarActivity {

    private ProgressDialog progressBar;
    static final private int MENU_PREFERENCE = Menu.FIRST+1;
    static final private int MENU_UPDATE = Menu.FIRST+2;
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

//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main, menu);
//        return true;

//        getMenuInflater().inflate(R.menu.main, menu);
//        super.onCreateOptionsMenu(menu);

        menu.add(0,1,0, R.string.menu_preferences);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        boolean value = false;

        switch (id){
            case (R.id.action_settings):
            {
                value = true;
            }
            break;

            case 1:{
                Intent i = new Intent(this,preferences_Activity.class);
                startActivityForResult(i,1);
                value = true;
            }
            break;
        }

        return value;
    }

    public void updateFromPreferences(){
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int minMagIndex = prefs.getInt(preferences_Activity.PREF_MIN_MAG_INDEX,0);
        if(minMagIndex<0){
            minMagIndex = 0;
        }

        int freqIndex = prefs.getInt(preferences_Activity.PREF_UPDATE_FREQ_INDEX,0);
        if(freqIndex<0){
            freqIndex = 0;
        }

        autoUpdateChecked = prefs.getBoolean(preferences_Activity.PREF_AUTO_UPDATE,false);

        Resources resources = getResources();

        String[] minMagValues = resources.getStringArray(R.array.magnitude);
        String[] freqValues = resources.getStringArray(R.array.update_freq_values);

        minimumMagnitude = Integer.valueOf(minMagValues[minMagIndex]);
        updateFreq = Integer.valueOf(freqValues[freqIndex]);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SHOW_PREFERENCES){
            if(resultCode == Activity.RESULT_OK){
                updateFromPreferences();
                FragmentManager fm = getFragmentManager();
                final EarthQuakeListFragment earthQuakeListFragment = (EarthQuakeListFragment) fm.findFragmentById(R.id.EarthQuakeListFragment);

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        earthQuakeListFragment.refreshEarthQuakes();
                    }
                });
                t.start();
            }
        }
    }
}
