package com.angopapo.aroundme.InternetHelper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.angopapo.aroundme.R;

/**
 * Created by Angopapo, LDA on 12.08.16.
 */
public class WaitFragment extends Fragment implements View.OnClickListener {

    public static final String ERROR_CODE = "ec";

    Button mConnect;
    RelativeLayout mProgressPanel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_wait, container, false);
        mConnect = (Button) v.findViewById(R.id.connect);
        mProgressPanel = (RelativeLayout) v.findViewById(R.id.progress_panel);
        mConnect.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.connect:
                mProgressPanel.setVisibility(View.VISIBLE);
                break;
        }
    }
}
