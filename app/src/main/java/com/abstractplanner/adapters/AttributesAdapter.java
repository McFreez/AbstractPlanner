package com.abstractplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abstractplanner.R;
import com.abstractplanner.dto.Attribute;

import java.util.ArrayList;
import java.util.List;

public class AttributesAdapter {

    private Context mContext;
    private List<Attribute> mAttributes;
    private List<View> mAttributeViews;
    private LinearLayout mAttributesContainer;

    public AttributesAdapter(Context context, List<Attribute> attributes, LinearLayout attributesContainer){
        mContext = context;
        mAttributes = attributes;
        mAttributesContainer = attributesContainer;

        createViews();
    }

    private void createViews(){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mAttributeViews = new ArrayList<>();

        for(int i = 0; i < mAttributes.size(); i++){

            View view = inflater.inflate(R.layout.attributes_item, null, false);

            AttributesViewHolder viewHolder = new AttributesViewHolder();
            viewHolder.attributeTitle = (TextView) view.findViewById(R.id.tv_attribute);
            viewHolder.attributeTitle.setText(mAttributes.get(i).getName());
            view.setTag(viewHolder);

            mAttributesContainer.addView(view);
            mAttributeViews.add(view);
        }
    }

/*    public void addAtribute(Attribute attribute){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.attributes_item, null, false);

        AttributesViewHolder viewHolder = new AttributesViewHolder();
        viewHolder.attributeTitle = (TextView) view.findViewById(R.id.tv_attribute);
        viewHolder.attributeTitle.setText(attribute.getName());
        view.setTag(viewHolder);

        mAttributes.add(attribute);
        mAttributesContainer.addView(view);
        mAttributeViews.add(view);
    }*/

    static class AttributesViewHolder{
        public TextView attributeTitle;
    }
}
