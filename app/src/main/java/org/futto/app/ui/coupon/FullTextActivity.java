package org.futto.app.ui.coupon;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;


public class FullTextActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LinearLayout l = new LinearLayout(this);
    l.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    l.setOrientation(LinearLayout.VERTICAL);
    l.setPadding(10,40,10,30);

    setContentView(l);
    Intent intent = getIntent();

    String content = intent.getStringExtra("content");
    String title = intent.getStringExtra("title");

    TextView tit = new TextView(this);
    tit.setGravity(Gravity.CENTER_HORIZONTAL);
    tit.setText(title);
    tit.setPadding(5,30,5,20);
    tit.setTextSize(18);
    tit.setTypeface(null, Typeface.BOLD);
    //content text
    TextView cont = new TextView(this);
    cont.setGravity(Gravity.CENTER_HORIZONTAL);
    cont.setText(content);
    l.addView(tit);
    l.addView(cont);

    }


}