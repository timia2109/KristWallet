package com.timia2109.kristwallet;


import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.timia2109.kristwallet.util.Transactions;


public class TransactionsFragrament extends Fragment {
    View myView;
    KristAPI api;
    Saver saver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_transactions, container, false);

        if (api != null) {
            TextView kristID = (TextView) myView.findViewById(R.id.kristID);
            kristID.setText(api.getAddress());

            final TextView kristCoins = (TextView) myView.findViewById(R.id.kristCoins);
            final TextView totalIn = (TextView) myView.findViewById(R.id.totalIn);
            totalIn.setTextColor(0xFF009900);
            final TextView totalOut = (TextView) myView.findViewById(R.id.totalOut);
            totalOut.setTextColor(0xFF990000);

            final Handler h = new Handler();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final long b = api.getBalance();
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                kristCoins.setText(Long.toString(b)+KristAPI.currency);
                                totalIn.setText("-> "+api.getTotalin());
                                totalOut.setText("<- "+api.getTotalout());
                            }
                        });
                    } catch (Exception e) {
                        System.out.println(e.toString());
                    }
                }
            }).start();
            
            class LoadTransTask extends AsyncTask<String, Long, Transactions[]> {
                protected Transactions[] doInBackground(String... in) {
                    try {
                        return api.getTransactions();
                    } catch (Exception e) {
                        System.out.println(e.toString());
                        System.exit(0);
                    }
                    return null;
                }

                protected void onPostExecute(Transactions[] result) {
                    LinearLayout.LayoutParams p;
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                        p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.8f);
                    else
                        p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.8f);

                    ProgressBar pb = (ProgressBar) myView.findViewById(R.id.progressBar1);
                    LinearLayout layout = (LinearLayout) myView.findViewById(R.id.linearLayout);
                    layout.removeView(pb);

                    RecyclerView rv = (RecyclerView) myView.findViewById(R.id.rv);
                    rv.setHasFixedSize(true);
                    rv.setLayoutParams(p);
                    rv.setVisibility(View.VISIBLE);

                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(myView.getContext());
                    rv.setLayoutManager(mLayoutManager);

                    TransactionsAdapter adapter = new TransactionsAdapter(result, api, saver.dateFormat);
                    rv.setAdapter(adapter);
                }
            }
            LoadTransTask loadTransTask = new LoadTransTask();
            loadTransTask.execute("");
        }

        return myView;
    }

    public void appendAPI(KristAPI api) {
        this.api = api;
    }
    public void appendSaver(Saver saver) {this.saver=saver;}
    public TransactionsFragrament getFragrament() {return this;}
}