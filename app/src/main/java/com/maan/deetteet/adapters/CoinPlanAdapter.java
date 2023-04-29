package com.maan.deetteet.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.maan.deetteet.R;
import com.maan.deetteet.databinding.ItemCoinplansBinding;
import com.maan.deetteet.models.CoinPackageRoot;
import com.maan.deetteet.retrofit.Const;

import java.util.ArrayList;
import java.util.List;

public class CoinPlanAdapter extends RecyclerView.Adapter<CoinPlanAdapter.CoinViewHolder> {
    private Context context;
    private List<CoinPackageRoot.DataItem> packages = new ArrayList<>();
    private OnCoinPlanClickListnear onCoinPlanClickListnear;

    public CoinPlanAdapter(List<CoinPackageRoot.DataItem> packages, OnCoinPlanClickListnear onCoinPlanClickListnear) {
        this.packages = packages;

        this.onCoinPlanClickListnear = onCoinPlanClickListnear;
    }

    @NonNull
    @Override
    public CoinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new CoinViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coinplans, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CoinViewHolder holder, int position) {
        CoinPackageRoot.DataItem plan = packages.get(position);

        if (position % 2 == 0) {
            holder.binding.lytmain.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_white_gray_10dp));
            holder.binding.tvamount.setTextColor(ContextCompat.getColor(context, R.color.black2));
            holder.binding.tvcoin.setTextColor(ContextCompat.getColor(context, R.color.black2));
            holder.binding.tvcoin.setText(plan.getCoinAmount());
            holder.binding.tvamount.setText(Const.CURRENCY + plan.getPrice());
        } else {
            holder.binding.tvcoin.setText(plan.getCoinAmount());
            holder.binding.tvamount.setText(Const.CURRENCY + plan.getPrice());
        }
        holder.itemView.setOnClickListener(v -> onCoinPlanClickListnear.onPlanClick(plan));
    }

    @Override
    public int getItemCount() {
        return packages.size();
    }

    public interface OnCoinPlanClickListnear {
        void onPlanClick(CoinPackageRoot.DataItem amount);
    }

    public class CoinViewHolder extends RecyclerView.ViewHolder {
        ItemCoinplansBinding binding;

        public CoinViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemCoinplansBinding.bind(itemView);
        }
    }
}
