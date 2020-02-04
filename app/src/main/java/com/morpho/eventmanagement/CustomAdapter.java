package com.morpho.eventmanagement;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class CustomAdapter  extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private List<Attendance> mData = new ArrayList<Attendance>();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    Context context;

    // data is passed into the constructor
    public CustomAdapter(Context context, List<Attendance> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.attendees_details, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(position %2 == 1)
        {
            holder.itemView.setBackgroundColor(Color.parseColor("#aaa7a7"));
        }
        else
        {
            holder.itemView.setBackgroundColor(Color.parseColor("#e8e6e6"));
        }
        holder.VIP.setText(mData.get(position).getVip());
        holder.Name.setText(mData.get(position).getTitle()+" "+mData.get(position).getFirst_name()+" "+mData.get(position).getLast_name());
        holder.Attended.setText(mData.get(position).getAtt_Flag());
        holder.Date.setText(mData.get(position).getDate());
        holder.Time.setText(mData.get(position).getTime());

        Collections.sort(mData, new Comparator<Attendance>() {
            @Override
            public int compare(Attendance o1, Attendance o2) {
                return o1.getAtt_Flag().compareTo(o2.getAtt_Flag());
            }
        });

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView VIP,Name,Attended,Date,Time;
        ViewHolder(View itemView) {
            super(itemView);
            VIP = itemView.findViewById(R.id.VIP);
            Name = itemView.findViewById(R.id.Name);
            Attended = itemView.findViewById(R.id.Attend);
            Date = itemView.findViewById(R.id.dateText);
            Time = itemView.findViewById(R.id.timeText);

        }

    }




    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onSelectViolation(List<Integer> SelectedItems,int position);
    }
}
