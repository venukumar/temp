package com.kellislabs.bartsy.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kellislabs.bartsy.R;
import com.kellislabs.bartsy.model.Section;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private Context mContext;
	private ExpandableListView mExpandableListView;
	private List<Section> mGroupCollection;
	private int[] groupStatus;
	LinearLayout layout;


	public ExpandableListAdapter(Context pContext,
			ExpandableListView pExpandableListView,
			List<Section> pGroupCollection) {
		mContext = pContext;
		mGroupCollection = pGroupCollection;
		mExpandableListView = pExpandableListView;
		groupStatus = new int[mGroupCollection.size()];

		setListEvent();
	}

	private void setListEvent() {

		mExpandableListView
				.setOnGroupExpandListener(new OnGroupExpandListener() {

					@Override
					public void onGroupExpand(int arg0) {

						groupStatus[arg0] = 1;

					}
				});

		mExpandableListView
				.setOnGroupCollapseListener(new OnGroupCollapseListener() {

					@Override
					public void onGroupCollapse(int arg0) {

						groupStatus[arg0] = 0;
					}
				});
	}

	@Override
	public String getChild(int arg0, int arg1) {

		return mGroupCollection.get(arg0).getDrinks().get(arg1).getDescription();
	}

	@Override
	public long getChildId(int arg0, int arg1) {

		return 0;
	}

	@Override
	public View getChildView(int arg0, int arg1, boolean arg2, View arg3,
			ViewGroup arg4) {

		ChildHolder childHolder;
		if (arg3 == null) {
			arg3 = LayoutInflater.from(mContext).inflate(
					R.layout.list_item_child, null);

			childHolder = new ChildHolder();

			childHolder.title = (TextView) arg3.findViewById(R.id.list_item_text_child);
			arg3.setTag(childHolder);
		} else {
			childHolder = (ChildHolder) arg3.getTag();
		}

		childHolder.title
				.setText(mGroupCollection.get(arg0).getDrinks()
						.get(arg1).getTitle());
		return arg3;

	}

	@Override
	public int getChildrenCount(int arg0) {

		return mGroupCollection.get(arg0).getDrinks().size();
	}

	@Override
	public Object getGroup(int arg0) {

		return mGroupCollection.get(arg0);
	}

	@Override
	public int getGroupCount() {
		int i = mGroupCollection.size();
		return i;
	}

	@Override
	public long getGroupId(int arg0) {

		return arg0;
	}

	@Override
	public View getGroupView(int arg0, boolean arg1, View arg2, ViewGroup arg3) {

		GroupHolder groupHolder;
		if (arg2 == null) {

			
			arg2 = LayoutInflater.from(mContext).inflate(R.layout.list_item_parent,
					null);
			groupHolder = new GroupHolder();
			groupHolder.title = (TextView) arg2.findViewById(R.id.list_item_text_view);

			arg2.setTag(groupHolder);
		} else {
			groupHolder = (GroupHolder) arg2.getTag();
		}
//		groupHolder.image.setImageResource(imgid[arg0]);
		groupHolder.title.setTypeface(null, Typeface.BOLD);
		if (groupStatus[arg0] == 0) {
//			groupHolder.img.setImageResource(R.drawable.arrowexpand);
			groupHolder.title.setTextColor(Color.BLACK);
		} else {
//			groupHolder.img.setImageResource(R.drawable.arrowexpanded);
			groupHolder.title.setTextColor(Color.GRAY);
		}

		groupHolder.title.setText(mGroupCollection.get(arg0).getName());
	

		return arg2;
	}

	class GroupHolder {
		ImageView img;
		TextView title;
		ImageView image;

	}

	class ChildHolder {
		TextView title;
	}

	@Override
	public boolean hasStableIds() {

		return true;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {

		return true;
	}

}
