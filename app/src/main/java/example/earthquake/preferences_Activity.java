package example.earthquake;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import java.util.prefs.Preferences;

/**
 * Created by nosovpavel on 14/10/14.
 */
public class preferences_Activity extends Activity {

    public static final String USER_PREFERENCE = "USER_PREFERENCE";
    public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
    public static final String PREF_MIN_MAG_INDEX = "PREF_MIN_MAG_INDEX";
    public static final String PREF_UPDATE_FREQ_INDEX = "PREF_UPDATE_FREQ_INDEX";

    SharedPreferences prefs;

    CheckBox autoUpdate;
    Spinner updateFreqSpinner;
    Spinner magnitudeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);

        updateFreqSpinner = (Spinner) findViewById(R.id.spinner_update_freq);
        magnitudeSpinner = (Spinner) findViewById(R.id.spinner_quake_mag);

        autoUpdate = (CheckBox) findViewById(R.id.checkbox_auto_update);

        populateSpinners();

        Context context = getApplicationContext();
        prefs= PreferenceManager.getDefaultSharedPreferences(context);

        updateUIFromPreferences();

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePreferences();
                preferences_Activity.this.setResult(RESULT_OK);
                finish();
            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferences_Activity.this.setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void savePreferences() {

        int updateIndex = updateFreqSpinner.getSelectedItemPosition();
        int minMagIndex = magnitudeSpinner.getSelectedItemPosition();
        boolean autoUpdChecked = autoUpdate.isChecked();

        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(PREF_AUTO_UPDATE,autoUpdChecked);
        editor.putInt(PREF_UPDATE_FREQ_INDEX,updateIndex);
        editor.putInt(PREF_MIN_MAG_INDEX,minMagIndex);

        editor.commit();
    }


    private void updateUIFromPreferences() {
        boolean autoUpdCheck = prefs.getBoolean(PREF_AUTO_UPDATE,false);
        int updateFreqIndex = prefs.getInt(PREF_UPDATE_FREQ_INDEX,2);
        int minMagIndex = prefs.getInt(PREF_MIN_MAG_INDEX,0);

        updateFreqSpinner.setSelection(updateFreqIndex);
        magnitudeSpinner.setSelection(minMagIndex);

        autoUpdate.setChecked(autoUpdCheck);
    }

    private void populateSpinners() {
        ArrayAdapter<CharSequence> fAdapter;
        fAdapter = ArrayAdapter.createFromResource(this,R.array.update_freq_options,android.R.layout.simple_spinner_item);

        int spinner_dd_item = android.R.layout.simple_spinner_dropdown_item;
        fAdapter.setDropDownViewResource(spinner_dd_item);

        updateFreqSpinner.setAdapter(fAdapter);


        ArrayAdapter<CharSequence> mAdapter;
        mAdapter = ArrayAdapter.createFromResource(this,R.array.magnitude_options,android.R.layout.simple_spinner_item);

//        int spinner_dd_item = android.R.layout.simple_spinner_dropdown_item;
        mAdapter.setDropDownViewResource(spinner_dd_item);

        magnitudeSpinner.setAdapter(mAdapter);


    }
}
