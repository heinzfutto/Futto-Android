package org.futto.app.ui.coupon;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;

import org.futto.app.R;
import org.futto.app.nosql.Notification;
import org.futto.app.nosql.NotificationDO;
import org.futto.app.storage.PersistentData;
import org.futto.app.ui.adapters.NotificationAdapter;

public class NotificationActivity extends AppCompatActivity {
    private NotificationAdapter notiAdapter;
    private DynamoDBMapper dynamoDBMapper;
    private String user = PersistentData.getPatientID();
    private PaginatedList<NotificationDO> result;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        displayToobar();
        setUpDB();

    }

    private void displayToobar() {
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Futto Coupons");
    }
    public void NotificationReady(final PaginatedList<NotificationDO> notis) {
        if (notis != null) {

            RecyclerView rvNotis = (RecyclerView) findViewById(R.id.rvNotis);
            notiAdapter = new NotificationAdapter(this, notis);

            rvNotis.setAdapter(notiAdapter);


            rvNotis.setLayoutManager(new LinearLayoutManager(this));

            notiAdapter.setOnItemClickListener(new NotificationAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    String name = notis.get(position).getTitle();
                    Toast.makeText(NotificationActivity.this, name + " was clicked!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(NotificationActivity.this, BarcodeActivity.class);
                    startActivity(intent);
                }
            });

        }

    }

    public void setUpAllNotifications() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                NotificationDO news = new NotificationDO();
                news.setUserId(user);
                news.setCreationDate(new Double("1529093763866"));

                Condition rangeKeyCondition = new Condition()
                        .withComparisonOperator(ComparisonOperator.BEGINS_WITH)
                        .withAttributeValueList(new AttributeValue().withS("Trial"));

                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                        .withHashKeyValues(news)
                        .withConsistentRead(false);

                result = dynamoDBMapper.query(NotificationDO.class, queryExpression);

            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(result != null)NotificationReady(result);
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
        setUpAllNotifications();

    }


}
