package com.timia2109.kristwallet;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.timia2109.kristwallet.util.Name;

public class KSTNameFragment extends Fragment {
    LinearLayout layout;
    LayoutInflater inflater;
    ViewGroup container;
    KristAPI api;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        View view = inflater.inflate(R.layout.fragment_kstname_list, container, false);
        layout = (LinearLayout) view.findViewById(R.id.name_layout);
        loadNames();
        return view;
    }

    private void loadNames() {
        if (api == null) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Name[] names = api.getNames();
                    Name aName;
                    for (int i=0; i<names.length; i++) {
                        aName = names[i];
                        final LinearLayout aView = (LinearLayout) inflater.inflate(R.layout.fragment_kstname, container, false);
                        ((TextView)aView.findViewById(R.id.id)).setText(aName.name);
                        ((TextView)aView.findViewById(R.id.content)).setText(aName.registered.toString());
                        layout.post(new Runnable() {
                            @Override
                            public void run() {
                                layout.addView(aView);
                            }
                        });
                    }
                } catch (final Exception e) {
                    layout.post(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(layout, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    public void appendAPI(KristAPI pAPI) {api=pAPI;}
}
