/*DEVLIN CORTENS
S1825992 */

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
import android.widget.ArrayAdapter;
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

public class MapDetailsIncidentsFragment extends Fragment implements AdapterView.OnItemClickListener {

    //Declare all the different xml elements declared in the xml file in order to get access to and use them
    private EditText searchEdit;
    private ListView parsedListView;
    private TextView titleText, descriptionText, linkText, pubDateText;

    //Global instance of the itemsList to use it in various methods
    private ArrayList<Item> itemsList;

    //Global instance of the basic arrayAdapter to use it in various methods
    private ArrayAdapter arrayAdapter;

    //Global instance of the google map to use it in various methods
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
            //one the map is initialised use set it as the global map variable to use it in various methods
            mMap = googleMap;

            //Set the map to we have the wee zoom buttons in the bottom corner
            mMap.getUiSettings().setZoomControlsEnabled(true);

            //This is the latitude and longitude of scotland, we need this so when the map first shows without anything clicked
            //the map is zoomed out over europe but centered on scotland
            LatLng scotland = new LatLng(56.4907, 4.2026);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(scotland));
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_details_incidents, container, false);

        //Quick check to see if the itemsList is empty
        //This is error handling in case if this fragment is called twice the itemsList wont carry over and have the data twice
        if(itemsList != null && !itemsList.isEmpty()) {
            itemsList.clear();
        }

        //Get the itemsList that was sent in a bundle from the main activity
        Bundle bundle = getArguments();
        itemsList = (ArrayList<Item>) bundle.getSerializable("ITEMLIST");

        //Set the itemsList to the arrayAdapter so it can be used in the listview
        //Since current incidents are a little bit more basic only the title wil be displayed
        //so because of this the basic arrayAdapter will be used instead of the custom ItemAdapter
        arrayAdapter = new ArrayAdapter(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, itemsList);

        //Find the global textviews, listview, and edittext variables in the xml document by their given id, so they can be used in this fragment
        searchEdit = (EditText) v.findViewById(R.id.searchEdit);
        parsedListView = (ListView) v.findViewById(R.id.parsedListView);
        titleText = (TextView) v.findViewById(R.id.titleText);
        descriptionText = (TextView) v.findViewById(R.id.descriptionText);
        linkText = (TextView) v.findViewById(R.id.linkText);
        pubDateText = (TextView) v.findViewById(R.id.pubDateText);

        //Set the adapter for the listview to be the globaladapter used on the itemsList
        parsedListView.setAdapter(arrayAdapter);

        //Set on click listener for the for the listview so its details can be shown when it is clicked
        parsedListView.setOnItemClickListener(this);

        //When the user first opens this fragment they may be slightly confused about the functionality and what they can do
        //This message appears only when the fragment is first launched so the user understands what is happening and how to interact with this page
        titleText.setText("Click an item on the list to see it on the map!");

        //Set a text changed listener to the edit text so the listview can be searched by title
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //For this searching method it will call on the arrayadapters get filter method
                //which searches the items toString()
                //current incidents toString() is the title, so the title will be searched
                arrayAdapter.getFilter().filter(searchEdit.getText().toString().trim());
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
        Item itemToDisplay = (Item) arrayAdapter.getItem(i);

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

        //Add a marker where that new latitude and longitude is from parsing our georss point
        mMap.addMarker(new MarkerOptions().position(location).title("Roadwork Location"));

        //Move the camera to the marker
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));

        //Animate the camera to the location
        //Without this the camera is zoomed out incredibly far away
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));
    }
}