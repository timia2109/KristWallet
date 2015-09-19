package com.timia2109.kristwallet;

import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ApiViewHolder>{
    ArrayList<KristAPI> kristAPIs;
    View.OnClickListener onClickListener;
    View.OnLongClickListener onLongClickListener;
    public ArrayList<View> views;

    public static class ApiViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView kristID;
        TextView kristState;

        ApiViewHolder(View itemView, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            kristID = (TextView) itemView.findViewById(R.id.kristID);
            kristState = (TextView) itemView.findViewById(R.id.kristState);
            itemView.setOnClickListener(onClickListener);
            itemView.setOnLongClickListener(onLongClickListener);
        }
    }

    public RVAdapter(List<KristAPI> kristAPIs, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener){
        this.kristAPIs = (ArrayList) kristAPIs;
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
        views = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return kristAPIs.size();
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
        holder.kristID.setText(kristAPIs.get(i).getAddress());

        class LoadBudgetTask extends AsyncTask<String, Long, Long> {
            protected Long doInBackground(String... in) {
                try {
                    return kristAPIs.get(i).getBalance();
                } catch (Exception e) {
                }
                return -1l;
            }

            protected void onPostExecute(Long result) {
                if (result == -1l)
                    holder.kristState.setText("Get result FAILED!");
                else
                    holder.kristState.setText(result.toString()+" KST");
            }
        }

        LoadBudgetTask loadBudgetTask = new LoadBudgetTask();
        loadBudgetTask.execute("");

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
