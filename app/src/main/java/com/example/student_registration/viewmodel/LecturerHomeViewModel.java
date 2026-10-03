package com.example.student_registration.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.student_registration.model.GroupOccupancy;
import com.example.student_registration.model.StudentListResponse;
import com.example.student_registration.repository.StudentRepository;

import java.util.List;

public class LecturerHomeViewModel extends AndroidViewModel {

    public static final class HomeUiState {
        public final boolean loading;
        public final String errorMessage;
        public final int totalStudents;
        public final int groupsFullCount;
        public final List<GroupOccupancy> groups;

        HomeUiState(boolean loading, String errorMessage, int totalStudents, int groupsFullCount, List<GroupOccupancy> groups) {
            this.loading = loading;
            this.errorMessage = errorMessage;
            this.totalStudents = totalStudents;
            this.groupsFullCount = groupsFullCount;
            this.groups = groups;
        }
    }

    private final StudentRepository studentRepository;
    private final MutableLiveData<HomeUiState> uiState =
            new MutableLiveData<>(new HomeUiState(false, null, 0, 0, null));

    public LecturerHomeViewModel(@NonNull Application application) {
        super(application);
        studentRepository = new StudentRepository(application);
    }

    public LiveData<HomeUiState> getUiState() {
        return uiState;
    }

    public void refresh() {
        uiState.setValue(new HomeUiState(true, null, 0, 0, null));

        studentRepository.searchRoster(null, null, null, 1, 1, new StudentRepository.ListCallback() {
            @Override
            public void onSuccess(StudentListResponse rosterResult) {
                loadGroups(rosterResult.getTotal());
            }

            @Override
            public void onApiError(com.example.student_registration.model.ApiErrorBody error) {
                uiState.postValue(new HomeUiState(false, error.getMessage(), 0, 0, null));
            }

            @Override
            public void onNetworkError(String message) {
                uiState.postValue(new HomeUiState(false, "Couldn't load the dashboard. Pull down to retry.", 0, 0, null));
            }
        });
    }

    private void loadGroups(int totalStudents) {
        studentRepository.getGroupOccupancy(new StudentRepository.GroupsCallback() {
            @Override
            public void onSuccess(List<GroupOccupancy> groups) {
                int full = 0;
                for (GroupOccupancy g : groups) {
                    if (g.isFull()) full++;
                }
                uiState.postValue(new HomeUiState(false, null, totalStudents, full, groups));
            }

            @Override
            public void onError(String message) {
                uiState.postValue(new HomeUiState(false, "Couldn't load group occupancy.", totalStudents, 0, null));
            }
        });
    }
}