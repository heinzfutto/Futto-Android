package org.futto.app.survey;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import org.futto.app.R;
import org.futto.app.session.SessionActivity;
import org.futto.app.storage.PersistentData;
import org.futto.app.ui.adapters.SurveyAdapter;
import org.futto.app.ui.user.MainMenuActivity;

import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;

public class SurveyListActivity extends SessionActivity {

    private ListView listView;
    private SurveyAdapter surveyAdapter;
    private ArrayList<String> surveyId;
    private ArrayList<String> surveyState;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_list);

        listView = (ListView) findViewById(R.id.surveylistView);
        Intent triggerIntent = getIntent();
        surveyId = new ArrayList<String>();
        surveyState = new ArrayList<String>();
        for(int i=0;i<PersistentData.getSurveyIds().size();i++){
            String tmpsurvey = PersistentData.getSurveyIds().get(i);
            Log.d("SurveyListActivity", tmpsurvey);
            Log.d("SurveyListActivity", PersistentData.getSurveyNotificationState( tmpsurvey).toString());
//            if(PersistentData.getSurveyIncompleteState(tmpsurvey) == true){
//                surveyId.add(PersistentData.getSurveyIds().get(i));
//                surveyState.add("Incomplete");
//            }else{
//                surveyId.add(PersistentData.getSurveyIds().get(i));
//                surveyState.add("new");
//            }
            if (PersistentData.getSurveyNotificationState( tmpsurvey) && Long.parseLong(PersistentData.getSurveyTimes(tmpsurvey)) < System.currentTimeMillis()) {
                if(PersistentData.getSurveyIncompleteState(tmpsurvey) == true){
                    surveyId.add(PersistentData.getSurveyIds().get(i));
                    surveyState.add("Incomplete");
                }else{
                    surveyId.add(PersistentData.getSurveyIds().get(i));
                    surveyState.add("new");
                }

            }
        }

        surveyAdapter = new SurveyAdapter(surveyId,surveyState,this);
        displayToobar();
        listView.setAdapter(surveyAdapter);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(this, MainMenuActivity.class));
        finish();

    }

    private void displayToobar() {
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Futto Survey");
    }

}
