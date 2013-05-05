package com.kellislabs.bartsy.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.kellislabs.bartsy.R;
import com.kellislabs.bartsy.model.MenuDrink;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<String> groups;
    private ArrayList<ArrayList<MenuDrink>> colors;
    private LayoutInflater inflater;

    public ExpandableListAdapter(Context context, 
                        ArrayList<String> groups,
						ArrayList<ArrayList<MenuDrink>> colors ) { 
        this.context = context;
		this.groups = groups;
        this.colors = colors;
        inflater = LayoutInflater.from( context );
    }

    public Object getChild(int groupPosition, int childPosition) {
        return colors.get( groupPosition ).get( childPosition );
    }

    public long getChildId(int groupPosition, int childPosition) {
        return (long)( groupPosition*1024+childPosition );  // Max 1024 children per group
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View v = null;
        if( convertView != null )
            v = convertView;
        else
            v = inflater.inflate(R.layout.drink_item, parent, false); 
        MenuDrink c = (MenuDrink)getChild( groupPosition, childPosition );
		TextView textView = (TextView)v.findViewById( R.id.view_drink_title );
		if( textView != null )
			textView.setText( c.getTitle() );
		
		textView = (TextView)v.findViewById( R.id.view_drink_description );
		if( textView != null )
			textView.setText( c.getDescription() );

		TextView rgb = (TextView)v.findViewById( R.id.view_drink_price );
		if( rgb != null )
			rgb.setText( "$"+c.getPrice() );
		
        return v;
    }

    public int getChildrenCount(int groupPosition) {
        return colors.get( groupPosition ).size();
    }

    public Object getGroup(int groupPosition) {
        return groups.get( groupPosition );        
    }

    public int getGroupCount() {
        return groups.size();
    }

    public long getGroupId(int groupPosition) {
        return (long)( groupPosition*1024 );  // To be consistent with getChildId
    } 

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View v = null;
        if( convertView != null )
            v = convertView;
        else
            v = inflater.inflate(R.layout.list_item_parent, parent, false); 
        String gt = (String)getGroup( groupPosition );
		TextView colorGroup = (TextView)v.findViewById( R.id.list_item_text_view);
		if( gt != null )
			colorGroup.setText( gt );
        return v;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    } 

    public void onGroupCollapsed (int groupPosition) {} 
    public void onGroupExpanded(int groupPosition) {}
}
