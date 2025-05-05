package com.example.projectapp;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int VERSION = 2;
    private static final String NAME = "toDoListDatabase";
    private static final String TODO_TABLE = "todo";
    private static final String ID = "id";
    private static final String TASK = "task";
    private static final String STATUS = "status";
    private static final String DUE_DATE = "due_date";
    private static final String DUE_TIME = "due_time";
    private static final String CREATE_TODO_TABLE = "CREATE TABLE " + TODO_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + TASK + " TEXT, "
            + STATUS + " INTEGER, " + DUE_DATE + " TEXT, " + DUE_TIME + " TEXT)";
    private SQLiteDatabase db;
    private static final String TAG = "DatabaseHandler";

    public DatabaseHandler(Context context) {
        super(context, NAME, null, VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TODO_TABLE);
            Log.d(TAG, "Database created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 2) {
                // Add new columns for date and time
                db.execSQL("ALTER TABLE " + TODO_TABLE + " ADD COLUMN " + DUE_DATE + " TEXT");
                db.execSQL("ALTER TABLE " + TODO_TABLE + " ADD COLUMN " + DUE_TIME + " TEXT");
            }
            Log.d(TAG, "Database upgraded successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database: " + e.getMessage());
            throw e;
        }
    }
    public void openDatabase() {
        try {
            db = this.getWritableDatabase();
            Log.d(TAG, "Database opened successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error opening database: " + e.getMessage());
            throw e;
        }
    }
    public void insertTask(ToDoModel task) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(TASK, task.getTask());
            cv.put(STATUS, 0);
            cv.put(DUE_DATE, task.getDueDate());
            cv.put(DUE_TIME, task.getDueTime());
            db.insert(TODO_TABLE, null, cv);
            Log.d(TAG, "Task inserted successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting task: " + e.getMessage());
            throw e;
        }
    }

    public List<ToDoModel> getAllTasks() {
        List<ToDoModel> taskList = new ArrayList<>();
        Cursor cur = null;
        try {
            db.beginTransaction();
            cur = db.query(TODO_TABLE, null, null, null, null, null, null);
            if (cur != null) {
                if (cur.moveToFirst()) {
                    do {
                        ToDoModel task = new ToDoModel();
                        task.setId(cur.getInt(cur.getColumnIndexOrThrow(ID)));
                        task.setTask(cur.getString(cur.getColumnIndexOrThrow(TASK)));
                        task.setStatus(cur.getInt(cur.getColumnIndexOrThrow(STATUS)));
                        task.setDueDate(cur.getString(cur.getColumnIndexOrThrow(DUE_DATE)));
                        task.setDueTime(cur.getString(cur.getColumnIndexOrThrow(DUE_TIME)));
                        taskList.add(task);
                    } while (cur.moveToNext());
                }
            }
            Log.d(TAG, "Tasks retrieved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error getting tasks: " + e.getMessage());
            throw e;
        } finally {
            if (cur != null) {
                cur.close();
            }
            db.endTransaction();
        }
        return taskList;
    }

    public void updateStatus(int id, int status) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(STATUS, status);
            db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
            Log.d(TAG, "Task status updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error updating task status: " + e.getMessage());
            throw e;
        }
    }
    public void updateTask(int id, String task, String dueDate, String dueTime) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(TASK, task);
            cv.put(DUE_DATE, dueDate);
            cv.put(DUE_TIME, dueTime);
            db.update(TODO_TABLE, cv, ID + "= ?", new String[] {String.valueOf(id)});
            Log.d(TAG, "Task updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error updating task: " + e.getMessage());
            throw e;
        }
    }
    public void deleteTask(int id) {
        try {
            db.delete(TODO_TABLE, ID + "= ?", new String[] {String.valueOf(id)});
            Log.d(TAG, "Task deleted successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error deleting task: " + e.getMessage());
            throw e;
        }
    }
}
