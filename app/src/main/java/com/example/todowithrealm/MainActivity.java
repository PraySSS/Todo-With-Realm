package com.example.todowithrealm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import android.Manifest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private RecyclerView recyclerView;
    private Button buttonAdd;
    private EditText taskInput;
    private MyAdapter adapter;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        buttonAdd = findViewById(R.id.btnAdd);
        taskInput = findViewById(R.id.edtInput);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(adapter);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToRealmAsync();
            }
        });
        realm = Realm.getDefaultInstance();
        readDataFromRealm();
    }

    private void readDataFromRealm() {
        RealmResults<TaskModel> newData = realm.where(TaskModel.class).sort("createdAt", Sort.DESCENDING).findAll();
        adapter = new MyAdapter(newData);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void saveDataToRealmAsync() {
        final String task = taskInput.getText().toString();
        final long currentTimeMillis = System.currentTimeMillis(); // Get current timestamp


        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                String id = UUID.randomUUID().toString();
                TaskModel taskModel = realm.createObject(TaskModel.class, id);
                taskModel.setTodoTask(task);
                taskModel.setCreatedAt(currentTimeMillis); // Set the createdAt value

            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {

                readDataFromRealm(); // Refresh the data after adding
                checkPermissionAndExport();

            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.e("onError", "onError: " + error);
                // Handle error
            }
        });

        taskInput.getText().clear();
        hideKeyboard();
        taskInput.setFocusable(false);
        taskInput.setFocusableInTouchMode(false);
        taskInput.requestFocus();
        taskInput.setFocusable(true);
        taskInput.setFocusableInTouchMode(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View focusedView = getCurrentFocus();
        if (focusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    private void checkPermissionAndExport() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            // Permission is already granted or targeting Android 10+, export data
            exportDataToJson();

            // Delete tasks older than a month
            deleteOldTasks();
        }
    }



    private void exportDataToJson() {
        Realm realm = Realm.getDefaultInstance();

        // Fetch the Realm data you want to export
        RealmResults<TaskModel> tasks = realm.where(TaskModel.class).sort("createdAt", Sort.DESCENDING).findAll();

        // Convert Realm data to JSON
        JSONArray jsonArray = new JSONArray();
        for (TaskModel task : tasks) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", task.getId());
                jsonObject.put("todoTask", task.getTodoTask());
                jsonObject.put("createdAt", task.getCreatedAt());
                jsonArray.put(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {

            // Define your desired folder and file names
            String folderName = "realm_json";
            String fileName = "exported_data.json";

            File externalDir = getExternalFilesDir(null); // Get the app's external storage directory

            // Create a File object for the desired directory path
            File folder = new File(externalDir, folderName);

            // Create the directory if it doesn't exist
            if (!folder.exists()) {
                if (folder.mkdirs()) {
                    showToast("Directory created successfully!");
                } else {
                    showToast("Failed to create directory!");
                    return; // Exit if directory creation fails
                }
            }

            // Create a File object for the file within the created directory
            File file = new File(folder, fileName);

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(jsonArray.toString(2)); // Using 2 spaces for indentation
            fileWriter.close();

            showToast("Data exported successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Error exporting data");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    private void deleteOldTasks() {
//        final long oneMonthAgo = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L); // Timestamp for a month ago
        final long oneMonthAgo = System.currentTimeMillis() - (3 * 60 * 1000); // Timestamp for 3 minutes ago
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<TaskModel> tasksToDelete = realm.where(TaskModel.class)
                        .lessThan("createdAt", oneMonthAgo)
                        .findAll();
                tasksToDelete.deleteAllFromRealm();
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                // Refresh the data after deleting old tasks
                readDataFromRealm();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.e("onError", "onError: " + error);
                // Handle error
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, export data
                exportDataToJson();
                deleteOldTasks();
            } else {
                // Permission denied, show a message or handle it accordingly
            }
        }
    }
}