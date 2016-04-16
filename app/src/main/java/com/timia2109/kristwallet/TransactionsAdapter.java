package com.timia2109.kristwallet;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timia2109.kristwallet.util.Transactions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransViewHolder>{
    Transactions[] transactions;
    String myAddress;
    SimpleDateFormat dateFormat;

    public static class TransViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView kristID;
        TextView kristState;
        TextView date;

        TransViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            kristID = (TextView) itemView.findViewById(R.id.kristID);
            kristState = (TextView) itemView.findViewById(R.id.kristStateTV);
            date = (TextView) itemView.findViewById(R.id.kristDate);
        }
    }

    public TransactionsAdapter(Transactions[] ptransactions, final KristAPI api, String dateFormat){
        this.transactions = ptransactions;
        this.myAddress = api.getAddress();
        try {
            this.dateFormat = new SimpleDateFormat(dateFormat, Locale.US);
        } catch (IllegalArgumentException ignored) {}
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
        int color = 0x9900FF00;
        if (transactions[i].isMined())
            holder.kristID.setText("Mined!");
        else {
            boolean incomming = transactions[i].getToAddr().equals(myAddress);
            String info = ((incomming ) ? "-> "+transactions[i].getFromAddr() : "<- "+transactions[i].getToAddr());
            holder.kristID.setText(info);
            holder.kristID.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
            if (!incomming)
                color = 0x99FF0000;
        }
        holder.cv.setBackgroundColor(color);

        Long amount = transactions[i].getAmount();
        holder.kristState.setTextSize(TypedValue.COMPLEX_UNIT_SP,30);

        holder.kristState.setText(amount.toString()+" KST");

        Date date = transactions[i].getTime();
        String meta = transactions[i].getMetadata();
        String dateS;
        if (dateFormat == null)
            dateS = "Wrong date format!";
        else
            dateS = dateFormat.format(date);
        if (meta != null)
            holder.date.setText( dateS+"\n"+meta );
        else
            holder.date.setText( dateS );
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public TransactionsAdapter getMe() {
        return this;
    }
}
