package com.example.meetingapp.activities;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.meetingapp.EventManager;
import com.example.meetingapp.R;
import com.example.meetingapp.adapters.EventStepperAdapter;
import com.example.meetingapp.models.Event;
import com.example.meetingapp.models.GeoPoint;
import com.stepstone.stepper.StepperLayout;

import java.util.ArrayList;

public class CreateEventActivity extends AppCompatActivity implements EventManager {

    private static final String CURRENT_STEP_POSITION_KEY = "position";
    private static final String DESCRIPTION = "description";
    private static final String DATE = "date";
    private static final String ID = "id";
    private static final String TIME = "time";
    private static final String LOCATION = "location";
    private static final String ACTION = "action";
    private static final String CATEGORIES = "categories";


    private StepperLayout mStepperLayout;

    private String action;
    private String mDescription;
    private int id;
    private String mDate;
    private String mTime;
    private GeoPoint mLocation;
    private ArrayList<String> categories;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.step);

        mStepperLayout = findViewById(R.id.stepperLayout);
        EventStepperAdapter mEventStepperAdapter = new EventStepperAdapter(getSupportFragmentManager(), this);
        mStepperLayout.setAdapter(mEventStepperAdapter);

        loadEvent();
    }

    private void loadEvent(){

        Intent intent = getIntent();
        this.saveAction(intent.getStringExtra("action"));

        if(intent.hasExtra("EXTRA_EVENT")){
            Event event = intent.getParcelableExtra("EXTRA_EVENT");
            if(event != null){
                this.saveId(event.getId());
                this.saveDescription(event.getDescription());
                this.saveDate(event.getDate());
                this.saveTime(event.getTime());
                this.saveLocation(event.getGeoPoint());
            }
        }
    }

    public Fragment getLatestFragment(){
        int index = getFragmentManager().getBackStackEntryCount() - 1;
        FragmentManager.BackStackEntry backEntry = (FragmentManager.BackStackEntry) getFragmentManager().getBackStackEntryAt(index);
        String tag = backEntry.getName();
        return getFragmentManager().findFragmentByTag(tag);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_STEP_POSITION_KEY, mStepperLayout.getCurrentStepPosition());

        outState.putInt(ID, id);
        outState.putString(DESCRIPTION, mDescription);
        outState.putString(DATE, mDate);
        outState.putString(TIME, mTime);
        outState.putParcelable(LOCATION, mLocation);
        outState.putStringArrayList(CATEGORIES, categories);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        final int currentStepPosition = mStepperLayout.getCurrentStepPosition();
        if (currentStepPosition > 0) {
            mStepperLayout.onBackClicked();
        } else {
            finish();
        }
    }

    @Override
    public void saveAction(String action) {
        this.action = action;
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public void saveId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void saveDescription(String description) {
        mDescription = description;
    }

    public String getDescription() {
        return mDescription;
    }

    @Override
    public void saveDate(String date) {
        mDate = date;
    }

    public String getDate() {
        return mDate;
    }

    @Override
    public void saveTime(String time) {
        mTime = time;
    }

    public String getTime() {
        return mTime;
    }

    @Override
    public void saveLocation(GeoPoint location) {
        mLocation = location;
    }

    public GeoPoint getLocation() {
        return mLocation;
    }

    @Override
    public void saveCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    @Override
    public ArrayList<String> getCategories() {
        return categories;
    }
}