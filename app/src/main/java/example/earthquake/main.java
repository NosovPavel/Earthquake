package example.earthquake;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
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
import android.view.View;
import android.view.MenuInflater;
import android.widget.SearchView;


public class main extends Activity {

    static final private int SHOW_PREFERENCES = 1;

    public int minimumMagnitude = 0;
    public boolean autoUpdateChecked = false;
    public int updateFreq = 0;

    TabListener<EarthQuakeListFragment> listTabListener;
    TabListener<earthQuakeMapFragment> mapTabListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateFromPreferences();



        ActionBar actionBar = getActionBar();

        View fragmentContainer = findViewById(R.id.EarthQuakeFragmentContainer);

        boolean tabletLayout = fragmentContainer == null;

        if(!tabletLayout){
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setDisplayShowTitleEnabled(false);

            ActionBar.Tab listTab = actionBar.newTab();

            listTabListener = new TabListener<EarthQuakeListFragment>(this,R.id.EarthQuakeFragmentContainer,EarthQuakeListFragment.class);

            listTab.setText("List of earthquakes").setTabListener(listTabListener);

            actionBar.addTab(listTab);

            //map

            ActionBar.Tab mapTab = actionBar.newTab();
            mapTabListener = new TabListener<earthQuakeMapFragment>(this,R.id.EarthQuakeFragmentContainer,earthQuakeMapFragment.class);

            mapTab.setText("Map of earthquakes").setTabListener(mapTabListener);

            actionBar.addTab(mapTab);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main,menu);

        // Use the Search Manager to find the SearchableInfo related to this
        // Activity.
        SearchManager searchManager =
                (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo =
                searchManager.getSearchableInfo(getComponentName());

        // Bind the Activity's SearchableInfo to the Search View
        SearchView searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchableInfo);


        // Inflate the menu; this adds items to the action bar if it is present.
//        menu.add(0,1,0, R.string.menu_preferences);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case (R.id.menu_refresh):{
                startService(new Intent(this, EarthQuakeService.class));
                return true;
            }
            case (R.id.menu_settings):{
                Class c = Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB? PreferenceActivity.class:FragmentPreferences.class;
                Intent i = new Intent(this,c);
                startActivityForResult(i,SHOW_PREFERENCES);
                return true;
            }

            default:
                return false;
        }

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
    }

    private static String ACTION_BAR_INDEX = "ACTION_BAR_INDEX";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        View fragmentContainer = findViewById(R.id.EarthQuakeFragmentContainer);
        boolean tabletLayout = fragmentContainer == null;

        if (!tabletLayout) {
            // Save the current Action Bar tab selection
            int actionBarIndex = getActionBar().getSelectedTab().getPosition();
            SharedPreferences.Editor editor = getPreferences(Activity.MODE_PRIVATE).edit();
            editor.putInt(ACTION_BAR_INDEX, actionBarIndex);
            editor.apply();

            // Detach each of the Fragments
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (mapTabListener.fragment != null)
                ft.detach(mapTabListener.fragment);
            if (listTabListener.fragment != null)
                ft.detach(listTabListener.fragment);
            ft.commit();
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        View fragmentContainer = findViewById(R.id.EarthQuakeFragmentContainer);
        boolean tabletLayout = fragmentContainer == null;

        if (!tabletLayout) {
            // Find the recreated Fragments and assign them to their associated Tab Listeners.
            listTabListener.fragment =
                    getFragmentManager().findFragmentByTag(EarthQuakeListFragment.class.getName());
            mapTabListener.fragment =
                    getFragmentManager().findFragmentByTag(earthQuakeMapFragment.class.getName());

            // Restore the previous Action Bar tab selection.
            SharedPreferences sp = getPreferences(Activity.MODE_PRIVATE);
            int actionBarIndex = sp.getInt(ACTION_BAR_INDEX, 0);
            getActionBar().setSelectedNavigationItem(actionBarIndex);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        View fragmentContainer = findViewById(R.id.EarthQuakeFragmentContainer);
        boolean tabletLayout = fragmentContainer == null;

        if (!tabletLayout) {
            SharedPreferences sp = getPreferences(Activity.MODE_PRIVATE);
            int actionBarIndex = sp.getInt(ACTION_BAR_INDEX, 0);
            getActionBar().setSelectedNavigationItem(actionBarIndex);
        }
    }
}
