package com.example.student_registration;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.student_registration.adapter.StudentAdapter;
import com.example.student_registration.model.ApiErrorBody;
import com.example.student_registration.model.Student;
import com.example.student_registration.repository.AuthRepository;
import com.example.student_registration.repository.StudentRepository;
import com.example.student_registration.viewmodel.RosterViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class StudentRosterActivity extends AppCompatActivity {

    private RosterViewModel viewModel;
    private AuthRepository authRepository;
    private StudentRepository studentRepository;
    private StudentAdapter adapter;

    private TextView textRecordsCount;
    private ImageButton buttonSignOut;
    private EditText editSearch;
    private ChipGroup chipGroupProgramme;
    private ChipGroup chipGroupLabGroup;

    private SwipeRefreshLayout swipeRefreshRoster;
    private RecyclerView recyclerStudents;
    private ProgressBar progressRoster;
    private TextView textRosterEmpty;
    private TextView textRosterError;
    private FloatingActionButton fabAddStudent;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_roster);

        authRepository = new AuthRepository(this);
        studentRepository = new StudentRepository(this);
        viewModel = new ViewModelProvider(this).get(RosterViewModel.class);

        textRecordsCount = findViewById(R.id.textRecordsCount);
        buttonSignOut = findViewById(R.id.buttonSignOut);
        editSearch = findViewById(R.id.editSearch);
        chipGroupProgramme = findViewById(R.id.chipGroupProgramme);
        chipGroupLabGroup = findViewById(R.id.chipGroupLabGroup);

        swipeRefreshRoster = findViewById(R.id.swipeRefreshRoster);
        recyclerStudents = findViewById(R.id.recyclerStudents);
        progressRoster = findViewById(R.id.progressRoster);
        textRosterEmpty = findViewById(R.id.textRosterEmpty);
        textRosterError = findViewById(R.id.textRosterError);
        fabAddStudent = findViewById(R.id.fabAddStudent);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        adapter = new StudentAdapter(student -> {
            Toast.makeText(this, student.getFullName() + " (" + student.getStudentNumber() + ")", Toast.LENGTH_SHORT).show();
        });

        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        recyclerStudents.setAdapter(adapter);

        buttonSignOut.setOnClickListener(v -> {
            authRepository.logout();
            Intent intent = new Intent(StudentRosterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        swipeRefreshRoster.setOnRefreshListener(() -> viewModel.refresh());

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.onSearchTextChanged(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        chipGroupProgramme.setOnCheckedChangeListener((group, checkedId) -> {
            String prog = null;
            if (checkedId == R.id.chipCs) prog = "CS";
            else if (checkedId == R.id.chipIt) prog = "IT";
            else if (checkedId == R.id.chipDs) prog = "DS";
            viewModel.onProgrammeFilterChanged(prog);
        });

        chipGroupLabGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String labGroup = null;
            if (checkedId == R.id.chipG01) labGroup = "G01";
            else if (checkedId == R.id.chipG02) labGroup = "G02";
            else if (checkedId == R.id.chipG03) labGroup = "G03";
            else if (checkedId == R.id.chipG04) labGroup = "G04";
            viewModel.onGroupFilterChanged(labGroup);
        });

        fabAddStudent.setOnClickListener(v -> showAddStudentDialog());

        bottomNavigation.setSelectedItemId(R.id.navRoster);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navHome) {
                startActivity(new Intent(StudentRosterActivity.this, LecturerHomeActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navSync) {
                Toast.makeText(StudentRosterActivity.this, "Sync status is up to date", Toast.LENGTH_SHORT).show();
                return true;
            }
            return itemId == R.id.navRoster;
        });

        viewModel.getUiState().observe(this, state -> {
            if (state == null) return;

            swipeRefreshRoster.setRefreshing(state.loading);
            progressRoster.setVisibility(state.loading && (state.students == null || state.students.isEmpty()) ? View.VISIBLE : View.GONE);

            if (state.errorMessage != null) {
                textRosterError.setText(state.errorMessage);
                textRosterError.setVisibility(View.VISIBLE);
                textRosterEmpty.setVisibility(View.GONE);
                recyclerStudents.setVisibility(View.GONE);
            } else if (state.students != null) {
                textRosterError.setVisibility(View.GONE);
                adapter.submitList(state.students);
                textRecordsCount.setText(state.total + " records");

                if (state.students.isEmpty() && !state.loading) {
                    textRosterEmpty.setVisibility(View.VISIBLE);
                    recyclerStudents.setVisibility(View.GONE);
                } else {
                    textRosterEmpty.setVisibility(View.GONE);
                    recyclerStudents.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.refresh();
    }

    private void showAddStudentDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_student, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        TextInputEditText editName = dialogView.findViewById(R.id.editFullName);
        TextInputEditText editNumber = dialogView.findViewById(R.id.editStudentNumber);
        ChipGroup chipProgrammeChoice = dialogView.findViewById(R.id.chipGroupProgrammeChoice);
        ChipGroup chipGroupChoice = dialogView.findViewById(R.id.chipGroupLabGroupChoice);
        TextView textError = dialogView.findViewById(R.id.textAddStudentError);
        ProgressBar progress = dialogView.findViewById(R.id.progressAddStudent);
        MaterialButton buttonSubmit = dialogView.findViewById(R.id.buttonSubmitAddStudent);
        MaterialButton buttonCancel = dialogView.findViewById(R.id.buttonCancelAddStudent);

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonSubmit.setOnClickListener(v -> {
            String name = editName.getText() != null ? editName.getText().toString().trim() : "";
            String number = editNumber.getText() != null ? editNumber.getText().toString().trim() : "";
            String programme = getSelectedProgramme(chipProgrammeChoice);
            String group = getSelectedGroup(chipGroupChoice);

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
                    Toast.makeText(StudentRosterActivity.this, "Student added successfully", Toast.LENGTH_SHORT).show();
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