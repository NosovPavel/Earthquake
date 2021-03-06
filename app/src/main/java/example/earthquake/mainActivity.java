package example.earthquake;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuInflater;
import android.widget.SearchView;


public class MainActivity extends Activity {

    static final private int SHOW_PREFERENCES = 1;

    public int minimumMagnitude = 0;
    public boolean autoUpdateChecked = false;
    public int updateFreq = 0;

    TabListener<EarthQuakeListFragment> listTabListener;
    TabListener<EarthQuakeMapFragment> mapTabListener;

    private static String ACTION_BAR_INDEX = "ACTION_BAR_INDEX";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getNewDataFromPreferences();

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
            mapTabListener = new TabListener<EarthQuakeMapFragment>(this,R.id.EarthQuakeFragmentContainer,EarthQuakeMapFragment.class);

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
        SearchView searchView = (SearchView)menu.findItem(R.id.menu_search_bar).getActionView();
        searchView.setSearchableInfo(searchableInfo);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        super.onOptionsItemSelected(item);

        boolean returnValue = false;

            if (item.getItemId() == R.id.menu_refresh){
                startService(new Intent(this, EarthQuakeUpdateService.class));
                returnValue =  true;
            }
            if (item.getItemId() == R.id.menu_settings){
                Class c = EarthQuakePreferenceActivity.class;
                Intent i = new Intent(this,c);
                startActivityForResult(i,SHOW_PREFERENCES);
                returnValue = true;
            }
            if (item.getItemId() == R.id.menu_search){
                Class c = EarthQuakeSearchResultsActivity.class;
                Intent i = new Intent(this,c);
                startActivity(i);
                returnValue = true;
            }
        return returnValue;
    }


    public void getNewDataFromPreferences(){
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        minimumMagnitude = Integer.parseInt(prefs.getString(EarthQuakeUpdateService.PREF_MIN_MAG,"1"));
        updateFreq = Integer.parseInt(prefs.getString(EarthQuakeUpdateService.PREF_UPDATE_FREQ, "60"));
        autoUpdateChecked = prefs.getBoolean(EarthQuakeUpdateService.PREF_AUTO_UPDATE,false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SHOW_PREFERENCES){
            getNewDataFromPreferences();
        }
    }


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
                    getFragmentManager().findFragmentByTag(EarthQuakeMapFragment.class.getName());

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
