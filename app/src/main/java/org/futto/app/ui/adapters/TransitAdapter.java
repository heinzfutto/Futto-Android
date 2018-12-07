package org.futto.app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.futto.app.R;
import org.futto.app.ui.handlers.Routes;

import java.util.List;

/**
 * Created by devsen on 3/25/18.
 */

public class TransitAdapter extends ArrayAdapter<Routes> {

    private final Context context;
    private final List<Routes> values;

    public TransitAdapter(Context context, List<Routes> values) {
        super(context, R.layout.row_layout, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_layout, parent, false);
        Routes row = values.get(position);
        TextView tvTitle = (TextView) rowView.findViewById(R.id.tvTitle);
        TextView tvName = (TextView) rowView.findViewById(R.id.tvName);
        TextView tvDuration = (TextView) rowView.findViewById(R.id.tvDuration);
        TextView tvFare = (TextView) rowView.findViewById(R.id.tvFare);

        tvTitle.setText(row.getTitle());
        tvName.setText(row.getName());
        tvDuration.setText(String.format("%.2f", row.getDuration() / 60) + " mins");
        tvFare.setText(row.getFare() + " $");
        return rowView;
    }
}
