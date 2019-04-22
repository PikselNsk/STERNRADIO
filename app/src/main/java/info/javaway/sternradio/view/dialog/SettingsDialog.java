package info.javaway.sternradio.view.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.DialogFragment;

import info.javaway.sternradio.App;
import info.javaway.sternradio.R;
import info.javaway.sternradio.handler.PrefManager;
import info.javaway.sternradio.service.NotificationHelper;


public class SettingsDialog extends DialogFragment {

    Button okButton;
    AppCompatCheckBox notificationCheckBox;
    private String message;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_settings, null);

        builder.setView(v);
        AlertDialog alertDialog = builder.create();

        notificationCheckBox = v.findViewById(R.id.notification_check_box);
        notificationCheckBox.setChecked(PrefManager.getNotification());
        notificationCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PrefManager.setNotification(isChecked);
                if(isChecked){
                    NotificationHelper.sendNotification(
                            App.getContext(),
                            "",
                            0,
                            null,
                            null,
                            null,
                            false);
                } else {
                    NotificationHelper.clearNotification();
                }
            }
        });

        okButton = v.findViewById(R.id.ok_button);
        okButton.setOnClickListener(v1 -> dismiss());
        return alertDialog;
    }

}