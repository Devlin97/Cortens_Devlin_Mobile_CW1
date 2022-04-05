//DEVLIN CORTENS
//S1825992

package org.me.gcu.devlin_cortens_cw1_mobile_dev;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Navigation Drawer
    private DrawerLayout drawer;

    // Traffic Scotland Planned Roadworks XML link
    //Planned Roadoworks
    private String urlPlanned="https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
    //Roadworks Link
    private String urlRoadworks = "https://trafficscotland.org/rss/feeds/roadworks.aspx";
    //Current Incidents Link
    private String urlIncidents = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";
    //String to initiate the the result which is the string that gets pulled from the XML feed
    private String result = "";

    //Initiate our arrayList of items.
    //Since many functions will use this list we need it to be a global variable
    ArrayList<Item> itemsList = new ArrayList<Item>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //This make sure that this only happens when the app first gets launched
        if(savedInstanceState == null) {
            //This opens the home page when we start the app
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_view, new HomeFragment()).commit();
            //Once the app opens and the home page is open we want to make sure the home nav button is selected in our nav drawer
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    //When the back button is pressed when the drawer is open we want the drawer to close so we need to override the onBackPressed function
    public void onBackPressed() {
        //if the drawer is open when the back button is pressed the drawer will close
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        //in any other case (i.e. the drawer is closed) behave as normal
        else {
            super.onBackPressed();
        }
    }

    //On click for the navigation slide drawer. Each click starts a new Thread which parses a feed
    //And then opens the fragment for that thread.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.nav_roadworks) {
            //Call the function to start parsing that specific feed
            startPlannedRoadworksParse();

            //Have long toast message pop up so the user understands something is happening behind the scenes and their feed will appear soon
            Toast.makeText(this, "Opening Roadworks...", Toast.LENGTH_LONG).show();

            Log.e("MyTag", "Starting Roadworks Parse");
        }
        if(item.getItemId() == R.id.nav_planned_roadworks) {
            //Call the function to start parsing that specific feed
            startRoadworksParse();

            //Have long toast message pop up so the user understands something is happening behind the scenes and their feed will appear soon
            Toast.makeText(this, "Opening Planned Roadworks...", Toast.LENGTH_LONG).show();

            Log.e("MyTag", "Starting Planned Roadworks Parse");
        }
        if(item.getItemId() == R.id.nav_incidents) {
            //Call the function to start parsing that specific feed
            startCurrentIncidentsParse();

            //Have long toast message pop up so the user understands something is happening behind the scenes and their feed will appear soon
            Toast.makeText(this, "Opening Current Incidents...", Toast.LENGTH_LONG).show();

            Log.e("MyTag", "Starting current incidents parse");
        }
        if(item.getItemId() == R.id.nav_home) {
            //when we click on home return to home
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_view, new HomeFragment()).commit();
        }

        //When we click on a page from the navigation slider, we want the drawer to close again and not stay open
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //We call this function from our on click which starts the thread to parse the current incidents feed
    public void startCurrentIncidentsParse() {
        //Clear the arraylist to so the arraylist doesn't contain any items from old parses
        itemsList.clear();

        //We need to set the result back to an empty string when we start a new thread or else
        //when we start the new string it will keep adding on to the string from the previously called thread
        result = "";

        //start the thread which takes in the url for current incidents
        new Thread(new CurrentIncidentTask(urlIncidents)).start();
    }

    //We call this function from our on click which starts the thread to parse the planned roadworks feed
    public void startPlannedRoadworksParse() {
        //Clear the arraylist to so the arraylist doesn't contain any items from old parses
        itemsList.clear();

        //We need to set the result back to an empty string when we start a new thread or else
        //when we start the new string it will keep adding on to the string from the previously called thread
        result = "";

        //start the thread which takes in the url for planned roadworks
        new Thread(new Task(urlPlanned)).start();
    }

    //We call this function from our on click which starts the thread to parse the current roadworks feed
    public void startRoadworksParse() {
        //Clear the arraylist to so the arraylist doesn't contain any items from old parses
        itemsList.clear();

        //We need to set the result back to an empty string when we start a new thread or else
        //when we start the new string it will keep adding on to the string from the previously called thread
        result = "";

        //start the thread which takes in the url for current roadworks
        new Thread(new Task(urlRoadworks)).start();
    }


    //======================================Roadworks thread and parsing functions=================================

    private class Task implements Runnable
    {
        private String url;

        public Task(String aurl)
        {
            url = aurl;
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run()
        {

            URL aurl;
            URLConnection yc;
            BufferedReader in = null;
            String inputLine = "";


            try
            {
                aurl = new URL(url);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

                while ((inputLine = in.readLine()) != null)
                {
                    result = result + inputLine;
                }
                in.close();
            }
            catch (IOException ae)
            {
                Log.e("MyTag", "ioexception in run");
            }

            //Parse the roadworks data
            //This function works for both the planned roadworks and the current roadworks
            parseDataRoadworks(result);

            MainActivity.this.runOnUiThread(new Runnable()
            {
                public void run() {
                    //We need to ship our itemsList to the fragment so the fragment can use it to populate the listview etc.
                    //So we start a new transaction
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    MapsDetailsFragment mapFragment = new MapsDetailsFragment();

                    //Create a new bundle which we will use to pass our list to our fragment
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("ITEMLIST", itemsList);

                    //Set the arguments of our fragment to bundle we created with our list
                    mapFragment.setArguments(bundle);

                    //Tell the activity we are swapping the frameview with our fragment
                    fragmentTransaction.replace(R.id.fragment_view, mapFragment);
                    fragmentTransaction.addToBackStack(null);

                    //Commit the fragment to the frameview
                    fragmentTransaction.commit();
                }
            });
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void parseDataRoadworks(String dataToParse) {

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(dataToParse));
            int eventType = xpp.getEventType();
            boolean insideOfItem = false;
            String tempTitle = "";
            String tempDescription = "";
            String tempLink = "";
            String tempGeorsspoint = "";
            String tempAuthor = "";
            String tempComments = "";
            String tempPubDate = "";
            LocalDate tempPubDateToDate = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {

                        insideOfItem = true;

                    } else if (xpp.getName().equalsIgnoreCase("title")) {
                        if (insideOfItem) {
                            tempTitle = xpp.nextText();
                        }
                    } else if (xpp.getName().equalsIgnoreCase("description")) {
                        if (insideOfItem) {
                            tempDescription = xpp.nextText();

                            //The description of each item usually has 1 or <br /> tags
                            //This would probably be useful is this was a web app but this has no use in this application
                            //So we'll replace all break tags with a space instead
                            tempDescription = tempDescription.replace("<br />", " ");
                        }
                    } else if (xpp.getName().equalsIgnoreCase("link")) {
                        if (insideOfItem) {
                            tempLink = xpp.nextText();
                        }
                    } else if (xpp.getName().equalsIgnoreCase("point")) {
                        if (insideOfItem) {
                            tempGeorsspoint = xpp.nextText();
                        }
                    } else if (xpp.getName().equalsIgnoreCase("author")) {
                        if (insideOfItem) {
                            tempAuthor = xpp.nextText();
                        }
                    } else if (xpp.getName().equalsIgnoreCase("comments")) {
                        if (insideOfItem) {
                            tempComments = xpp.nextText();
                        }
                    } else if (xpp.getName().equalsIgnoreCase("pubdate")) {
                        if (insideOfItem) {
                            tempPubDate = xpp.nextText();

                            //In the xml feed each of the publication dates has a time, and its always 00:00
                            //This is redundant and not needed so i'm going to trim it off the end of each item
                            int dateTrim = tempPubDate.indexOf("00:00");
                            tempPubDate = tempPubDate.substring(0, dateTrim - 1);

                            //Since the date is stored as a string I want it converted to a date time
                            //This date time formatter formats the specific type of string into a LocalDate
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH);
                            LocalDate date = LocalDate.parse(tempPubDate, formatter);
                            tempPubDateToDate = date;
                        }
                    }

                } else if (eventType == XmlPullParser.END_TAG
                        && xpp.getName().equalsIgnoreCase("item")) {
                    insideOfItem = false;

                    //No we add all these temporary item information attributes into a new item
                    //Even though the xml feed doesn't have an author or comments for any of the items we will add them to this constructor
                    //This is to make the constructor different from the current incidents constructor

                    //In this constructor we will call a function which parses the description to calculate how long each roadwork will take
                    //The current incident feed doesn't have this information so we can't call that function to parse it
                    //So we add the author and comments to differentiate the constructors

                    //public Item(String titleIn, String desIn, String linkIn, String geoIn,
                    // String authorIn, String commentsIn, String pubDateIn)
                    Item newItem = new Item(tempTitle, tempDescription, tempLink, tempGeorsspoint, tempAuthor, tempComments, tempPubDateToDate);

                    //Now push that new item into our global ArrayList.
                    itemsList.add(newItem);


                }
                eventType = xpp.next();

            }
        }//End try
        catch (XmlPullParserException err)
        {
            Log.e("MyTag","Parsing failed. Reason: " + err.toString());
        }
        catch (IOException err)
        {
            Log.e("MyTag","IO Error.");
        }


    }//End parseData

    //=============================================================================================================================

    //================================ Current Incident Thread and Parsing Function ================================================

    private class CurrentIncidentTask implements Runnable
    {
        private String url;

        public CurrentIncidentTask(String aurl)
        {
            url = aurl;
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run()
        {

            URL aurl;
            URLConnection yc;
            BufferedReader in = null;
            String inputLine = "";


            Log.e("MyTag","in run");

            try
            {
                Log.e("MyTag","in try");
                aurl = new URL(url);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                Log.e("MyTag","after ready");
                //
                // Now read the data. Make sure that there are no specific hedrs
                // in the data file that you need to ignore.
                // The useful data that you need is in each of the item entries
                //
                while ((inputLine = in.readLine()) != null)
                {
                    result = result + inputLine;
                    //Log.e("MyTag",inputLine);

                }
                in.close();
            }
            catch (IOException ae)
            {
                Log.e("MyTag", "ioexception in run");
            }

            //Call the specific function that just parses the current incident feed

            //The incident feed is missing start and end dates in the description, so because of this we need a different function
            //that parses this feed and adds it to a different constructor that wont try and parse the description to find
            //the length between the start and end times.
            parseDataCurrentIncident(result);


            MainActivity.this.runOnUiThread(new Runnable()
            {
                public void run() {
                    //We need to ship our itemsList to the fragment so the fragment can use it to populate the listview etc.
                    //So we start a new transaction
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    MapDetailsIncidentsFragment mapFragment = new MapDetailsIncidentsFragment();

                    //Create a new bundle which we will use to pass our list to our fragment
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("ITEMLIST", itemsList);

                    //Set the arguments of our fragment to bundle we created with our list
                    mapFragment.setArguments(bundle);

                    //Tell the activity we are swapping the frameview with our fragment
                    fragmentTransaction.replace(R.id.fragment_view, mapFragment);
                    fragmentTransaction.addToBackStack(null);

                    //Commit the fragment to the frameview
                    fragmentTransaction.commit();
                }
            });
        }

    }//End current incident task

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void parseDataCurrentIncident(String dataToParse) {

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(dataToParse));
            int eventType = xpp.getEventType();
            boolean insideOfItem = false;
            String tempTitle = "";
            String tempDescription = "";
            String tempLink = "";
            String tempGeorsspoint = "";
            String tempAuthor = "";
            String tempComments = "";
            String tempPubDate = "";
            LocalDate tempPubDateToDate = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {

                        insideOfItem = true;

                    } else if (xpp.getName().equalsIgnoreCase("title")) {
                        if (insideOfItem) {
                            tempTitle = xpp.nextText();
                        }
                    } else if (xpp.getName().equalsIgnoreCase("description")) {
                        if (insideOfItem) {
                            tempDescription = xpp.nextText();
                        }
                    } else if (xpp.getName().equalsIgnoreCase("link")) {
                        if (insideOfItem) {
                            tempLink = xpp.nextText();
                            //Log.e("Cheking", tempLink);
                        }
                    } else if (xpp.getName().equalsIgnoreCase("point")) {
                        if (insideOfItem) {
                            tempGeorsspoint = xpp.nextText();
                            //Log.e("Cheking", tempGeorsspoint);
                        }
                    } else if (xpp.getName().equalsIgnoreCase("author")) {
                        if (insideOfItem) {
                            tempAuthor = xpp.nextText();
                        }
                    } else if (xpp.getName().equalsIgnoreCase("comments")) {
                        if (insideOfItem) {
                            tempComments = xpp.nextText();
                        }
                    } else if (xpp.getName().equalsIgnoreCase("pubdate")) {
                        if (insideOfItem) {
                            tempPubDate = xpp.nextText();

//                            int dateTrim = tempPubDate.indexOf("00:00");
//                            tempPubDate = tempPubDate.substring(0, dateTrim - 1);
                            //Log.e("Checking" , tempPubDate);


                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
                            LocalDate date = LocalDate.parse(tempPubDate, formatter);
                            //Log.e("Checking date ", date.toString());
                            tempPubDateToDate = date;
                        }
                    }

                } else if (eventType == XmlPullParser.END_TAG
                        && xpp.getName().equalsIgnoreCase("item")) {
                    insideOfItem = false;

                    //Creating a new Item object, this constructor takes in everything bar the author and comments

                    //Because it doesn't take these in the item wont call the internal functions
                    //that parse the start and end time of the roadworks to find the duration.

                    //We don't do this because current incidents don't have a set start and end time

                    //Item(String titleIn, String desIn, String linkIn, String geoIn, LocalDate pubDateIn)
                    Item newItem = new Item(tempTitle, tempDescription, tempLink, tempGeorsspoint, tempPubDateToDate);

                    //Now push this item to our global arraylist
                    itemsList.add(newItem);


                }
                eventType = xpp.next();

            }
        }//End try
        catch (XmlPullParserException err)
        {
            Log.e("MyTag","Parsing failed. Reason: " + err.toString());
        }
        catch (IOException err)
        {
            Log.e("MyTag","IO Error.");
        }


    }//End parseData

    //===================================================================================================================
}