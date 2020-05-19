package org.futto.app.survey;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.ListView;

import org.futto.app.R;
import org.futto.app.session.SessionActivity;
import org.futto.app.storage.PersistentData;
import org.futto.app.ui.adapters.SurveyAdapter;
import org.futto.app.ui.user.MainMenuActivity;

import java.util.ArrayList;

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
            if (PersistentData.getSurveyNotificationState( tmpsurvey)) {
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
