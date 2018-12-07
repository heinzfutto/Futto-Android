package org.futto.app;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.futto.app.networking.PostRequest;
import org.futto.app.storage.PersistentData;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import io.sentry.Sentry;
import io.sentry.event.BreadcrumbBuilder;

public class CrashHandler implements java.lang.Thread.UncaughtExceptionHandler{
	private final Context errorHandlerContext;
	private int millisecondsUntilRestart = 500;
	public CrashHandler(Context context) { this.errorHandlerContext = context; }

	/** This function is where any errors that occur in any Activity that inherits RunningBackgroundServiceActivity
	 * will dump its errors.  We roll it up, stick it in a file, and try to restart the app after exiting it.
	 * (using a new alarm like we do in the BackgroundService). */
	public void uncaughtException(Thread thread, Throwable exception){

		Log.w("CrashHandler Raw","start original stacktrace");
		exception.printStackTrace();
		Log.w("CrashHandler Raw","end original stacktrace");

		//Write that log file
		Sentry.getContext().recordBreadcrumb(
				new BreadcrumbBuilder().setMessage("Attempting application restart").build()
		);
		writeCrashlog(exception, errorHandlerContext);
//		Log.i("inside crashlog", "does this line happen");  //keep this line for debugging crashes in the crash handler (yup.)
		//setup to restart service
		Intent restartServiceIntent = new Intent( errorHandlerContext, thread.getClass() );
		restartServiceIntent.setPackage( errorHandlerContext.getPackageName() );
		PendingIntent restartServicePendingIntent = PendingIntent.getService( errorHandlerContext, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT );
		AlarmManager alarmService = (AlarmManager) errorHandlerContext.getSystemService( Context.ALARM_SERVICE );
		alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + millisecondsUntilRestart, restartServicePendingIntent);
		//exit beiwe
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(10);
	}


	/**Creates a crash log file that will be uploaded at the next upload event.
	 * Also writes error to the error log so that it is visible in logcat.
	 * @param exception A Throwable (probably your error).
	 * @param context An android Context */
	public static void writeCrashlog(Throwable exception, Context context) {

		try {
			Sentry.getContext().addTag("user_id", PersistentData.getPatientID());
			Sentry.getContext().addTag("server_url", PostRequest.addWebsitePrefix(""));
			Sentry.capture(exception);
		}
		catch(Exception e1) {
			String exceptionInfo =  System.currentTimeMillis() + "\n"
					+ "BeiweVersion:" + DeviceInfo.getBeiweVersion()
					+ ", AndroidVersion:" + DeviceInfo.getAndroidVersion()
					+ ", Product:" + DeviceInfo.getProduct()
					+ ", Brand:" + DeviceInfo.getBrand()
					+ ", HardwareId:" + DeviceInfo.getHardwareId()
					+ ", Manufacturer:" + DeviceInfo.getManufacturer()
					+ ", Model:" + DeviceInfo.getModel() + "\n";

			exceptionInfo += "Error message: " + exception.getMessage() + "\n";
			exceptionInfo += "Error type: " + exception.getClass() + "\n";

			if (exception.getSuppressed().length > 0) {
				for (Throwable throwable: exception.getSuppressed() ) {
					exceptionInfo += "\nSuppressed Error:\n";
					for (StackTraceElement element : throwable.getStackTrace() ) { exceptionInfo +="\t" + element.toString() + "\n"; }
				}
			}

			//We encountered an error exactly once where we had a null reference inside this function,
			// this occurred when downloading a new survey to test that randomized surveys worked,
			// crashed with a null reference error on an element of a stacktrace. We now check for null.
			if (exception.fillInStackTrace().getStackTrace() != null) {
				exceptionInfo += "\nError-fill:\n";
				for (StackTraceElement element : exception.fillInStackTrace().getStackTrace()) {
					exceptionInfo += "\t" + element.toString() + "\n";
				}
			}
			else { exceptionInfo += "java threw an error with an error-fill stack trace that was null."; }

			if (exception.getCause() != null && exception.getCause().getStackTrace() != null) {
				exceptionInfo += "\nActual Error:\n";
				for (StackTraceElement element : exception.getCause().getStackTrace()) {
					exceptionInfo += "\t" + element.toString() + "\n";
				}
			}
			else { exceptionInfo += "java threw an error with a null error cause or stack trace, this means we are manually creating a crash report."; }

			//Print an error log if debug mode is active.
			if (BuildConfig.APP_IS_BETA) {
				Log.e("BEIWE ENCOUNTERED THIS ERROR", exceptionInfo); //Log error...
			}

			FileOutputStream outStream; //write a file...
			try { outStream = context.openFileOutput("crashlog_" + System.currentTimeMillis(), Context.MODE_APPEND);
				outStream.write( ( exceptionInfo ).getBytes() );
				outStream.flush(); outStream.close(); }
			catch (FileNotFoundException e) {
				Log.e("Error Handler Failure", "Could not write to file, file DNE.");
				e.printStackTrace(); }
			catch (IOException e) {
				Log.e("Error Handler Failure", "Could not write to file, IOException.");
				e.printStackTrace(); }
		}
	}
}