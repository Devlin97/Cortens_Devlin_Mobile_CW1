//DEVLIN CORTENS
//S1825992

package org.me.gcu.devlin_cortens_cw1_mobile_dev;

import android.os.Build;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
//implements serializable to bundle it between fragments
public class Item implements Serializable {

    //Declaring all the variables that are inside of an <item></item> tag
    private String title;
    private String description;
    private String link;
    private String georsspoint;
    private String author;
    private String comments;
    private LocalDate pubDate;

    //This variable isnt inside the item tag but is used to hold the duration of the roadworks after its been parsed from the description
    private long hours;
    private boolean isIncident;

    //Planned and Current Roadworks Constructor
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Item(String titleIn, String desIn, String linkIn, String geoIn, String authorIn, String commentsIn, LocalDate pubDateIn) {
        title = titleIn;
        description = desIn;
        link = linkIn;
        georsspoint = geoIn;
        author = authorIn;
        comments = commentsIn;
        pubDate = pubDateIn;

        //Parse the description for the start and end time
        String theStart = parseDescription(description)[0];
        String theEnd = parseDescription(description)[1];

        //Run the algorithm that finds the difference in hours between the two dates and set this to hours
        hours = findHours(theStart, theEnd);

        isIncident = false;

    }

    //Current Incidents Constructor
    //Sine current incidents doesn't have description parsing we're going to make a separate constructor for it
    //Just not passing in the author or comments
    public Item(String titleIn, String desIn, String linkIn, String geoIn, LocalDate pubDateIn) {
        title = titleIn;
        description = desIn;
        link = linkIn;
        georsspoint = geoIn;
        pubDate = pubDateIn;
        isIncident = true;

    }

    //toString method
    //this method is only needed for current incidents
    //since current incidents don't have a duration they wont need colour coded.
    //It doesn't need to use a custom array adapter so the basic array adapter just calls the items toString method
    //and this will just return the title. Then when the user clicks on it they can see the description and pubdate etc etc
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public String toString() {
        if(isIncident == true) {
            return title;
        }
        return title + description;
    }

    //Getters and Setters for each of the attributes

    public String getTitle() {
        return title;
    }

    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String newLink) {
        this.link = newLink;
    }

    public String getGeorsspoint() {
        return georsspoint;
    }

    public void setGeorsspoint(String newGeorsspoint) {
        this.georsspoint = newGeorsspoint;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String newAuthor) {
        this.author = newAuthor;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String newComments) {
        this.comments = newComments;
    }

    public LocalDate getPubDate() {
        return pubDate;
    }

    public void setPubDate(LocalDate newPubDate) {
        this.pubDate = newPubDate;
    }

    public long getHours() {
        return hours;
    }

    public void setHours(long newHours) {
        this.hours = newHours;
    }

    public boolean getIsIncident() {
        return isIncident;
    }

    public void setIsIncident(boolean newIsIncident) {
        this.isIncident = newIsIncident;
    }

    //This method is used to parse the description of current and planned roadworks
    //It finds the strings where it says "start date: xxx-xxx-xxx" and "end date: xxx-xxx-xxx"
    //This function then returns those two dates as strings.
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String[] parseDescription(String descIn) {
        //: and - are the start and end of where the date starts so this finds the index of them in the string
        int startDateEnd = descIn.indexOf("-");
        int startDateStart = descIn.indexOf(":");

        //Create a substring based on the two indexes found. +2 and -1 are to delete the space around the date
        String startDate = descIn.substring(startDateStart + 2, startDateEnd - 1);

        //For end date "End Date: " is where the end date is stored so this finds its index in the string
        //End date ends with "- 00:00"
        int findEndDate = descIn.indexOf("End Date: ");
        int findEndOfEndDate = descIn.lastIndexOf("- 00:00");
        //Make another substring from these indexes
        String endDateString = descIn.substring(findEndDate, findEndOfEndDate);

        //Find the ":" so from the substring we just created to trim of the "End Date:" part of the string
        int startEndDate = endDateString.indexOf(":");

        //Create a substring based on the two index found and the end of the endDateString. +2 and -1 are to delete the space around the date
        endDateString = endDateString.substring(startEndDate + 2, endDateString.length() - 1);

        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);

        LocalDate date1 = LocalDate.parse(startDate, formatter2);
        LocalDate date2 = LocalDate.parse(endDateString, formatter2);

        long hours = date1.atStartOfDay().until(date2.atStartOfDay(), ChronoUnit.HOURS);

        //return these two strings in an array of strings
        return new String[]{ startDate, endDateString };
    }

    //This function takes those two string returned by the parseDescription function, converts them to LocalDates
    //and then finds the amount of time between them in hours
    //We need to do this to colour the items in the ListView depending on the duration in hours of each roadwork
    @RequiresApi(api = Build.VERSION_CODES.O)
    public long findHours(String startDateIn, String endDateIn) {
        //This is the format needed to parse the date correctly from the string to a LocalDate
        //This was found through testing
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);

        //Create two dates from the strings passed in using the formatter created above
        //These strings will be the strings found from parseDescription method
        LocalDate date1 = LocalDate.parse(startDateIn, formatter2);
        LocalDate date2 = LocalDate.parse(endDateIn, formatter2);

        //This is the method which finds the hours between the two dates
        long hours = date1.atStartOfDay().until(date2.atStartOfDay(), ChronoUnit.HOURS);

        //return the hours
        return hours;
    }
}
