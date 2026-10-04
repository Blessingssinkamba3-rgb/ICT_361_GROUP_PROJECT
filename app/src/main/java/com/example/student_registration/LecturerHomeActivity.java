package com.example.student_registration;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.student_registration.model.ApiErrorBody;
import com.example.student_registration.model.GroupOccupancy;
import com.example.student_registration.model.Student;
import com.example.student_registration.repository.AuthRepository;
import com.example.student_registration.repository.StudentRepository;
import com.example.student_registration.session.SessionManager;
import com.example.student_registration.viewmodel.LecturerHomeViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class LecturerHomeActivity extends AppCompatActivity {

    private LecturerHomeViewModel viewModel;
    private AuthRepository authRepository;
    private StudentRepository studentRepository;

    private SwipeRefreshLayout swipeRefreshHome;
    private ImageButton buttonSignOut;
    private TextView textHomeError;

    private View statStudents;
    private View statGroupFull;
    private View statPending;
    private View statConflicts;

    private LinearLayout containerGroupOccupancy;
    private ProgressBar progressGroupsLoading;

    private View actionNeedsAttention;
    private View actionStudentRoster;
    private View actionAddStudent;

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_home);

        authRepository = new AuthRepository(this);
        studentRepository = new StudentRepository(this);
        viewModel = new ViewModelProvider(this).get(LecturerHomeViewModel.class);

        swipeRefreshHome = findViewById(R.id.swipeRefreshHome);
        buttonSignOut = findViewById(R.id.buttonSignOut);
        textHomeError = findViewById(R.id.textHomeError);

        statStudents = findViewById(R.id.statStudents);
        statGroupFull = findViewById(R.id.statGroupFull);
        statPending = findViewById(R.id.statPending);
        statConflicts = findViewById(R.id.statConflicts);

        containerGroupOccupancy = findViewById(R.id.containerGroupOccupancy);
        progressGroupsLoading = findViewById(R.id.progressGroupsLoading);

        actionNeedsAttention = findViewById(R.id.actionNeedsAttention);
        actionStudentRoster = findViewById(R.id.actionStudentRoster);
        actionAddStudent = findViewById(R.id.actionAddStudent);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        setupStatCards();
        setupQuickActions();

        buttonSignOut.setOnClickListener(v -> {
            authRepository.logout();
            Intent intent = new Intent(LecturerHomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        swipeRefreshHome.setOnRefreshListener(() -> viewModel.refresh());

        bottomNavigation.setSelectedItemId(R.id.navHome);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navRoster) {
                startActivity(new Intent(LecturerHomeActivity.this, StudentRosterActivity.class));
                return true;
            } else if (itemId == R.id.navSync) {
                Toast.makeText(LecturerHomeActivity.this, "Sync status is up to date", Toast.LENGTH_SHORT).show();
                return true;
            }
            return itemId == R.id.navHome;
        });

        viewModel.getUiState().observe(this, state -> {
            if (state == null) return;

            swipeRefreshHome.setRefreshing(state.loading);

            if (state.errorMessage != null) {
                textHomeError.setText(state.errorMessage);
                textHomeError.setVisibility(View.VISIBLE);
            } else {
                textHomeError.setVisibility(View.GONE);
            }

            updateStatCard(statStudents, String.valueOf(state.totalStudents), "Total Enrolled");
            updateStatCard(statGroupFull, String.valueOf(state.groupsFullCount), "Groups Full");
            updateStatCard(statPending, "0", "Pending Sync");
            updateStatCard(statConflicts, "0", "Conflicts");

            if (state.groups != null) {
                progressGroupsLoading.setVisibility(View.GONE);
                displayGroupOccupancy(state.groups);
            } else {
                progressGroupsLoading.setVisibility(state.loading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.refresh();
    }

    private void setupStatCards() {
        updateStatCard(statStudents, "0", "Total Enrolled");
        updateStatCard(statGroupFull, "0", "Groups Full");
        updateStatCard(statPending, "0", "Pending Sync");
        updateStatCard(statConflicts, "0", "Conflicts");
    }

    private void updateStatCard(View card, String value, String label) {
        if (card == null) return;
        TextView textNumber = card.findViewById(R.id.textStatNumber);
        TextView textLabel = card.findViewById(R.id.textStatLabel);
        if (textNumber != null) textNumber.setText(value);
        if (textLabel != null) textLabel.setText(label);
    }

    private void setupQuickActions() {
        bindQuickAction(actionNeedsAttention, "Needs Attention", "View conflicts and sync status", R.drawable.ic_warning, v -> {
            Toast.makeText(this, "No sync items needing attention", Toast.LENGTH_SHORT).show();
        });

        bindQuickAction(actionStudentRoster, "Student Roster", "Manage students, programmes and groups", R.drawable.ic_roster, v -> {
            startActivity(new Intent(this, StudentRosterActivity.class));
        });

        bindQuickAction(actionAddStudent, "Add New Student", "Register a student to a programme and group", R.drawable.ic_add, v -> {
            showAddStudentDialog();
        });
    }

    private void bindQuickAction(View view, String title, String subtitle, int iconRes, View.OnClickListener listener) {
        if (view == null) return;
        TextView textTitle = view.findViewById(R.id.textQuickActionTitle);
        TextView textSub = view.findViewById(R.id.textQuickActionSubtitle);
        ImageView icon = view.findViewById(R.id.iconQuickAction);

        if (textTitle != null) textTitle.setText(title);
        if (textSub != null) textSub.setText(subtitle);
        if (icon != null && iconRes != 0) icon.setImageResource(iconRes);

        view.setOnClickListener(listener);
    }

    private void displayGroupOccupancy(List<GroupOccupancy> groups) {
        containerGroupOccupancy.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (GroupOccupancy group : groups) {
            View itemView = inflater.inflate(R.layout.item_group_occupancy, containerGroupOccupancy, false);
            TextView textCode = itemView.findViewById(R.id.textGroupCode);
            ProgressBar progress = itemView.findViewById(R.id.progressGroupOccupancy);
            TextView textLabel = itemView.findViewById(R.id.textGroupOccupancyLabel);

            if (textCode != null) textCode.setText(group.getGroup());
            if (progress != null) {
                progress.setMax(group.getCapacity());
                progress.setProgress(group.getOccupied());
            }
            if (textLabel != null) {
                textLabel.setText(group.getOccupied() + " / " + group.getCapacity());
            }
            containerGroupOccupancy.addView(itemView);
        }
    }

    private void showAddStudentDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        TextInputEditText editName = dialogView.findViewById(R.id.editFullName);
        TextInputEditText editNumber = dialogView.findViewById(R.id.editStudentNumber);
        ChipGroup chipProgramme = dialogView.findViewById(R.id.chipGroupProgrammeChoice);
        ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroupLabGroupChoice);
        TextView textError = dialogView.findViewById(R.id.textAddStudentError);
        ProgressBar progress = dialogView.findViewById(R.id.progressAddStudent);
        MaterialButton buttonSubmit = dialogView.findViewById(R.id.buttonSubmitAddStudent);
        MaterialButton buttonCancel = dialogView.findViewById(R.id.buttonCancelAddStudent);

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonSubmit.setOnClickListener(v -> {
            String name = editName.getText() != null ? editName.getText().toString().trim() : "";
            String number = editNumber.getText() != null ? editStudentNumberText(editNumber) : "";
            String programme = getSelectedProgramme(chipProgramme);
            String group = getSelectedGroup(chipGroup);

            if (name.isEmpty() || number.isEmpty()) {
                textError.setText("Please enter both full name and student number.");
                textError.setVisibility(View.VISIBLE);
                return;
            }

            progress.setVisibility(View.VISIBLE);
            textError.setVisibility(View.GONE);
            buttonSubmit.setEnabled(false);

            studentRepository.createStudent(name, number, programme, group, new StudentRepository.CreateCallback() {
                @Override
                public void onSuccess(Student student) {
                    dialog.dismiss();
                    Toast.makeText(LecturerHomeActivity.this, "Student added successfully", Toast.LENGTH_SHORT).show();
                    viewModel.refresh();
                }

                @Override
                public void onApiError(ApiErrorBody error) {
                    progress.setVisibility(View.GONE);
                    buttonSubmit.setEnabled(true);
                    textError.setText(error.getMessage());
                    textError.setVisibility(View.VISIBLE);
                }

                @Override
                public void onNetworkError(String message) {
                    progress.setVisibility(View.GONE);
                    buttonSubmit.setEnabled(true);
                    textError.setText("Network error. Please try again.");
                    textError.setVisibility(View.VISIBLE);
                }
            });
        });

        dialog.show();
    }

    private String editStudentNumberText(TextInputEditText edit) {
        return edit.getText() != null ? edit.getText().toString().trim() : "";
    }

    private String getSelectedProgramme(ChipGroup chipGroup) {
        int id = chipGroup.getCheckedChipId();
        if (id == R.id.chipChooseCs) return "CS";
        if (id == R.id.chipChooseIt) return "IT";
        if (id == R.id.chipChooseDs) return "DS";
        return "CS";
    }

    private String getSelectedGroup(ChipGroup chipGroup) {
        int id = chipGroup.getCheckedChipId();
        if (id == R.id.chipChooseG01) return "G01";
        if (id == R.id.chipChooseG02) return "G02";
        if (id == R.id.chipChooseG03) return "G03";
        if (id == R.id.chipChooseG04) return "G04";
        return "G01";
    }
}