package org.futto.app.survey;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

/** A customized slider ui element so that we can have a grayed out effect for untouched sliders.
 * Based on http://stackoverflow.com/a/19008611
 * @author Josh Zagorsky
 *
 */
public class SeekBarEditableThumb extends SeekBar {

	public SeekBarEditableThumb(Context context) {
		super(context);
	}
	
	public SeekBarEditableThumb(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SeekBarEditableThumb(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	
	private Drawable compatThumb;
	private Boolean hasBeenTouched;
	private int min;
	
	@Override
	public void setThumb(Drawable thumb) {
		super.setThumb(thumb);
		compatThumb = thumb;
	}
	
	/**
	 * Make the SeekBar's "Thumb" invisible, and mark it as "user hasn't touched this yet"
	 */
	public void markAsUntouched() {
		compatThumb.mutate().setAlpha(0);
		hasBeenTouched = false;
	}

	/**
	 * Make the SeekBar's "Thumb" visible, and mark it as "user has touched this"
	 */
	public void markAsTouched() {
		compatThumb.mutate().setAlpha(255);
		hasBeenTouched = true;
	}
	
	/**
	 * Return a Boolean of whether or not the user has touched the SeekBar yet 
	 * @return
	 */
	public Boolean getHasBeenTouched() {
		return hasBeenTouched;
	}
	
	
	/**
	 * The minimum doesn't have to be zero; the SeekBar can start at another number (even negative)
	 * @param min
	 */
	public void setMin(int min) {
		this.min = min;
	}
	
	/**
	 * The minimum doesn't have to be zero; the SeekBar can start at another number (even negative)
	 * @param min
	 */
	public int getMin() {
		return this.min;
	}
	
}
