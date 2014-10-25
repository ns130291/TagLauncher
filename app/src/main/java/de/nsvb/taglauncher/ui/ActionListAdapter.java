package de.nsvb.taglauncher.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.nsvb.taglauncher.R;
import de.nsvb.taglauncher.action.Action;

import java.util.ArrayList;

/**
 * Created by ns130291 on 24.05.13.
 */
public class ActionListAdapter extends ArrayAdapter<Action> {

    private ArrayList<Action> mActions;
    private LayoutInflater mLayoutInflater;
    private int mTextViewResourceId;

    public ActionListAdapter(Context context, int textViewResourceId,
                             ArrayList<Action> actions) {
        super(context, textViewResourceId, actions);
        mActions = actions;
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mTextViewResourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {

        if (v == null) {
            v = mLayoutInflater.inflate(mTextViewResourceId, null);
        }

        Action action = mActions.get(position);
        if (action != null) {
            TextView name = (TextView) v.findViewById(R.id.actionText);
            ImageView image = (ImageView) v.findViewById(R.id.actionImg);
            name.setText(action.getDescription(getContext()));
            image.setImageResource(action.getImage());
        }

        return v;
    }

}
