package org.me.gcu.equakestartercode;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

// Student Name: Ben Maxwell
// Student Number: S1917932
public class DetailsActivity extends AppCompatActivity implements OnMapReadyCallback
{
    // Layout Declarations
    private ImageButton homeBtn;
    private TextView titleTxt;
    private TextView descTxt;

    // Google Maps
    SupportMapFragment mapFragment;
    private GoogleMap mMap;

    // Variable Declarations
    private Earthquake earthquake;
    private String title;
    private String desc;
    private double lat;
    private double longitude;
    private String stringDate;

    // Variable to hold converted date value
    private Date date;

    // Magnitude to change marker colour based on value
    private String magStrength;
    private double magDouble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Set Layout Elements
        homeBtn = (ImageButton) findViewById(R.id.homeButton);
        titleTxt = (TextView) findViewById(R.id.titleTV);
        descTxt = (TextView) findViewById(R.id.descTV);

        // onClick set to home button so user can return to MainActivity
        homeBtn.setOnClickListener(this::onClick);

        // Get intent for data transfer from the main activity
        // Passes the Earthquake object from Main to Details
        Intent intent = getIntent();

        // Finding the map fragment on the layout then running getMapAsync to load the map data
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.quakeMap);
        mapFragment.getMapAsync(this);

        // Import Variables from Intent
        earthquake = (Earthquake) getIntent().getSerializableExtra("Earthquake");

        title = earthquake.getTitle();;
        desc = earthquake.getDesc();
        lat = Double.parseDouble(earthquake.getLat()); // Parses to double for use as a mathematical value in the GoogleMaps API
        longitude = Double.parseDouble(earthquake.getLongitude()); // Parses to double for use as a mathematical value in the GoogleMaps API
        stringDate = earthquake.getDate();

        // Convert date from String to Date
        try
        {
            DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.getDefault()); // Setting the format of the date using the string from the RSS feed as a guide
            date = format.parse(stringDate); // Formatting stringDate to a Date object using DateFormat
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        // Start async task ProcessInBG to load data into the layout
        new ProcessInBG().execute();
    }

    public void onClick(View aview)
    {
        // Returns the user back to the main activity
        Intent intHome = new Intent(DetailsActivity.this, MainActivity.class);
        startActivity(intHome);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(10.0f); // Setting the minimum zoom so the user can immediately view the region
        LatLng quake = new LatLng(lat, longitude); // Set position using the earthquake latitude and longitude

        // Set magnitude equal to the last value in description
        // This used to be handled in onCreate but due to issues it was moved here and is now functional
        magStrength = desc.substring(desc.lastIndexOf(" ") + 1);
        magStrength.trim();
        magDouble = Double.parseDouble(magStrength);

        // If statements to change marker colour based on magnitude
        // Danger/Red is Over or Equal to 4
        if (magDouble >= 4)
        {
            mMap.addMarker(new MarkerOptions()
                    .position(quake)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(quake));
        }
        else if (magDouble >= 2 && magDouble < 4) // Moderate/Yellow Quakes are Below 4 or Greater than or Equal to 2
        {
            mMap.addMarker(new MarkerOptions()
                    .position(quake)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(quake));
        }
        else // Anything under 2 is Green/Minor
        {
            mMap.addMarker(new MarkerOptions()
                    .position(quake)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(quake));
        }
    }

    public class ProcessInBG extends AsyncTask<Integer, Void, Exception>
    {
        ProgressDialog progressDialog = new ProgressDialog(DetailsActivity.this); // Progress dialog displayed when loading the data
        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Loading"); // Progress Message
        }

        @Override
        protected Exception doInBackground(Integer... integers) {
            try
            {
                // Split Description by the Semicolon
                String descValues[] = desc.split(";\\s*");

                StringBuilder builder = new StringBuilder();
                for (String descList: descValues) {
                    builder.append(descList);
                    builder.append("\n");
                }

                // Setting the TextViews to the passed data
                titleTxt.setText(title);
                descTxt.setText(builder.toString());
            }
            catch (Exception e)
            {
                exception = e;
            }

            return exception;
        }

        @Override
        protected void onPostExecute(Exception s) {
            super.onPostExecute(s);

            progressDialog.dismiss(); // Dismiss progress dialog after execute
        }
    }
}