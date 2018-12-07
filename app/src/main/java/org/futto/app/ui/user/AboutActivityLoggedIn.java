package org.futto.app.ui.user;

import org.futto.app.R;
import org.futto.app.session.SessionActivity;
import org.futto.app.storage.PersistentData;

import android.os.Bundle;
import android.widget.TextView;

/**The about page!
 * @author Everyone! */
public class AboutActivityLoggedIn extends SessionActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		TextView aboutPageBody = (TextView) findViewById(R.id.about_page_body);
		aboutPageBody.setText(PersistentData.getAboutPageText());
	}
}
