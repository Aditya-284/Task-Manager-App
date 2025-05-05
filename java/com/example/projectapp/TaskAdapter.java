package com.example.projectapp;
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;
import android.app.DatePickerDialog;
import android.widget.Button;
import java.util.Calendar;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private ArrayList<String> taskList;
    private ArrayList<Boolean> completedTasks;
    private Context context;
    private OnTaskUpdatedListener listener;
    private ArrayList<String> dueDates;

    public TaskAdapter(Context context, ArrayList<String> taskList, ArrayList<String> dueDates, ArrayList<Boolean> completedTasks, OnTaskUpdatedListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.completedTasks = new ArrayList<>();
        this.listener = listener;
        for (int i = 0; i < taskList.size(); i++) {
            this.completedTasks.add(false); // Default unchecked
        }

        // Load saved task completion states
        loadCompletedTasks();
    }


    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String task = taskList.get(position);
        holder.textViewTask.setText(task);
        holder.checkBoxTask.setChecked(completedTasks.get(position));
        holder.textViewDueDate.setText(dueDates.get(position).isEmpty() ? "No due date" : "Due: " + dueDates.get(position));

        // Handle checkbox toggle
        holder.checkBoxTask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                completedTasks.set(position, isChecked);
                saveCompletedTasks();
                listener.onTaskUpdated();// Save state when changed
            }
        });
        holder.textViewTask.setOnClickListener(v -> showEditDialog(position));
    }
    @Override
    public int getItemCount() {
        return taskList.size();
    }
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTask;
        CheckBox checkBoxTask;
        TextView textViewDueDate;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTask = itemView.findViewById(R.id.textViewTask);
            checkBoxTask = itemView.findViewById(R.id.checkBoxTask);
            textViewDueDate = itemView.findViewById(R.id.textViewDueDate);
        }
    }
    private void showEditDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Task");
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_task, null);
        EditText editTextTask = dialogView.findViewById(R.id.editTextTask);
        Button buttonPickDate = dialogView.findViewById(R.id.buttonPickDate);
        TextView textViewSelectedDate = dialogView.findViewById(R.id.textViewSelectedDate);
        textViewSelectedDate.setText(dueDates.get(position).isEmpty() ? "No due date" : dueDates.get(position));
        buttonPickDate.setOnClickListener(v -> showDatePicker(textViewSelectedDate));
        builder.setView(dialogView);
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(taskList.get(position));
        builder.setView(input);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newTask = input.getText().toString().trim();
            if (!newTask.isEmpty()) {
                taskList.set(position, newTask);
                notifyItemChanged(position);
                listener.onTaskUpdated(); // Save changes
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    public interface OnTaskUpdatedListener {
        void onTaskUpdated();
    }
    private void showDatePicker(TextView textViewSelectedDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            textViewSelectedDate.setText(selectedDate);
        }, year, month, day);
        datePickerDialog.show();
    }
    private void saveCompletedTasks() {
        SharedPreferences prefs = context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray jsonArray = new JSONArray();
        for (boolean completed : completedTasks) {
            jsonArray.put(completed);
        }
        editor.putString("completed_tasks", jsonArray.toString());
        editor.apply();
    }

    // Load completed tasks from SharedPreferences
    private void loadCompletedTasks() {
        SharedPreferences prefs = context.getSharedPreferences("task_prefs", Context.MODE_PRIVATE);
        String completedTasksJson = prefs.getString("completed_tasks", "[]");
        try {
            JSONArray jsonArray = new JSONArray(completedTasksJson);
            for (int i = 0; i < taskList.size(); i++) {
                completedTasks.add(i < jsonArray.length() && jsonArray.getBoolean(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void updateTaskList(ArrayList<String> newTasks, ArrayList<String> newDueDates, ArrayList<Boolean> filteredCompleted) {
        this.taskList = newTasks;
        dueDates = newDueDates;
        notifyDataSetChanged();
    }
    public void toggleTaskCompletion(int position) {
        completedTasks.set(position, !completedTasks.get(position));
        notifyDataSetChanged();
    }

}
