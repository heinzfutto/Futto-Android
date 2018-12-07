package org.futto.app;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityHandler;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.futto.app.BackgroundService.BackgroundServiceBinder;
import org.futto.app.networking.NetworkUtility;
import org.futto.app.nosql.NotificationDO;
import org.futto.app.storage.PersistentData;
import org.futto.app.ui.coupon.NotificationActivity;
import org.futto.app.ui.user.AboutActivityLoggedOut;
import org.futto.app.ui.user.JobsActivity;
import org.futto.app.ui.user.MapsActivity;

/**
 * All Activities in the app extend this Activity.  It ensures that the app's key services (i.e.
 * BackgroundService, LoginManager, PostRequest, DeviceInfo, and WifiListener) are running before
 * the interface tries to interact with any of those.
 * <p>
 * Activities that require the user to be logged in (SurveyActivity, GraphActivity,
 * AudioRecorderActivity, etc.) extend SessionActivity, which extends this.
 * Activities that do not require the user to be logged in (the login, registration, and password-
 * reset Activities) extend this activity directly.
 * Therefore all Activities have this Activity's functionality (binding the BackgroundService), but
 * the login-protected Activities have additional functionality that forces the user to log in.
 *
 * @author Eli Jones, Josh Zagorsky
 */
public class RunningBackgroundServiceActivity extends AppCompatActivity {
    /**
     * The backgroundService variable is an Activity's connection to the ... BackgroundService.
     * We ensure the BackgroundService is running in the onResume call, and functionality that
     * relies on the BackgroundService is always tied to UI elements, reducing the chance of
     * a null backgroundService variable to essentially zero.
     */
    protected BackgroundService backgroundService;
    protected static DynamoDBMapper dynamoDBMapper;
    private AWSCredentialsProvider credentialsProvider;
    private AWSConfiguration configuration;


    //an unused variable for tracking whether the background Service is connected, uncomment if we ever need that.
//	protected boolean isBound = false;

    /**
     * The ServiceConnection Class is our trigger for events that rely on the BackgroundService
     */
    protected ServiceConnection backgroundServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            // Log.d("ServiceConnection", "Background Service Connected");
            BackgroundServiceBinder some_binder = (BackgroundServiceBinder) binder;
            backgroundService = some_binder.getService();
            doBackgroundDependentTasks();
//	        isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w("ServiceConnection", "Background Service Disconnected");
            backgroundService = null;
//	        isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(getApplicationContext()));
        PersistentData.initialize(getApplicationContext());
        initialAWS();
        setUpDB();
    }

    private void initialAWS() {
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {

                // Obtain the reference to the AWSCredentialsProvider and AWSConfiguration objects
                credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
                configuration = AWSMobileClient.getInstance().getConfiguration();

                // Use IdentityManager#getUserID to fetch the identity id.
                IdentityManager.getDefaultIdentityManager().getUserID(new IdentityHandler() {
                    @Override
                    public void onIdentityId(String identityId) {
                        Log.d("YourMainActivity", "Identity ID = " + identityId);

                        // Use IdentityManager#getCachedUserID to
                        //  fetch the locally cached identity id.
                        final String cachedIdentityId =
                                IdentityManager.getDefaultIdentityManager().getCachedUserID();
                    }

                    @Override
                    public void handleError(Exception exception) {
                        Log.d("YourMainActivity", "Error in retrieving the identity" + exception);
                    }
                });
            }
        }).execute();


    }

    private void setUpDB() {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:ee5d02f7-4e0f-4637-875e-8eeea9b80588", // Identity pool ID
                Regions.US_EAST_1 // Region
        );


        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(configuration)
                .build();


    }

    public static void createNews(String title, String content) {

        final NotificationDO newItem = new NotificationDO();

        newItem.setUserId(PersistentData.getPatientID());
        newItem.setCreationDate((double) System.currentTimeMillis());
        newItem.setTitle(title);
        newItem.setIsReaded(false);
        newItem.setContent(content);

        new Thread(new Runnable() {
            @Override
            public void run() {
                dynamoDBMapper.save(newItem);
                // Item saved
                Log.d("Aws save1","aws save1");
            }
        }).start();
        Log.d("Aws save","aws save");
    }

    /**
     * Override this function to do tasks on creation, but only after the background Service has been initialized.
     */
    protected void doBackgroundDependentTasks() { /*Log.d("RunningBackgroundServiceActivity", "doBackgroundDependentTasks ran as default (do nothing)");*/ }

    @Override
    /**On creation of RunningBackgroundServiceActivity we guarantee that the BackgroundService is
     * actually running, we then bind to it so we can access program resources. */
    protected void onResume() {
        super.onResume();

        Intent startingIntent = new Intent(this.getApplicationContext(), BackgroundService.class);
        startingIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
        startService(startingIntent);
        bindService(startingIntent, backgroundServiceConnection, Context.BIND_AUTO_CREATE);
        setupRestartTimer();
    }

    private void setupRestartTimer() {
        Intent restartServiceIntent = new Intent(getApplicationContext(), BackgroundService.class);
        restartServiceIntent.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, 0);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 60 * 2, 100 * 60 * 2, restartServicePendingIntent);
    }


    @Override
    /** disconnect BackgroundServiceConnection when the Activity closes, otherwise we have a
     * memory leak warning (and probably an actual memory leak, too). */
    protected void onPause() {
        super.onPause();
        activityNotVisible = true;
        unbindService(backgroundServiceConnection);
    }
	
	
	/*####################################################################
	########################## Common UI #################################
	####################################################################*/

    @Override
    /** Common UI element, the menu button.*/
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logged_out_menu, menu);
        //menu.findItem(R.id.menu_call_clinician).setTitle(PersistentData.getCallClinicianButtonText());
        return true;
    }


    @Override
    /** Common UI element, items in menu.*/
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_about:
                startActivity(new Intent(getApplicationContext(), AboutActivityLoggedOut.class));
                return true;
		/*case R.id.menu_call_clinician:
			callClinician(null);
			return true;
		case R.id.menu_call_research_assistant:
			callResearchAssistant(null);
			return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * sends user to phone, calls the user's clinician.
     */
    @SuppressWarnings("MissingPermission")
    public void callClinician(View v) {
        startPhoneCall(PersistentData.getPrimaryCareNumber());
    }

    /**
     * sends user to phone, calls the user's clinician.
     */
    @SuppressWarnings("MissingPermission")
    public void transit(View v) {
        Intent i = new Intent(this, MapsActivity.class);
        if(NetworkUtility.networkIsAvailable(this))startActivity(i);
        else NetWorkIsNotAvailable(v);
    }

    public void notification(View v) {
        Intent i = new Intent(this, NotificationActivity.class);
        if(NetworkUtility.networkIsAvailable(this))startActivity(i);
        else NetWorkIsNotAvailable(v);
    }

    public void NetWorkIsNotAvailable(View v) {
        StyleableToast.makeText(this, "Sorry, please turn on your network", R.style.mytoast).show();
    }
    //display the unavailable feature
    public void featureIsNotAvailable(View v) {
        StyleableToast.makeText(this, "Sorry, this feature is temporary unavailable", R.style.mytoast).show();
    }

    public void web(View v) {
        Intent i = new Intent(this, JobsActivity.class);
        startActivity(i);
    }

    /**
     * sends user to phone, calls the study's research assistant.
     */
    @SuppressWarnings("MissingPermission")
    public void callResearchAssistant(View v) {
        startPhoneCall(PersistentData.getPasswordResetNumber());
    }

    private void startPhoneCall(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        try {
            startActivity(callIntent);
        } catch (SecurityException e) {
            showAlertThatEnablesUserToGrantPermission(this,
                    getString(R.string.cant_make_a_phone_call_permissions_alert),
                    getString(R.string.cant_make_phone_call_alert_title),
                    0);
        }
    }

	/*####################################################################
	###################### Permission Prompting ##########################
	####################################################################*/

    private static Boolean prePromptActive = false;
    private static Boolean postPromptActive = false;
    private static Boolean powerPromptActive = false;
    private static Boolean thisResumeCausedByFalseActivityReturn = false;
    private static Boolean aboutToResetFalseActivityReturn = false;
    private static Boolean activityNotVisible = false;

    public Boolean isAudioRecorderActivity() {
        return false;
    }

    private void goToSettings(Integer permissionIdentifier) {
        // Log.i("sessionActivity", "goToSettings");
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, permissionIdentifier);
    }

    @TargetApi(23)
    private void goToPowerSettings(Integer powerCallbackIdentifier) {
        // Log.i("sessionActivity", "goToSettings");
        @SuppressLint("BatteryLife") Intent powerSettings = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName()));
        powerSettings.addCategory(Intent.CATEGORY_DEFAULT);
        powerSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(powerSettings, powerCallbackIdentifier);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Log.i("sessionActivity", "onActivityResult. requestCode: " + requestCode + ", resultCode: " + resultCode );
        aboutToResetFalseActivityReturn = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Log.i("sessionActivity", "onRequestPermissionResult");
        if (!activityNotVisible) checkPermissionsLogic();
    }

    protected void checkPermissionsLogic() {
        //gets called as part of onResume,
        activityNotVisible = false;
        // Log.i("sessionactivity", "checkPermissionsLogic");
        // Log.i("sessionActivity", "prePromptActive: " + prePromptActive);
        // Log.i("sessionActivity", "postPromptActive: " + postPromptActive);
        // Log.i("sessionActivity", "thisResumeCausedByFalseActivityReturn: " + thisResumeCausedByFalseActivityReturn);
        // Log.i("sessionActivity", "aboutToResetFalseActivityReturn: " + aboutToResetFalseActivityReturn);

        if (aboutToResetFalseActivityReturn) {
            aboutToResetFalseActivityReturn = false;
            thisResumeCausedByFalseActivityReturn = false;
            return;
        }

        if (!thisResumeCausedByFalseActivityReturn) {
            String permission = PermissionHandler.getNextPermission(getApplicationContext(), this.isAudioRecorderActivity());
            if (permission == null) {
                return;
            }

            if (!prePromptActive && !postPromptActive && !powerPromptActive) {
                if (permission == PermissionHandler.POWER_EXCEPTION_PERMISSION) {
                    showPowerManagementAlert(this, getString(R.string.power_management_exception_alert), 1000);
                    return;
                }
                // Log.d("sessionActivity", "shouldShowRequestPermissionRationale "+ permission +": " + shouldShowRequestPermissionRationale( permission ) );
                if (shouldShowRequestPermissionRationale(permission)) {
                    if (!prePromptActive && !postPromptActive) {
                        showAlertThatForcesUserToGrantPermission(this, PermissionHandler.getBumpingPermissionMessage(permission),
                                PermissionHandler.permissionMap.get(permission));
                    }
                } else if (!prePromptActive && !postPromptActive) {
                    showRegularPermissionAlert(this, PermissionHandler.getNormalPermissionMessage(permission),
                            permission, PermissionHandler.permissionMap.get(permission));
                }
            }
        }
    }

    /* Message Popping */
    public static void showRegularPermissionAlert(final Activity activity, final String message, final String permission, final Integer permissionCallback) {
        // Log.i("sessionActivity", "showPreAlert");
        if (prePromptActive) {
            return;
        }
        prePromptActive = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Permissions Requirement:");
        builder.setMessage(message);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activity.requestPermissions(new String[]{permission}, permissionCallback);
                prePromptActive = false;
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        }); //Okay button
        builder.create().show();
    }

    public static void showAlertThatForcesUserToGrantPermission(final RunningBackgroundServiceActivity activity, final String message, final Integer permissionCallback) {
        // Log.i("sessionActivity", "showPostAlert");
        if (postPromptActive) {
            return;
        }
        postPromptActive = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Permissions Requirement:");
        builder.setMessage(message);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                thisResumeCausedByFalseActivityReturn = true;
                activity.goToSettings(permissionCallback);
                postPromptActive = false;
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        }); //Okay button
        builder.create().show();
    }

    public static void showAlertThatEnablesUserToGrantPermission(final RunningBackgroundServiceActivity activity, final String message, final String title, final Integer permissionCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                activity.goToSettings(permissionCallback);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
            }
        });
        builder.create().show();
    }

    public static void showPowerManagementAlert(final RunningBackgroundServiceActivity activity, final String message, final Integer powerCallbackIdentifier) {
        Log.i("sessionActivity", "power alert");
        if (powerPromptActive) {
            return;
        }
        powerPromptActive = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Permissions Requirement:");
        builder.setMessage(message);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d("power management alert", "bumping");
                thisResumeCausedByFalseActivityReturn = true;
                activity.goToPowerSettings(powerCallbackIdentifier);
                powerPromptActive = false;
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        }); //Okay button
        builder.create().show();
    }
}