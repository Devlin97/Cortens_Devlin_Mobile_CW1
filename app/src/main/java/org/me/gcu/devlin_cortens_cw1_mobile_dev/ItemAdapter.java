//DEVLIN CORTENS
//S1825992

package org.me.gcu.devlin_cortens_cw1_mobile_dev;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

public class ItemAdapter extends ArrayAdapter<Item> implements Filterable {
    private Context mContext;
    int mResource;

    private ArrayList<Item> fullList;
    private ArrayList<Item> fullListForFilter;

    public ItemAdapter(@NonNull Context context, int resource, ArrayList<Item> itemsList) {
        super(context, resource, itemsList);
        mContext = context;
        mResource = resource;

        fullList = new ArrayList<>(itemsList);
        fullListForFilter = new ArrayList<>(itemsList);
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
        //return super.getFilter();
        return titleFilter;
    }

    @Override
    public int getCount() {
        return fullList.size();
    }

    @Nullable
    @Override
    public Item getItem(int position) {
        return fullList.get(position);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    private Filter titleFilter = new Filter() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<Item> filterList = new ArrayList<>();

            if(charSequence == null || charSequence.length() == 0)
            {
                filterList.addAll(fullListForFilter);
            }
            else
            {
                String filteredString = charSequence.toString().toLowerCase().trim();

                for(Item item : fullListForFilter) {
                    if (item.getTitle().toLowerCase().contains(filteredString) || item.parseDescription(item.getDescription())[0].toLowerCase().contains(filteredString)) {
                        System.out.println(item.getTitle());
                        filterList.add(item);
                    }
                }
            }
            System.out.println(filterList.size());
            FilterResults filterResults = new FilterResults();
            filterResults.values = filterList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            fullList = (ArrayList<Item>) filterResults.values;
            System.out.println(fullList.size());
            notifyDataSetChanged();
            Log.e("Checking publishing", "In the method where data set has been notified");
        }
    };
}
