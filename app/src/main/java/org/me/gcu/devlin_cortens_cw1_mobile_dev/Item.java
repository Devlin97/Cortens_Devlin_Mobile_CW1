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

public class Item implements Serializable {

    //Declaring all the variables that are inside of an <item></item> tag
    private String title;
    private String description;
    private String link;
    private String georsspoint;
    private String author;
    private String comments;
    private LocalDate pubDate;
    private long hours;

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

        String theStart = parseDescription(description)[0];
        String theEnd = parseDescription(description)[1];

        hours = findHours(theStart, theEnd);

    }

    //Current Incidents Constructor
    //Sine current incidents doesnt have description parsing we're going to make a seperate constructor for it
    //Just not passing in the author or comments
    public Item(String titleIn, String desIn, String linkIn, String geoIn, LocalDate pubDateIn) {
        title = titleIn;
        description = desIn;
        link = linkIn;
        georsspoint = geoIn;
        pubDate = pubDateIn;

    }

    //toString method
    //this method is only needed for current incidents
    //since current incidents dont have a duration and wont need colour coded
    //we dont need to use a custom array adapter so the basic array adapter just calls the items toString method
    //and we will just return the title. Then when the user clicks on it they can see the description and pubdate etc etc
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public String toString() {
        return title;
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

    //This method is used to parse the description of current and planned roadworks
    //It finds the strings where it says "start date: xxx-xxx-xxx" and "end date: xxx-xxx-xxx"
    //This function then returns those two dates as strings.
    @RequiresApi(api = Build.VERSION_CODES.O)
    public String[] parseDescription(String descIn) {
        int startDateEnd = descIn.indexOf("-");
        int startDateStart = descIn.indexOf(":");

        String startDate = descIn.substring(startDateStart + 2, startDateEnd - 1);
        //System.out.println(startDate);

        int findEndDate = descIn.indexOf("End Date: ");
        int findEndOfEndDate = descIn.lastIndexOf("- 00:00");
        String endDateString = descIn.substring(findEndDate, findEndOfEndDate);

        int startEndDate = endDateString.indexOf(":");

        endDateString = endDateString.substring(startEndDate + 2, endDateString.length() - 1);

        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);

        LocalDate date1 = LocalDate.parse(startDate, formatter2);
        LocalDate date2 = LocalDate.parse(endDateString, formatter2);

        long hours = date1.atStartOfDay().until(date2.atStartOfDay(), ChronoUnit.HOURS);

        return new String[]{ startDate, endDateString };
    }

    //This function takes those two string returned by the parseDescription function, converts them to LocalDates
    //and then finds the amount of time between them in hours
    //We need to do this to colour the items in the ListView depending on the duration in hours of each roadwork
    @RequiresApi(api = Build.VERSION_CODES.O)
    public long findHours(String startDateIn, String endDateIn) {
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);

        LocalDate date1 = LocalDate.parse(startDateIn, formatter2);
        LocalDate date2 = LocalDate.parse(endDateIn, formatter2);

        long hours = date1.atStartOfDay().until(date2.atStartOfDay(), ChronoUnit.HOURS);

        return hours;
    }
}
