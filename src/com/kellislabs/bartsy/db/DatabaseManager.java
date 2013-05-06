
package com.kellislabs.bartsy.db;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.kellislabs.bartsy.model.MenuDrink;
import com.kellislabs.bartsy.model.Section;

/**
 * @author Seenu
 */
public class DatabaseManager {

	private static DatabaseManager manager;

	private DatabaseHelper dbHelper;

	private DatabaseManager(Context context) {
		dbHelper = new DatabaseHelper(context);
		manager = this;
	}

	public static DatabaseManager getNewInstance(Context context) {
		if (manager == null) {
			manager = new DatabaseManager(context);
		}
		return manager;
	}

	public static DatabaseManager getInstance() {

		return manager;
	}
	/**
	 * To save Menu Drink data in db
	 * @param drink
	 */
	public void saveDrink(MenuDrink drink) {
		try {
			dbHelper.getDrinkDao().createOrUpdate(drink);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * To save section data in db
	 * @param section
	 */
	public void saveSection(Section section) {
		try {
			dbHelper.getSectionDao().createOrUpdate(section);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	/**
	 * To get list of sections from the db
	 * @param id
	 * @return
	 */
	public List<Section> getMenuSections() {
		try {
			return dbHelper.getSectionDao().queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * To get list of Menu Drinks by section from the db
	 * @param id
	 * @return
	 */
	public List<MenuDrink> getMenuDrinks(Section section) {
		try {
			QueryBuilder<MenuDrink, Integer> surveyQb = dbHelper
					.getDrinkDao().queryBuilder();
			surveyQb.where().eq("section_id", section.getId());
			PreparedQuery<MenuDrink> query = surveyQb.prepare();
			return dbHelper.getDrinkDao().query(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * To get list of Menu Drink from the db
	 * @param id
	 * @return
	 */
	public List<MenuDrink> getMenuDrinks() {
		try {
			QueryBuilder<MenuDrink, Integer> surveyQb = dbHelper
					.getDrinkDao().queryBuilder();
			surveyQb.where().isNull("section_id");
			PreparedQuery<MenuDrink> query = surveyQb.prepare();
			return dbHelper.getDrinkDao().query(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
