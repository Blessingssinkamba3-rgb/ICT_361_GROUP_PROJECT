package com.example.student_registration.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student_registration.R;
import com.example.student_registration.model.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    public interface OnStudentClickListener {
        void onStudentClick(Student student);
    }

    private final List<Student> students = new ArrayList<>();
    private final OnStudentClickListener listener;

    public StudentAdapter(OnStudentClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Student> newStudents) {
        students.clear();
        if (newStudents != null) students.addAll(newStudents);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bind(students.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        private final TextView initials;
        private final TextView name;
        private final TextView number;
        private final TextView programme;
        private final TextView group;

        StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            initials = itemView.findViewById(R.id.textAvatarInitials);
            name = itemView.findViewById(R.id.textStudentName);
            number = itemView.findViewById(R.id.textStudentNumber);
            programme = itemView.findViewById(R.id.textProgrammeBadge);
            group = itemView.findViewById(R.id.textGroupBadge);
        }

        void bind(Student student, OnStudentClickListener listener) {
            name.setText(student.getFullName());
            number.setText(student.getStudentNumber());
            programme.setText(student.getProgramme());
            group.setText(student.getGroup());
            initials.setText(initialsOf(student.getFullName()));
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onStudentClick(student);
            });
        }

        private static String initialsOf(String fullName) {
            if (fullName == null || fullName.trim().isEmpty()) return "?";
            String[] parts = fullName.trim().split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(2, parts.length); i++) {
                if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
            }
            return sb.length() > 0 ? sb.toString() : "?";
        }
    }
}