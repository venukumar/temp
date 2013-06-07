package com.vendsy.bartsy.model;

import java.util.ArrayList;

/**
 * Category model
 * 
 * @author Seenu Malireddy
 */

public class Category {
	
	public static final String SPIRITS_TYPE = "spirit"; 
	public static final String MIXER_TYPE = "mixer";
	public static final String COCKTAILS_TYPE = "Cocktails";
			
	private String name;
	// Contains list of ingredients which is belongs to particular category
	private ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Ingredient> getIngredients() {
		return ingredients;
	}

	public void setIngredients(ArrayList<Ingredient> ingredients) {
		this.ingredients = ingredients;
	}
	
	
	
}
