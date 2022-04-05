//DEVLIN CORTENS
//S1825992

package org.me.gcu.devlin_cortens_cw1_mobile_dev;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class ItemAdapter extends ArrayAdapter<Item> {
    private Context mContext;
    int mResource;

    public ItemAdapter(@NonNull Context context, int resource, ArrayList<Item> itemsList) {
        super(context, resource, itemsList);
        mContext = context;
        mResource = resource;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        System.out.println("IN get view");

        String title = getItem(position).getTitle();
        String startDate = getItem(position).parseDescription(getItem(position).getDescription())[0];
        String endDate = getItem(position).parseDescription(getItem(position).getDescription())[1];

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView titleText = (TextView) convertView.findViewById(R.id.itemTitleText);
        TextView startDateText = (TextView) convertView.findViewById(R.id.startDateText);
        TextView endDateText = (TextView) convertView.findViewById(R.id.endDateText);

        titleText.setText(title);
        startDateText.setText(startDate);
        endDateText.setText(endDate);

        if(getItem(position).getHours() <= 24)
        {
            convertView.setBackgroundColor(Color.GREEN);
        }
        else if(getItem(position).getHours() <= 120)
        {
            convertView.setBackgroundColor(Color.YELLOW);
        }
        else
        {
            convertView.setBackgroundColor(Color.RED);
        }

        return convertView;
    }

}
