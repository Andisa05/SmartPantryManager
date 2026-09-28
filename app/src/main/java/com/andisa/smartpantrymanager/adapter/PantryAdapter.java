package com.andisa.smartpantrymanager.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.andisa.smartpantrymanager.R;
import com.andisa.smartpantrymanager.data.PantryDataSource;
import com.andisa.smartpantrymanager.model.PantryItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * RecyclerView adapter for the Pantry List screen. Follows the same
 * ViewHolder + Adapter pattern taught for ContactAdapter: the
 * adapter owns the data set, inflates list_item_pantry for each
 * row, and handles the delete button and row-click-to-edit itself.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    private ArrayList<PantryItem> pantryData;
    private final Context parentContext;
    private final OnItemClickListener itemClickListener;

    public PantryAdapter(ArrayList<PantryItem> pantryData, Context context, OnItemClickListener listener) {
        this.pantryData = pantryData;
        this.parentContext = context;
        this.itemClickListener = listener;
    }

    public void setData(ArrayList<PantryItem> newData) {
        this.pantryData = newData;
        notifyDataSetChanged();
    }

    public class PantryViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textQuantity;
        private final TextView textExpiry;
        private final Button buttonDelete;

        public PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textItemName);
            textQuantity = itemView.findViewById(R.id.textItemQuantity);
            textExpiry = itemView.findViewById(R.id.textItemExpiry);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteItem);
        }
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_pantry, parent, false);
        return new PantryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        final PantryItem item = pantryData.get(position);

        holder.textName.setText(item.getName());
        holder.textQuantity.setText(String.format("%s %s", trimTrailingZero(item.getQuantity()), item.getUnit()));

        if (item.hasExpiryDate()) {
            String dateText = DateFormat.format("yyyy/MM/dd", item.getExpiryDate()).toString();
            boolean alertsEnabled = parentContext.getSharedPreferences("SmartPantryPrefs", Context.MODE_PRIVATE)
                    .getBoolean("expiry_alerts_enabled", true);
            if (alertsEnabled && isExpiringSoon(item.getExpiryDate())) {
                holder.textExpiry.setText("Expires " + dateText + " - expiring soon!");
            } else {
                holder.textExpiry.setText("Expires " + dateText);
            }
        } else {
            holder.textExpiry.setText("No expiry date set");
        }

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(item);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> deleteItem(item, holder.getAdapterPosition()));
    }

    private boolean isExpiringSoon(long expiryMillis) {
        long now = Calendar.getInstance().getTimeInMillis();
        long threeDaysMillis = TimeUnit.DAYS.toMillis(3);
        // Already expired, or due to expire within the next 3 days.
        return (expiryMillis - now) <= threeDaysMillis;
    }

    private String trimTrailingZero(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private void deleteItem(PantryItem item, int position) {
        PantryDataSource ds = new PantryDataSource(parentContext);
        try {
            ds.open();
            boolean didDelete = ds.deletePantryItem(item.getId());
            ds.close();
            if (didDelete) {
                pantryData.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, pantryData.size());
            } else {
                Toast.makeText(parentContext, "Could not delete item", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(parentContext, "Could not delete item", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return pantryData.size();
    }
}
