package com.example.student_registration.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.student_registration.model.ApiErrorBody;
import com.example.student_registration.model.GroupOccupancy;
import com.example.student_registration.model.Student;
import com.example.student_registration.model.StudentListResponse;
import com.example.student_registration.repository.StudentRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RosterViewModel extends AndroidViewModel {

    public static final String ALL_PROGRAMMES = null;
    public static final String ALL_GROUPS = null;

    public static final class RosterUiState {
        public final boolean loading;
        public final String errorMessage;
        public final List<Student> students;
        public final int total;

        RosterUiState(boolean loading, String errorMessage, List<Student> students, int total) {
            this.loading = loading;
            this.errorMessage = errorMessage;
            this.students = students;
            this.total = total;
        }
    }

    private static final long SEARCH_DEBOUNCE_MS = 350;

    private final StudentRepository studentRepository;
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private final AtomicInteger requestTicket = new AtomicInteger(0);
    private Runnable pendingSearch;

    private String currentSearch = "";
    private String currentProgramme = ALL_PROGRAMMES;
    private String currentGroup = ALL_GROUPS;

    private final MutableLiveData<RosterUiState> uiState =
            new MutableLiveData<>(new RosterUiState(false, null, null, 0));
    private final MutableLiveData<List<GroupOccupancy>> groupOccupancy = new MutableLiveData<>();

    public RosterViewModel(@NonNull Application application) {
        super(application);
        studentRepository = new StudentRepository(application);
    }

    public LiveData<RosterUiState> getUiState() { return uiState; }
    public LiveData<List<GroupOccupancy>> getGroupOccupancy() { return groupOccupancy; }

    public void loadGroupChipCounts() {
        studentRepository.getGroupOccupancy(new StudentRepository.GroupsCallback() {
            @Override public void onSuccess(List<GroupOccupancy> groups) { groupOccupancy.postValue(groups); }
            @Override public void onError(String message) { /* chips just fall back to plain labels */ }
        });
    }

    public void onSearchTextChanged(String text) {
        currentSearch = text == null ? "" : text;
        if (pendingSearch != null) debounceHandler.removeCallbacks(pendingSearch);
        pendingSearch = this::runSearch;
        debounceHandler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS);
    }

    public void onProgrammeFilterChanged(String programmeCode) {
        currentProgramme = programmeCode;
        runSearch();
    }

    public void onGroupFilterChanged(String groupCode) {
        currentGroup = groupCode;
        runSearch();
    }

    public void refresh() {
        runSearch();
    }

    private void runSearch() {
        final int ticket = requestTicket.incrementAndGet();
        uiState.setValue(new RosterUiState(true, null, uiState.getValue() != null ? uiState.getValue().students : null, 0));

        studentRepository.searchRoster(currentSearch, currentProgramme, currentGroup, 1, 50,
                new StudentRepository.ListCallback() {
                    @Override
                    public void onSuccess(StudentListResponse result) {
                        if (ticket != requestTicket.get()) return;
                        uiState.postValue(new RosterUiState(false, null, result.getItems(), result.getTotal()));
                    }

                    @Override
                    public void onApiError(ApiErrorBody error) {
                        if (ticket != requestTicket.get()) return;
                        uiState.postValue(new RosterUiState(false, error.getMessage(), null, 0));
                    }

                    @Override
                    public void onNetworkError(String message) {
                        if (ticket != requestTicket.get()) return;
                        uiState.postValue(new RosterUiState(false, "Couldn't load the roster. Pull down to retry.", null, 0));
                    }
                });
    }
}