package com.wwwebteam.clients.tfare.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseEngine {
	private static final String DB_NAME = "t_fare";
	private static final int DB_VERSION = 1;
	public static final String DB_TAB_TICKETS = "tickets";
	public static final String DB_TAB_USER = "users";

	//USER COLUMNS
	public static final String TAB_USER_ID = "user_id";
	public static final String TAB_USER_FULLNAME = "fullname";
	public static final String TAB_USER_TOKEN = "user_token";
	public static final String TAB_USER_PHONE_NO = "phone_number";
	public static final String TAB_USER_LOCATION = "location";
	public static final String TAB_USER_BALANCE = "topup_balance";
	//DONW WITH USER COLUMNS
	
	//DO THE QUERIES TO CREATE TABLES
	private static final String QUERY_CREATE_USER = "CREATE TABLE " + DB_TAB_USER + " (" +
			TAB_USER_ID + " text primary key not null, " +
			TAB_USER_FULLNAME + " text not null, " +
			TAB_USER_TOKEN + " text not null, " +
			TAB_USER_PHONE_NO + " text not null, " +
			TAB_USER_BALANCE + " text not null, " +
			TAB_USER_LOCATION + " text not null)";
	//DONE WITH CREATE QUERIES.
	
	//Get the database helper and the SQLite database itself.
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	// CONSTRUCTOR METHOD
	private final Context mCtx;
	
	public DatabaseEngine(Context ctx) {
		this.mCtx = ctx;
	}
	
	public DatabaseEngine open() throws android.database.SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		mDbHelper.close();
	}
	
	//METHODS TO INSERT, SELECT, UPDATE, AND DELETE ENTRIES IN THE DATABASE TABLES.
	// RETURNS LONG FOR INSERT
	// RETURNS CURSOR FOR SELECT
	// RETURNS BOOLEAN FOR UPDATE
	
	// INSERT DATA TO DATABASE TABLE
	public long insertIntoDatabaseTable(ContentValues initialValues, String tableName) {
		long returnId = mDb.insert(tableName, null, initialValues);
		return returnId;
	}
	
	//GET DATA FROM DATABASE
	public Cursor getDistinctDataFromDatabaseTable(String[] gets, String tableName, String whereClause) {
		Cursor mCursor = mDb.query(true, tableName, gets, whereClause, null, null, null, null, null);
		return mCursor;
	}
	
	public Cursor getDataFromDatabaseTable(String[] gets, String tableName, String whereClause, String orderBy, String limit) {
		Cursor mCursor = mDb.query(tableName, gets, whereClause, null, null, null, orderBy, limit);
		return mCursor;
	}
	
	//UPDATE DATA IN DATABASE
	public boolean updateDataInDatabase(String tableName, ContentValues initialValues, String whereClause) {
		return mDb.update(tableName, initialValues, whereClause, null) > 0;
	}
	
	//DELETE DATA FROM DATABASE
	public boolean deleteDataFromDatabase(String tableName, String whereClause) {
		return mDb.delete(tableName, whereClause, null) > 0;
	}
	
	//INSERT, SELECT, UPDATE, DELETE OPERATIONS DONE.
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			// Create tables here for the first time.
			db.execSQL(QUERY_CREATE_USER);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Update the database tables and columns.
			if (oldVersion < newVersion) {
				db.execSQL("DROP TABLE IF EXISTS " + DB_TAB_USER);

				db.execSQL(QUERY_CREATE_USER);
			}
		}
	}
}
