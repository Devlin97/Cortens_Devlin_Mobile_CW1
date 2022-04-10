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

    //In order to filter the arraylists the current items list will be stored as a global variable so it can be changed when it gets filtered
    private ArrayList<Item> fullList;

    //Another arrayList will be created which is used to check the full list when filtering is being done,
    //and then updating the other list with the results from this list
    private ArrayList<Item> fullListForFilter;

    public ItemAdapter(@NonNull Context context, int resource, ArrayList<Item> itemsList) {
        super(context, resource, itemsList);
        mContext = context;
        mResource = resource;

        //set the global arraylists to the list which will be passed in (itemList)
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
        startDateText.setText("From: " + startDate);
        endDateText.setText("Until: " + endDate);

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

    //overrided getFilter function which will return the custom titleFilter to filter the lists results
    @NonNull
    @Override
    public Filter getFilter() {
        //return super.getFilter();
        return titleFilter;
    }

    //since custom filtering is happening getCount() needs to be overridden in order to
    //get the count from the internal global arrayList which has been filtered instead of the normal super.size();
    @Override
    public int getCount() {
        return fullList.size();
    }

    //same reason as getCount(), this needs to be overidden to get the item for the list which is being filtered
    @Nullable
    @Override
    public Item getItem(int position) {
        return fullList.get(position);
    }

    //notifies that the data set has been changed
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    //custom filtering which is being returned by the overridden getFilter() class
    private Filter titleFilter = new Filter() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            //this method gets called when the adapters getFilter() class is classes
            //this is what holds the logic of the filtering

            //create a new arrayList to hold the all the items which satisfy the filter conditions
            ArrayList<Item> filterList = new ArrayList<>();

            //if the charSequence (character sequence which is passed in) is null or 0 characters long this list will add the
            //entire contents of the fullListForFilter global arrayList
            //this means the full list is visible when nothing is getting searched
            if(charSequence == null || charSequence.length() == 0)
            {
                filterList.addAll(fullListForFilter);
            }
            //else will get called when the the character sequence is not null or 0
            //which means something has been entered and is being searched
            //so this else holds the logic for the filtering
            else
            {
                //as everything that is to be searched in item is held as a string, not a CharSequence
                //the CharSequence needs to be converted to a string first
                //it will also be changed to lowercase and have the space trimmed off the ends
                //this is basic error handling so entering a character in the wrong case doesn't ruin the search
                String filteredString = charSequence.toString().toLowerCase().trim();

                //for each item in the global list which holds every single roadwork
                for(Item item : fullListForFilter) {
                    //if that current items title contains the string which is currently being searched
                    //OR if the current items start date contains the string which is being searched

                    //Note: parseDescription is an internal item method which finds the start and end date in the description
                    //parseDescription[0] returns the start date
                    if (item.getTitle().toLowerCase().contains(filteredString) || item.parseDescription(item.getDescription())[0].toLowerCase().contains(filteredString)) {
                        //if that item's title or start date does contain the searched string then add that to the list
                        //which is holding all the filtered items
                        filterList.add(item);
                    }
                }
            }
            //create a new FilterResult as this is what is passed into the publishResults method
            FilterResults filterResults = new FilterResults();

            //add the list which holds all the filters to the filterResult's .values
            filterResults.values = filterList;

            //return that filterResult, and it will now be used by publishResults
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            //fullList is the global list which is dictates the contents of this adapter that will be displayed in the list view
            //so now it needs set to filteredList which holds all the items that satisfy the search condition
            //that filtered list is stored in the filterResults.values, so take that and parse it as an ArrayList of items
            fullList = (ArrayList<Item>) filterResults.values;

            //now notify the adapter that the dataset has been changed so it can update
            notifyDataSetChanged();
        }
    };
}
