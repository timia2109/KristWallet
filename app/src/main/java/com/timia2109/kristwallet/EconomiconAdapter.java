package com.timia2109.kristwallet;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.timia2109.kristwallet.util.Address;

public class EconomiconAdapter extends RecyclerView.Adapter<EconomiconAdapter.ApiViewHolder>{
    Address[] addresses;
    View.OnClickListener onClickListener;
    String dateFormat;
    Context context;

    public static class ApiViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView kristID;
        TextView kristState, date;

        ApiViewHolder(View itemView, View.OnClickListener onClickListener) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            kristID = (TextView) itemView.findViewById(R.id.kristID);
            kristState = (TextView) itemView.findViewById(R.id.kristStateTV);
            date = (TextView) itemView.findViewById(R.id.kristDate);
            itemView.setOnClickListener(onClickListener);
        }
    }

    public EconomiconAdapter(Address[] pAddresses, View.OnClickListener onClickListener, String dateFormat, Context c){
        this.onClickListener = onClickListener;
        this.addresses = pAddresses;
        this.dateFormat = dateFormat;
        context = c;
    }

    @Override
    public int getItemCount() {
        return addresses.length;
    }

    @Override
    public ApiViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_cardview, viewGroup, false);
        ApiViewHolder pvh = new ApiViewHolder(v, onClickListener);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final ApiViewHolder holder, final int i) {
        holder.kristID.setText(addresses[i].getAddress());
        holder.kristState.setText(addresses[i].getBalance()+KristAPI.currency);
        holder.date.setText( Saver.stringifyDate(addresses[i].getFirstSeen(), dateFormat, context));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}