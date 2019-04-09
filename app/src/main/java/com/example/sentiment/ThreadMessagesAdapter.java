package com.example.sentiment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ThreadMessagesAdapter extends RecyclerView.Adapter<ThreadMessagesAdapter.ThreadMessagesViewHolder> {

    private List<String> threadMessagesList;
    private LayoutInflater layoutInflater;
    //private ItemClickListener itemClickListener; //we should not need this here

    //data is passed into the constructor
    public ThreadMessagesAdapter(List<String> threadMessagesList) {
        //this.threadMessagesList.addAll(threadMessagesList);
        this.threadMessagesList = threadMessagesList;
    }

    //inflates the row layout from xml when needed
    @NonNull
    @Override
    public ThreadMessagesAdapter.ThreadMessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listview_row, parent, false);

        ThreadMessagesViewHolder viewHolder = new ThreadMessagesViewHolder(view);
        return viewHolder;
    }

    //binds the data to the Textview in each row
    @Override
    public void onBindViewHolder(@NonNull ThreadMessagesAdapter.ThreadMessagesViewHolder viewHolder, int position) {
        //get element from the dataset
        //replace the contents of the view with that element
        String threadMessage = threadMessagesList.get(position);
        viewHolder.threadMessage.setText(threadMessage);
    }

    //total number of rows
    @Override
    public int getItemCount() {
        return threadMessagesList.size();
    }

    //convenience method for getting data at click position
    String getItem(int id) {
        return threadMessagesList.get(id);
    }

    //allows click events to be caught
    //we should not need this
//    void setClickListener(ItemClickListener itemClickListener) {
//        this.itemClickListener = itemClickListener;
//    }

    //stores and recycles view as they are scrolled off screen
    //does not need to implement onclicklistener I think
    public class ThreadMessagesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView threadMessage;

        public ThreadMessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            threadMessage = itemView.findViewById(R.id.textviewThreadName); //this is from listview_row used for ThreadLists
            //itemView.setOnClickListener(this); //we don't need this probably
        }

        @Override
        public void onClick(View v) {
            //if (itemClickLister != null) itemClickLister.onItemClick(v, getAdapterPosition());
        }
    }

    //parent activity will implement this method to respond to click events
    //we do not need this here
//    public interface ItemClickListener {
//        void onItemClick(View view, int position);
//    }


}
