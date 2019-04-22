package info.javaway.sternradio.view.dialog;

import android.app.Dialog;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import info.javaway.sternradio.App;
import info.javaway.sternradio.R;
import info.javaway.sternradio.storage.ConstantStorage;


public class InfoDialog extends DialogFragment {

    Button okButton;
    private String message;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setRetainInstance(true);
        String string = getArguments().getString(ConstantStorage.INFO_TEXT, "");
        switch (string){
            case ConstantStorage.ABOUT_RADIO_TEXT :{
                message = App.getContext().getString(R.string.info_radio);
                break;
            }
            case ConstantStorage.BETA_VERSION_TEXT :{
                message =  App.getContext().getString(R.string.beta_info);
                break;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_info, null);

        builder.setView(v);
        AlertDialog alertDialog = builder.create();
        ((TextView) v.findViewById(R.id.info_text_tv)).setText(message);
        okButton = v.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
//        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return alertDialog;
    }

}