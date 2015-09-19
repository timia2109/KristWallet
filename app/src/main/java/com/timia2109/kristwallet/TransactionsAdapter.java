package com.timia2109.kristwallet;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.apemanzilla.kwallet.Transaction;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransViewHolder>{
    Transaction[] transactions;
    String myAddress;

    public static class TransViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView kristID;
        TextView kristState;
        TextView date;

        TransViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            kristID = (TextView) itemView.findViewById(R.id.kristID);
            kristState = (TextView) itemView.findViewById(R.id.kristState);
            date = (TextView) itemView.findViewById(R.id.kristDate);
        }
    }

    public TransactionsAdapter(Transaction[] ptransactions, final KristAPI api){
        this.transactions = ptransactions;
        this.myAddress = api.getAddress();
    }

    @Override
    public int getItemCount() {
        return transactions.length;
    }

    @Override
    public TransViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_cardview, viewGroup, false);
        TransViewHolder pvh = new TransViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final TransViewHolder holder, final int i) {
        int color;
        if (transactions[i].isMined()) {
            holder.kristID.setText("Mined!");
            color = Color.GREEN;
        }
        else {
            String info = ((transactions[i].plus()) ? "-> .h" : "<- ");
            holder.kristID.setText(info+transactions[i].getAddr());
            if (transactions[i].plus())
                color = Color.GREEN;
            else
                color = Color.RED;
        }
        holder.kristID.setTextColor(color);

        Long amount = transactions[i].getAmount();
        holder.kristState.setText(amount.toString());

        holder.date.setText(transactions[i].getTime().toLocaleString());

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public TransactionsAdapter getMe() {
        return this;
    }
}
