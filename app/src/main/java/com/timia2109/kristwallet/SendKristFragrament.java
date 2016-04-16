package com.timia2109.kristwallet;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.timia2109.kristwallet.util.KristSender;

public class SendKristFragrament extends Fragment {
    KristAPI[] apis;
    CharSequence[] apisLabels;
    int apiPointer, targetPointer;
    Button walletChooser, send;
    ImageButton chooseTarget;
    EditText kristAns, kristTaget, kristMeta;
    public static String preTo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        targetPointer = -1;
        View myView = inflater.inflate(R.layout.fragment_sendkrist, container, false);

        walletChooser = (Button) myView.findViewById(R.id.walletChoose);
        send = (Button) myView.findViewById(R.id.send);
        kristAns = (EditText) myView.findViewById(R.id.kristAns);
        kristTaget = (EditText) myView.findViewById(R.id.kristTaget);
        if (preTo != null && !preTo.equals(""))
            kristTaget.setText(preTo);
        else
            kristTaget.setText(KristAPI.donateAddress);
        kristMeta = (EditText) myView.findViewById(R.id.kristMetaEdit);

        View.OnClickListener chooseAPI = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Pick a Wallet");
                builder.setItems(apisLabels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (v.equals(walletChooser)) {
                            apiPointer = which;
                            refresh();
                        }
                        else if (v.equals(chooseTarget))
                            kristTaget.setText(apis[which].getAddress());
                    }
                });
                builder.show();
            }
        };

        walletChooser.setOnClickListener(chooseAPI);
        chooseTarget = (ImageButton) myView.findViewById(R.id.cButton);
        chooseTarget.setOnClickListener(chooseAPI);

        TextWatcher edittable = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refresh();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        kristAns.addTextChangedListener(edittable);
        kristTaget.addTextChangedListener(edittable);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SendKristFragrament.this.getActivity());
                builder.setTitle(SendKristFragrament.this.getString(R.string.shureSend, kristAns.getText()));
                builder.setMessage(SendKristFragrament.this.getString(R.string.shureSendCont, kristAns.getText(), apis[apiPointer].getAddress(), kristTaget.getText()));
                builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long ans = 0;
                        try {
                            ans = Long.parseLong(kristAns.getText().toString());
                            new KristSender(ans, kristTaget.getText().toString(), apis[apiPointer], kristMeta.getText().toString(), getContext()).start();
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), getString(R.string.noNumber), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
        refresh();

        return myView;
    }

    protected void refresh() {
        walletChooser.setText("Wallet: " + apis[apiPointer].getAddress() + " (" + apis[apiPointer].getCachedBalance() + ")");
        String text;
        if (kristTaget.getText().toString().equals(KristAPI.donateAddress))
            text = "Donate "+kristAns.getText()+ " KST from "+apis[apiPointer].getAddress()+ " to the developer";
        else
            text = "Send "+kristAns.getText()+" KST from "+apis[apiPointer].getAddress()+" to "+kristTaget.getText().toString();
        send.setText(text);
    }

    public void setAPIs(KristAPI[] pAPIs, KristAPI pointer) {
        apis = pAPIs;
        apisLabels = new CharSequence[apis.length];
        for (int i=0; i<apis.length;i++) {
            apisLabels[i] = apis[i].getAddress();
            if (pointer == pAPIs[i])
                apiPointer = i;
        }
    }

}
