package org.futto.app.survey;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.futto.app.JSONUtils;
import org.futto.app.R;
import org.futto.app.session.SessionActivity;
import org.futto.app.storage.PersistentData;
import org.futto.app.storage.TextFileManager;
import org.futto.app.ui.user.MainMenuActivity;
import org.futto.app.ui.utils.SurveyNotifications;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**The SurveyActivity displays to the user the survey that has been pushed to the device.
 * Layout in this activity is rendered, not static.
 * @author Josh Zagorsky, Eli Jones */

public class SurveyActivity extends SessionActivity implements
        QuestionFragment.OnGoToNextQuestionListener,
        SurveySubmitFragment.OnSubmitButtonClickedListener {
	private String surveyId;
	private JsonSkipLogic surveySkipLogic;
	private boolean hasLoadedBefore = false;
	private long initialViewMoment;
	private QuestionFragment questionFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialViewMoment = System.currentTimeMillis();
		setContentView(R.layout.activity_survey);
		Intent triggerIntent = getIntent();
		surveyId = triggerIntent.getStringExtra("surveyId");
//		if (savedInstanceState == null) {
//			Bundle extras = getIntent().getExtras();
//			if (extras != null) {
//				answers = new ArrayList<>();
//			}
//		}
	}


	@Override
	protected void doBackgroundDependentTasks() {
		super.doBackgroundDependentTasks();
		if (!hasLoadedBefore ) {
			setUpQuestions(surveyId);
			// Run the logic as if we had just pressed next without answering a hypothetical question -1
			goToNextQuestion(null);
			// Record the time that the survey was first visible to the user
			SurveyTimingsRecorder.recordSurveyFirstDisplayed(surveyId);
			// Onnela lab requested this line in the debug log
			TextFileManager.getDebugLogFile().writeEncrypted(initialViewMoment + " opened survey " + surveyId + ".");
			hasLoadedBefore = true;
		}
	}


	@Override
    public void goToNextQuestion(QuestionData dataFromOldQuestion) {
		// store the answer from the previous question
		if (dataFromOldQuestion != null) {
			surveySkipLogic.setAnswer(dataFromOldQuestion);
		}

	    JSONObject nextQuestion = surveySkipLogic.getNextQuestion();
        // If you've run out of questions, display the Submit button
        if (nextQuestion == null) { displaySurveySubmitFragment(); }
        else { displaySurveyQuestionFragment(nextQuestion, surveySkipLogic.onFirstQuestion()); }
    }


	@Override
	public void onBackPressed() {
        super.onBackPressed();
		// In order oto do that we need to execute the fragment's getAnswer function.
		// surveySkipLogic.setAnswer( questionFragment.getAnswer(...) );
		surveySkipLogic.goBackOneQuestion();
	}


    private void displaySurveyQuestionFragment(JSONObject jsonQuestion, Boolean isFirstQuestion) {
		// Create a question fragment with the attributes of the question
		questionFragment = new QuestionFragment();
		questionFragment.setArguments(QuestionJSONParser.getQuestionArgsFromJSONString(jsonQuestion));

		// Put the fragment into the view
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if (isFirstQuestion) {
            fragmentTransaction.add(R.id.questionFragmentGoesHere, questionFragment);
        } else {
            fragmentTransaction.replace(R.id.questionFragmentGoesHere, questionFragment);
            fragmentTransaction.addToBackStack(null);
        }
		fragmentTransaction.commit();
	}


    private void displaySurveySubmitFragment() {
		Bundle args = new Bundle();
		args.putStringArrayList("unansweredQuestions", surveySkipLogic.getUnansweredQuestions());

		SurveySubmitFragment submitFragment = new SurveySubmitFragment();
		submitFragment.setArguments(args);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.questionFragmentGoesHere, submitFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


	private void setUpQuestions(String surveyId) {
		// Get survey settings
		Boolean randomizeWithMemory = false;
		Boolean randomize = false;
		int numberQuestions = 0;
		try {
			JSONObject surveySettings = new JSONObject(PersistentData.getSurveySettings(surveyId));
			randomizeWithMemory = surveySettings.optBoolean(getString(R.string.randomizeWithMemory), false);
			randomize = surveySettings.optBoolean(getString(R.string.randomize), false);
			numberQuestions = surveySettings.optInt(getString(R.string.numberQuestions), 0);
		} catch (JSONException e) {
			Log.e("Survey Activity", "There was an error parsing survey settings");
			e.printStackTrace();
		}

		try { // Get survey content as an array of questions; each question is a JSON object
			JSONArray jsonQuestions = new JSONArray(PersistentData.getSurveyContent(surveyId));
			// If randomizing the question order, reshuffle the questions in the JSONArray
			if (randomize && !randomizeWithMemory) { jsonQuestions = JSONUtils.shuffleJSONArray(jsonQuestions, numberQuestions); }
			if (randomize && randomizeWithMemory) { jsonQuestions = JSONUtils.shuffleJSONArrayWithMemory(jsonQuestions, numberQuestions, surveyId); }
			//construct the survey's skip logic.
			//(param 2: If randomization is enabled do not run the skip logic for the survey.)
			surveySkipLogic = new JsonSkipLogic(jsonQuestions, !randomize, getApplicationContext());
		} catch (JSONException e) { e.printStackTrace(); }
	}

	public QuestionData getCurrentQuestionData(){ return surveySkipLogic.getCurrentQuestionData(); }

	/**Called when the user presses "Submit" at the end of the survey,
	 * saves the answers, and takes the user back to the main page. */
	@Override
	public void submitButtonClicked() {
		SurveyTimingsRecorder.recordSubmit(getApplicationContext());

		// Write the data to a SurveyAnswers file
		SurveyAnswersRecorder answersRecorder = new SurveyAnswersRecorder();
		// Show a Toast telling the user either "Thanks, success!" or "Oops, there was an error"
		String toastMsg = null;
		if (answersRecorder.writeLinesToFile(surveyId, surveySkipLogic.getQuestionsForSerialization())) {
			toastMsg = PersistentData.getSurveySubmitSuccessToastText();
		} else {
			toastMsg = getApplicationContext().getResources().getString(R.string.survey_submit_error_message);
		}
		Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();

		// Close the Activity
		startActivity(new Intent(getApplicationContext(), MainMenuActivity.class));
		PersistentData.setSurveyNotificationState(surveyId, false);
		SurveyNotifications.dismissNotification(getApplicationContext(), surveyId);
		finish();
	}
}
