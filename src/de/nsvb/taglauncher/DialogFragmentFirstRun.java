package de.nsvb.taglauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by ns130291 on 17.05.13.
 */
public class DialogFragmentFirstRun extends DialogFragment {
    public interface FirstRunDialogListener {
        public void onFirstRunDialogClick();
    }

    private FirstRunDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the
    // NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the
            // host
            mListener = (FirstRunDialogListener) activity;
        } catch (ClassCastException e) {
            try {
                mListener = (FirstRunDialogListener) getTargetFragment();
            } catch (ClassCastException f) {
                // The fragment doesn't implement the interface, throw exception
                throw new ClassCastException(
                        activity.toString()
                                + " must implement NfcDisabledDialogListener (or its fragment)");
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(getResources().getString(R.string.first_run_dialog))
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onFirstRunDialogClick();
                    }
                });
        return builder.create();
    }
}
