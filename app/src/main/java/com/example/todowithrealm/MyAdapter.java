package com.example.todowithrealm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmResults;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private RealmResults<TaskModel> tasks;

    public MyAdapter(RealmResults<TaskModel> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
        TaskModel task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView textItem;
        private ImageButton buttonDelete;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textItem = itemView.findViewById(R.id.txtItem);
            buttonDelete = itemView.findViewById(R.id.btnDelete);

            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        TaskModel task = tasks.get(position);
                        deleteDataFromRealm(task.getId());
                    }
                }
            });
        }

        private void deleteDataFromRealm(final String dataId) {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    TaskModel dataModel = realm.where(TaskModel.class).equalTo("id", dataId).findFirst();
                    if (dataModel != null) {
                        dataModel.deleteFromRealm();
                    }
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        notifyItemRemoved(position);
                        notifyItemChanged(position - 1); // Update the previous item
                        notifyDataSetChanged(); // Refresh the list to reflect the new sorting order
                    }
                }


            });
            realm.close();
        }

        public void bind(TaskModel task) {

            textItem.setText(String.valueOf(task.getTodoTask()));
        }
    }
}
