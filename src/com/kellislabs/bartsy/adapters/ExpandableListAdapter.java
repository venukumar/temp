package com.kellislabs.bartsy.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
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
            v = inflater.inflate(R.layout.child_row, parent, false); 
        MenuDrink c = (MenuDrink)getChild( groupPosition, childPosition );
		TextView color = (TextView)v.findViewById( R.id.childname );
		if( color != null )
			color.setText( c.getTitle() );
		TextView rgb = (TextView)v.findViewById( R.id.rgb );
		if( rgb != null )
			rgb.setText( c.getPrice() );
		
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
            v = inflater.inflate(R.layout.group_row, parent, false); 
        String gt = (String)getGroup( groupPosition );
		TextView colorGroup = (TextView)v.findViewById( R.id.childname );
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


//public class ExpandableListAdapter extends BaseExpandableListAdapter {
//
//	private Context mContext;
//	private ExpandableListView mExpandableListView;
//	private List<Section> mGroupCollection;
//	private int[] groupStatus;
//	LinearLayout layout;
//
//
//	public ExpandableListAdapter(Context pContext,
//			ExpandableListView pExpandableListView,
//			List<Section> pGroupCollection) {
//		mContext = pContext;
//		mGroupCollection = pGroupCollection;
//		mExpandableListView = pExpandableListView;
//		groupStatus = new int[mGroupCollection.size()];
//
//		setListEvent();
//	}
//
//	private void setListEvent() {
//
//		mExpandableListView
//				.setOnGroupExpandListener(new OnGroupExpandListener() {
//
//					@Override
//					public void onGroupExpand(int arg0) {
//
//						groupStatus[arg0] = 1;
//
//					}
//				});
//
//		mExpandableListView
//				.setOnGroupCollapseListener(new OnGroupCollapseListener() {
//
//					@Override
//					public void onGroupCollapse(int arg0) {
//
//						groupStatus[arg0] = 0;
//					}
//				});
//	}
//
//	@Override
//	public String getChild(int arg0, int arg1) {
//
//		return mGroupCollection.get(arg0).getDrinks().get(arg1).getDescription();
//	}
//
//	@Override
//	public long getChildId(int arg0, int arg1) {
//
//		return 0;
//	}
//
//	@Override
//	public View getChildView(int arg0, int arg1, boolean arg2, View arg3,
//			ViewGroup arg4) {
//
//		ChildHolder childHolder;
//		if (arg3 == null) {
//			arg3 = LayoutInflater.from(mContext).inflate(
//					R.layout.list_item_child, null);
//
//			childHolder = new ChildHolder();
//
//			childHolder.title = (TextView) arg3.findViewById(R.id.list_item_text_child);
//			arg3.setTag(childHolder);
//		} else {
//			childHolder = (ChildHolder) arg3.getTag();
//		}
//
//		childHolder.title
//				.setText(mGroupCollection.get(arg0).getDrinks()
//						.get(arg1).getTitle());
//		return arg3;
//
//	}
//
//	@Override
//	public int getChildrenCount(int arg0) {
//
//		return mGroupCollection.get(arg0).getDrinks().size();
//	}
//
//	@Override
//	public Object getGroup(int arg0) {
//
//		return mGroupCollection.get(arg0);
//	}
//
//	@Override
//	public int getGroupCount() {
//		int i = mGroupCollection.size();
//		return i;
//	}
//
//	@Override
//	public long getGroupId(int arg0) {
//
//		return arg0;
//	}
//
//	@Override
//	public View getGroupView(int arg0, boolean arg1, View arg2, ViewGroup arg3) {
//
//		GroupHolder groupHolder;
//		if (arg2 == null) {
//
//			
//			arg2 = LayoutInflater.from(mContext).inflate(R.layout.list_item_parent,
//					null);
//			groupHolder = new GroupHolder();
//			groupHolder.title = (TextView) arg2.findViewById(R.id.list_item_text_view);
//
//			arg2.setTag(groupHolder);
//		} else {
//			groupHolder = (GroupHolder) arg2.getTag();
//		}
////		groupHolder.image.setImageResource(imgid[arg0]);
//		groupHolder.title.setTypeface(null, Typeface.BOLD);
//		if (groupStatus[arg0] == 0) {
////			groupHolder.img.setImageResource(R.drawable.arrowexpand);
//			groupHolder.title.setTextColor(Color.BLACK);
//		} else {
////			groupHolder.img.setImageResource(R.drawable.arrowexpanded);
//			groupHolder.title.setTextColor(Color.GRAY);
//		}
//
//		groupHolder.title.setText(mGroupCollection.get(arg0).getName());
//	
//
//		return arg2;
//	}
//
//	class GroupHolder {
//		ImageView img;
//		TextView title;
//		ImageView image;
//
//	}
//
//	class ChildHolder {
//		TextView title;
//	}
//
//	@Override
//	public boolean hasStableIds() {
//
//		return true;
//	}
//
//	@Override
//	public boolean isChildSelectable(int arg0, int arg1) {
//
//		return true;
//	}
//
//}
