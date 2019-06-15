package org.futto.app.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.futto.app.R;
import org.futto.app.survey.SurveyActivity;

import java.util.ArrayList;

public class SurveyAdapter extends BaseAdapter {


    private ArrayList<String> mData;
    private Context mContext;
    public SurveyAdapter(ArrayList<String> mData,Context mContext){
        this.mData=mData;
        this.mContext=mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_survey_item,parent,false);
        }


        final LinearLayout surveylinearlayout = (LinearLayout) convertView.findViewById(R.id.surveylinearlayout);
        final TextView surveyName=(TextView) convertView.findViewById(R.id.surveyNameText);
        surveyName.setText(mData.get(position));

        surveylinearlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(true){
                    Intent intent=new Intent(mContext,SurveyActivity.class);
                    Bundle b = new Bundle();
                    b.putString("surveyId",surveyName.getText().toString());
                    intent.putExtras(b);
                    mContext.startActivity(intent);
                }
            }
        });

        return convertView;
    }



}
