package se.cgi.android.rdh.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import se.cgi.android.rdh.R;
import se.cgi.android.rdh.data.DatabaseHelper;
import se.cgi.android.rdh.models.Trans;
import se.cgi.android.rdh.models.WorkOrder;
import se.cgi.android.rdh.utils.Utils;

public class TransactionRecyclerViewAdapter extends RecyclerView.Adapter<TransactionRecyclerViewAdapter.MyViewHolder> implements Filterable {
    List<Trans> transList;
    List<Trans> transListFull; // For filtering
    Context context;
    OnTransactionListener onTransactionListener;
    DatabaseHelper dbHelper;

    public TransactionRecyclerViewAdapter(List<Trans> transList, Context context, OnTransactionListener onTransactionListener) {
        this.transList = transList;
        this.transListFull = transList == null ? new ArrayList<Trans>() : new ArrayList<>(transList);
        this.context = context;
        this.onTransactionListener = onTransactionListener;

        // Get singleton instance of database
        dbHelper = DatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new MyViewHolder(view, onTransactionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.tvTransType.setText(transList.get(position).getTransType());
        holder.tvTransWorkOrderNo.setText("Arbetsorder: " + transList.get(position).getWorkOrderNo());
        WorkOrder workOrder =  dbHelper.getWorkOrderById(transList.get(position).getWorkOrderId());
        holder.tvTransWorkOrderName.setText("Namn: " + workOrder.getWorkOrderName());
        holder.tvTransArticleNo.setText("Fbet: " + transList.get(position).getArticleNo());
        holder.tvTransQuantity.setText("Antal: " + String.valueOf(transList.get(position).getQuantity()));
        holder.tvTransDateTime.setText(Utils.getFormattedDateTime(transList.get(position).getDateTime()));
    }

    // För sökning
    @SuppressWarnings("unchecked")
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                List<Trans> filteredList = new ArrayList<>();

                if (charSequence == null || charSequence.length() == 0) {
                    filteredList = transListFull;
                } else {
                    String fillerPattern = charSequence.toString().toLowerCase().trim();

                    for (Trans trans : transListFull) {
                        // Search on transaction type, article number, indvidual number and signature
                        if ( trans.getArticleNo().toLowerCase().contains(fillerPattern)
                                || trans.getWorkOrderNo().toLowerCase().contains(fillerPattern)) {
                            filteredList.add(trans);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                transList.clear();
                transList.addAll((List<Trans>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemCount() {
        return transList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTransType, tvTransWorkOrderNo, tvTransWorkOrderName, tvTransArticleNo, tvTransQuantity, tvTransDateTime;
        OnTransactionListener onTransactionListener;

        public MyViewHolder(@NonNull View itemView, OnTransactionListener onTransactionListener) {
            super(itemView);
            tvTransType = itemView.findViewById(R.id.tv_trans_type);
            tvTransWorkOrderNo = itemView.findViewById(R.id.tv_trans_work_order_no);
            tvTransWorkOrderName = itemView.findViewById(R.id.tv_trans_work_order_name);
            tvTransArticleNo = itemView.findViewById(R.id.tv_trans_article_no);
            tvTransQuantity = itemView.findViewById(R.id.tv_trans_quantity);
            tvTransDateTime = itemView.findViewById(R.id.tv_trans_datetime);
            this.onTransactionListener = onTransactionListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onTransactionListener.onTransClick(getAdapterPosition());
        }
    }

    public interface OnTransactionListener {
        void onTransClick(int position);
    }
}
