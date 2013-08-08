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
	
	public String name = null;
	public ArrayList<String> headings = new ArrayList<String>() ;
	public ArrayList<ArrayList<Item>> items = new ArrayList<ArrayList<Item>>();
	
	public Menu () {
		
	}

	public Menu (JSONArray menus, HashMap<String, JSONObject> savedSelections) throws JSONException {
		
//		String errorCode = json.getString("errorCode");
//		String errorMessage = json.getString("errorMessage");
		
		for(int m=0; m<menus.length();m++){
			
			JSONObject menuJson = menus.getJSONObject(m);
			JSONArray sectionsJson = menuJson.getJSONArray("sections");
			
			boolean showMenu = menuJson.has("show_menu") ? (menuJson.getString("show_menu").equals("No") ? false : true) : true;
			
			// Parse sections 
			for (int i = 0; i < sectionsJson.length(); i++) {
	
				JSONObject sectionJson = sectionsJson.getJSONObject(i);
				
				if (sectionJson.has("subsections")) {

					JSONArray subsections = sectionJson.getJSONArray("subsections");
					
					if (subsections != null && subsections.length() > 0) {

						for (int j = 0; j < subsections.length(); j++) {
							
							JSONObject subsectionJson = subsections.getJSONObject(j);
							
							// Hack - for now correction menu and sub section names for favorites
							if (sectionJson.has("favoriteDrinkId")) {
								menuJson.put("menu_name", "Available favorites");
								subsectionJson.remove("subsection_name");
							}
							
							// Create a heading by flattening the hierarchy of headings into one string
							String heading = "";
							if (menuJson.has("menu_name"))
								name = heading = menuJson.getString("menu_name");
							String sectionName = "";
							if (sectionJson.has("section_name"))
								sectionName = sectionJson.getString("section_name");
							if (!heading.equals("") && !sectionName.equals(""))
								heading += " > ";
							heading += sectionName;
							String subsectionName = "";
							if (subsectionJson.has("subsection_name"))
								subsectionName = subsectionJson.getString("subsection_name");
							if (!heading.equals("") && !subsectionName.equals(""))
								heading += " > ";
							heading += subsectionName;
							if (heading.equals(""))
								heading += "Various items";


							// Add the list of items under that heading to the items list
							JSONArray contents = subsectionJson.getJSONArray("contents");
							ArrayList<Item> subsection_contents = new ArrayList<Item>();
							
							Log.v(TAG, "Parsing " + heading + " with " + contents.length() + " items");

							for (int k = 0; k < contents.length(); k++) {
								Item item = null;
								try {
									JSONObject itemJson = contents.getJSONObject(k);

									// Hack - for now insert favorite id from section as it's in the wrong place
									if (sectionJson.has("favoriteDrinkId"))
										itemJson.put("favorite_id", sectionJson.getString("favoriteDrinkId"));
									
									item = new Item(itemJson, savedSelections);

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
								item.setMenuPath(heading);
							}

							if (showMenu) {
								if (subsection_contents.size() > 0) {
									// Add the heading title to the headings list 
									headings.add(heading);
		
									// Add the contents of the subsection to the list of items
									items.add(subsection_contents);
								}
							} else {
								Log.v(TAG, "Skipping hidden section " + heading);
							}

						}
					}
				}
			}	
		}
	}
}
