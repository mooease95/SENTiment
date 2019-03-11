package com.example.thorac;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomListAdapter extends ArrayAdapter {

    //to reference the activity
    private final Activity context;

    //to store the threads
    private final String[] threadList;

    public CustomListAdapter(Activity context, String[] threadListParam) {
        super(context, R.layout.listview_row, threadListParam);

        this.context = context;
        this.threadList = threadListParam;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.listview_row, null, true);

        //this code gets references to objects in the listview_row.xml file
        TextView threadContactTextField = (TextView) rowView.findViewById(R.id.threadName);

        //this code sets the values of the objects to values of the array
        threadContactTextField.setText(threadList[position]);

        return rowView;
    }


}
