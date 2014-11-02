package de.nsvb.taglauncher;

import java.util.ArrayList;

import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

import android.view.MenuItem;
import de.nsvb.taglauncher.FragmentActionBundleDetails.FragmentActionBundleListener;
import de.nsvb.taglauncher.FragmentActionBundleList.OnActionBundleSelectedListener;
import de.nsvb.taglauncher.FragmentActionExtended.OnApplyListener;
import de.nsvb.taglauncher.action.ActionBundle;
import de.nsvb.taglauncher.db.Store;
import de.nsvb.taglauncher.util.Log;

public class ActivityMain extends ActionBarActivity implements
        OnActionBundleSelectedListener, FragmentActionBundleListener,
        OnApplyListener, DialogFragmentFirstRun.FirstRunDialogListener, DialogFragmentNfcDisabled.NfcDisabledDialogListener {

    private static final String PREFS_NAME = "prefs";
    public static ArrayList<ActionBundle> mActionBundles = new ArrayList<ActionBundle>();
    private static final String ACTION_BUNDLE_LIST = "abl";
    public static boolean noNFC;

    @Override
    public void onBackPressed() { // TODO evaluate why I need this workaround since using Toolbars as ActionBars
        if(getFragmentManager().getBackStackEntryCount()>0){
            getFragmentManager().popBackStackImmediate();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        // TODO set to false
        //Log.DEBUG = true;

        // Background von Activity entfernen um Overdraw zu vermeiden --> macht
        // animation kaputt
        // getWindow().setBackgroundDrawable(null);

        Store.setContext(getApplicationContext());

        if (savedInstanceState == null || mActionBundles.size() == 0) {
            loadFromDB();
            //Log.d("##-onCreate-## savedInstanceState == null || mActionBundles.size() == 0 " + mActionBundles.getClass().getName() + '@' + Integer.toHexString(mActionBundles.hashCode()) + " ��");
        }

        super.onCreate(savedInstanceState); // at this position because app crashes after being reopened after a long time, db has to be loaded first

        long start = System.currentTimeMillis();

        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if (nfcAdapter == null) {
            noNFC = true;
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            boolean firstRun = settings.getBoolean("first_run", true);
            if (firstRun) {
                DialogFragmentFirstRun dialogFragmentFirstRun = new DialogFragmentFirstRun();
                dialogFragmentFirstRun.show(getFragmentManager(), "FirstRunDialog");
            }
        }else{
            noNFC = false;
            if(!nfcAdapter.isEnabled()){
                DialogFragmentNfcDisabled dialogFragmentNfcDisabled = new DialogFragmentNfcDisabled();
                dialogFragmentNfcDisabled.show(getFragmentManager(), "NfcDisabledDialog");
            }
        }

        Log.d("** Ladezeit der Einstellungen " + (System.currentTimeMillis() - start) + " ms");

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            FragmentActionBundleList fragmentActionList = new FragmentActionBundleList();
            fragmentActionList.setArguments(getIntent().getExtras());
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, fragmentActionList,
                            ACTION_BUNDLE_LIST).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mActionBundles.size() == 0) {
            loadFromDB();
            //Log.d("##-onResume-## mActionBundles.size() == 0 " + mActionBundles.getClass().getName() + '@' + Integer.toHexString(mActionBundles.hashCode()) + " ��");
        }
    }

    private void loadFromDB() {
        //long start = System.currentTimeMillis();
        //long init = 0;

        SQLiteDatabase db = Store.instance().getReadableDatabase();
        Cursor cs;
        if (db != null) {
            cs = db.query(Store.DB_AB_TABLENAME, new String[]{"id",
                    Store.DB_AB_NAME, Store.DB_AB_MESSAGE}, null, null, null,
                    null, "id");

            cs.moveToFirst();
            while (!cs.isAfterLast()) {
                ActionBundle ab = new ActionBundle(getApplicationContext());
                ab.setName(cs.getString(cs.getColumnIndex(Store.DB_AB_NAME)));
                //long i = System.currentTimeMillis();
                ab.init(cs.getBlob(cs.getColumnIndex(Store.DB_AB_MESSAGE)));
                //init += System.currentTimeMillis()-i;
                ab.setId((int) cs.getLong(cs.getColumnIndex("id")));
                mActionBundles.add(ab);
                cs.moveToNext();
            }
        }

        //Log.d(">-#-< Laden aus DB "+(System.currentTimeMillis()-start)+" ms, init-Dauer "+init+" ms");
        //Log.d("## " + mActionBundles.getClass().getName() + '@' + Integer.toHexString(mActionBundles.hashCode()) + " ��");
    }

    private void loadFromDB(int id) {
        SQLiteDatabase db = Store.instance().getReadableDatabase();
        Cursor cs;
        if (db != null) {
            cs = db.query(Store.DB_AB_TABLENAME, new String[]{"id",
                    Store.DB_AB_NAME, Store.DB_AB_MESSAGE}, "id = " + id, null, null,
                    null, "id");

            cs.moveToFirst();
            while (!cs.isAfterLast()) {
                ActionBundle ab = new ActionBundle(getApplicationContext());
                ab.setName(cs.getString(cs.getColumnIndex(Store.DB_AB_NAME)));
                ab.init(cs.getBlob(cs.getColumnIndex(Store.DB_AB_MESSAGE)));
                ab.setId((int) cs.getLong(cs.getColumnIndex("id")));
                mActionBundles.add(ab);
                cs.moveToNext();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_settings:
                showSettings();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettings() {
        FragmentSettings fragmentSettings = new FragmentSettings();

        FragmentTransaction fragmentTransaction = getFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.fragment_open_enter, 0, 0, R.animator.fragment_close_exit);
        fragmentTransaction.replace(R.id.fragment_container,
                fragmentSettings);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onActionBundleSelected(int position) {
        FragmentActionBundleDetails fragmentActionDetails = new FragmentActionBundleDetails();

        Bundle args = new Bundle();
        args.putInt(FragmentActionBundleDetails.ARG_POSITION, position);
        fragmentActionDetails.setArguments(args);

        FragmentTransaction fragmentTransaction = getFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.fragment_open_enter, 0, 0, R.animator.fragment_close_exit);
        fragmentTransaction.replace(R.id.fragment_container,
                fragmentActionDetails);
        fragmentTransaction.addToBackStack("fragmentActionDetails");
        fragmentTransaction.commit();
    }

    @Override
    public void onActionBundleImported(int id) {
        if (id != -1 && id != 0) {
            loadFromDB(id);
        }
    }

    @Override
    public void onDuplicate(int position) {
        getFragmentManager().popBackStackImmediate();
        FragmentActionBundleList frag = (FragmentActionBundleList) getFragmentManager()
                .findFragmentByTag(ACTION_BUNDLE_LIST);
        frag.scrollToBottom();
    }

    @Override
    public void onActionSelected(int abPos, int actionPos) {
        FragmentActionExtended fragmentActionExtended = new FragmentActionExtended();

        Bundle args = new Bundle();
        args.putInt(FragmentActionExtended.AB_ID, abPos);
        args.putInt(FragmentActionExtended.ACTION_ID, actionPos);
        args.putBoolean(FragmentActionExtended.OBJECT_MODE, true);
        fragmentActionExtended.setArguments(args);

        FragmentTransaction fragmentTransaction = getFragmentManager()
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.fragment_open_enter, 0, 0, R.animator.fragment_close_exit);
        fragmentTransaction.replace(R.id.fragment_container,
                fragmentActionExtended);
        fragmentTransaction.addToBackStack("fragmentActionExtended");
        fragmentTransaction.commit();
    }

    @Override
    public void onApply() {
        getFragmentManager().popBackStackImmediate();
    }

    @Override
    public void onFirstRunDialogClick() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("first_run", false);
        editor.commit();
    }

    @Override
    public void onNfcDisabledDialogPositiveClick() {
        try {
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNfcDisabledDialogNegativeClick() {

    }
}
