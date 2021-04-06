package org.me.gcu.equakestartercode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// Student Name: Ben Maxwell
// Student Number: S1917932
public class SearchActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener
{
    // Layout Declarations
    private ImageButton homeBtn;
    private ImageButton startBtn;
    private ImageButton endBtn;
    private ImageButton searchBtn;
    private ListView quakeList;

    // Date Picker Dialog Fragment Declaration
    private DialogFragment datePicker;

    // Variable Declarations
    private Date startDate;
    private Date endDate;
    private ArrayList<Earthquake> earthquakeArrayList;
    private ArrayList<String> searchedList;

    // Filter Variables
    // These will be used during search to filter by direction, depth and strength
    private Earthquake dirNorth; // Maximum Value can be 90 degrees
    private Earthquake dirSouth; // Maximum Value can be -90 degrees
    private Earthquake dirWest; // Maximum Value can be -180 degrees
    private Earthquake dirEast; // Maximum Value can be 180 degrees
    private Earthquake shallowQuake; // Will check against the other searched objects for shallowest quake
    private Earthquake deepQuake; // Will check against the other searched objects for deepest quake
    private Earthquake strongQuake; // Will check highest magnitude

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Set Layout Elements
        // onClick set to home button so user can return to MainActivity
        homeBtn = (ImageButton) findViewById(R.id.homeButton);
        homeBtn.setOnClickListener(this::onClick);
        searchBtn = (ImageButton)  findViewById(R.id.searchDate);
        searchBtn.setOnClickListener(this::onClick);

        // Setting ListView
        quakeList = (ListView)findViewById(R.id.quakeList); // ListView for displaying all earthquakes

        // Setting up Calendar buttons to allow for access to date dialog
        // For the first date button
        startBtn = (ImageButton) findViewById(R.id.startDate);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenDialog("StartDatePicker");
            }
        });

        // For the second date button
        endBtn = (ImageButton) findViewById(R.id.endDate);
        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenDialog("EndDatePicker");
            }
        });

        // Create intent to retrieve the bundle from Main
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE"); // Retrieves bundle
        earthquakeArrayList = (ArrayList<Earthquake>) args.getSerializable("EARTHQUAKES"); // Set ArrayList to earthquakeArrayList from Main

        // Init searchedList
        searchedList = new ArrayList<String>();

        // Sets an onClick for individual items within the ListView
        // Passes that items data through to the details activity based on it's position in the array
        quakeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), DetailsActivity.class);
                intent.putExtra("Earthquake", earthquakeArrayList.get(position)); // Using the position from the earthquakeArrayList ArrayList, get the Earthquake object from the Earthquake ArrayList and pass it in the intent
                startActivity(intent);
            }
        });
    }

    // Takes the tags specified in OnCreate as an argument to determine which Dialog is being clicked
    private void OpenDialog(String tag)
    {
        datePicker = new DatePickerFragment();
        datePicker.show(getSupportFragmentManager(), tag);
    }

    public void onClick(View aview)
    {
        // Switch statement to handle different onClick tasks depending on button ID
        switch (aview.getId())
        {
            case R.id.homeButton:
                // Redirects the user to the main activity
                Intent intHome = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intHome);
                break;

            case R.id.searchDate :
                new ProcessInBG().execute(); // Execute search function
                break;

            default:
                break;
        }
    }

    // Gets value from the DatePicker Dialog
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year); // Gets year from Calendar
        c.set(Calendar.MONTH, month); // Gets month from Calendar
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth); // Gets day from Calendar

        // Checks Dialog Tag specified before in OpenDialog to extract value from the seperate DatePicker Dialogs
        if(datePicker.getTag().equals("StartDatePicker"))
        {
            startDate = c.getTime(); // Gets selected date
        }
        else
        {
            endDate = c.getTime(); // Gets selected date
        }
    }

    public class ProcessInBG extends AsyncTask<Integer, Void, Exception>
    {
        ProgressDialog progressDialog = new ProgressDialog(SearchActivity.this); // Progress dialog displayed when loading the data
        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            searchedList.clear(); // Clear previous searches in ArrayList
            progressDialog.setMessage("Loading"); // Progress Message
        }

        @Override
        protected Exception doInBackground(Integer... integers) {
            try
            {
                // Sets filter variables to the first index of the earthquakeArrayList
                dirNorth = earthquakeArrayList.get(0);
                dirSouth = earthquakeArrayList.get(0);
                dirWest = earthquakeArrayList.get(0);
                dirEast = earthquakeArrayList.get(0);
                shallowQuake = earthquakeArrayList.get(0);
                deepQuake = earthquakeArrayList.get(0);
                strongQuake = earthquakeArrayList.get(0);

                // To accommodate single choices to find a specific date
                // End Date will be the same as Start Date if there is no input and vice versa
                if(startDate == null)
                {
                    startDate = endDate;
                }
                else if(endDate == null)
                {
                    endDate = startDate;
                }

                for(Earthquake eq : earthquakeArrayList) // For each earthquake in earthquakeArrayList loop through If
                {
                    if(!eq.getTrueDate().before(startDate) && !eq.getTrueDate().after(endDate) || eq.getTrueDate().after(startDate) && eq.getTrueDate().before(endDate))
                    {
                        // Check and Filter by conditions
                        // Variables will be set here and added to searchedList after the For loop
                        if(eq.getDblLat() > dirNorth.getDblLat()) // The maximum value for north is positive so it would be greater than
                        {
                            dirNorth = eq;
                        }
                        else if(eq.getDblLat() < dirSouth.getDblLat()) // The maximum value for south is negative then it would have to lesser than
                        {
                            dirSouth = eq;
                        }
                        else if(eq.getDblLong() < dirWest.getDblLong()) // The maximum value of west is negative so it is lesser than
                        {
                            dirWest = eq;
                        }
                        else if(eq.getDblLong() > dirEast.getDblLong()) // The maximum value of east is positive so it is greater than
                        {
                            dirEast = eq;
                        }

                        if(eq.getDepth() > deepQuake.getDepth())
                        {
                            deepQuake = eq;
                        }
                        else if(eq.getDepth() < shallowQuake.getDepth())
                        {
                            shallowQuake = eq;
                        }

                        if(eq.getMagni() > strongQuake.getMagni())
                        {
                            strongQuake = eq;
                        }
                    }
                }

                // Add our filtered variables after for loop to searched list ArrayList
                searchedList.add("Most Northerly:"); // Variable Label
                searchedList.add(dirNorth.getDesc()); // Adds the most northerly earthquake belonging to the date range to ArrayList searchedList
                searchedList.add("Most Southerly:"); // Variable Label
                searchedList.add(dirSouth.getDesc()); // Adds the most southerly earthquake belonging to the date range to ArrayList searchedList
                searchedList.add("Most Westerly:"); // Variable Label
                searchedList.add(dirWest.getDesc()); // Adds the most westerly earthquake belonging to the date range to ArrayList searchedList
                searchedList.add("Most Easterly:"); // Variable Label
                searchedList.add(dirEast.getDesc()); // Adds the most easterly earthquake belonging to the date range to ArrayList searchedList
                searchedList.add("Deepest Earthquake:"); // Variable Label
                searchedList.add(deepQuake.getDesc()); // Adds the deepest earthquake belonging to date range to ArrayList searchedList
                searchedList.add("Shallowest Earthquake:"); // Variable Label
                searchedList.add(shallowQuake.getDesc()); // Adds the shallowest earthquake belonging to the date range to ArrayList searchedList
                searchedList.add("Highest Magnitude:"); // Variable Label
                searchedList.add(strongQuake.getDesc()); // Adds the strongest magnitude earthquake belonging to the date range to ArrayList searchedList
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

            // ArrayAdapter for the searched list
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, searchedList) // Add ArrayList searchedList to quakeList ListView
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View view = super.getView(position, convertView, parent);

                    TextView textView = (TextView) view.findViewById(android.R.id.text1);
                    String text = textView.getText().toString();
                    double strDouble = 0.0;

                    // Gets last value from the description tag to get the magnitude
                    if(text.startsWith("Origin"))
                    {
                        String eStrength = text.substring(text.lastIndexOf(" ") + 1);
                        eStrength.trim();
                        strDouble = Double.parseDouble(eStrength); // Parses the magnitude string as a double for comparison
                    }

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
                    else if (strDouble > 0 && strDouble < 2) // Minor Quakes are Green
                    {
                        textView.setBackgroundColor(Color.GREEN);
                    }
                    else
                    {
                        textView.setBackgroundColor(Color.WHITE);
                        textView.setTextSize(20);
                    }

                    return view;
                }
            };

            quakeList.setAdapter(adapter); // Set quakeList to the adapter
            progressDialog.dismiss(); // Dismiss progress dialog after execute
        }
    }
}