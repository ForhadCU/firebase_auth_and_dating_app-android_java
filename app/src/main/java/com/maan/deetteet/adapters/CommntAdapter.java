package com.maan.deetteet.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.maan.deetteet.R;
import com.maan.deetteet.databinding.ItemCommentBinding;
import com.maan.deetteet.models.CommentRoot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CommntAdapter extends RecyclerView.Adapter<CommntAdapter.CommentViewHolder> {
    Random rand = new Random();
    private List<CommentRoot.DataItem> data = new ArrayList<>();

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {


        holder.binding.tvcomment.setText(data.get(position).getComment());
        holder.binding.tvName.setText(data.get(position).getName());

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void addData(List<CommentRoot.DataItem> data) {

        for (int i = 0; i < data.size(); i++) {
            this.data.add(data.get(i));
            notifyItemInserted(this.data.size() - 1);
        }
    }

    public void addSingleComment(CommentRoot.DataItem comment) {
        this.data.add(comment);
        notifyItemInserted(data.size());
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        ItemCommentBinding binding;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemCommentBinding.bind(itemView);
        }
    }
}
