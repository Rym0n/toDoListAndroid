package com.example.todolist;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class TaskAdd extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";
    public static final String NAZWA_ZADANIA = "Nazwa zadania";
    public static final String KATEGORIA = "kategoria";
    public static final String ZADANIE = "zadanie";
    public static final String DATA = "data";
    public static final String CZAS = "czas";


    private EditText newTitleText;
    private EditText newCategory;
    private EditText newTaskText;
    private Button newTaskSaveButton;
    private Button newDate;
    private Button newTime;
    private Button url;
    private Switch switchButton;
    private boolean switchChecked;
    private int year, month, day;
    private String date;
    private int hour, minute;
    private boolean pickedTime = false;
    private boolean pickedDate = false;
    private android.widget.ImageView ImageView;

    private Database db;

    public static TaskAdd newInstance(){
        return new TaskAdd();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.new_task, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newTitleText = requireView().findViewById(R.id.newTitleText);
        newCategory = requireView().findViewById(R.id.newCategory);
        newTaskText = requireView().findViewById(R.id.newTaskText);
        newDate = requireView().findViewById(R.id.newDate);
        url = requireView().findViewById(R.id.url);
        newTime = requireView().findViewById(R.id.newTime);
        switchButton = requireView().findViewById(R.id.switchButton);
        newTaskSaveButton = getView().findViewById(R.id.newTaskButton);
        ImageView = view.findViewById(R.id.imageViewUrl);

        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if(bundle != null){
            isUpdate = true;
            String taskTitle = bundle.getString(NAZWA_ZADANIA);
            String category = bundle.getString(KATEGORIA);
            String task = bundle.getString(ZADANIE);
            String date = bundle.getString(DATA);
            String time = bundle.getString(CZAS);
            newTaskText.setText(task);
            newTitleText.setText(taskTitle);
            newCategory.setText(category);
            newDate.setText(getTodayDate());
            newTime.setText(time);
            assert task != null;
            if(task.length()>0)
                newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1));
        }

        db = new Database(getActivity());
        db.openDatabase();

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
                else {
                    if(!newTitleText.getText().toString().isEmpty() && !newCategory.getText().toString().isEmpty() && pickedDate && pickedTime) {
                        newTaskSaveButton.setEnabled(true);
                        newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        newTitleText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    newTaskSaveButton.setEnabled(false);
                    newTaskSaveButton.setTextColor(Color.GRAY);
                }
                else {
                    if(!newCategory.getText().toString().isEmpty() && !newTaskText.getText().toString().isEmpty() && pickedDate && pickedTime) {
                        newTaskSaveButton.setEnabled(true);
                        newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        newCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    newTaskSaveButton.setEnabled(false);
                    newTaskSaveButton.setTextColor(Color.GRAY);
                }
                else {
                    if(!newTitleText.getText().toString().isEmpty() && !newTaskText.getText().toString().isEmpty() && pickedDate && pickedTime) {
                        newTaskSaveButton.setEnabled(true);
                        newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=23){
                    if(checkPermission()){
                        filePicker();
                    }
                    else{
                        requestPermission();
                    }
                }
                else{
                    filePicker();

                }

            }

        });
        final boolean finalIsUpdate = isUpdate;
        newTaskSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = newTaskText.getText().toString();
                String textTitle = newTitleText.getText().toString();
                String category = newCategory.getText().toString();
                String date = newDate.getText().toString();
                String time = newTime.getText().toString();
                if(finalIsUpdate){
                    db.updateTask(bundle.getInt("id"), text);
                    db.updateTaskTitle(bundle.getInt("id"), textTitle);
                    db.updateCategory(bundle.getInt("id"), category);
                    db.updateDate(bundle.getInt("id"), date);
                    db.updateTime(bundle.getInt("id"), time);
                    if(switchChecked) {
                        ModelToDo task = new ModelToDo();
                        task.setTaskTitle(textTitle);
                        task.setCategory(category);
                        task.setTask(text);
                        task.setDate(date);
                        task.setTime(time);
                        task.setStatus(0);
                        ((MainActivity) getActivity()).setAlarm(task);
                    }
                }
                else {
                    ModelToDo task = new ModelToDo();
                    task.setTaskTitle(textTitle);
                    task.setCategory(category);
                    task.setTask(text);
                    task.setDate(date);
                    task.setTime(time);
                    task.setStatus(0);
                    db.insertTask(task);
                    if(switchChecked) {
                        ((MainActivity) getActivity()).setAlarm(task);
                    }
                }
                dismiss();
            }
        });

        newDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popDatePicker(view);
            }
        });

        newTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popTimePicker(view);
            }
        });

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    switchChecked = true;
                    if(!newTitleText.getText().toString().isEmpty() && !newTaskText.getText().toString().isEmpty() && !newCategory.getText().toString().isEmpty() && pickedDate && pickedTime)
                        newTaskSaveButton.setEnabled(true);
                } else {
                    switchChecked = false;
                    if(!newTitleText.getText().toString().isEmpty() && !newTaskText.getText().toString().isEmpty() && !newCategory.getText().toString().isEmpty() && pickedDate && pickedTime)
                        newTaskSaveButton.setEnabled(true);
                }
            }
        });
    }

    private String getTodayDate() {
            Calendar cal = Calendar.getInstance();
            int yearStart = cal.get(Calendar.YEAR);
            int monthStart = cal.get(Calendar.MONTH);
            month = month+1;
            int dayStart = cal.get(Calendar.DAY_OF_MONTH);
            return makeDateString(dayStart,monthStart,yearStart);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog){
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if(activity instanceof TaskDialogCloseListener)
            ((TaskDialogCloseListener)activity).handleDialogClose(dialog);
    }

    public void popDatePicker(View view) {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, selectedYear, selectedMonth, selectedDay) -> {
            year = selectedYear;
            month = selectedMonth + 1;
            day = selectedDay;
            date = makeDateString(day,month,year);
            newDate.setText(date);
        };


        int style = AlertDialog.THEME_HOLO_LIGHT;

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), style,dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        datePickerDialog.setTitle("WYBIERZ DATE");
        datePickerDialog.show();

        pickedDate = true;
        if(!newTitleText.getText().toString().isEmpty() && !newTaskText.getText().toString().isEmpty() && !newCategory.getText().toString().isEmpty() && pickedDate && pickedTime) {
            newTaskSaveButton.setEnabled(true);
            newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1));
        }

    }
    private String makeDateString(int day,int month,int year){
        return day + " " + month + " " + year;
    }


    public void popTimePicker(View view) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, selectedHour, selectedMinute) -> {
            hour = selectedHour;
            minute = selectedMinute;
            newTime.setText(String.format(Locale.getDefault(),"%02d:%02d",hour ,minute));
        };

        int style = AlertDialog.THEME_HOLO_LIGHT;

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),style,onTimeSetListener, hour, minute, true);

        timePickerDialog.setTitle("Select time");
        timePickerDialog.show();

        pickedTime = true;
        if(!newTitleText.getText().toString().isEmpty() && !newTaskText.getText().toString().isEmpty() && !newCategory.getText().toString().isEmpty() && pickedDate && pickedTime) {
            newTaskSaveButton.setEnabled(true);
            newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue1));
        }
    }
    private void filePicker(){

        //.Now Permission Working
        Toast.makeText( getContext(), "File Picker Call", Toast.LENGTH_SHORT).show();
        //Let's Pick File
        Intent data = new Intent(Intent.ACTION_GET_CONTENT);
        data.setType("/");
        startActivityForResult(data,1);


    }
    private boolean checkPermission(){
        int result= ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(result== PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            return false;
        }
    }
    private void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(getContext(), "Please Give Permission to Upload File", Toast.LENGTH_SHORT).show();
        }
        else{
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode== Activity.RESULT_OK){
            String filePath = data.getData().getPath();
            //String filePath=getRealPathFromUri(data.getData(),getActivity());
            //if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            File fileToSave = new File(Environment.getExternalStorageDirectory(), filePath);
            ImageView.setImageURI(data.getData());
            // Bitmap bitmap = MediaStore.Images.Media.getBitmap( this , fileName.getText());

        }
    }
}
