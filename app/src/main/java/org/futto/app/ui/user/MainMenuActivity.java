package org.futto.app.ui.user;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import org.futto.app.R;
import org.futto.app.networking.NetworkUtility;
import org.futto.app.nosql.NotificationDO;
import org.futto.app.session.SessionActivity;
import org.futto.app.storage.PersistentData;

import java.util.List;

/**
 * The main menu activity of the app. Currently displays 4 buttons - Audio Recording, Graph, Call Clinician, and Sign out.
 *
 * @author Dor Samet
 */
public class MainMenuActivity extends SessionActivity {
    //extends a SessionActivity
    TextView username;
    TextView notification;
    private Toolbar toolbar;
    List<NotificationDO> result;
    DynamoDBMapper dynamoDBMapper;
    String user = PersistentData.getPatientID();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        displayUsername();


        displayToobar();

        if(NetworkUtility.networkIsAvailable(this))setUpDB();
//        SettingUpdate.downloadSetting( getApplicationContext() );

    }

    private void setUpDB() {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:ee5d02f7-4e0f-4637-875e-8eeea9b80588", // Identity pool ID
                Regions.US_EAST_1 // Region
        );


        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
        dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();
        try {
            readNews();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
    public void readNews() throws InterruptedException {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                NotificationDO news = new NotificationDO();
                news.setUserId(user);
                news.setCreationDate(new Double("1529093763866"));

                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                        .withHashKeyValues(news)
                        .withConsistentRead(false);

                result = dynamoDBMapper.query(NotificationDO.class, queryExpression);

                if (result.isEmpty()) {
                    // There were no items matching your query.
                }else{
                    Log.d("query", result.get(result.size()-1).getTitle());
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setUpNotification();
    }


    private void setUpNotification() {
        notification = (TextView) findViewById(R.id.noti_content);
        NotificationDO  latestNote = null;
        if(result != null && result.size() > 0) latestNote = result.get(result.size()-1);
        else notification.setText("You don't have new message now.");
        if (latestNote != null && !latestNote.getIsReaded()) {
            notification.setText(latestNote.getTitle());
        } else {
            notification.setText("You don't have new message now.");
        }
    }

    private void displayToobar() {
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Futto Main Menu");
    }


    private void displayUsername() {
        username = findViewById(R.id.username_name);
        username.setText(user);
    }
}
