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
import se.cgi.android.rdh.models.WorkOrder;

public class WorkOrderRecyclerViewAdapter extends RecyclerView.Adapter<WorkOrderRecyclerViewAdapter.MyViewHolder> implements Filterable {
    List<WorkOrder> workOrderList;
    List<WorkOrder> workOrderListFull; // Used when filtering
    Context context;
    OnWorkOrderListener onWorkOrderListener;

    public WorkOrderRecyclerViewAdapter(List<WorkOrder> workOrderList, Context context, OnWorkOrderListener onWorkOrderListener) {
        this.workOrderList = workOrderList;
        this.workOrderListFull = workOrderList == null ? new ArrayList<WorkOrder>() : new ArrayList<>(workOrderList);
        this.context = context;
        this.onWorkOrderListener = onWorkOrderListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.work_order_item, parent, false);
        return new MyViewHolder(view, onWorkOrderListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.tvWorkOrderNo.setText(workOrderList.get(position).getWorkOrderNo());
        holder.tvWorkOrderName.setText((workOrderList.get(position).getWorkOrderName()));
    }

    // För sökning (filtrering av listan i vyn)
    @SuppressWarnings("unchecked")
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<WorkOrder> filteredList = new ArrayList<>();

                if (charSequence == null || charSequence.length() == 0) {
                    filteredList = workOrderListFull;
                } else {
                    String fillerPattern = charSequence.toString().toLowerCase().trim();

                    for (WorkOrder workOrder : workOrderListFull) {
                        // name match condition. this might differ depending on your requirement
                        if (workOrder.getWorkOrderNo().toLowerCase().contains(fillerPattern)
                                || workOrder.getWorkOrderName().toLowerCase().contains(fillerPattern)) {
                            filteredList.add(workOrder);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                workOrderList.clear();
                workOrderList.addAll((List<WorkOrder>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemCount() {
        return workOrderList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvWorkOrderNo, tvWorkOrderName;
        OnWorkOrderListener onWorkOrderListener;

        public MyViewHolder(@NonNull View itemView, OnWorkOrderListener onWorkOrderListener) {
            super(itemView);
            tvWorkOrderNo = itemView.findViewById(R.id.tv_work_order_no);
            tvWorkOrderName = itemView.findViewById(R.id.tv_work_order_name);
            this.onWorkOrderListener = onWorkOrderListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onWorkOrderListener.onWorkOrderClick(getAdapterPosition());
        }
    }

    public interface OnWorkOrderListener {
        void onWorkOrderClick(int position);
    }
}
