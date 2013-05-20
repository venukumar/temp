
package com.vendsy.bartsy.db;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import com.vendsy.bartsy.model.MenuDrink;
import com.vendsy.bartsy.model.Section;

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
	public List<Section> getMenuSections(String venueId) {
		try {
			QueryBuilder<Section, Integer> surveyQb = dbHelper
					.getSectionDao().queryBuilder();
			surveyQb.where().eq("venueId", venueId);
			PreparedQuery<Section> query = surveyQb.prepare();
			return dbHelper.getSectionDao().query(query);
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
	public List<MenuDrink> getMenuDrinks(Section section, String venueId) {
		try {
			QueryBuilder<MenuDrink, Integer> surveyQb = dbHelper
					.getDrinkDao().queryBuilder();
			surveyQb.where().eq("section_id", section.getId()).and().eq("venueId", venueId);
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
	public List<MenuDrink> getMenuDrinks(String venueId) {
		try {
			QueryBuilder<MenuDrink, Integer> surveyQb = dbHelper
					.getDrinkDao().queryBuilder();
			surveyQb.where().isNull("section_id").and().eq("venueId", venueId);
			PreparedQuery<MenuDrink> query = surveyQb.prepare();
			return dbHelper.getDrinkDao().query(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * To delete all the drinks based on the venue ID
	 * 
	 * @param venueId
	 */
	public void deleteDrinks(String venueId){
		try {
			DeleteBuilder<MenuDrink, Integer> db = dbHelper
					.getDrinkDao().deleteBuilder();
			db.where().eq("venueId", venueId);
			dbHelper.getDrinkDao().delete(db.prepare());
			
			DeleteBuilder<Section, Integer> sectionDB = dbHelper
					.getSectionDao().deleteBuilder();
			sectionDB.where().eq("venueId", venueId);
			dbHelper.getSectionDao().delete(sectionDB.prepare());
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
