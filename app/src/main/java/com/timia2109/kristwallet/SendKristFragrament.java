package com.timia2109.kristwallet;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class SendKristFragrament extends Fragment {
    ArrayList<KristAPI> apis;
    CharSequence[] apisLabels;
    int apiPointer;
    Button walletChooser, send;
    EditText kristAns, kristTaget;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_sendkrist, container, false);

        walletChooser = (Button) myView.findViewById(R.id.walletChoose);
        send = (Button) myView.findViewById(R.id.send);
        kristAns = (EditText) myView.findViewById(R.id.kristAns);
        kristTaget = (EditText) myView.findViewById(R.id.kristTaget);
        kristTaget.setText(KristAPI.donateAddress);

        walletChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Pick a Wallet");
                builder.setItems(apisLabels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        apiPointer = which;
                        refresh();
                    }
                });
                builder.show();
            }
        });

        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    refresh();
            }
        };

        kristTaget.setOnFocusChangeListener(onFocusChangeListener);
        kristAns.setOnFocusChangeListener(onFocusChangeListener);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                class LoadBudgetTask extends AsyncTask<String, KristAPI.TransferResults, KristAPI.TransferResults> {
                    protected KristAPI.TransferResults doInBackground(String... in) {
                        long a = 0;
                        try {
                            a = Long.parseLong(in[0]);
                        } catch (NumberFormatException n) {
                            return KristAPI.TransferResults.NoLong;
                        }
                        try {
                            return apis.get(apiPointer).sendKrist(a, in[1]);
                        } catch (Exception e) {
                            return KristAPI.TransferResults.WebConnectFail;
                        }
                    }

                    protected void onPostExecute(KristAPI.TransferResults result) {
                        String text = "";
                        switch (result) {
                            case WebConnectFail:
                                text = "FAIL: Can't connect to the Krist Server!";
                                break;
                            case NoLong:
                                text = "FAIL: The amount of Krists is not a number!";
                                break;
                            case NotEnoughKST:
                                text = "FAIL: You don't have enouth KST!";
                                break;
                            case BadValue:
                                text = "FAIL: Bad Value";
                                break;
                            case InvalidRecipient:
                                text = "FAIL: Invaild Recipient";
                                break;
                            case InsufficientFunds:
                                text = "FAIL: Insufficent Funds!";
                                break;
                            case SelfSend:
                                text = "FAIL: You can't send yourself Krists!";
                                break;
                            case Success:
                                text = "Krists send!";
                                break;
                        }
                        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_LONG);
                        toast.show();
                    }
                }

                LoadBudgetTask loadBudgetTask = new LoadBudgetTask();
                loadBudgetTask.execute(kristAns.getText().toString(), kristTaget.getText().toString());
            }
        });
        refresh();

        return myView;
    }

    protected void refresh() {
        walletChooser.setText("Wallet: " + apis.get(apiPointer).getAddress());
        String text;
        if (kristTaget.getText().toString().equals(KristAPI.donateAddress))
            text = "Donate "+kristAns.getText()+ "KST from "+apis.get(apiPointer).getAddress()+ " to the developer";
        else
            text = "Send "+kristAns.getText()+" KST from "+apis.get(apiPointer).getAddress()+" to "+kristTaget.getText().toString();
        send.setText(text);
    }

    public void setAPIs(ArrayList<KristAPI> pAPIs) {
        apis = pAPIs;
        apisLabels = new CharSequence[apis.size()];
        for (int i=0; i<apis.size();i++) {
            apisLabels[i] = apis.get(i).getAddress();
        }
        apiPointer = 0;
    }
}
