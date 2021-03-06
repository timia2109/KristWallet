package com.timia2109.kristwallet;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timia2109.kristwallet.util.Address;

import java.text.SimpleDateFormat;


public class EconomiconFragrament extends Fragment {
    View myView;
    KristAPI api;
    Saver saver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.activity_main, container, false);

        class LoadTransTask extends AsyncTask<String, Long, Address[]> {
                protected Address[] doInBackground(String... in) {
                    try {
                        return api.getRichList();
                    } catch (Exception e) {

                    }
                    return new Address[] {null};
                }

                protected void onPostExecute(Address[] result) {
                    RecyclerView rv = (RecyclerView) myView.findViewById(R.id.rv);
                    rv.setHasFixedSize(true);

                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(myView.getContext());
                    rv.setLayoutManager(mLayoutManager);

                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TextView t = (TextView) v.findViewById(R.id.kristID);
                            Intent wallet = new Intent(getActivity(), WalletActivity.class);

                            KristAPI k = new KristAPI("");
                            k.makeViewAddress(t.getText().toString());
                            wallet.putExtra("api",k);

                            startActivity(wallet);
                            getActivity().finish();
                        }
                    };

                    EconomiconAdapter adapter = new EconomiconAdapter(result, onClickListener, saver.dateFormat, getContext());
                    rv.setAdapter(adapter);
                }
            }
            LoadTransTask loadTransTask = new LoadTransTask();
            loadTransTask.execute("");

        return myView;
    }

    public void appendAPI(KristAPI pAPI) {
        api = pAPI;
    }
    public void appendSaver(Saver s) {saver=s;}
}