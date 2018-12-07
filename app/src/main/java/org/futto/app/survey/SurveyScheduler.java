package org.futto.app.survey;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.futto.app.BackgroundService;
import org.futto.app.CrashHandler;
import org.futto.app.JSONUtils;
import org.futto.app.R;
import org.futto.app.storage.PersistentData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
  
//TODO: Low priority. Eli. document.
/**I am ... 85% certain time zones work like this:
When a survey trigger is scheduled, that event is in terms of relative time, i.e. "in 3 hours do a thing."
That relative-time event is (pretty sure) calculated using a time-zone aware entity.
The result is that any currently-waiting event triggers will not be changed by a time zone transition, but any new survey trigger calculations will be in the current (new) timezone.
*/

public class SurveyScheduler {
	
	public static void checkImmediateTriggerSurvey(Context appContext, String surveyId) {
		JSONObject surveySettings;
		try { surveySettings = new JSONObject( PersistentData.getSurveySettings(surveyId) ); }
		catch (JSONException e) {
			Log.e("SurveyScheduler", "There was an error parsing survey settings");
			e.printStackTrace();
			CrashHandler.writeCrashlog(e, appContext);
			surveySettings = new JSONObject();
		}
		// Log.i("SurveyScheduler", "id: " + surveyId + ", survey settings: " + surveySettings );
		if (surveySettings.optBoolean(appContext.getString(R.string.triggeredSurvey), false) ) {
//			Log.i("SurveyScheduler", "it's triggered! yaaay!");
			appContext.sendBroadcast(new Intent(surveyId));
		}
	}
	
	public static void scheduleSurvey(String surveyId) {
		int today;
		JSONArray JSONTimes;
		JSONArray dayJSON;
		ArrayList<Integer> dayInts;
		ArrayList<ArrayList<Integer>> timesList = new ArrayList<ArrayList<Integer>>(7);
		String timeString = PersistentData.getSurveyTimes(surveyId);
		
		Calendar thisDay = Calendar.getInstance();
		//turns out the first day of the week is not necessarily Sunday. So, in case the user is in such a Locale we manually set that.
		thisDay.setFirstDayOfWeek(Calendar.SUNDAY);
		today = thisDay.get(Calendar.DAY_OF_WEEK) - 1;
		
		try { JSONTimes = new JSONArray(timeString); }
		catch (JSONException e) { e.printStackTrace(); //If this fails we have significant problems, but probably the errors come from external factors.
			throw new NullPointerException(e.getMessage()); }
		List<String> jsonDays = JSONUtils.jsonArrayToStringList(JSONTimes);
		
		//create a list of days of the week, with order like this:
		// 0: today, 1: tomorrow, 2: day after ...
		List<String> reorderedDays = new ArrayList<String>();
		reorderedDays.addAll( jsonDays.subList( today, jsonDays.size() ) );
		reorderedDays.addAll( jsonDays.subList(0, today) );
		
		for (int i=0; i <= 6; i++) {
			try { dayJSON = new JSONArray(reorderedDays.get(i)); }  //convert to json array
			catch (JSONException e) { e.printStackTrace(); //Again, if this crashes we have problems but they probably come from external factors.
				throw new NullPointerException(e.getMessage()); }
			dayInts = JSONUtils.jsonArrayToIntegerList(dayJSON); //convert to (iteraable) list of ints
			Collections.sort(dayInts); //ensure sorted because... because.
			timesList.add( dayInts );
		}
//		Log.d("Scheduler", "day list before sorting: " + reorderedDays);
		// Log.d("Scheduler", "day list after sorting:  " + timesList);
		
//		we now have a double nested list of lists.  element 0 is today, element 1 is tomorrow
		// the inner list contains the times of day at which the survey should trigger, these times are values between 0 and 86,400
		// these values are should be sorted.
		// these values indicate the "seconds past midnight" of the day that the alarm should trigger.
		// we iterate through the nested list to come to the next time that is after right now.
		Calendar newAlarmTime = findNextAlarmTime(timesList);
		if (newAlarmTime == null) {
//			Log.w("SurveyScheduler", "there were no times at all in the provided timings list.");
			return; }
		BackgroundService.setSurveyAlarm(surveyId, newAlarmTime);
	}
	
	private static Calendar findNextAlarmTime( ArrayList<ArrayList<Integer>> timesList) {
		Calendar now = Calendar.getInstance();
		Calendar possibleAlarmTime = null;
		Calendar firstPossibleAlarmTime = null;
		Boolean firstFlag = true;
		int days = 0;
		for ( ArrayList<Integer> day : timesList ) { //iterate through the days of the week
//			Log.i("scheduler", "day: " + days);
			for (int time : day) { //iterate through the times in each day
//				Log.i("scheduler", "time: " + time);
				if (time > 86400 || time < 0) { throw new NullPointerException("time parser received an invalid value in the time parsing: " + time); }
				possibleAlarmTime = getTimeFromStartOfDayOffset(time);
				if (firstFlag) { //grab the first time we come across in case it falls into the edge case.
					firstPossibleAlarmTime = (Calendar) possibleAlarmTime.clone();
					firstFlag = false;
				}
				possibleAlarmTime.add(Calendar.DATE, days); //add to this time the appropriate number of days
				if ( possibleAlarmTime.after( now ) ) { //If the time is in the future, return that time.
					// Log.d("Scheduler", "checked, yup: " + possibleAlarmTime );
					return possibleAlarmTime;
				}
//				Log.d("Scheduler", "checked, nope: " + possibleAlarmTime);
			}
			days++; //advance to next day...
		}
		/* Warning: for some reason... if you try to throw a null pointer exception in here the app freezes. */
		//TODO: Low priority. Eli/Josh.  determine why the app stalls when nullpointerexceptions are thrown on... non gui threads?  insert a null pointer exception here and comment out the remainer of the function to see what I am talking about.
//		throw new NullPointerException("totally arbitrary message");
		if (firstPossibleAlarmTime == null) { return null; }
		firstPossibleAlarmTime.add(Calendar.DATE, 7);  // advance the date to the following week.
//		Log.d("Scheduler", "reverting to fallback: " + firstPossibleAlarmTime );
		return firstPossibleAlarmTime;
	}
	
	private static Calendar getTimeFromStartOfDayOffset(int offset) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, offset / 3600 ); //seconds divided by seconds per hour yields hour of day (this is the 24-hour time of day
//		calendar.set(Calendar.HOUR, offset / 3600 ); //do not set this value, it will override the hour_of_day value and do it incorrectly.
		calendar.set(Calendar.MINUTE, offset / 60 % 60); //seconds divided by sixty mod sixty yields minutes
		calendar.set(Calendar.SECOND, offset % 60); //seconds mod 60 yields seconds into minute
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.setFirstDayOfWeek(Calendar.SUNDAY);
//		Log.d("generated time", calendar.toString());
		return calendar;
	}
}