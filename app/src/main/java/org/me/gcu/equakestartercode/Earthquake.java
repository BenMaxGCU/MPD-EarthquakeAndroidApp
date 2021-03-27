package org.me.gcu.equakestartercode;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

// Student Name: Ben Maxwell
// Student Number: S1917932
public class Earthquake implements Serializable {
    // Variable Declaration
    String title;
    String desc;
    String link;
    String lat;
    String longitude;
    String date;

    // Getters and Setters for each Variable
    // Title
    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }

    // Desc
    public void setDesc(String desc) { this.desc = desc; }
    public String getDesc() { return desc; }

    // Link
    public void setLink(String link) { this.link = link; }
    public String getLink() { return link; }

    // Lat
    public void setLat(String lat) { this.lat = lat; }
    public String getLat() { return lat; }

    // Longitude
    public void setLongitude(String longitude) { this.longitude = longitude; }
    public String getLongitude() { return longitude; }

    // Date
    public void setDate(String date) { this.date = date; }
    public String getDate() { return date; }

    // Conversion Methods
    public Date getTrueDate()
    {
        // Convert date from String to Date
        try
        {
            DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()); // Setting the format of the date using the string from the RSS feed as a guide
            Date trueDate = format.parse(date); // Formatting String date to a Date object using DateFormat
            return trueDate;
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public Double getMagni()
    {
        // Gets last value from the description tag to get the magnitude
        String eStrength = desc.substring(desc.lastIndexOf(" ") + 1);
        eStrength.trim(); // Remove Whitespace

        double strDouble = Double.parseDouble(eStrength); // Parses the magnitude string as a double for comparison

        return strDouble;
    }

    public Double getDblLat()
    {
        Double trueLat = Double.parseDouble(lat); // Parses original string into double
        return trueLat; // Returns as Double
    }

    public Double getDblLong()
    {
        Double trueLong = Double.parseDouble(longitude); // Parses original string into double
        return trueLong; // Returns as Double
    }

    public int getDepth()
    {
        try
        {
            int trueDepth = 0; // Init trueDepth
            String qDepth = desc.substring(desc.indexOf("Depth") + 7, desc.lastIndexOf("km")); //Finds the depth number within the desc string
            qDepth = qDepth.replaceAll("[^\\d]", " "); // Replace anything that isn't a digit with a space
            qDepth = qDepth.trim(); // Trim qDepth of excess whitespace

            trueDepth = Integer.parseInt(qDepth); // Parse qDepth string to Integer
            return trueDepth; // Returns as integer
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0; // Returns 0 if an exception is triggered
        }
    }

}
