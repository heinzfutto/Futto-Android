package org.futto.app.survey;

import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionJSONParser {
    public static Bundle getQuestionArgsFromJSONString(JSONObject jsonQuestion) {
        Bundle args = new Bundle();
        String questionType = getStringFromJSONObject(jsonQuestion, "question_type");
        args.putString("question_type", questionType);
        if (questionType.equals("info_text_box")) { return getArgsForInfoTextBox(jsonQuestion, args); }
        else if (questionType.equals("slider")) { return getArgsForSliderQuestion(jsonQuestion, args); }
        else if (questionType.equals("radio_button")) { return getArgsForRadioButtonQuestion(jsonQuestion, args); }
        else if (questionType.equals("checkbox")) { return getArgsForCheckboxQuestion(jsonQuestion, args); }
        else if (questionType.equals("free_response")) { return getArgsForFreeResponseQuestion(jsonQuestion, args); }
        return args;
    }


    // Gets and cleans the parameters necessary to create an Info Text Box
    private static Bundle getArgsForInfoTextBox(JSONObject jsonQuestion, Bundle args) {
        args.putString("question_id", getStringFromJSONObject(jsonQuestion, "question_id"));
        args.putString("question_text", getStringFromJSONObject(jsonQuestion, "question_text"));
        return args;
    }


    // Gets and cleans the parameters necessary to create a Slider Question
    private static Bundle getArgsForSliderQuestion(JSONObject jsonQuestion, Bundle args) {
        args.putString("question_id", getStringFromJSONObject(jsonQuestion, "question_id"));
        args.putString("question_text", getStringFromJSONObject(jsonQuestion, "question_text"));
        args.putInt("min", getIntFromJSONObject(jsonQuestion, "min"));
        args.putInt("max", getIntFromJSONObject(jsonQuestion, "max"));
        return args;
    }


    // Gets and cleans the parameters necessary to create a Radio Button Question
    private static Bundle getArgsForRadioButtonQuestion(JSONObject jsonQuestion, Bundle args) {
        args.putString("question_id", getStringFromJSONObject(jsonQuestion, "question_id"));
        args.putString("question_text", getStringFromJSONObject(jsonQuestion, "question_text"));
        args.putStringArray("answers", getStringArrayFromJSONObject(jsonQuestion, "answers"));
        return args;
    }


    // Gets and cleans the parameters necessary to create a Checkbox Question
    private static Bundle getArgsForCheckboxQuestion(JSONObject jsonQuestion, Bundle args) {
        args.putString("question_id", getStringFromJSONObject(jsonQuestion, "question_id"));
        args.putString("question_text", getStringFromJSONObject(jsonQuestion, "question_text"));
        args.putStringArray("answers", getStringArrayFromJSONObject(jsonQuestion, "answers"));
        return args;
    }


    // Gets and cleans the parameters necessary to create a Free-Response Question
    private static Bundle getArgsForFreeResponseQuestion(JSONObject jsonQuestion, Bundle args) {
        args.putString("question_id", getStringFromJSONObject(jsonQuestion, "question_id"));
        args.putString("question_text", getStringFromJSONObject(jsonQuestion, "question_text"));
        args.putInt("text_field_type", getTextFieldTypeAsIntFromJSONObject(jsonQuestion, "text_field_type"));
        return args;
    }


    /**Get a String from a JSONObject key
     * @param obj a generic JSONObject
     * @param key the JSON key
     * @return return an empty String instead of throwing a JSONException */
    private static String getStringFromJSONObject(JSONObject obj, String key) {
        try { return obj.getString(key); }
        catch (JSONException e) { return ""; }
    }


    /**Get an int from a JSONObject key
     * @param obj a generic JSONObject
     * @param key the JSON key
     * @return return -1 instead of throwing a JSONException */
    private static int getIntFromJSONObject(JSONObject obj, String key) {
        try { return obj.getInt(key); }
        catch (JSONException e) { return -1; }
    }


    /**Get an array of Strings from a JSONObject key
     * @param obj a generic JSONObject
     * @param key the JSON key
     * @return return a one-String array instead of throwing a JSONException */
    private static String[] getStringArrayFromJSONObject(JSONObject obj, String key) {
        JSONArray jsonArray;
        try { jsonArray = obj.getJSONArray(key); }
        catch (JSONException e1) {
            String[] errorArray = {""};
            return errorArray;
        }
        String[] strings = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            try { strings[i] = jsonArray.getJSONObject(i).getString("text"); }
            catch (JSONException e) { strings[i] = ""; }
        }
        return strings;
    }


    /**Get an Enum TextFieldType from a JSONObject key
     * @param obj a generic JSONObject
     * @param key the JSON key
     * @return return SINGLE_LINE_TEXT as the default instead of throwing a JSONException */
    private static int getTextFieldTypeAsIntFromJSONObject(JSONObject obj, String key) {
        try { return TextFieldType.Type.valueOf(obj.getString(key)).ordinal(); }
        catch (JSONException e) { return TextFieldType.Type.SINGLE_LINE_TEXT.ordinal(); }
    }

}