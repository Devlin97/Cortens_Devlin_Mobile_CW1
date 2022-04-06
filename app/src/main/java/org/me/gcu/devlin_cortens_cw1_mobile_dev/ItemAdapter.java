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
import android.widget.Filter;
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

    //We need this custom array adapter to get hold of this method
    //This allows us to us the custom layout we have created and colour each item in the list based on the duration of the roadworks
    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //For each item in the list we only need the title, start date, and end date
        //We grab these specific details from each item
        //We get the start date and end date from the internal method in Item which parses the description to find them
        String title = getItem(position).getTitle();
        String startDate = getItem(position).parseDescription(getItem(position).getDescription())[0];
        String endDate = getItem(position).parseDescription(getItem(position).getDescription())[1];

        //Inflate each of these views from our layout into the listview
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        //This is kind of the same as how we initiate textviews, buttons, etc in our main activity
        //We find these textviews in our layout resource file and initiate them
        TextView titleText = (TextView) convertView.findViewById(R.id.itemTitleText);
        TextView startDateText = (TextView) convertView.findViewById(R.id.startDateText);
        TextView endDateText = (TextView) convertView.findViewById(R.id.endDateText);

        //Set text of each of these textviews to be what we pulled earlier of this specific item in the beginning of this method
        titleText.setText(title);
        startDateText.setText(startDate);
        endDateText.setText(endDate);

        //This is the logic to colour each of the individual items within the list
        //An internal method in item already parses the description to find out how long each roadwork will last
        //It is saved in each item as "hours"
        //We get these hours and set colours accordingly

        //If this specific roadwork is less than 24 hours long, it will be coloured green
        if(getItem(position).getHours() <= 24)
        {
            //set the background colour to green
            convertView.setBackgroundColor(Color.GREEN);
        }
        //If the roadwork is equal to or less then 5 days (120 hours) long, it will be coloured yellow
        else if(getItem(position).getHours() <= 120)
        {
            //set the background colour to yellow
            convertView.setBackgroundColor(Color.YELLOW);
        }
        //If the duration is anything else, then this roadwork is longer than 5 days
        //This means its really really long so we will colour it red
        else
        {
            //set background colour to red
            convertView.setBackgroundColor(Color.RED);
        }

        //return the view
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return super.getFilter();
    }
}
