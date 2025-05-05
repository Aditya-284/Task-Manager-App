package com.example.projectapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Collections;
import java.util.List;
import com.example.projectapp.ToDoAdapter;
import com.example.projectapp.ToDoModel;
import com.example.projectapp.DatabaseHandler;
import com.example.projectapp.DialogCloseListener;
import android.content.Context;

public class MainActivity extends Activity implements DialogCloseListener {
    private DatabaseHandler db;
    private RecyclerView tasksRecyclerView;
    private ToDoAdapter tasksAdapter;
    private FloatingActionButton fab;
    private List<ToDoModel> taskList;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        try {
            Log.d(TAG, "Initializing app...");
            initializeDatabase();
            setupRecyclerView();
            setupFab();
            loadTasks();
            Log.d(TAG, "App initialization completed");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing app: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void initializeDatabase() {
        Log.d(TAG, "Initializing database...");
        db = new DatabaseHandler(getApplicationContext());
        db.openDatabase();
        Log.d(TAG, "Database initialized successfully");
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView...");
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        tasksAdapter = new ToDoAdapter(db, this);
        tasksRecyclerView.setAdapter(tasksAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerItemTouchHelper(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);
        Log.d(TAG, "RecyclerView setup completed");
    }

    private void setupFab() {
        Log.d(TAG, "Setting up FAB...");
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            AddNewTask fragment = AddNewTask.newInstance();
            fragment.show(getFragmentManager(), AddNewTask.TAG);
        });
        Log.d(TAG, "FAB setup completed");
    }

    private void loadTasks() {
        Log.d(TAG, "Loading tasks...");
        taskList = db.getAllTasks();
        Log.d(TAG, "Retrieved " + taskList.size() + " tasks from database");
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
        Log.d(TAG, "Tasks loaded and displayed");
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        try {
            Log.d(TAG, "Handling dialog close...");
            taskList = db.getAllTasks();
            Log.d(TAG, "Retrieved " + taskList.size() + " tasks after dialog close");
            Collections.reverse(taskList);
            tasksAdapter.setTasks(taskList);
            tasksAdapter.notifyDataSetChanged();
            Log.d(TAG, "Tasks updated after dialog close");
        } catch (Exception e) {
            Log.e(TAG, "Error updating tasks: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Error updating tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}