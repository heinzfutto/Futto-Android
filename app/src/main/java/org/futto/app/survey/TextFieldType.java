package org.futto.app.survey;

/** this was an extremely common code pattern, so we pulled it out and made it a class. */

public class TextFieldType {
	public enum Type {
		NUMERIC,
		SINGLE_LINE_TEXT,
		MULTI_LINE_TEXT;
	}
}