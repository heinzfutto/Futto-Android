package org.futto.app.survey;

// TODO: Low priority. Josh. is it OK to not use support.v4?

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.futto.app.R;

import java.util.Arrays;

import static org.futto.app.survey.SurveyAnswersRecorder.getAnswerIntegerValue;
import static org.futto.app.survey.SurveyAnswersRecorder.getAnswerString;
import static org.futto.app.survey.SurveyAnswersRecorder.getSelectedCheckboxes;

/**
 * Created by Josh Zagorsky on 11/30/16.
 */

public class QuestionFragment extends Fragment {
    OnGoToNextQuestionListener goToNextQuestionListener;
    QuestionData questionData;
    QuestionType.Type questionType;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /* XML views inflated by an Activity render with the app's default
		 * style (set in the Manifest.XML), but for some reason, XML views
		 * inflated by this class don't render with the app's default style,
		 * unless we set it manually: */

        // Render the question and inflate the layout for this fragment
        ScrollView fragmentQuestionLayout = (ScrollView) inflater.inflate(R.layout.fragment_question, null);
        FrameLayout questionContainer = (FrameLayout) fragmentQuestionLayout.findViewById(R.id.questionContainer);
        final View questionLayout = createQuestion(inflater, getArguments());
        questionContainer.addView(questionLayout);

        // Set an onClickListener for the "Next" button
        Button nextButton = (Button) fragmentQuestionLayout.findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextQuestionListener.goToNextQuestion(getAnswer(questionLayout, questionType));
            }
        });

        return fragmentQuestionLayout;
    }

    /* The following dual declaration is due to a change/deprecation in the Android
    Fragment _handling_ code.  It is difficult to determine exactly which API version
    this change occurs in, but the linked stack overflow article assumes API 23 (6.0).
    The difference in the declarations is that one takes an activity, and one takes
    a context.  Starting in 6 the OS guarantees a call to the one that takes an
    activity, previous versions called the one that takes an activity.
    ...
    If one of these is missing then goToNextQuestionListener fails to
    instantiate, causing a crash inside the onClick function for the next button.
    ...
    http://stackoverflow.com/questions/32604552/onattach-not-called-in-fragment */

    @Override
    /** This function will get called on NEW versions of Android (6+). */
    public void onAttach(Context context) {
        super.onAttach(context);
        goToNextQuestionListener = (OnGoToNextQuestionListener) context;
    }

    @Override
    /** This function will get called on OLD versions of Android (<6). */
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        goToNextQuestionListener = (OnGoToNextQuestionListener) activity;
    }

    private QuestionData getAnswer(View questionLayout, QuestionType.Type questionType) {
        String answerString = getAnswerString(questionLayout, questionType);
        if (answerString != null) { questionData.setAnswerString(answerString); }
        Integer answerIntegerValue = getAnswerIntegerValue(questionLayout, questionType);
        if (answerIntegerValue != null) { questionData.setAnswerInteger(answerIntegerValue); }
        return questionData;
    }

    private View createQuestion(LayoutInflater inflater, Bundle args) {
        String questionID = args.getString("question_id");
        String questionType = args.getString("question_type");
        String questionText = args.getString("question_text");
        if (questionType.equals("info_text_box")) {
            this.questionType = QuestionType.Type.INFO_TEXT_BOX;
            return createInfoTextbox(inflater, questionID, questionText);
        } else if (questionType.equals("slider")) {
            this.questionType = QuestionType.Type.SLIDER;
            int min = args.getInt("min");
            int max = args.getInt("max");
            return createSliderQuestion(inflater, questionID, questionText, min, max);
        } else if (questionType.equals("radio_button")) {
            this.questionType = QuestionType.Type.RADIO_BUTTON;
            String[] answers = args.getStringArray("answers");
            return createRadioButtonQuestion(inflater, questionID, questionText, answers);
        } else if (questionType.equals("checkbox")) {
            this.questionType = QuestionType.Type.CHECKBOX;
            String[] answers = args.getStringArray("answers");
            return createCheckboxQuestion(inflater, questionID, questionText, answers);
        } else if (questionType.equals("free_response")) {
            this.questionType = QuestionType.Type.FREE_RESPONSE;
            int textFieldTypeInt = args.getInt("text_field_type");
            TextFieldType.Type textFieldType = TextFieldType.Type.values()[textFieldTypeInt];
            return createFreeResponseQuestion(inflater, questionID, questionText, textFieldType);
        }
        // Default: return an error message
        return inflater.inflate(R.layout.survey_info_textbox, null);
    }


    private void conditionallyPrepareExistingAnswers(){
        /* We need to check whether questionData already has an object assigned to it, which occurs
        when the back button gets pressed and pops the backstack.  When the next button is pressed
        we pull any answer that has been saved by the activity.
        This operation may do nothing (re-set value to null) if there is no answer, that is fine. */
        if (questionData == null) {
            questionData = ((SurveyActivity) getActivity()).getCurrentQuestionData();
        }
    }


    /**
     * Creates an informational text view that does not have an answer type
     * @param infoText The informational text
     * @return TextView (to be displayed as question text)
     */
    private TextView createInfoTextbox(LayoutInflater inflater, String questionID, String infoText) {

        MarkDownTextView infoTextbox = (MarkDownTextView) inflater.inflate(R.layout.survey_info_textbox, null);

        // Clean inputs
        if (infoText == null) {
            // Set the question text to the error string
            try {
                infoText = getContext().getResources().getString(R.string.question_error_text);
            } catch (NoSuchMethodError e) {
                infoText = getActivity().getResources().getString(R.string.question_error_text);
            }
        }
        infoTextbox.setText(infoText);
        return infoTextbox;
    }


    /**
     * Creates a slider with a range of discrete values
     * @param questionText The text of the question to be asked
     * @return LinearLayout A slider bar
     */
    private LinearLayout createSliderQuestion(LayoutInflater inflater, String questionID,
                                             String questionText, int min, int max) {

        LinearLayout question = (LinearLayout) inflater.inflate(R.layout.survey_slider_question, null);
        SeekBarEditableThumb slider = (SeekBarEditableThumb) question.findViewById(R.id.slider);

        // Set the text of the question itself
        MarkDownTextView questionTextView = (MarkDownTextView) question.findViewById(R.id.questionText);
        if (questionText != null) { questionTextView.setText(questionText); }

        // The min must be greater than the max, and the range must be at most 100.
        // If the min and max don't fit that, reset min to 0 and max to 100.
        if ((min > (max - 1)) || ((max - min) > 100)) {
            min = 0;
            max = 100;
        }

        // Set the slider's range and default/starting value
        slider.setMax(max - min);
        slider.setMin(min);

        conditionallyPrepareExistingAnswers();
        if ((questionData != null) && (questionData.getAnswerInteger() != null)) {
            slider.setProgress(questionData.getAnswerInteger());
            slider.markAsTouched();
        } else {
            // Make the slider invisible until it's touched (so there's effectively no default value)
            slider.setProgress(0);
            makeSliderInvisibleUntilTouched(slider);
            // Create text strings that represent the question and its answer choices
            String options = "min = " + min + "; max = " + max;
            questionData = new QuestionData(questionID, QuestionType.Type.SLIDER, questionText, options);
        }

        // Add a label above the slider with numbers that mark points on a scale
        addNumbersLabelingToSlider(inflater, question, min, max);

        // Set the slider to listen for and record user input
        slider.setOnSeekBarChangeListener(new SliderListener(questionData));

        return question;
    }


    /**
     * Creates a group of radio buttons
     * @param questionText The text of the question
     * @param answers An array of strings that are options matched with radio buttons
     * @return RadioGroup A vertical set of radio buttons
     */
    private LinearLayout createRadioButtonQuestion(LayoutInflater inflater, String questionID, String questionText, String[] answers) {

        LinearLayout question = (LinearLayout) inflater.inflate(R.layout.survey_radio_button_question, null);
        RadioGroup radioGroup = (RadioGroup) question.findViewById(R.id.radioGroup);

        // Set the text of the question itself
        MarkDownTextView questionTextView = (MarkDownTextView) question.findViewById(R.id.questionText);
        if (questionText != null) { questionTextView.setText(questionText); }

        // If the array of answers is null or too short, replace it with an error message
        if ((answers == null) || (answers.length < 2)) {
            String replacementAnswer;
            try {
                replacementAnswer = getContext().getResources().getString(R.string.question_error_text);
            } catch (NoSuchMethodError e) {
                replacementAnswer = getActivity().getResources().getString(R.string.question_error_text);
            }
            String[] replacementAnswers = {replacementAnswer, replacementAnswer};
            answers = replacementAnswers;
        }

        // Loop through the answer strings, and make each one a radio button option
        for (int i = 0; i < answers.length; i++) {
            RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.survey_radio_button, null);
            if (answers[i] != null) {
                radioButton.setText(answers[i]);
            }
            radioGroup.addView(radioButton);
        }

        conditionallyPrepareExistingAnswers();
        if ((questionData != null) && (questionData.getAnswerInteger() != null)) {
            radioGroup.check(radioGroup.getChildAt(questionData.getAnswerInteger()).getId());
        } else {
            // Create text strings that represent the question and its answer choices
            questionData = new QuestionData(questionID, QuestionType.Type.RADIO_BUTTON, questionText, Arrays.toString(answers));
        }

        // Set the group of radio buttons to listen for and record user input
        radioGroup.setOnCheckedChangeListener(new RadioButtonListener(questionData));

        return question;
    }



    /**
     * Creates a question with an array of checkboxes
     * @param questionText The text of the question
     * @param options Each string in options[] will caption one checkbox
     * @return LinearLayout a question with a list of checkboxes
     */
    private LinearLayout createCheckboxQuestion(LayoutInflater inflater, String questionID, String questionText, String[] options) {

        LinearLayout question = (LinearLayout) inflater.inflate(R.layout.survey_checkbox_question, null);
        LinearLayout checkboxesList = (LinearLayout) question.findViewById(R.id.checkboxesList);

        // Set the text of the question itself
        MarkDownTextView questionTextView = (MarkDownTextView) question.findViewById(R.id.questionText);
        if (questionText != null) { questionTextView.setText(questionText); }

        String[] checkedAnswers = null;
        conditionallyPrepareExistingAnswers();
        if ((questionData != null) && (questionData.getAnswerString() != null)) {
            String answerString = questionData.getAnswerString();
            if (answerString.length() > 2) {
                checkedAnswers = answerString.substring(1, answerString.length() - 1).split(", ");
            }
        } else {
            // Create text strings that represent the question and its answer choices
            questionData = new QuestionData(questionID, QuestionType.Type.CHECKBOX, questionText, Arrays.toString(options));
        }

        // Loop through the options strings, and make each one a checkbox option
        if (options != null) {
            for (int i = 0; i < options.length; i++) {

                // Inflate the checkbox from an XML layout file
                CheckBox checkbox = (CheckBox) inflater.inflate(R.layout.survey_checkbox, null);

                // Set the text if it's provided; otherwise leave text as default error message
                if (options[i] != null) {
                    checkbox.setText(options[i]);
                    // If it should be checked, check it
                    if ((checkedAnswers != null) && (Arrays.asList(checkedAnswers).contains(options[i]))) {
                        checkbox.setChecked(true);
                    }
                }

                // Make the checkbox listen for and record user input
                checkbox.setOnClickListener(new CheckboxListener(questionData));

                // Add the checkbox to the list of checkboxes
                checkboxesList.addView(checkbox);
            }
        }

        return question;
    }


    /**
     * Creates a question with an open-response, text-input field
     * @param questionText The text of the question
     * @param inputTextType The type of answer (number, text, etc.)
     * @return LinearLayout question and answer
     */
    private LinearLayout createFreeResponseQuestion(LayoutInflater inflater, String questionID,
                                                   String questionText, TextFieldType.Type inputTextType) {
        //TODO: Josh. Give open response questions autofocus and make the keyboard appear
        LinearLayout question = (LinearLayout) inflater.inflate(R.layout.survey_open_response_question, null);

        // Set the text of the question itself
        MarkDownTextView questionTextView = (MarkDownTextView) question.findViewById(R.id.questionText);
        if (questionText != null) { questionTextView.setText(questionText); }

        EditText editText = null;
        switch (inputTextType) {
            case NUMERIC:
                editText = (EditText) inflater.inflate(R.layout.survey_free_number_input, null);
                break;

            case SINGLE_LINE_TEXT:
                editText = (EditText) inflater.inflate(R.layout.survey_free_text_input, null);
                break;

            case MULTI_LINE_TEXT:
                editText = (EditText) inflater.inflate(R.layout.survey_multiline_text_input, null);
                break;

            default:
                editText = (EditText) inflater.inflate(R.layout.survey_free_text_input, null);
                break;
        }

		/* Improvement idea: if you want to add date and time pickers as input
		 * types, here's a start: http://stackoverflow.com/a/14933515 */

		/* Improvement idea: when the user presses Enter, jump to the next
		 * input field */
        conditionallyPrepareExistingAnswers();
        if ((questionData != null) && (questionData.getAnswerString() != null)) {
            editText.setText(questionData.getAnswerString());
        } else {
            // Create text strings that represent the question and its answer choices
            String options = "Text-field input type = " + inputTextType.toString();
            questionData = new QuestionData(questionID, QuestionType.Type.FREE_RESPONSE, questionText, options);
        }

        // Set the text field to listen for and record user input
        editText.setOnFocusChangeListener(new OpenResponseListener(questionData));

        LinearLayout textFieldContainer = (LinearLayout) question.findViewById(R.id.textFieldContainer);
        textFieldContainer.addView(editText);

        return question;
    }


    /**
     * Adds a numeric scale above a Slider Question
     * @param question the Slider Question that needs a number scale
     * @param min the lowest number on the scale
     * @param max the highest number on the scale
     */
    private void addNumbersLabelingToSlider(LayoutInflater inflater, LinearLayout question, int min, int max) {
        // Replace the numbers label placeholder view (based on http://stackoverflow.com/a/3760027)
        View numbersLabel = (View) question.findViewById(R.id.numbersPlaceholder);
        int index = question.indexOfChild(numbersLabel);
        question.removeView(numbersLabel);
        numbersLabel = inflater.inflate(R.layout.survey_slider_numbers_label, question, false);
        LinearLayout label = (LinearLayout) numbersLabel.findViewById(R.id.linearLayoutNumbers);

		/* Decide whether to put 2, 3, 4, or 5 number labels. Pick the highest number of labels
		 * that can be achieved with each label above an integer value, and even spacing between
		 * all labels. */
        int numberOfLabels = 0;
        int range = max - min;
        if (range % 4 == 0) {
            numberOfLabels = 5;
        }
        else if (range % 3 == 0) {
            numberOfLabels = 4;
        }
        else if (range % 2 == 0) {
            numberOfLabels = 3;
        }
        else { numberOfLabels = 2; }

        // Create labels and spacers
        int numberResourceID = R.layout.survey_slider_single_number_label;
        for (int i = 0; i < numberOfLabels - 1; i++) {
            TextView number = (TextView) inflater.inflate(numberResourceID, label, false);
            label.addView(number);
            number.setText("" + (min + (i * range) / (numberOfLabels - 1)));

            View spacer = (View) inflater.inflate(R.layout.horizontal_spacer, label, false);
            label.addView(spacer);
        }
        // Create one last label (the rightmost one) without a spacer to its right
        TextView number = (TextView) inflater.inflate(numberResourceID, label, false);
        label.addView(number);
        number.setText("" + max);

        // Add the set of numeric labels to the question
        question.addView(numbersLabel, index);
    }


    /**
     * Make the "thumb" (the round circle/progress knob) of a Slider almost
     * invisible until the user touches it.  This way the user is forced to
     * answer every slider question; otherwise, we would not be able to tell
     * the difference between a user ignoring a slider and a user choosing to
     * leave a slider at the default value.  This makes it like there is no
     * default value.
     * @param slider
     */
    @SuppressLint("ClickableViewAccessibility")
    private void makeSliderInvisibleUntilTouched(SeekBarEditableThumb slider) {
        // Before the user has touched the slider, make the "thumb" transparent/ almost invisible
		/* Note: this works well on Android 4; there's a weird bug on Android 2 in which the first
		 * slider question in the survey sometimes appears with a black thumb (once you touch it,
		 * it turns into a white thumb). */
        slider.markAsUntouched();

        slider.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // When the user touches the slider, make the "thumb" opaque and fully visible
                SeekBarEditableThumb slider = (SeekBarEditableThumb) v;
                slider.markAsTouched();
                return false;
            }
        });
    }


    /**************************** ANSWER LISTENERS ***************************/

    /** Listens for a touch/answer to a Slider Question, and records the answer */
    private class SliderListener implements SeekBar.OnSeekBarChangeListener {

        QuestionData questionDescription;

        public SliderListener(QuestionData questionDescription) {
            this.questionDescription = questionDescription;
        }

        @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
			/* Improvement idea: record when the user started touching the
			 * slider bar, not just when they let go of it */
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            String answer = "";
            if (seekBar instanceof SeekBarEditableThumb) {
                SeekBarEditableThumb slider = (SeekBarEditableThumb) seekBar;
                answer += slider.getProgress() + slider.getMin();
            }
            else { answer += seekBar.getProgress(); }
            SurveyTimingsRecorder.recordAnswer(answer, questionDescription);
        }
    }


    /** Listens for a touch/answer to a Radio Button Question, and records the answer */
    private class RadioButtonListener implements RadioGroup.OnCheckedChangeListener {

        QuestionData questionDescription;

        public RadioButtonListener(QuestionData questionDescription) {
            this.questionDescription = questionDescription;
        }

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            RadioButton selectedButton = (RadioButton) group.findViewById(checkedId);
            if (selectedButton.isChecked()) {
                SurveyTimingsRecorder.recordAnswer(selectedButton.getText().toString(), questionDescription);
            }
            else {
				/* It should not be possible to un-check a radio button, but if
				 * that happens, record the answer as an empty string */
                SurveyTimingsRecorder.recordAnswer("", questionDescription);
            }
        }
    }


    /** Listens for a touch/answer to a Checkbox Question, and records the answer */
    private class CheckboxListener implements View.OnClickListener {

        QuestionData questionDescription;

        public CheckboxListener(QuestionData questionDescription) {
            this.questionDescription = questionDescription;
        }

        @Override
        public void onClick(View view) {
            // If it's a CheckBox and its parent is a LinearLayout
            if ((view instanceof CheckBox) && (view.getParent() instanceof LinearLayout)) {
                LinearLayout checkboxesList = (LinearLayout) view.getParent();
                String answersList = getSelectedCheckboxes(checkboxesList);
                SurveyTimingsRecorder.recordAnswer(answersList, questionDescription);
            }
        }
    }


    /** Listens for an input/answer to an Open/Free Response Question, and records the answer */
    private class OpenResponseListener implements View.OnFocusChangeListener {

        QuestionData questionDescription;

        public OpenResponseListener(QuestionData questionDescription) {
            this.questionDescription = questionDescription;
        }

        // TODO: Josh. replace this with a listener on the Next button; that'd probably make more sense
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                // The user just selected the input box

				/* Improvement idea: record when the user first touched the
				 * input field; right now it only records when the user
				 * selected away from the input field. */

                // Set the EditText so that if the user taps outside, the keyboard disappears
                if (v instanceof EditText) {
                    TextFieldKeyboard keyboard;
                    try {
                        keyboard = new TextFieldKeyboard(getContext());
                    } catch (NoSuchMethodError e) {
                        keyboard = new TextFieldKeyboard(getActivity());
                    }
                    keyboard.makeKeyboardBehave((EditText) v);
                }
            }
            else {
                // The user just selected away from the input box
                if (v instanceof EditText) {
                    EditText textField = (EditText) v;
                    String answer = textField.getText().toString();
                    SurveyTimingsRecorder.recordAnswer(answer, questionDescription);
                }
            }
        }
    }

    // Interface for the "Next" button to signal the Activity
    public interface OnGoToNextQuestionListener {
        void goToNextQuestion(QuestionData dataFromCurrentQuestion);
    }

}
