package com.timia2109.kristwallet;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.apemanzilla.kwallet.Address;

public class EconomiconAdapter extends RecyclerView.Adapter<EconomiconAdapter.ApiViewHolder>{
    Address[] addresses;
    View.OnClickListener onClickListener;

    public static class ApiViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView kristID;
        TextView kristState, date;

        ApiViewHolder(View itemView, View.OnClickListener onClickListener) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            kristID = (TextView) itemView.findViewById(R.id.kristID);
            kristState = (TextView) itemView.findViewById(R.id.kristState);
            date = (TextView) itemView.findViewById(R.id.kristDate);
            itemView.setOnClickListener(onClickListener);
        }
    }

    public EconomiconAdapter(Address[] pAddresses, View.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
        this.addresses = pAddresses;
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
        holder.kristState.setText(addresses[i].getBalance()+" KST");
        holder.date.setText(addresses[i].getLastSeen().toString());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}