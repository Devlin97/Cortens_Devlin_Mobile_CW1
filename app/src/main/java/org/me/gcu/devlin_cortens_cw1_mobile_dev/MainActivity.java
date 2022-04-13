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

    // Traffic Scotland Roadworks / Current Incidents XML links
    //Better to set these as global variables as they're all in one place if the link ever changes
    //Planned Roadoworks
    private String urlPlanned="https://trafficscotland.org/rss/feeds/plannedroadworks.aspx";
    //Roadworks Link
    private String urlRoadworks = "https://trafficscotland.org/rss/feeds/roadworks.aspx";
    //Current Incidents Link
    private String urlIncidents = "https://trafficscotland.org/rss/feeds/currentincidents.aspx";

    //Initiate the arrayList of items.
    //Since many functions will use this list it needs to be a global variable
    ArrayList<Item> itemsList = new ArrayList<Item>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find the toolbar and set the supportAction
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Find the navigation drawer by id in the xml file so it can be used as a navigation drawer
        drawer = findViewById(R.id.drawer_layout);

        //Find the navigation view in the xml file and set a nav item selected listener to it
        //This listener will be what swaps the fragments inside this main activity
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Set a new actionBarDrawerToggle and set a drawer listener to it
        //This is the hamburger icon that appears in the top left which basically allows the drawer to open
        //by clicking on it instead of swiping from the right of the screen
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //This make sure that this only happens when the app first gets launched
        if(savedInstanceState == null) {
            //This opens the home page when the app is started
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
            startRoadworksParse();

            //Have long toast message pop up so the user understands something is happening behind the scenes and their feed will appear soon
            Toast.makeText(this, "Opening Roadworks...", Toast.LENGTH_LONG).show();
        }
        if(item.getItemId() == R.id.nav_planned_roadworks) {
            //Call the function to start parsing that specific feed
            startPlannedRoadworksParse();

            //Have long toast message pop up so the user understands something is happening behind the scenes and their feed will appear soon
            Toast.makeText(this, "Opening Planned Roadworks...", Toast.LENGTH_LONG).show();
        }
        if(item.getItemId() == R.id.nav_incidents) {
            //Call the function to start parsing that specific feed
            startCurrentIncidentsParse();

            //Have long toast message pop up so the user understands something is happening behind the scenes and their feed will appear soon
            Toast.makeText(this, "Opening Current Incidents...", Toast.LENGTH_LONG).show();
        }
        if(item.getItemId() == R.id.nav_home) {
            //when user clicks on home return to home
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_view, new HomeFragment()).commit();
        }

        //When user clicks on a page from the navigation slider, we want the drawer to close again and not stay open
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //This function is called from our on click which starts the thread to parse the current incidents feed
    public void startCurrentIncidentsParse() {
        //Clear the arraylist to so the arraylist doesn't contain any items from old parses
        itemsList.clear();

        //start the thread which takes in the url for current incidents
        new Thread(new CurrentIncidentTask(urlIncidents)).start();
    }

    //This function is called from our on click which starts the thread to parse the planned roadworks feed
    public void startPlannedRoadworksParse() {
        //Clear the arraylist to so the arraylist doesn't contain any items from old parses
        itemsList.clear();

        //start the thread which takes in the url for planned roadworks
        new Thread(new Task(urlPlanned)).start();
    }

    //This function is called from our on click which starts the thread to parse the current roadworks feed
    public void startRoadworksParse() {
        //Clear the arraylist to so the arraylist doesn't contain any items from old parses
        itemsList.clear();

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

            //String to store the result of all the text grabbed from the xml feed
            //This will be a large xml document stored as a string from where the xml pull parser can go through it
            String result = "";


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

        //Try catch so if anything breaks it will be caught and not completely break the app
        try {
            //Initiate the xml pull parser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(dataToParse));
            int eventType = xpp.getEventType();

            //For the logic of parsing the data, a boolean value is here that checks the tag we are checking is inside of an item to get all the values for that item
            boolean insideOfItem = false;

            //Next temporary string are needed to be initiated
            //These will hold the data for each value between each tag before it is then put inside of a constructor to create said item
            String tempTitle = "";
            String tempDescription = "";
            String tempLink = "";
            String tempGeorsspoint = "";
            String tempAuthor = "";
            String tempComments = "";
            String tempPubDate = "";
            LocalDate tempPubDateToDate = null;

            //While the event type is not the end of the document start going through the xml feed
            while (eventType != XmlPullParser.END_DOCUMENT) {

                //When the xml pull parser finds a start tag it will go inside this if statement
                if (eventType == XmlPullParser.START_TAG) {
                    //If the start tag the item found is item (<item>) set the boolean value of insideOfItem to true so this method now knows
                    //it is inside of an item to get all related values for that said item
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        insideOfItem = true;
                    }
                    //else if that start tag is title (<title>) go inside this if
                    else if (xpp.getName().equalsIgnoreCase("title")) {
                        //if the start tag was title AND the method is also iterating over xml tags inside of an item
                        //get the text inside this title tag and set the tempTitle variable to it
                        if (insideOfItem) {
                            tempTitle = xpp.nextText();
                        }
                    }
                    //else if the start tag is description (<description>) go inside this if
                    else if (xpp.getName().equalsIgnoreCase("description")) {
                        //if the start tag was description AND the method is also iterating over xml tags inside of an item
                        //get the text inside this description tag and set the tempDescription variable to it
                        if (insideOfItem) {
                            tempDescription = xpp.nextText();

                            //The description of each item usually has 1 or more <br /> tags in it
                            //This would probably be useful is this was a web app but this has no use in this application
                            //So we'll replace all break tags with a space instead
                            tempDescription = tempDescription.replace("<br />", " ");
                        }
                    }
                    //else if the start tag is link (<link>) go inside this if
                    else if (xpp.getName().equalsIgnoreCase("link")) {
                        //if the start tag was link AND the method is also iterating over xml tags inside of an item
                        //get the text inside this link tag and set the tempLink variable to it
                        if (insideOfItem) {
                            tempLink = xpp.nextText();
                        }
                    }
                    //else if the start tag is point (<point>) go inside this if
                    //Note: the actual tag within the rss feed is <georss:point>, but the xml pull parser interprets this as <point> due to the ':' in the tag
                    else if (xpp.getName().equalsIgnoreCase("point")) {
                        //if the start tag was point AND the method is also iterating over xml tags inside of an item
                        //get the text inside this point tag and set the tempPoint variable to it
                        if (insideOfItem) {
                            //This georsspoint is a string containing a latitude and longitude divded by a space
                            //This will need to be parsed into numbers at some point in order to be used by google maps
                            //There a variety of different ways this could be parsed, including right here
                            //But for this application the georsspoint will only be parsed as it is called to show on a map
                            tempGeorsspoint = xpp.nextText();
                        }
                    }
                    //else if the start tag is author (<author>) go inside this if
                    else if (xpp.getName().equalsIgnoreCase("author")) {
                        //if the start tag was author AND the method is also iterating over xml tags inside of an item
                        //get the text inside this author tag and set the tempAuthor variable to it
                        if (insideOfItem) {
                            //Note: in the XML feed Author is never used by any item but is still included in the feed
                            //this will still be added as to differentiate the roadworks from the current incidents item constructor
                            //Leaving this in will also be helpful if in the future the traffic scotland roadworks feed start using this tag for more information
                            tempAuthor = xpp.nextText();
                        }
                    }
                    //else if the start tag is comments (<comments>) go inside this if
                    else if (xpp.getName().equalsIgnoreCase("comments")) {
                        //if the start tag was comments AND the method is also iterating over xml tags inside of an item
                        //get the text inside this comments tag and set the tempComments variable to it
                        if (insideOfItem) {
                            //Note: in the XML feed Comments is never used by any item but is still included in the feed
                            //this will still be added as to differentiate the roadworks from the current incidents item constructor
                            //Leaving this in will also be helpful if in the future the traffic scotland roadworks feed start using this tag for more information
                            tempComments = xpp.nextText();
                        }
                    }
                    //else if the start tag is pubdate (<pubdate>) go inside this if
                    else if (xpp.getName().equalsIgnoreCase("pubdate")) {
                        //if the start tag was pubdate AND the method is also iterating over xml tags inside of an item
                        //get the text inside this pubdate tag and set the tempPubDate variable to it
                        if (insideOfItem) {
                            tempPubDate = xpp.nextText();

                            //In the xml feed each of the publication dates have a time, and its always 00:00
                            //This is redundant and not needed so i'm going to trim it off the end of each item
                            int dateTrim = tempPubDate.indexOf("00:00");
                            tempPubDate = tempPubDate.substring(0, dateTrim - 1);

                            //Since the date is stored as a string I want it converted to a LocalDate
                            //This date time formatter formats the specific type of string into a LocalDate
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH);
                            LocalDate date = LocalDate.parse(tempPubDate, formatter);
                            tempPubDateToDate = date;
                        }
                    }
                    //Else if the event type is an end tag (</>) AND that end tag is item (</item>) go inside this if
                } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                    //The item end tag has been reach so we are no longer inside of an item
                    //So the boolean value checking if we are inside an item is set back to false
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
                //Check the next tag
                eventType = xpp.next();

            }
        }//End try
        catch (XmlPullParserException err)
        {
            Log.e("Error Parsing","Parsing failed. Reason: " + err.toString());
        }
        catch (IOException err)
        {
            Log.e("Error IO","IO Error.");
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

            //String to store the result of all the text grabbed from the xml feed
            //This will be a large xml document stored as a string from where the xml pull parser can go through it
            String result = "";

            try
            {
                aurl = new URL(url);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

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
            //Create a new xml pull parser factory to start parsing the string which is a big xml string
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(dataToParse));
            int eventType = xpp.getEventType();

            //The logic for this parse will be essentially the same as a roadworks, but with some slight differences
            //Still need the boolean variable to check if the current tag is inside of an item and to grab all the data for that item
            boolean insideOfItem = false;

            //Temporary strings to store all the information of a the item as its being looped over before they are put
            //into a current incidents item constructor
            String tempTitle = "";
            String tempDescription = "";
            String tempLink = "";
            String tempGeorsspoint = "";

            //For current incidents, just like roadworks, <author> and <comments> are not used
            //They are not included in a current incidents constructor at all as that is what is used to differentiate
            //if said item is a current incident or roadwork

            //But for this they will still be added here
            //As they are still included in the item tag, the could possibly be used at a later date if traffic scotland decide to start using these tags
            //Keeping these in will mean if traffic scotland start using these tags, all that needs to be done is changing the constructor to include these
            String tempAuthor = "";
            String tempComments = "";
            String tempPubDate = "";
            LocalDate tempPubDateToDate = null;

            //while the pullparser is not at the end of a document keep looping
            while (eventType != XmlPullParser.END_DOCUMENT) {

                //If a start tag (<>) is found go inside of this if
                if (eventType == XmlPullParser.START_TAG) {
                    //If the tag was a start and is also an item (<item>) go inside this
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        //Being in here means the pull parser is currently at the start of an item
                        //Now set the insideOfItem variable to true so the method knows it is currently inside of an item
                        //and while this is true grab everything inside of this item
                        insideOfItem = true;
                    }
                    //Else if this is currently a start tag being looked at and its name is title (<title>) go inside this if
                    else if (xpp.getName().equalsIgnoreCase("title")) {
                        //If this is a starting title tag AND the method is currently looking inside of an item, grab the title between the item tags
                        if (insideOfItem) {
                            tempTitle = xpp.nextText();
                        }
                    }
                    //else if this is a start tag and its name is description (<description>) go inside this if
                    else if (xpp.getName().equalsIgnoreCase("description")) {
                        //if this is a description tag AND the method is currently inside of an item, grab the description text between the description tags
                        if (insideOfItem) {
                            tempDescription = xpp.nextText();
                        }
                    }
                    //else if this is currently a start tag and its name is link (<link>) go inside this if
                    else if (xpp.getName().equalsIgnoreCase("link")) {
                        //if this a link tag AND the method is currently inside of an item grab the link text from between the link tags
                        if (insideOfItem) {
                            tempLink = xpp.nextText();
                        }
                    }
                    //else if this is currently a start tag and the name is (<point>) go inside this if
                    //Note: the actual tag within the rss feed is <georss:point>, but the xml pull parser interprets this as <point> due to the ':' in the tag
                    else if (xpp.getName().equalsIgnoreCase("point")) {
                        //if the tag was a point tag and the method is currently looking in an item grab the georsspoint
                        if (insideOfItem) {
                            //This georsspoint will be stored as a string here and then later parsed into numbers when this specific item is selected
                            tempGeorsspoint = xpp.nextText();
                        }
                    }
                    //else if this is currently a starting tag and the name is author (<author>) go inside this if
                    else if (xpp.getName().equalsIgnoreCase("author")) {
                        //if the tag was author and the method is currnetly looking inside of an item grab the author text
                        if (insideOfItem) {
                            //Note: This isn't used in the xml feed and is not included in the current incident constructor
                            //this will be left in as it is still included in the xml feed and could be possibly be used in the future
                            tempAuthor = xpp.nextText();
                        }
                    }
                    //else if this is currently a starting tag and the name is comments (<comments>) go inside this if
                    else if (xpp.getName().equalsIgnoreCase("comments")) {
                        //if the tag was comments and the method is currently looking inside of an item grab the comments text
                        if (insideOfItem) {
                            //Note: This isn't used in the xml feed and is not included in the current incident constructor
                            //this will be left in as it is still included in the xml feed and could be possibly be used in the future
                            tempComments = xpp.nextText();
                        }
                    }
                    //else if this is currently a starting tag and the name is pubdate (<pubdate>) go inside this if
                    else if (xpp.getName().equalsIgnoreCase("pubdate")) {
                        //if the name was pubdate and the method is currently looking inside of an item grab the text between the pubdate tags
                        if (insideOfItem) {
                            tempPubDate = xpp.nextText();

                            //the pubdate is currently stored as a string but it needs to be stored as a local date
                            //Format the string as a date so it is more usable
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
                            LocalDate date = LocalDate.parse(tempPubDate, formatter);

                            tempPubDateToDate = date;
                        }
                    }

                }
                //Else if the event type is an end tag (</>) AND that end tag is called item (</item>) go inside this if
                else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                    //now that the method is at the end of an item, it is not longer inside of an item
                    //set the boolean to false
                    insideOfItem = false;

                    //Creating a new Item object, this constructor takes in everything bar the author and comments

                    //Because it doesn't take these in the item wont call the internal functions
                    //that parse the start and end time of the roadworks to find the duration.

                    //We don't do this because current incidents don't have a set start and end time

                    //Item(String titleIn, String desIn, String linkIn, String geoIn, LocalDate pubDateIn)
                    Item newItem = new Item(tempTitle, tempDescription, tempLink, tempGeorsspoint, tempPubDateToDate);

                    //Now push this item to the global arraylist
                    itemsList.add(newItem);


                }
                //go to next event
                eventType = xpp.next();

            }
        }//End try
        //Catch to catch any errors present during parsing
        catch (XmlPullParserException err)
        {
            Log.e("Error Parsing","Parsing failed. Reason: " + err.toString());
        }
        catch (IOException err)
        {
            Log.e("Error IO","IO Error.");
        }


    }//End parseData

    //===================================================================================================================
}