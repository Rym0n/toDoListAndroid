package com.example.todolist;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private List<ModelToDo> todoList;
    private Database db;
    private MainActivity activity;

    public Adapter(Database db, MainActivity activity) {
        this.db = db;
        this.activity = activity;
    }

    public void setFilteredList(List <ModelToDo> filteredList) {
        this.todoList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        db.openDatabase();

        final ModelToDo item = todoList.get(position);
        holder.title.setText(item.getTaskTitle());
        holder.category.setText(item.getCategory());
        holder.task.setText(item.getTask());
        holder.date.setText(item.getDate());
        holder.time.setText(item.getTime());
        holder.task.setChecked(toBoolean(item.getStatus()));
        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    db.updateStatus(item.getId(), 1);
                    // db.getAllTasks();
                    // System.out.println(db.getAllTasks());
                } else {
                    db.updateStatus(item.getId(), 0);
                }
            }
        });
    }

    private boolean toBoolean(int n) {
        return n != 0;
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public Context getContext() {
        return activity;
    }

    public void setTasks(List<ModelToDo> todoList) {
        this.todoList = todoList;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        ModelToDo item = todoList.get(position);
        db.deleteTask(item.getId());
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public void editItem(int position) {
        ModelToDo item = todoList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString(TaskAdd.NAZWA_ZADANIA, item.getTaskTitle());
        bundle.putString(TaskAdd.KATEGORIA, item.getCategory());
        bundle.putString(TaskAdd.ZADANIE, item.getTask());
        bundle.putString(TaskAdd.DATA, item.getDate());
        bundle.putString(TaskAdd.CZAS, item.getTime());
        TaskAdd fragment = new TaskAdd();
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), TaskAdd.TAG);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;
        TextView title;
        TextView category;
        TextView date;
        TextView time;
        ImageView url;

        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.todoCheckBox);
            title = view.findViewById(R.id.textViewTitle);
            category = view.findViewById(R.id.textViewCategory);
            date = view.findViewById(R.id.textViewDate);
            time = view.findViewById(R.id.textViewTime);
            url = view.findViewById(R.id.imageViewUrl);
        }
    }
}
