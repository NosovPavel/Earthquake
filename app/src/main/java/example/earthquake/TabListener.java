package example.earthquake;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

/**
 * Created by nosovpavel on 26/11/14.
 */
public class TabListener<T extends Fragment> implements ActionBar.TabListener {

    public Fragment fragment;
    private Activity activity;
    private Class<T> fragmentClass;
    private int fragmentContainer;

    public TabListener(Activity activity, int fragmentContainer,Class<T> fragmentClass){
        this.activity = activity;
        this.fragmentContainer= fragmentContainer;
        this.fragmentClass = fragmentClass;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        if(fragment == null){
            String fragmentName = fragmentClass.getName();
            fragment = Fragment.instantiate(activity,fragmentName);
            ft.add(fragmentContainer,fragment,fragmentName);
        } else {
            ft.attach(fragment);
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (fragment != null){
            ft.detach(fragment);
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (fragment != null){
            ft.attach(fragment);
        }
    }
}
