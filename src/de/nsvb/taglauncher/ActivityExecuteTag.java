package de.nsvb.taglauncher;

import java.math.BigInteger;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;
import de.nsvb.taglauncher.action.ActionBundle;
import de.nsvb.taglauncher.util.Log;
import de.nsvb.taglauncher.R;

public class ActivityExecuteTag extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		Intent intent = getIntent();
		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Ndef ndefTag = Ndef.get(tag);
		
		NdefMessage ndefMessage = ndefTag.getCachedNdefMessage();
		
		if(ndefMessage != null){
			NdefRecord[] ndefRecords =  ndefMessage.getRecords();
			if(ndefRecords.length > 0){
				byte[] message =  ndefRecords[0].getPayload();
				
				Log.d(toHex(message));
				
				if(message.length > 0){
					ActionBundle actionBundle = new ActionBundle(getApplicationContext());
					actionBundle.init(message);				
					if(actionBundle.execute()){
						Toast.makeText(getApplicationContext(), R.string.tag_execute_success, Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(getApplicationContext(), R.string.tag_execute_failure, Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		finish();
		
	}
	
	public static String toHex(byte[] bytes) {
		if(bytes.length == 0){
			return "leer";
		}
	    BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "X", bi);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_execute_tag, menu);
		return true;
	}

}
