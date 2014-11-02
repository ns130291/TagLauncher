package de.nsvb.taglauncher;

import android.app.ActionBar;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.nsvb.taglauncher.util.Log;

/**
 * Created by ns130291 on 23.05.13.
 */
public class FragmentSettings extends PreferenceFragment {

    private Toolbar mActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View settings = super.onCreateView(inflater, container, savedInstanceState);

        LinearLayout toolbarContainer = (LinearLayout) inflater.inflate(R.layout.fragment_preference, container, false);
        toolbarContainer.addView(settings);

        mActionBar = (Toolbar) toolbarContainer.findViewById(R.id.action_bar_toolbar);
        ((ActionBarActivity) getActivity()).setSupportActionBar(mActionBar);

        return toolbarContainer;
    }

    @Override
    public void onResume() {
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
