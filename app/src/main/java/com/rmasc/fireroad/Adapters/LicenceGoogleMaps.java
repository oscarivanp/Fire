package com.rmasc.fireroad.Adapters;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;
import com.rmasc.fireroad.R;

/**
 * Created by rafaelmartinez on 6/05/16.
 */
public class LicenceGoogleMaps extends Fragment
{

    TextView txtlicence ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String Contenido=GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(getActivity().getBaseContext());
        txtlicence =(TextView) getView().findViewById(R.id.txtAisoLegal);
        txtlicence.setText(Contenido);
    }
}
