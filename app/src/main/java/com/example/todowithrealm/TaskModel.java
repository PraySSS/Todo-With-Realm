package com.example.todowithrealm;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TaskModel extends RealmObject {
    @PrimaryKey
    private String id;
    private String todoTask;
    private long createdAt;
    public TaskModel() {
    }

    public TaskModel(String id, String todoTask, long createdAt) {
        this.id = id;
        this.todoTask = todoTask;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTodoTask() {
        return todoTask;
    }

    public void setTodoTask(String todoTask) {
        this.todoTask = todoTask;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}