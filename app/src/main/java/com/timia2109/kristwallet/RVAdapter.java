package com.timia2109.kristwallet;


import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ApiViewHolder>{
    KristAPI[] kristAPIs;
    View.OnClickListener onClickListener;
    View.OnLongClickListener onLongClickListener;
    public long summery = 0;
    public ArrayList<View> views;
    private int length;
    private android.support.v7.app.ActionBar ab;

    public static class ApiViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView kristID;
        TextView kristState;

        ApiViewHolder(View itemView, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            kristID = (TextView) itemView.findViewById(R.id.kristID);
            kristState = (TextView) cv.findViewById(R.id.kristStateTV);
            itemView.setOnClickListener(onClickListener);
            itemView.setOnLongClickListener(onLongClickListener);
        }
    }

    public RVAdapter(KristAPI[] kristAPIs, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener, android.support.v7.app.ActionBar ab){
        this.kristAPIs = kristAPIs;
        length = kristAPIs.length-1;
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
        this.ab = ab;
        views = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return kristAPIs.length;
    }

    @Override
    public ApiViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_cardview, viewGroup, false);
        ApiViewHolder pvh = new ApiViewHolder(v, onClickListener, onLongClickListener);
        views.add(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final ApiViewHolder holder, final int i) {
        holder.kristID.setText(kristAPIs[i].getName());
        final Handler h = new Handler();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final long balance = kristAPIs[i].getBalance();
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            holder.kristState.setText(Long.toString(balance) + KristAPI.currency);
                            summery += balance;
                            ab.setTitle("Krist Wallet ("+summery+" "+KristAPI.currency+")");
                        }
                    });
                }
                catch (final Exception e) {
                    System.out.println(e.getClass().getName()+"\t\t"+e.getMessage());
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            holder.kristState.setText("Get result FAILED :( !\t"+e.getMessage());
                        }
                    });
                }
            }
        });
        t.start();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
