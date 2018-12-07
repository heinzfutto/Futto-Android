package org.futto.app.survey;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.futto.app.CrashHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Created by elijones on 12/1/16. */

//TODO: implement a serialization and/or iterable output blob.  need to see what survey anwsers actually look like to determine all the values it needs

public class JsonSkipLogic {

	/*############################# Assets ###################################*/

	//Comparator sets
	private static Set<String> COMPARATORS = new HashSet<String>(4);
	private static Set<String> BOOLEAN_OPERATORS = new HashSet<String>(2);
	static {
		COMPARATORS.add("<"); //Setup numerics
		COMPARATORS.add(">");
		COMPARATORS.add("<=");
		COMPARATORS.add(">=");
		COMPARATORS.add("=="); //Setup == / !=
		COMPARATORS.add("!=");
		BOOLEAN_OPERATORS.add("and"); //Setup boolean operators
		BOOLEAN_OPERATORS.add("or");
		BOOLEAN_OPERATORS = Collections.unmodifiableSet(BOOLEAN_OPERATORS); //Block modification of all the sets.
		COMPARATORS = Collections.unmodifiableSet(COMPARATORS);
	}

	// For checking equality between Doubles.
	// http://stackoverflow.com/questions/25160375/comparing-double-values-for-equality-in-java
	private static final String NUMERIC_OPEN_RESPONSE_FORMAT = "%.5f";
	public static final Double ANSWER_COMPARISON_EQUALITY_DELTA = 0.00001;


	private static boolean isEqual(double d1, double d2) {
		return d1 == d2 || isRelativelyEqual(d1,d2); //this short circuit just makes it faster
	}

	/** checks if the numbers are separated by a predefined absolute difference.*/
	private static boolean isRelativelyEqual(double d1, double d2) {
		return ANSWER_COMPARISON_EQUALITY_DELTA > Math.abs(d1 - d2) / Math.max(Math.abs(d1), Math.abs(d2));
	}


	private HashMap<String, QuestionData> QuestionAnswer;
	private HashMap<String, JSONObject> QuestionSkipLogic;
	private HashMap<String, JSONObject> Questions;
	private ArrayList<String> QuestionOrder;
	private Integer currentQuestion;
	private Boolean runDisplayLogic;
	private Context appContext;

	/**@param jsonQuestions The content of the "content" key in a survey
	 * @param runDisplayLogic A boolean value for whether skip Logic should be run on this survey.
	 * @param applicationContext An application context is required in order to extend exception handling.
	 * @throws JSONException thrown if there are any questions without question ids. */
	public JsonSkipLogic(JSONArray jsonQuestions, Boolean runDisplayLogic, Context applicationContext) throws JSONException {
		appContext = applicationContext;
		final int MAX_SIZE = jsonQuestions.length();
		String questionId;
		JSONObject question;
		JSONObject displayLogic;

		//construct the various question id collections
		QuestionAnswer = new HashMap<String, QuestionData> (MAX_SIZE);
		QuestionSkipLogic = new HashMap<String, JSONObject> (MAX_SIZE);
		Questions = new HashMap<String, JSONObject> (MAX_SIZE);
		QuestionOrder = new ArrayList<String> (MAX_SIZE);

		for (int i = 0; i < MAX_SIZE; i++) { //uhg, you can't iterate over a JSONArray.
			question = jsonQuestions.optJSONObject(i);
			questionId = question.getString("question_id");

			Questions.put(questionId, question); //store questions by id
			QuestionOrder.add(questionId); //setup question order

			//setup question logic
			if ( question.has("display_if") ) { //skip item if it has no display_if item
				Log.v("debugging json content", " " + question.toString() );
				displayLogic = question.optJSONObject("display_if");
				if (displayLogic != null) { //skip if display logic exists but is null
					QuestionSkipLogic.put(questionId, displayLogic);
				}
			}
		}
		this.runDisplayLogic = runDisplayLogic;
		currentQuestion = -1; //set the current question to -1, makes getNextQuestionID less annoying.
	}


	/**@param questionId Takes a question id
	 * @return returns a QuestionData if it has been answered, otherwise null. */
	public QuestionData getQuestionAnswer(String questionId) { return QuestionAnswer.get(questionId); }

	/** @return the QuestionData object for the current question, null otherwise */
	public QuestionData getCurrentQuestionData() {
		if (currentQuestion >= QuestionOrder.size()) { return null; }
		return QuestionAnswer.get(QuestionOrder.get(currentQuestion));
	}

	/** Determines question should be displayed next.
	 * @return a question id string, null if there is no next item. */
	@SuppressWarnings("TailRecursion")
	private JSONObject getQuestion(Boolean goForward) {
		if (goForward) currentQuestion++;
		else currentQuestion--;

		//if currentQuestion is set to anything less than zero, reset to zero (shouldn't occur)
		if (currentQuestion < 0) {
//			Log.w("json logic", "underflowed...");
			currentQuestion = 0; }

		//if it is the first question it should invariably display.
		if (currentQuestion == 0) {
//			Log.i("json logic", "skipping logic and displaying first question");
			if (QuestionOrder.size() == 0) { return null; }
			else { return Questions.get(QuestionOrder.get(0)); }}

		//if we would overflow the list (>= size) we are done, return null.
		if (currentQuestion >= QuestionOrder.size()) {
//			Log.w("json logic", "overflowed...");
			return null; }
		//if display logic has been disabled we skip logic processing and return the next question
		if (!runDisplayLogic) {
//			Log.d("json logic", "runDisplayLogic set to true! doing all questions!");
			return Questions.get(QuestionOrder.get(currentQuestion)); }

		String questionId = QuestionOrder.get(currentQuestion);
//		Log.v("json logic", "starting question " + QuestionOrder.indexOf(questionId) + " (" + questionId + "))");
		// if questionId does not have skip logic we display it.

		if ( !QuestionSkipLogic.containsKey(questionId) ) {
//			Log.d("json logic", "Question " + QuestionOrder.indexOf(questionId) + " (" + questionId + ") has no skip logic, done.");
			return Questions.get(questionId);
		}
		if ( shouldQuestionDisplay(questionId) ) {
//			Log.d("json logic", "Question " + QuestionOrder.indexOf(questionId) + " (" + questionId + ") evaluated as true, done.");
			return Questions.get(questionId);
		}
		else {
//			Log.d("json logic", "Question " + QuestionOrder.indexOf(questionId) + " (" + questionId + ") did not evaluate as true, proceeding to next question...");
			/* If it didn't meet any of the above conditions (and didn't display a question), call
			this function recursively, and keep doing that until you reach a question that should
			display. */
			return getQuestion(goForward);
		}
	}

	public JSONObject getNextQuestion() { return getQuestion(true); }

	public JSONObject goBackOneQuestion() { return getQuestion(false); }


	/** @return whether the current logic is on question 1 */
	public Boolean onFirstQuestion(){ return currentQuestion < 1; }


	/** This function wraps the logic processing code.  If the logic processing encounters an error
	 * due to json parsing the behavior is to invariably return true.
	 * @param questionId
	 * @return Boolean result of the logic */
	private Boolean shouldQuestionDisplay(String questionId){
		try {
			JSONObject question = QuestionSkipLogic.get(questionId);
			//If the survey display logic object is null or is empty, display
			if (question == null || question.length() == 0) { return true; }
			return parseLogicTree(questionId, question);
		} catch (JSONException e) {
			Log.w("json exception while doing a logic parse", "=============================================================================================================================================");
			e.printStackTrace();
			CrashHandler.writeCrashlog(e, appContext);
		}
		return true;
	}

	private Boolean parseLogicTree(String questionId, JSONObject logic ) throws JSONException {
		// extract the comparator, force it as lower case.
		String comparator = logic.keys().next().toLowerCase();

		// logic.getXXX(comparator_as_key) is the only way to grab the value due to strong typing.
		// This object has many uses, so the following logic has been written for explicit clarity,
		// rather than optimized code length or performance.

		//We'll get the NOT out of the way first.
		//todo: Eli. test not, it may not be in the reference.
		if ( comparator.equals("not") ) {
			//we need to pass in the Json _Object_ of the next layer in
//			Log.d("json logic", "evaluating as NOT (invert)");
			return !parseLogicTree(questionId, logic.getJSONObject(comparator) );
		}

		if ( COMPARATORS.contains(comparator) ) {
			// in this case logic.getString(comparator) contains a json list/array with the first
			// element being the referencing question ID, and the second being a value to compare to.
			return runNumericLogic(comparator, logic.getJSONArray(comparator) );
		}

		if ( BOOLEAN_OPERATORS.contains(comparator) ) {
			//get array of logic operations
			JSONArray manyLogics = logic.getJSONArray(comparator);
			List<Boolean> results = new ArrayList<Boolean>(manyLogics.length());
//			Log.v("json logic", "evaluating as boolean, " + manyLogics.length() + " things to process...");

			//iterate over array, get the booleans into a list
			for (int i = 0; i < manyLogics.length(); i++) { //jsonArrays are not iterable...
				results.add( parseLogicTree(questionId, manyLogics.getJSONObject(i) ) );
			} //results now contains the boolean evaluation of all nested logics.

//			Log.v("json logic", "returning inside of " + QuestionOrder.indexOf(questionId) + " (" + questionId + ") after processing logic for boolean.");

			//And. if anything is false, return false. If those all pass, return true.
			if ( comparator.equals("and") ) {
				Log.d("logic meanderings", questionId + " AND bools: " + results.toString());
				for (Boolean bool : results) { if ( !bool ) { return false; } }
				return true;
			}
			//Or. if anything is true, return true. If those all pass, return false.
			if ( comparator.equals("or") ) {
				Log.d("logic meanderings", questionId + " OR bools: " + results.toString());
				for (Boolean bool : results) { if ( bool ) { return true; } }
				return false;
			}
		}
		throw new NullPointerException("received invalid comparator: " + comparator);
	}


	/** Processes the logical operation implemented of a comparator.
	 * If there has been no answer for the question a logic operation references this function returns false.
	 * @param comparator a string that is in the COMPARATORS constant.
	 * @param parameters json array 2 elements in length.  The first element is a target question ID to pull an answer from, the second is the survey's value to compare to.
	 * @return Boolean result of the operation, or false if the referenced question has no answer.
	 * @throws JSONException */
	private Boolean runNumericLogic(String comparator, JSONArray parameters) throws JSONException {
//		Log.d("json logic", "inside numeric logic: " + comparator + ", " + parameters.toString());
		String targetQuestionId = parameters.getString(0);
		if ( !QuestionAnswer.containsKey(targetQuestionId) ) { return false; } // false if DNE
		Double userAnswer = QuestionAnswer.get(targetQuestionId).getAnswerDouble();
		Double surveyValue = parameters.getDouble(1);

//		Log.d("logic...", "evaluating useranswer " + userAnswer + comparator + surveyValue);

		//If we encounter an unanswered question, that evaluates as false. (defined in the spec.)
		if ( userAnswer == null ) { return false; }

		if ( comparator.equals("<") ) {
			return userAnswer < surveyValue && !isEqual(userAnswer, surveyValue);  }
		if ( comparator.equals(">") ) {
			return userAnswer > surveyValue && !isEqual(userAnswer, surveyValue); }
  		if ( comparator.equals("<=") ) {
		    return userAnswer <= surveyValue || isEqual(userAnswer, surveyValue); } //the <= is slightly redundant, its fine.
		if ( comparator.equals(">=") ) {
			return userAnswer >= surveyValue || isEqual(userAnswer, surveyValue); } //the >= is slightly redundant, its fine.
		if ( comparator.equals("==") ) {
			return isEqual(userAnswer, surveyValue); }
		if ( comparator.equals("!=") ) {
			return !isEqual(userAnswer, surveyValue); }
		throw new NullPointerException("numeric logic fail");
	}


    @SuppressLint("DefaultLocale")
	public void setAnswer(QuestionData questionData) {
	    QuestionType.Type questionType = questionData.getType();
		// Ignore checkbox questions, since they don't have numeric answers
	    if ( questionType.equals(QuestionType.Type.FREE_RESPONSE) ) {//comes in as a string, coerce to float (don't bother coercing to integer
			if (questionData.getAnswerString() != null) {
				try {
					questionData.setAnswerDouble(Double.parseDouble(questionData.getAnswerString()));
				} catch (NumberFormatException e) {
					// If the number is un-parse-able, do nothing
				}
			}
	    }
        if ( questionType.equals(QuestionType.Type.SLIDER) ) { //comes in as an integer, coerce to float, coerce to string
		    if (questionData.getAnswerInteger() != null) {
			    questionData.setAnswerDouble( Double.valueOf(questionData.getAnswerInteger()) ); }
		    if (questionData.getAnswerDouble() != null) {
			    questionData.setAnswerString("" + questionData.getAnswerInteger()); }
	    }
	    if ( questionType.equals(QuestionType.Type.RADIO_BUTTON) ) {//comes in as an integer, coerce to float, coerce to string
		    if (questionData.getAnswerInteger() != null) {
			    questionData.setAnswerDouble( Double.valueOf(questionData.getAnswerInteger()) ); }
		    if (questionData.getAnswerDouble() != null) {
			    questionData.setAnswerString("" + questionData.getAnswerInteger()); }
	    }
	    QuestionAnswer.put(questionData.getId(), questionData);
    }


	/** @return a list of QuestionData objects for serialization to the answers file. */
	@SuppressWarnings("ObjectAllocationInLoop")
	public List<QuestionData> getQuestionsForSerialization() {
		List<QuestionData> answers = new ArrayList<QuestionData>(QuestionOrder.size());
		for (String questionId : QuestionOrder)
			if ( QuestionAnswer.containsKey(questionId) )
				answers.add(QuestionAnswer.get(questionId));
		return answers;
	}

	/**@return a list of QuestionData objects A) should have displayed, B) will be accessible by paging back, C) don't have answers.*/
	public ArrayList<String> getUnansweredQuestions() {
		ArrayList<String> unanswered = new ArrayList<> (QuestionOrder.size());
		QuestionData question;
		int questionDisplayNumber = 0;
		Boolean questionWouldDisplay;

		//Guarantee: the questions in QuestionAnswer will consist of all _displayed_ questions.
		for (String questionId : QuestionOrder) {

			//QuestionData objects are put in the QuestionAnswers dictionary if they are ever
			// displayed. (The user must also proceed to the next question, but that has no effect.)
			//No QuestionAnswer object means question did not display, which means we can skip it.
			if ( QuestionAnswer.containsKey(questionId) ) {
				//A user may have viewed questions along a Display Logic Path A, but failed to answer
				// certain questions, then reversed and changed some answers. If so they may have created
				// a logic path B that no longer displays a previously answered question.
				//If that occurred then we have a a QuestionData object in QuestionAnswers that
				// effectively should not have displayed (returns false on evaluation of
				// shouldQuestionDisplay), so we need to catch that case.
				//The only way to catch that case is to run shouldQuestionDisplay on every QuestionData
				// object in QuestionAnswers.
				//There is one exception: INFO_TEXT_BOX questions are always ignored.
				question = QuestionAnswer.get(questionId);

				//INFO_TEXT_BOX - skip it.
				if ( question.getType().equals(QuestionType.Type.INFO_TEXT_BOX) ) {
					continue;
				}
				//check if should display, store value
				questionWouldDisplay = shouldQuestionDisplay(questionId);

				if ( questionWouldDisplay ) { //If would display, increment question number.
					questionDisplayNumber++;
				} else { //If would not display not, skip it without incrementing.
					continue;
				}

				//If question is actually unanswered construct a display string.
				if ( !question.questionIsAnswered() ){
					unanswered.add("Question " + questionDisplayNumber + ": " + question.getText());
				}
			}
		}
		return unanswered;
	}
}
