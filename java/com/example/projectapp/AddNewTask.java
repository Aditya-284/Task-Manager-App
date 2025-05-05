package com.example.projectapp;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;

public class AddNewTask extends DialogFragment {

    public static final String TAG = "ActionBottomDialog";
    private static final String LOG_TAG = "AddNewTask";
    private EditText newTaskText;
    private Button newTaskSaveButton;
    private Button dateButton;
    private Button timeButton;
    private TextView selectedDateTime;
    private DatabaseHandler db;
    private NotificationHelper notificationHelper;
    private String selectedDate = "";
    private String selectedTime = "";

    public static AddNewTask newInstance(){
        return new AddNewTask();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);
        Log.d(LOG_TAG, "Dialog created");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_task, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        Log.d(LOG_TAG, "Dialog view created");
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(LOG_TAG, "Initializing views...");
        newTaskText = view.findViewById(R.id.newTaskText);
        newTaskSaveButton = view.findViewById(R.id.newTaskButton);
        dateButton = view.findViewById(R.id.dateButton);
        timeButton = view.findViewById(R.id.timeButton);
        selectedDateTime = view.findViewById(R.id.selectedDateTime);
        notificationHelper = new NotificationHelper(getActivity());
        boolean isUpdate = false;
        final Bundle bundle = getArguments();
        if(bundle != null){
            isUpdate = true;
            String task = bundle.getString("task");
            selectedDate = bundle.getString("due_date", "");
            selectedTime = bundle.getString("due_time", "");
            newTaskText.setText(task);
            updateDateTimeDisplay();
            if(task != null && task.length()>0)
                newTaskSaveButton.setTextColor(getResources().getColor(R.color.colorSkyBlue));
            Log.d(LOG_TAG, "Updating existing task: " + task);
        }
        db = new DatabaseHandler(getActivity());
        db.openDatabase();
        Log.d(LOG_TAG, "Database initialized");
        dateButton.setOnClickListener(v -> showDatePicker());
        timeButton.setOnClickListener(v -> showTimePicker());
        newTaskText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    newTaskSaveButton.setEnabled(false);
                    newTaskSaveButton.setTextColor(Color.GRAY);
                }
                else{
                    newTaskSaveButton.setEnabled(true);
                    newTaskSaveButton.setTextColor(getResources().getColor(R.color.colorSkyBlue));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        final boolean finalIsUpdate = isUpdate;
        newTaskSaveButton.setOnClickListener(v -> {
            String text = newTaskText.getText().toString();
            Log.d(LOG_TAG, "Saving task: " + text);
            try {
                if(finalIsUpdate){
                    int taskId = bundle.getInt("id");
                    db.updateTask(taskId, text, selectedDate, selectedTime);
                    // Cancel existing reminder and schedule new one
                    notificationHelper.cancelTaskReminder(taskId);
                    if (!selectedDate.isEmpty() && !selectedTime.isEmpty()) {
                        notificationHelper.scheduleTaskReminder(taskId, text, selectedDate, selectedTime);
                    }
                    Log.d(LOG_TAG, "Task updated successfully");
                }
                else {
                    ToDoModel task = new ToDoModel();
                    task.setTask(text);
                    task.setStatus(0);
                    task.setDueDate(selectedDate);
                    task.setDueTime(selectedTime);
                    db.insertTask(task);
                    // Schedule reminder for new task
                    if (!selectedDate.isEmpty() && !selectedTime.isEmpty()) {
                        notificationHelper.scheduleTaskReminder(task.getId(), text, selectedDate, selectedTime);
                    }
                    Log.d(LOG_TAG, "New task inserted successfully");
                }
                dismiss();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error saving task: " + e.getMessage());
                Toast.makeText(getActivity(), "Error saving task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    updateDateTimeDisplay();
                    Log.d(LOG_TAG, "Date selected: " + selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                (view, selectedHour, selectedMinute) -> {
                    selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    updateDateTimeDisplay();
                    Log.d(LOG_TAG, "Time selected: " + selectedTime);
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private void updateDateTimeDisplay() {
        String displayText = "";
        if (!selectedDate.isEmpty()) {
            displayText += "Date: " + selectedDate;
        }
        if (!selectedTime.isEmpty()) {
            if (!displayText.isEmpty()) {
                displayText += " ";
            }
            displayText += "Time: " + selectedTime;
        }
        selectedDateTime.setText(displayText);
        Log.d(LOG_TAG, "DateTime display updated: " + displayText);
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        Log.d(LOG_TAG, "Dialog dismissed");
        Activity activity = getActivity();
        if(activity instanceof DialogCloseListener)
            ((DialogCloseListener)activity).handleDialogClose(dialog);
    }
}