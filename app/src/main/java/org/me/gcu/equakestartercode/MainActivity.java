package org.me.gcu.equakestartercode;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;

import org.me.gcu.equakestartercode.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

// Student Name: Ben Maxwell
// Student Number: S1917932
public class MainActivity extends AppCompatActivity implements OnClickListener
{
    // Layout Declarations
    private ListView quakeList;
    private ImageButton startButton;
    private ImageButton searchButton;

    // Variable Declarations
    private Earthquake earthquake;
    private ArrayList<Earthquake> earthquakeArrayList;
    private ArrayList<String> descs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Layout Components Assignment
        startButton = (ImageButton)findViewById(R.id.startButton); // Defining the startButton using the XML ID of startButton (Refresh Symbol)
        startButton.setOnClickListener(this); // Sets OnClick listener for startButton
        searchButton = (ImageButton)findViewById(R.id.searchButton); // Defining the searchButton using the XML ID of searchButton (Magnifying Glass Symbol)
        searchButton.setOnClickListener(this); // Sets OnClick listener for searchButton
        quakeList = (ListView)findViewById(R.id.quakeList); // ListView for displaying all earthquakes

        // Variable Assignment
        earthquakeArrayList = new ArrayList<Earthquake>(); // ArrayList for all of the Earthquake objects
        descs = new ArrayList<String>(); // Description data from XML Item, used to circumvent dealing with the ArrayList for a specific variable (Position remains the same in each ArrayList)

        // Sets an onClick for individual items within the ListView
        // Passes that items data through to the details activity based on it's position in the array
        quakeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), DetailsActivity.class);
                intent.putExtra("Earthquake", earthquakeArrayList.get(position)); // Using the position from the descs ArrayList, get the Earthquake object from the Earthquake ArrayList and pass it in the intent
                startActivity(intent);
            }
        });

        refresh();
    }

    // Method to refresh the RSS feed every 2 minutes
    private void refresh() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new ProcessInBG().execute();
                handler.postDelayed(this, 120000);
            }
        };

        handler.post(runnable);
    }

    public void onClick(View aview)
    {
        // Switch statement to handle different onClick tasks depending on button ID
        switch (aview.getId())
        {
            case R.id.searchButton:
                // Redirects the user to the search activity
                Intent intSearch = new Intent(MainActivity.this, SearchActivity.class);
                Bundle args = new Bundle();
                args.putSerializable("EARTHQUAKES", (Serializable)earthquakeArrayList);
                intSearch.putExtra("BUNDLE", args);
                startActivity(intSearch);
                break;

            case R.id.startButton :
                new ProcessInBG().execute(); // Executes XML Pull Parse Method to retrieve RSS feed (Used for manual refresh by user)
                break;

            default:
                break;
        }
    }

    public InputStream getInputStream(URL url)
    {
        try
        {
            return url.openConnection().getInputStream();
        }
        catch (IOException e)
        {
            return null;
        }
    }

    public class ProcessInBG extends AsyncTask<Integer, Void, Exception>
    {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this); // Progress dialog displayed when loading the data
        Exception exception = null; // Due to multiple catches it seemed more logical to have an exception object

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            descs.clear(); // Clear titles array
            quakeList.setAdapter(null); // Clear ListView in case of multiple presses
            progressDialog.setMessage("Loading RSS Feed!"); // Progress Message
        }

        @Override
        protected Exception doInBackground(Integer... integers) {
            try
            {
                URL quakeURL = new URL("http://quakes.bgs.ac.uk/feeds/MhSeismology.xml");
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);

                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(getInputStream(quakeURL), "UTF_8");

                boolean insideItem = false;
                int eventType = xpp.getEventType();

                // While the event type hasn't reached the end of the xml document
                while (eventType != XmlPullParser.END_DOCUMENT)
                {
                    if (eventType == XmlPullParser.START_TAG)
                    {
                        if (xpp.getName().equalsIgnoreCase("item")) // Find tag with name item
                        {
                            insideItem = true;
                            earthquake = new Earthquake(); // Create new earthquake object to store Item data
                        }
                        else if (xpp.getName().equalsIgnoreCase("title")) // Find tag with name title
                        {
                            if (insideItem) // If insideItem = true then add string from title tag to ArrayList titles
                            {
                                earthquake.setTitle(xpp.nextText()); // Add string to title variable in Earthquake object
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("description")) // Find tag with name link
                        {
                            if (insideItem) // If insideItem = true then add string from link tag to ArrayList links
                            {
                                String desc = xpp.nextText(); // Had issues with only one description tag being found so retrieved the value as a string variable then set the objects after
                                earthquake.setDesc(desc); // Add string to desc variable in Earthquake object
                                descs.add(desc); // Add string to descs ArrayList
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("link")) // Find tag with name link
                        {
                            if (insideItem) // If insideItem = true then add string from link tag to ArrayList links
                            {
                                earthquake.setLink(xpp.nextText()); // Add string to links variable in Earthquake object
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("geo:lat")) // Find tag with name link
                        {
                            if (insideItem) // If insideItem = true then add string from link tag to ArrayList links
                            {
                                earthquake.setLat(xpp.nextText()); // Add string to lats variable in Earthquake object
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("geo:long")) // Find tag with name link
                        {
                            if (insideItem) // If insideItem = true then add string from link tag to ArrayList links
                            {
                                earthquake.setLongitude(xpp.nextText()); // Add string to longitudes variable in Earthquake object
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("pubDate")) // Find tag with name link
                        {
                            if (insideItem) // If insideItem = true then add string from link tag to ArrayList links
                            {
                                earthquake.setDate(xpp.nextText()); // Add string to date variable in Earthquake object
                            }
                        }
                    }
                    else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item"))
                    {
                        earthquakeArrayList.add(earthquake); // Add each earthquake to ArrayList
                        insideItem = false; // Once the end of the item tag is reached, set insideItem to false
                    }

                    eventType = xpp.next(); // Iterate to the next tag
                }
            }
            catch (MalformedURLException e) // Exception in case of a bad URL input
            {
                exception = e;
            }
            catch (XmlPullParserException e) // Exception for XML Pull Parser errors
            {
                exception = e;
            }
            catch (IOException e) // Exception for input and output errors
            {
                exception = e;
            }

            return exception;
        }

        @Override
        protected void onPostExecute(Exception s) {
            super.onPostExecute(s);

            // ArrayAdapter for Descs, used to use titles but changed to descriptions to get the magnitude from the last substring
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, descs) // Add ArrayList titles to quakeList ListView
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View view = super.getView(position, convertView, parent);

                    TextView textView = (TextView) view.findViewById(android.R.id.text1);
                    String text = textView.getText().toString();

                    // Gets last value from the description tag to get the magnitude
                    String eStrength = text.substring(text.lastIndexOf(" ") + 1);
                    eStrength.trim();
                    double strDouble = Double.parseDouble(eStrength); // Parses the magnitude string as a double for comparison

                    // Using the richter scale would be more appropriate but due to the nature of the UK having very minor or no earthquakes
                    // It made more sense to use a smaller scale with anything over 4 regarded as strong and anything below 2 as weak or not felt
                    // Sets background colour of the individual TextViews in the ListView to the appropriate colour based on the items magnitude
                    if (strDouble >= 4) // Strong Quakes are Red
                    {
                        textView.setBackgroundColor(Color.RED);
                    }
                    else if (strDouble >= 2 && strDouble < 4) // Moderate Quakes are Yellow
                    {
                        textView.setBackgroundColor(Color.YELLOW);
                    }
                    else // Minor Quakes are Green
                    {
                        textView.setBackgroundColor(Color.GREEN);
                    }

                    return view;
                }
            };

            quakeList.setAdapter(adapter); // Set quakeList to the adapter

            progressDialog.dismiss(); // Dismiss progress dialog after execute
        }
    }
}