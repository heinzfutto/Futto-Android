package org.futto.app.survey;

/**
 * Created by Josh Zagorsky on 12/11/16.
 */

public class QuestionType {
    public enum Type {
        INFO_TEXT_BOX("Info Text Box"),
        SLIDER("Slider Question"),
        RADIO_BUTTON("Radio Button Question"),
        CHECKBOX("Checkbox Question"),
        FREE_RESPONSE("Open Response Question");

        private final String stringName;

        Type(String stringName) {
            this.stringName = stringName;
        }

        public String getStringName() {
            return stringName;
        }
    }
}
