package com.example.todolist;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements TaskDialogCloseListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private Database db;

    private RecyclerView tasksRecyclerView;
    private Adapter tasksAdapter;
    private FloatingActionButton fab;
    private SearchView searchView;

    private List<ModelToDo> taskList;

    @Override
    protected void onStart() {
        super.onStart();
        ActivityPreferenceSettings.registerPref(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
        PreferenceManager.setDefaultValues(this, R.xml.settings, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new Database(this);
        db.openDatabase();

        createNotificationChannel();

        tasksRecyclerView = findViewById(R.id.ZadaniaRecyclerView);

        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksAdapter = new Adapter(db, MainActivity.this);
        tasksRecyclerView.setAdapter(tasksAdapter);

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new TouchHelperRecyclerItem(tasksAdapter));
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);

        fab = findViewById(R.id.plus);

        taskList = db.getAllTasks();
        Collections.reverse(taskList);

        searchView = findViewById(R.id.WyszukajView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterListSearch(newText);
                return true;
            }
        });
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            searchView.setQuery(bundle.getString("title"),false);
            searchView.clearFocus();
        }

        tasksAdapter.setTasks(taskList);

        fab.setOnClickListener(v -> {
            TaskAdd.newInstance().show(getSupportFragmentManager(), TaskAdd.TAG);
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("task_option_preference")) {
            if(sharedPreferences.getBoolean("task_option_preference", false)) {

                db.openDatabase();
                taskList = db.getAllTasks();

                filterListHideItems(taskList);
            }
            else {
                tasksAdapter.setTasks(taskList);
            }
        } if(key.equals("category_preference")) {
            String category = sharedPreferences.getString("category_preference", "");
            if(!category.equals("Other")) {
                filterListCategory(taskList, category);
            } else {
                tasksAdapter.setTasks(taskList);
            }
        }
    }

    public void createNotificationChannel() {
        CharSequence name = "testChannel";


        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel("channelID", name, importance);
        //channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public void setAlarm(ModelToDo task) {
        Random rand = new Random();
        int uppRand = 500000;
        int requestCall = rand.nextInt(uppRand);
        Intent intent = new Intent(MainActivity.this, Notification.class);

        // ustawiam nazwe i powiadomienia
        intent.putExtra(Notification.TITLE_EXTRA, task.getTaskTitle());
        intent.putExtra(Notification.MESSAGE_EXTRA, task.getTask());
        intent.putExtra(Notification.ID_EXTRA, task.getId());
        intent.putExtra("requestCall", requestCall);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, requestCall, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        String date = task.getDate();
        String[] dateSplit = date.split(" ");
        int yearEnd = Integer.parseInt(dateSplit[2]);
        int monthEnd = Integer.parseInt(dateSplit[1]);
        int dayEnd = Integer.parseInt(dateSplit[0]);

        System.out.println(yearEnd + " " + monthEnd + " " + dayEnd);

        String time = task.getTime();
        String[] timeSplit = time.split(":");
        int hourEnd = Integer.parseInt(timeSplit[0]);
        int minuteEnd = Integer.parseInt(timeSplit[1]);

        LocalDateTime dateTimeEnd = LocalDateTime.of(yearEnd, monthEnd, dayEnd, hourEnd, minuteEnd);
        long dateEnd = toLong(dateTimeEnd);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String notificationPreference = sharedPref.getString("notification_preference", "5 min");
        String[] notificationPreferenceSplit = notificationPreference.split("\\s");
        int notificationPreferenceSplitTime = Integer.parseInt(notificationPreferenceSplit[0]);

        long triggerAtMillis = dateEnd * 1000L - notificationPreferenceSplitTime * 60L * 1000L;
        System.out.println(triggerAtMillis);

        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

    public static long toLong(LocalDateTime date) {
        ZonedDateTime zdt = ZonedDateTime.of(date, ZoneId.systemDefault());
        return zdt.toInstant().getEpochSecond();
    }

    private boolean toBoolean(int n) {
        return n != 0;
    }

    private void filterListHideItems(List<ModelToDo> taskList) {
        List<ModelToDo> filteredList = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            System.out.println(taskList.get(i).getStatus());
            System.out.println(toBoolean(taskList.get(i).getStatus()));
            if (!toBoolean(taskList.get(i).getStatus())) {
                System.out.println("index = " + i);
                filteredList.add(taskList.get(i));
            }
        }

        tasksAdapter.setFilteredList(filteredList);
    }

    private void filterListCategory(List<ModelToDo> taskList, String category) {
        List<ModelToDo> filteredList = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            if (taskList.get(i).getCategory().equals(category)) {
                filteredList.add(taskList.get(i));
            }
        }
        if(!filteredList.isEmpty()) {
            tasksAdapter.setFilteredList(filteredList);
        } else {
            Toast.makeText(this, "Brak dantej kategorii", Toast.LENGTH_SHORT).show();
            tasksAdapter.setFilteredList(taskList);
        }
    }

    private void filterListSearch(String text) {
        List<ModelToDo> filteredList = new ArrayList<>();
        for (ModelToDo item : taskList) {
            if (item.getTaskTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Nie znaleziono daty", Toast.LENGTH_SHORT).show();
        } else {
            tasksAdapter.setFilteredList(filteredList);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, ActivityPreferenceSettings.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        taskList = db.getAllTasks();
        Collections.reverse(taskList);
        tasksAdapter.setTasks(taskList);
        tasksAdapter.notifyDataSetChanged();
    }
}