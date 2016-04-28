package com.rmasc.fireroad.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.rmasc.fireroad.AudioTextActivity;
import com.rmasc.fireroad.MainActivity;
import com.rmasc.fireroad.MotoActivity;
import com.rmasc.fireroad.PerfilActivity;
import com.rmasc.fireroad.R;
import com.rmasc.fireroad.listMotos;

/**
 * Created by ADMIN on 23/02/2016.
 */
public class MenuFragment extends Fragment {

    View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent goTo;
            switch (v.getId())
            {
                case R.id.btnHome:

                    //goTo = new Intent(getContext(), MainActivity.class);
                    //NavUtils.navigateUpFromSameTask(getActivity());
                    //startActivity(goTo);

                    Intent openMainActivity= new Intent(getContext(), MainActivity.class);
                    openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    //openMainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(openMainActivity);
                    break;
                case R.id.btnPerfil:
                    goTo = new Intent(getContext(), PerfilActivity.class);
                    startActivity(goTo);
                    break;
                case R.id.btnMotos:
                    goTo = new Intent(getContext(), MotoActivity.class);
                    startActivity(goTo);
                    break;
                case R.id.btnRutas:
                    goTo = new Intent(getContext(), listMotos.class);
                    startActivity(goTo);
                    break;
                case R.id.btnSettings:
                    //goTo = new Intent(getContext(), AudioTextActivity.class);
                    //startActivity(goTo);
                    break;
                default:
                    break;
            }
        }
    };

    View.OnLongClickListener buttonInfo = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            switch (v.getId())
            {
                case R.id.btnPerfil:
                    ShowMessage("Perfil");
                    break;
                case R.id.btnMotos:
                    ShowMessage("Motos");
                    break;
                case R.id.btnRutas:
                    ShowMessage("Mis rutas");
                    break;
                case R.id.btnSettings:
                    ShowMessage("Ajustes");
                    break;
                case R.id.btnHome:
                    ShowMessage("Home");
                    break;

                default:
                    break;
            }
            return false;
        }
    };

    private void ShowMessage(final String message)
    {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View menu = inflater.inflate(R.layout.menu_bar, container, false);

        ImageButton btnHome = (ImageButton) menu.findViewById(R.id.btnHome);
        btnHome.setOnClickListener(buttonListener);
        btnHome.setOnLongClickListener(buttonInfo);

        ImageButton btnPerfil = (ImageButton) menu.findViewById(R.id.btnPerfil);
        btnPerfil.setOnClickListener(buttonListener);
        btnPerfil.setOnLongClickListener(buttonInfo);

        ImageButton btnMotos = (ImageButton) menu.findViewById(R.id.btnMotos);
        btnMotos.setOnClickListener(buttonListener);
        btnMotos.setOnLongClickListener(buttonInfo);

        ImageButton btnRutas = (ImageButton) menu.findViewById(R.id.btnRutas);
        btnRutas.setOnClickListener(buttonListener);
        btnRutas.setOnLongClickListener(buttonInfo);

        ImageButton btnSettings = (ImageButton) menu.findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(buttonListener);
        btnSettings.setOnLongClickListener(buttonInfo);

        return menu;
    }
}
