package com.example.sentiment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ThreadListsAdapter extends RecyclerView.Adapter<ThreadListsAdapter.ThreadListsViewHolder> {

    private String[] threadContactList;
    private LayoutInflater layoutInflater;
    private ItemClickListener mClickListener;

    //data is passed into the constructor
    public ThreadListsAdapter(String[] threadContactList) {
        this.threadContactList = threadContactList;
    }

    //inflates the row layout from xml when needed
    @NonNull
    @Override
    public ThreadListsAdapter.ThreadListsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_row, parent, false); //Redo the entirety of this class

        ThreadListsViewHolder viewHolder = new ThreadListsViewHolder(view);
        return viewHolder;
    }

    //binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull ThreadListsAdapter.ThreadListsViewHolder holder, int position) {
        //get element from the dataset
        //replace the contents of the view with that element
        String threadContact = threadContactList[position];
        holder.nameOfThread.setText(threadContact);
    }

    //stores and recycles view as they are scrolled off screen
    public class ThreadListsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nameOfThread;

        ThreadListsViewHolder(View view) {
            super(view);
            nameOfThread = view.findViewById(R.id.textviewThreadName);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mClickListener != null) mClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    //total number of rows
    @Override
    public int getItemCount() {
        return threadContactList.length;
    }


    //convenience method for getting data at click position
    String getItem(int id) {
        return threadContactList[id];
    }

    //allows click events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    //parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }


}
