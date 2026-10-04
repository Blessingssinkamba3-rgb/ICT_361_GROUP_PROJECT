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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.student_registration.model.GroupOccupancy;
import com.example.student_registration.repository.AuthRepository;
import com.example.student_registration.viewmodel.LecturerHomeViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class LecturerHomeActivity extends AppCompatActivity {

    private LecturerHomeViewModel viewModel;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout containerGroupOccupancy;
    private ProgressBar progressGroupsLoading;
    private TextView textHomeError;

    private View statStudents, statGroupFull, statPending, statConflicts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecturer_home);

        swipeRefresh = findViewById(R.id.swipeRefreshHome);
        containerGroupOccupancy = findViewById(R.id.containerGroupOccupancy);
        progressGroupsLoading = findViewById(R.id.progressGroupsLoading);
        textHomeError = findViewById(R.id.textHomeError);

        statStudents = findViewById(R.id.statStudents);
        statGroupFull = findViewById(R.id.statGroupFull);
        statPending = findViewById(R.id.statPending);
        statConflicts = findViewById(R.id.statConflicts);
        bindStatLabel(statStudents, getString(R.string.stat_students));
        bindStatLabel(statGroupFull, getString(R.string.stat_group_full));
        bindStatLabel(statPending, getString(R.string.stat_pending));
        bindStatLabel(statConflicts, getString(R.string.stat_conflicts));

        ImageButton buttonSignOut = findViewById(R.id.buttonSignOut);
        buttonSignOut.setOnClickListener(v -> signOut());

        View actionStudentRoster = findViewById(R.id.actionStudentRoster);
        bindQuickAction(actionStudentRoster, R.drawable.ic_roster,
                getString(R.string.student_roster_title), getString(R.string.student_roster_subtitle));
        actionStudentRoster.setOnClickListener(v -> openRoster());

        View actionAddStudent = findViewById(R.id.actionAddStudent);
        bindQuickAction(actionAddStudent, R.drawable.ic_add,
                getString(R.string.add_student_title), getString(R.string.add_student_subtitle));
        actionAddStudent.setOnClickListener(v -> openRoster());

        View actionNeedsAttention = findViewById(R.id.actionNeedsAttention);
        bindQuickAction(actionNeedsAttention, R.drawable.ic_warning,
                getString(R.string.needs_attention_title), "");

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.navHome);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navHome) return true;
            if (id == R.id.navRoster) { openRoster(); return true; }
            return false;
        });

        viewModel = new ViewModelProvider(this).get(LecturerHomeViewModel.class);
        swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());

        viewModel.getUiState().observe(this, this::render);
        viewModel.refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.refresh();
    }

    private void render(LecturerHomeViewModel.HomeUiState state) {
        swipeRefresh.setRefreshing(state.loading);
        progressGroupsLoading.setVisibility(
                state.loading && state.groups == null ? View.VISIBLE : View.GONE);

        if (state.errorMessage != null) {
            textHomeError.setText(state.errorMessage);
            textHomeError.setVisibility(View.VISIBLE);
        } else {
            textHomeError.setVisibility(View.GONE);
        }

        setStatNumber(statStudents, state.totalStudents);
        setStatNumber(statGroupFull, state.groupsFullCount);
        setStatNumber(statPending, 0);
        setStatNumber(statConflicts, 0);

        if (state.groups != null) {
            renderGroupOccupancy(state.groups);
        }
    }

    private void renderGroupOccupancy(List<GroupOccupancy> groups) {
        int childCount = containerGroupOccupancy.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View child = containerGroupOccupancy.getChildAt(i);
            if (child != progressGroupsLoading) {
                containerGroupOccupancy.removeView(child);
            }
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (GroupOccupancy group : groups) {
            View row = inflater.inflate(R.layout.item_group_occupancy, containerGroupOccupancy, false);
            ((TextView) row.findViewById(R.id.textGroupCode)).setText(group.getGroup());
            ((TextView) row.findViewById(R.id.textGroupOccupancyLabel))
                    .setText(group.getOccupied() + "/" + group.getCapacity());
            ProgressBar bar = row.findViewById(R.id.progressGroupOccupancy);
            bar.setMax(group.getCapacity());
            bar.setProgress(group.getOccupied());
            containerGroupOccupancy.addView(row);
        }
    }

    private void bindStatLabel(View statTile, String label) {
        TextView textLabel = statTile.findViewById(R.id.textStatLabel);
        textLabel.setText(label);
    }

    private void setStatNumber(View statTile, int number) {
        TextView textNumber = statTile.findViewById(R.id.textStatNumber);
        textNumber.setText(String.valueOf(number));
    }

    private void bindQuickAction(View actionRow, int iconRes, String title, String subtitle) {
        ((ImageView) actionRow.findViewById(R.id.iconQuickAction)).setImageResource(iconRes);
        ((TextView) actionRow.findViewById(R.id.textQuickActionTitle)).setText(title);
        TextView subtitleView = actionRow.findViewById(R.id.textQuickActionSubtitle);
        if (subtitle == null || subtitle.isEmpty()) {
            subtitleView.setVisibility(View.GONE);
        } else {
            subtitleView.setVisibility(View.VISIBLE);
            subtitleView.setText(subtitle);
        }
    }

    private void openRoster() {
        startActivity(new Intent(this, StudentRosterActivity.class));
    }

    private void signOut() {
        new AuthRepository(this).logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}