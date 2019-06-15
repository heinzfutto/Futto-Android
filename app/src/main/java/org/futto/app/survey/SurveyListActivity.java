package org.futto.app.survey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.futto.app.R;
import org.futto.app.session.SessionActivity;
import org.futto.app.storage.PersistentData;
import org.futto.app.ui.adapters.SurveyAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyListActivity extends SessionActivity {

    private ListView listView;
    private SurveyAdapter surveyAdapter;
    private ArrayList<String> surveyId;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_list);

        listView = (ListView) findViewById(R.id.surveylistView);
        Intent triggerIntent = getIntent();
        surveyId = new ArrayList<String>();
        for(int i=0;i<PersistentData.getSurveyIds().size();i++){
            surveyId.add(PersistentData.getSurveyIds().get(i));
        }

        surveyAdapter = new SurveyAdapter(surveyId,this);
        displayToobar();
        listView.setAdapter(surveyAdapter);
    }

    private void displayToobar() {
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Futto Survey");
    }

}
