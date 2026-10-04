package com.example.student_registration;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.student_registration.adapter.StudentAdapter;
import com.example.student_registration.model.GroupOccupancy;
import com.example.student_registration.repository.AuthRepository;
import com.example.student_registration.viewmodel.RosterViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudentRosterActivity extends AppCompatActivity {

    private RosterViewModel viewModel;
    private StudentAdapter adapter;

    private TextView textRecordsCount;
    private RecyclerView recyclerStudents;
    private SwipeRefreshLayout swipeRefreshRoster;
    private View progressRoster;
    private TextView textRosterEmpty;
    private TextView textRosterError;
    private ChipGroup chipGroupProgramme;
    private ChipGroup chipGroupLabGroup;

    private Set<String> fullGroupCodes = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_roster);

        textRecordsCount = findViewById(R.id.textRecordsCount);
        recyclerStudents = findViewById(R.id.recyclerStudents);
        swipeRefreshRoster = findViewById(R.id.swipeRefreshRoster);
        progressRoster = findViewById(R.id.progressRoster);
        textRosterEmpty = findViewById(R.id.textRosterEmpty);
        textRosterError = findViewById(R.id.textRosterError);
        chipGroupProgramme = findViewById(R.id.chipGroupProgramme);
        chipGroupLabGroup = findViewById(R.id.chipGroupLabGroup);

        findViewById(R.id.buttonSignOut).setOnClickListener(v -> signOut());

        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(student -> {
            // Student editor is a later build slice.
        });
        recyclerStudents.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(RosterViewModel.class);

        EditText editSearch = findViewById(R.id.editSearch);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.onSearchTextChanged(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        chipGroupProgramme.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = checkedIds.isEmpty() ? R.id.chipAllProgrammes : checkedIds.get(0);
            viewModel.onProgrammeFilterChanged(programmeForChip(id));
        });

        chipGroupLabGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = checkedIds.isEmpty() ? R.id.chipAllGroups : checkedIds.get(0);
            viewModel.onGroupFilterChanged(groupForChip(id));
        });

        swipeRefreshRoster.setOnRefreshListener(() -> {
            viewModel.refresh();
            viewModel.loadGroupChipCounts();
        });

        FloatingActionButton fabAddStudent = findViewById(R.id.fabAddStudent);
        fabAddStudent.setOnClickListener(v -> openAddStudentDialog());

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.navRoster);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navRoster) return true;
            if (id == R.id.navHome) { finish(); return true; }
            return false;
        });

        viewModel.getUiState().observe(this, this::render);
        viewModel.getGroupOccupancy().observe(this, this::renderGroupChips);

        viewModel.refresh();
        viewModel.loadGroupChipCounts();
    }

    private void render(RosterViewModel.RosterUiState state) {
        swipeRefreshRoster.setRefreshing(false);
        progressRoster.setVisibility(state.loading && state.students == null ? View.VISIBLE : View.GONE);

        textRecordsCount.setText(getString(R.string.records_count_format, state.total));

        if (state.errorMessage != null) {
            textRosterError.setText(state.errorMessage);
            textRosterError.setVisibility(View.VISIBLE);
            textRosterEmpty.setVisibility(View.GONE);
            recyclerStudents.setVisibility(View.GONE);
            return;
        }
        textRosterError.setVisibility(View.GONE);

        List<com.example.student_registration.model.Student> students = state.students;
        boolean empty = !state.loading && (students == null || students.isEmpty());
        textRosterEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerStudents.setVisibility(empty ? View.GONE : View.VISIBLE);

        adapter.submitList(students);
    }

    private void renderGroupChips(List<GroupOccupancy> groups) {
        fullGroupCodes = new HashSet<>();
        for (GroupOccupancy g : groups) {
            if (g.isFull()) fullGroupCodes.add(g.getGroup());
            setChipLabel(g.getGroup(), g.getOccupied(), g.getCapacity());
        }
    }

    private void setChipLabel(String code, int occupied, int capacity) {
        Chip chip = findChipForGroup(code);
        if (chip != null) chip.setText(code + " · " + occupied + "/" + capacity);
    }

    private Chip findChipForGroup(String code) {
        switch (code) {
            case "G01": return findViewById(R.id.chipG01);
            case "G02": return findViewById(R.id.chipG02);
            case "G03": return findViewById(R.id.chipG03);
            case "G04": return findViewById(R.id.chipG04);
            default: return null;
        }
    }

    private String programmeForChip(int chipId) {
        if (chipId == R.id.chipCs) return "CS";
        if (chipId == R.id.chipIt) return "IT";
        if (chipId == R.id.chipDs) return "DS";
        return RosterViewModel.ALL_PROGRAMMES;
    }

    private String groupForChip(int chipId) {
        if (chipId == R.id.chipG01) return "G01";
        if (chipId == R.id.chipG02) return "G02";
        if (chipId == R.id.chipG03) return "G03";
        if (chipId == R.id.chipG04) return "G04";
        return RosterViewModel.ALL_GROUPS;
    }

    private void openAddStudentDialog() {
        AddStudentDialogFragment dialog = new AddStudentDialogFragment();
        dialog.setOnStudentAddedListener(student -> {
            viewModel.refresh();
            viewModel.loadGroupChipCounts();
        });
        dialog.show(getSupportFragmentManager(), "add_student");
    }

    private void signOut() {
        new AuthRepository(this).logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}