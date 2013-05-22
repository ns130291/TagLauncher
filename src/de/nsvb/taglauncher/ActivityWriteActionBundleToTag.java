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
import android.content.ActivityNotFoundException;
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
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityWriteActionBundleToTag extends Activity implements DialogFragmentNfcDisabled.NfcDisabledDialogListener {

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
        mButtonCancel = (Button) findViewById(R.id.cancel);

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
        } else {
            mText.setText(R.string.waiting_for_tag);
        }

        mButtonCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                canceledWriting();
            }
        });

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

    @Override
    public void onNfcDisabledDialogPositiveClick() {
        try {
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    public void onNfcDisabledDialogNegativeClick() {
        finish();
        Toast.makeText(getApplicationContext(), getString(R.string.nfc_disabled),
                Toast.LENGTH_LONG).show();
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
                Toast.makeText(getApplicationContext(), getString(R.string.writing_completed),
                        Toast.LENGTH_SHORT).show();
                //Log.d("result true");
                finish();
            } else {
                mText.setText(mText.getText() + "\n\n"
                        + getString(R.string.waiting_for_tag));
                //Log.d("result false");
            }
            super.onPostExecute(result);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected Boolean doInBackground(NdefMessage... message) {

            boolean result = false;

            if (mTag == null || message.length < 1) {
                publishProgress(getString(R.string.no_tag_found));
                return result;
            }

            Ndef tag = Ndef.get(mTag);

            if (tag == null) {
                NdefFormatable ndefFormatable = NdefFormatable.get(mTag);
                if (ndefFormatable == null) {
                    publishProgress(getString(R.string.tag_not_compatible));
                    return result;
                }

                try {
                    ndefFormatable.connect();
                    if (ndefFormatable.isConnected()) {
                        ndefFormatable.format(null);
                        publishProgress(getString(R.string.tag_formatted_please_rescan));
                    } else {
                        publishProgress(getString(R.string.lost_connection_please_rescan));
                    }
                } catch (TagLostException e) {
                    publishProgress(getString(R.string.lost_connection_please_rescan));
                    e.printStackTrace();
                } catch (IOException e) {
                    publishProgress(getString(R.string.write_error_please_retry));
                    e.printStackTrace();
                } catch (FormatException e) {
                    publishProgress(getString(R.string.format_exception));
                    e.printStackTrace();
                } catch (Exception e) {
                    publishProgress(getString(R.string.generic_error));
                    e.printStackTrace();
                }
                return result;
            }

            try {
                tag.connect();
            } catch (IOException e) {
                publishProgress(getString(R.string.connection_not_possible_please_retry));
                return result;
            }

            if (!tag.isWritable()) {
                publishProgress(getString(R.string.tag_write_protected));
                return result;
            }

            int size;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                size = message[0].getByteArrayLength();
            } else {
                size = message[0].toByteArray().length;
            }

            if (size > tag.getMaxSize()) {
                Log.d("Aktionsbündel zu groß: max Größe'" + tag.getMaxSize()
                        + "' - tatsächliche Größe'"
                        + message[0].getByteArrayLength() + "'");
                publishProgress(String.format(
                        getString(R.string.tag_too_small), tag.getMaxSize()));
                return result;
            }

            if (!tag.isConnected()) {
                publishProgress(getString(R.string.lost_connection_please_rescan));
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
                publishProgress(getString(R.string.lost_connection_please_rescan));
                e.printStackTrace();
            } catch (FormatException e) {
                result = false;
                publishProgress(getString(R.string.format_exception));
                e.printStackTrace();
            } catch (IOException e) {
                result = false;
                publishProgress(getString(R.string.write_error_please_retry));
                e.printStackTrace();
            } catch (Exception e) {
                result = false;
                publishProgress(getString(R.string.generic_error));
                e.printStackTrace();
            }
            return result;
        }
    }
}
