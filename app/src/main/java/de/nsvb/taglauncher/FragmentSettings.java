package de.nsvb.taglauncher;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import de.nsvb.taglauncher.util.Log;

/**
 * Created by ns130291 on 23.05.13.
 */
public class FragmentSettings extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onResume() {
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                getFragmentManager().popBackStack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
