package com.timia2109.kristwallet;


import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import io.github.apemanzilla.kwallet.Transaction;

public class TransactionsFragrament extends Fragment {
    View myView;
    KristAPI api;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_transactions, container, false);

        if (api != null) {
            TextView kristID = (TextView) myView.findViewById(R.id.kristID);
            kristID.setText(api.getAddress());

            final TextView kristCoins = (TextView) myView.findViewById(R.id.kristCoins);

            class LoadBudgetTask extends AsyncTask<String, Long, Long> {
                protected Long doInBackground(String... in) {
                    try {
                        return api.getBalance();
                    } catch (Exception e) {
                        Toast toast = Toast.makeText(getActivity(), "FAIL TO GET TRANSACTIONS", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    return -1l;
                }

                protected void onPostExecute(Long result) {
                    kristCoins.setText(result.toString()+" KST");
                }
            }

            LoadBudgetTask loadBudgetTask = new LoadBudgetTask();
            loadBudgetTask.execute("");

            class LoadTransTask extends AsyncTask<String, Long, Transaction[]> {
                protected Transaction[] doInBackground(String... in) {
                    try {
                        return api.getTransactions();
                    } catch (Exception e) {

                    }
                    return null;
                }

                protected void onPostExecute(Transaction[] result) {
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

                    TransactionsAdapter adapter = new TransactionsAdapter(result, api);
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
    public TransactionsFragrament getFragrament() {return this;}
}