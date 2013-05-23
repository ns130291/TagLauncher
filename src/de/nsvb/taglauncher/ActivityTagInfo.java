package de.nsvb.taglauncher;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.*;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import de.nsvb.taglauncher.action.Action;
import de.nsvb.taglauncher.action.ActionBundle;
import de.nsvb.taglauncher.util.Log;

/**
 * Created by ns130291 on 20.05.13.
 */
public class ActivityTagInfo extends Activity implements DialogFragmentNfcDisabled.NfcDisabledDialogListener {

    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;
    private ActionBundle mAB;

    public static String ACTION_BUNDLE_ID = "ActionBundleId";

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_tag_info);

        // TODO test availability, fallback if not available
        mAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mAdapter == null) {
            finish();
            Toast.makeText(getApplicationContext(), getString(R.string.no_nfc_support),
                    Toast.LENGTH_LONG).show();
        }

        if (!mAdapter.isEnabled()) {
            DialogFragmentNfcDisabled dialogFragmentNfcDisabled = new DialogFragmentNfcDisabled();
            dialogFragmentNfcDisabled.setCancelable(false);
            dialogFragmentNfcDisabled.show(getFragmentManager(), "NfcDisabledDialog");
        }

        // Create a generic PendingIntent that will be delivered to this
        // activity.
        // The NFC stack will fill in the intent with the details of the
        // discovered tag before
        // delivering to
        // this activity.
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Setup an intent filter for all MIME based dispatches
        IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        mFilters = new IntentFilter[]{tech,};

        // Setup a tech list for all Nfc tags
        mTechLists = new String[][]{new String[]{NfcA.class.getName()},
                new String[]{NfcB.class.getName()},
                new String[]{NfcF.class.getName()},
                new String[]{NfcV.class.getName()},
                new String[]{IsoDep.class.getName()},
                new String[]{MifareClassic.class.getName()},
                new String[]{NdefFormatable.class.getName()},
                new String[]{Ndef.class.getName()}};
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_tag_info, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null)
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
                    mTechLists);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d("Discovered tag with intent: " + intent);

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            Ndef ndefTag = Ndef.get(tag);
            if (ndefTag != null) {
                /*if (mAdapter != null) {
                    mAdapter.disableForegroundDispatch(this);
                }*/
                NdefMessage message = ndefTag.getCachedNdefMessage();
                decodeMessage(message, ndefTag);
            }
        }
    }

    private void decodeMessage(NdefMessage message, Ndef tag) {
        ViewGroup parent = (ViewGroup) findViewById(R.id.parent);
        parent.removeAllViews();
        View v = View.inflate(getApplicationContext(), R.layout.view_tag_info, null);
        TextView heading = (TextView) v.findViewById(R.id.heading);
        TextView size = (TextView) v.findViewById(R.id.size);
        LinearLayout container = (LinearLayout) v.findViewById(R.id.container);

        heading.setText(tag.getType());
        if (message != null) {
            size.setText(String.format(
                    getResources().getString(R.string.s_byte_2),
                    message.getByteArrayLength(), tag.getMaxSize())
            );
            NdefRecord[] ndefRecords = message.getRecords();
            if (ndefRecords.length > 0) {
                NdefRecord record = ndefRecords[0];
                if ((new String(record.getType())).equals("nsvb.de:taglauncher")) {
                    if (record.getTnf() == 0x04) {
                        byte[] m = record.getPayload();
                        if (m.length > 0) {
                            try {
                                mAB = new ActionBundle(getApplicationContext());
                                mAB.init(m);
                                for (Action action : mAB) {
                                    View actionView = View.inflate(getApplicationContext(),
                                            R.layout.list_item_action, null);

                                    ImageView actionImg = (ImageView) actionView
                                            .findViewById(R.id.actionImg);
                                    actionImg.setImageResource(action.getImage());

                                    TextView actionText = (TextView) actionView
                                            .findViewById(R.id.actionText);
                                    actionText.setText(action.getDescription(getApplicationContext()));

                                    container.addView(actionView);
                                }
                                invalidateOptionsMenu();
                            } catch (Exception e) {
                                e.printStackTrace();
                                mAB = null;
                            }
                        }
                    }
                }
            }
        } else {
            size.setText(String.format(
                    getResources().getString(R.string.s_byte_2),
                    0, tag.getMaxSize())
            );

        }

        parent.addView(v);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mAB != null) {
            menu.findItem(R.id.importAB).setEnabled(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.importAB:
                importActionBundle();
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void importActionBundle() {
        if (mAB != null) {
            mAB.store();
            Intent returnIntent = new Intent();
            returnIntent.putExtra(ACTION_BUNDLE_ID, mAB.getId());
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }else{
            finish();
        }
    }

    @Override
    public void onNfcDisabledDialogPositiveClick() {

    }

    @Override
    public void onNfcDisabledDialogNegativeClick() {

    }
}
