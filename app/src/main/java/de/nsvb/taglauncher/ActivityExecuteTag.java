package de.nsvb.taglauncher;

import java.math.BigInteger;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import de.nsvb.taglauncher.action.Action;
import de.nsvb.taglauncher.action.ActionBundle;
import de.nsvb.taglauncher.ui.ActionListAdapter;
import de.nsvb.taglauncher.util.Log;
import de.nsvb.taglauncher.R;

public class ActivityExecuteTag extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndefTag = Ndef.get(tag);

        NdefMessage ndefMessage = ndefTag.getCachedNdefMessage();

        if (ndefMessage != null) {
            NdefRecord[] ndefRecords = ndefMessage.getRecords();
            if (ndefRecords.length > 0) {
                byte[] message = ndefRecords[0].getPayload();

                Log.d(toHex(message));

                if (message.length > 0) {
                    final ActionBundle actionBundle = new ActionBundle(getApplicationContext());
                    actionBundle.init(message);
                    if (sharedPref.getBoolean("show_executed_actions", false)) {
                        Log.d("## aktionen anzeigen");
                        setTheme(android.R.style.Theme_Holo_Light_Dialog);
                        setContentView(R.layout.activity_execute_tag);
                        setListAdapter(new ActionListAdapter(this,
                                R.layout.list_item_action_all, (ArrayList<Action>) actionBundle.getActionList()));

                        Button ok = (Button) findViewById(R.id.ok);
                        ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (actionBundle.execute()) {
                                    Toast.makeText(getApplicationContext(), R.string.tag_execute_success, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.tag_execute_failure, Toast.LENGTH_LONG).show();
                                }
                                finish();
                            }
                        });
                    }else{
                        if (actionBundle.execute()) {
                            Toast.makeText(getApplicationContext(), R.string.tag_execute_success, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.tag_execute_failure, Toast.LENGTH_LONG).show();
                        }
                        finish();
                    }
                }
            }
        }
    }

    private static String toHex(byte[] bytes) {
        if (bytes.length == 0) {
            return "leer";
        }
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

}
