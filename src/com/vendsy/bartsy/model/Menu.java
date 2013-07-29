/**
 * 
 */
package com.vendsy.bartsy.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.vendsy.bartsy.model.Item;

/**
 * @author PeterKellis
 *
 * Our menu is a compacted version of the full locu menu. The difference is that the hierarchies
 * are flattened so that Menu > Section > Subsection becomes the title of the heading and all 
 * items get packed under it. That's because we use an expandable list to represent the entire menu.
 */
public class Menu {

	private static final String TAG = "Menu";
	
	public ArrayList<String> headings = new ArrayList<String>() ;
	public ArrayList<ArrayList<Item>> items = new ArrayList<ArrayList<Item>>();
	
	public Menu () {
		
	}

	public Menu (JSONArray menusArryObj, HashMap<String, OptionGroup> savedSelections) throws JSONException {
		
//		String errorCode = json.getString("errorCode");
//		String errorMessage = json.getString("errorMessage");
		
		for(int m=0; m<menusArryObj.length();m++){
			
			JSONObject menuObj = menusArryObj.getJSONObject(m);
			JSONArray sections = menuObj.getJSONArray("sections");
			
			// Parse sections 
			for (int i = 0; i < sections.length(); i++) {
	
				JSONObject section = sections.getJSONObject(i);
				
				if (section.has("subsections")) {

					JSONArray subsections = section.getJSONArray("subsections");
					
					if (subsections != null && subsections.length() > 0) {

						for (int j = 0; j < subsections.length(); j++) {
							
							JSONObject subSection = subsections.getJSONObject(j);
							
							// Create a heading by flattening the hierarchy of headings into one string
							String heading = "";
							if (menuObj.has("menu_name"))
								heading = menuObj.getString("menu_name");
							String sectionName = "";
							if (section.has("section_name"))
								sectionName = section.getString("section_name");
							if (!heading.equals("") && !sectionName.equals(""))
								heading += " > ";
							heading += sectionName;
							String subsectionName = "";
							if (subSection.has("subsection_name"))
								subsectionName = subSection.getString("subsection_name");
							if (!heading.equals("") && !subsectionName.equals(""))
								heading += " > ";
							heading += subsectionName;
							if (heading.equals(""))
								heading += "Various items";


							// Add the list of items under that heading to the items list
							JSONArray contents = subSection.getJSONArray("contents");
							ArrayList<Item> subsection_contents = new ArrayList<Item>();
							
							Log.v(TAG, "Parsing " + heading + " with " + contents.length() + " items");

							for (int k = 0; k < contents.length(); k++) {
								Item item = null;
								try {
									item = new Item(contents.getJSONObject(k), savedSelections);
								} catch (JSONException e) {
									// Couldn't parse the item. Skip it.
									e.printStackTrace();
									continue;
								} catch (NumberFormatException e1) {
									e1.printStackTrace();
									continue;
								}
								
								// Add the item to the menu
								subsection_contents.add(item);
							}

							if (subsection_contents.size() > 0) {
								// Add the heading title to the headings list 
								headings.add(heading);
	
								// Add the contents of the subsection to the list of items
								items.add(subsection_contents);
							}
						}
					}
				}
			}	
		}
	}
}
