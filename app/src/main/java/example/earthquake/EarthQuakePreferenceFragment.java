package example.earthquake;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by nosovpavel on 15/10/14.
 */
public class EarthQuakePreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.userpreferences);
    }
}
