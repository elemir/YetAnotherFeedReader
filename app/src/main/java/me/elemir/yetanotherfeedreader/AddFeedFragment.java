package me.elemir.yetanotherfeedreader;

import android.app.Activity;
import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;


public class AddFeedFragment extends DialogFragment implements View.OnClickListener {
    private OnFragmentInteractionListener mListener;
    private EditText uriText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_feed, container, false);

        getDialog().setTitle(R.string.title_fragment_add_feed);

        ((Button) v.findViewById(R.id.ok)).setOnClickListener(this);
        ((Button) v.findViewById(R.id.cancel)).setOnClickListener(this);
        uriText = ((EditText) v.findViewById(R.id.uriText));

        return v;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok:
                if (mListener != null)
                    mListener.onFeedAdded(uriText.getText().toString());
            case R.id.cancel:
                dismiss();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onFeedAdded(String link);
    }

}
