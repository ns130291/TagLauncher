/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package de.nsvb.taglauncher;

import java.io.IOException;

import de.nsvb.taglauncher.util.Log;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityWriteActionBundleToTag extends Activity {

	public static final String MESSAGE = "de.nsvb.taglauncher.message";

	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	private TextView mText;
	private Button mButtonCancel;
	private ScrollView mScrollView;
	private Tag mTag;
	private NdefMessage mMessage;

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.activity_write_message_to_tag);

		mScrollView = (ScrollView) findViewById(R.id.scrollView);

		mText = (TextView) findViewById(R.id.status);
		mText.setText(R.string.waiting_for_tag);

		mButtonCancel = (Button) findViewById(R.id.cancel);

		mButtonCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				canceledWriting();
			}
		});

		// TODO test availability, fallback if not available
		mAdapter = NfcAdapter.getDefaultAdapter(this);

		mMessage = getIntent().getParcelableExtra(MESSAGE);

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
		// try {
		// ndef.addDataType("*/*");
		/*
		 * } catch (MalformedMimeTypeException e) { throw new
		 * RuntimeException("fail", e); }
		 */
		mFilters = new IntentFilter[] { tech, };

		// Setup a tech list for all Nfc tags
		mTechLists = new String[][] { new String[] { NfcA.class.getName() },
				new String[] { NfcB.class.getName() },
				new String[] { NfcF.class.getName() },
				new String[] { NfcV.class.getName() },
				new String[] { IsoDep.class.getName() },
				new String[] { MifareClassic.class.getName() },
				new String[] { NdefFormatable.class.getName() },
				new String[] { Ndef.class.getName() } };
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mAdapter != null)
			mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
					mTechLists);
	}

	private void canceledWriting() {
		finish();
		Toast.makeText(getApplicationContext(), R.string.cancel_write,
				Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			canceledWriting();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onNewIntent(Intent intent) {
		Log.d("Discovered tag with intent: " + intent);

		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
				&& mMessage != null) {
			mTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			mText.setText(mText.getText() + "\n"
					+ getString(R.string.writing_to_tag));
			scrollToBottom();
			new WriteToTag().execute(mMessage);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mAdapter != null)
			mAdapter.disableForegroundDispatch(this);
	}

	private void scrollToBottom() {
		mScrollView.post(new Runnable() {
			public void run() {
				mScrollView.smoothScrollTo(0, mText.getBottom());
			}
		});
	}

	private class WriteToTag extends AsyncTask<NdefMessage, String, Boolean> {

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);

			for (String value : values) {
				mText.setText(mText.getText() + "\n" + value);
				scrollToBottom();
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {

			if (result) {
				Toast.makeText(getApplicationContext(), "Tag beschrieben",
						Toast.LENGTH_SHORT).show();
				Log.d("result true");
				finish();
			} else {
				mText.setText(mText.getText() + "\n\n"
						+ getString(R.string.waiting_for_tag));
				Log.d("result false");
			}
			super.onPostExecute(result);
		}

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		@Override
		protected Boolean doInBackground(NdefMessage... message) {

			boolean result = false;

			if (mTag == null || message.length < 1) {
				publishProgress("Es wurde kein Tag erkannt");
				return result;
			}

			Ndef tag = Ndef.get(mTag);

			if (tag == null) {
				NdefFormatable ndefFormatable = NdefFormatable.get(mTag);
				if (ndefFormatable == null) {
					publishProgress("Tag ist mit dem Ger�t nicht kompatibel");
					return result;
				}

				try {
					ndefFormatable.connect();
					if (ndefFormatable.isConnected()) {
						ndefFormatable.format(null);
						publishProgress("Tag wurde formatiert. Bitte erneut an das Ger�t halten");
					} else {
						publishProgress("Verbindung zum Tag verloren. Bitte erneut an das Ger�t halten");
					}
				} catch (TagLostException e) {
					publishProgress("Verbindung zum Tag verloren. Bitte erneut an das Ger�t halten");
					e.printStackTrace();
				} catch (IOException e) {
					publishProgress("Fehler beim Beschreiben. Bitte erneut probieren");
					e.printStackTrace();
				} catch (FormatException e) {
					publishProgress("FormatException Exception: Allgemeiner Fehler");
					e.printStackTrace();
				} catch (Exception e) {
					publishProgress("Allgemeiner Fehler");
					e.printStackTrace();
				}
				return result;
			}

			try {
				tag.connect();
			} catch (IOException e) {
				publishProgress("Verbindung zum Tag konnte nicht aufgebaut werden");
				return result;
			}

			if (!tag.isWritable()) {
				publishProgress("Tag ist schreibgesch�tzt und kann nicht beschreiben werden");
				return result;
			}

			int size;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
				size = message[0].getByteArrayLength();
			} else {
				size = message[0].toByteArray().length;
			}

			if (size > tag.getMaxSize()) {
				Log.d("Aktionsb�ndel zu gro�: max Gr��e'" + tag.getMaxSize()
						+ "' - tats�chliche Gr��e'"
						+ message[0].getByteArrayLength() + "'");
				publishProgress(String.format(
						getString(R.string.tag_too_small), tag.getMaxSize()));
				return result;
			}

			if (!tag.isConnected()) {
				publishProgress("Verbindung zum Tag verloren. Bitte erneut an das Ger�t halten");
				return result;
			}

			try {
				long now = System.currentTimeMillis();
				tag.writeNdefMessage(message[0]);
				Log.d("Write time: " + (System.currentTimeMillis() - now)
						+ " ms");
				result = true;
			} catch (TagLostException e) {
				result = false;
				publishProgress("Verbindung zum Tag verloren. Bitte erneut an das Ger�t halten");
				e.printStackTrace();
			} catch (FormatException e) {
				result = false;
				publishProgress("FormatException Exception: Allgemeiner Fehler");
				e.printStackTrace();
			} catch (IOException e) {
				result = false;
				publishProgress("Fehler beim Beschreiben. Bitte erneut probieren");
				e.printStackTrace();
			} catch (Exception e) {
				result = false;
				publishProgress("Allgemeiner Fehler");
				e.printStackTrace();
			}
			return result;
		}
	}
}