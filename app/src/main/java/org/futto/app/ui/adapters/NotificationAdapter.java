package org.futto.app.ui.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.futto.app.R;
import org.futto.app.nosql.NotificationDO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by sharonyu on 2018/3/30.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

// Provide a direct reference to each of the views within a data item
// Used to cache the views within the item layout for fast access
    // ... view holder defined above...

    // Store a member variable for the contacts
    private List<NotificationDO> notifications;
    // Store the context for easy access
    private Context context;


    // Pass in the contact array into the constructor
    public NotificationAdapter(Context context, List<NotificationDO> notis) {
        this.notifications = notis;
        this.context = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return context;
    }

    // Define listener member variable
    private OnItemClickListener listener;
    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }
    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.noti_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(NotificationAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        NotificationDO notification = notifications.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.titleTextView;
        textView.setText(notification.getTitle());
        TextView contentView = viewHolder.contentTextView;
        contentView.setText(notification.getContent());
        TextView dateTextView = viewHolder.dateTextView;
        dateTextView.setText(getFormatedDate(notification.getCreationDate()));

    }

    private String getFormatedDate(Double milliseconds ){
        Long newMilliseconds = new Long(milliseconds.longValue());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm");
        Date resultdate = new Date(newMilliseconds);
        return sdf.format(resultdate);
    }


    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return notifications.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView titleTextView;
        public TextView contentTextView;
        public ImageView couponPic;
        public TextView dateTextView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            titleTextView = (TextView) itemView.findViewById(R.id.titleText);
            contentTextView= (TextView) itemView.findViewById(R.id.contentText);
//            ratingText = (TextView) itemView.findViewById(R.id.ratingText);
            couponPic = (ImageView)itemView.findViewById(R.id.couponPic);
            dateTextView =  (TextView) itemView.findViewById(R.id.pubDateText);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });

        }

    }
}