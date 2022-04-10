/**
//DEVLIN CORTENS
//S1825992
*/

package org.me.gcu.devlin_cortens_cw1_mobile_dev;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsDetailsFragment extends Fragment implements AdapterView.OnItemClickListener {

    private EditText searchEdit;
    private ListView parsedListView;
    private TextView titleText, descriptionText, linkText, pubDateText;
    private ArrayList<Item> itemsList;
    private ItemAdapter arrayAdapter;
    private GoogleMap mMap;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            //when the map initiates we want to make it a global variable so we can change it later
            //then we get the lat lon of scotland so the map centers on scotland zoomed out on europe
            mMap = googleMap;
            //initiates the map to have the small zoom in and out buttons on it
            mMap.getUiSettings().setZoomControlsEnabled(true);
            LatLng scotland = new LatLng(56.4907, 4.2026);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(scotland));
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_maps_details, container, false);

        //quick check to clear the itemsList in case it is still storing data from when it was last called
        if(itemsList != null && !itemsList.isEmpty()) {
            itemsList.clear();
        }

        //Grab the itemsList which has been passed to this fragment
        Bundle bundle = getArguments();
        itemsList = (ArrayList<Item>) bundle.getSerializable("ITEMLIST");

        //System.out.println(itemsList);


        //Set the arrayAdapter to the custom ItemAdapter class which uses the application context, the item list custom layout, and the the itemsList
        arrayAdapter = new ItemAdapter(getActivity().getApplicationContext(), R.layout.item_list, itemsList);
        //System.out.println(arrayAdapter);



        //Find all the editTexts, Textviews, and ListViews in the xml file by id to be accessed within this fragment
        searchEdit = (EditText) v.findViewById(R.id.searchEdit);
        parsedListView = (ListView) v.findViewById(R.id.parsedListView);
        titleText = (TextView) v.findViewById(R.id.titleText);
        descriptionText = (TextView) v.findViewById(R.id.descriptionText);
        linkText = (TextView) v.findViewById(R.id.linkText);
        pubDateText = (TextView) v.findViewById(R.id.pubDateText);

        //set the parsedList adapter to the array adapter, which is a custom ItemAdapter
        parsedListView.setAdapter(arrayAdapter);

        //set the onItemClickListener on the list to be able to click an item and see more details about it
        parsedListView.setOnItemClickListener(this);

        //When this fragment first gets called, the user needs to understand what it is they're looking at, and how to interact with this page.
        //This sets the title text to a message which lets the user know they have the ability to click an item on the list
        //and that items details will be shown in this white space below the list
        titleText.setText("Click an item on the list to see it on the map!");

        //text changed listener on the search edit
        //this allows a user to search the listview to find a specific roadwork by start date or road
        //this textchangedlistener gets called anytime text is entered or removed from the editText
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            //The text watcher allows for methods to be called during three different times of text being changed.
            //Before, During, and After.
            //for this specific search, searching should happen as the text is being entered
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //call the getFilter() method on the customer array adapter class
                //this has been changed within the custom adapter to search the list by the title
                //i.e. the road that work is being done on
                //OR by the start date of the roadworks
                //this passes in the character sequence that is currently being entered and the filtering logic is handled by the custom adapter
                arrayAdapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //arrayAdapter.getFilter().filter(searchEdit.getText().toString().trim());
            }
        });


        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //get the item from that index that we clicked in the listview
        Item itemToDisplay = arrayAdapter.getItem(i);

        //now we have that item, set all the textviews to the various parts of the item
        titleText.setText(itemToDisplay.getTitle());
        descriptionText.setText(itemToDisplay.getDescription());
        linkText.setText(itemToDisplay.getLink());
        pubDateText.setText("Published: " + itemToDisplay.getPubDate().toString());

        //Time to parse the georss point into latitude and longitude
        //Grab the georss point
        String geoPoint = itemToDisplay.getGeorsspoint();

        //Find the space which separates the latitude and longitude
        //Then set the latitude to the first numbers
        //Set the longitude to the second set of numbers
        int breakPoint = geoPoint.indexOf(" ");
        double latitude = Double.parseDouble(geoPoint.substring(0, breakPoint));
        double longitude = Double.parseDouble(geoPoint.substring(breakPoint + 1, geoPoint.length()));
        LatLng location = new LatLng(latitude, longitude);

        //First we clear the map to get rid of all the markers
        //We do this so if we press multiple items the map doesnt have multiple markers on it
        mMap.clear();

        //Add a marker where that new latitude and longitude is from parsing the georss point
        mMap.addMarker(new MarkerOptions().position(location).title("Roadwork Location"));

        //Move the camera to the marker
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));

        //Animate the camera to the location
        //Without this the camera is zoomed out incredibly far away
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));
    }
}