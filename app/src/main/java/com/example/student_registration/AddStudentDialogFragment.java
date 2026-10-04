package com.example.student_registration;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.student_registration.model.ApiErrorBody;
import com.example.student_registration.model.GroupOccupancy;
import com.example.student_registration.model.Student;
import com.example.student_registration.repository.StudentRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class AddStudentDialogFragment extends BottomSheetDialogFragment {

    private static final Pattern STUDENT_NUMBER_PATTERN = Pattern.compile("^\\d{9}$");

    public interface OnStudentAddedListener {
        void onStudentAdded(Student student);
    }

    private OnStudentAddedListener listener;

    private TextInputLayout inputLayoutFullName;
    private TextInputLayout inputLayoutStudentNumber;
    private TextInputEditText editFullName;
    private TextInputEditText editStudentNumber;
    private ChipGroup chipGroupProgramme;
    private ChipGroup chipGroupLabGroup;
    private View textAddStudentError;
    private View progressAddStudent;
    private View buttonSubmit;

    public void setOnStudentAddedListener(OnStudentAddedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_student, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
    }

    private void bindViews(View view) {
        inputLayoutFullName = view.findViewById(R.id.inputLayoutFullName);
        inputLayoutStudentNumber = view.findViewById(R.id.inputLayoutStudentNumber);
        editFullName = view.findViewById(R.id.editFullName);
        editStudentNumber = view.findViewById(R.id.editStudentNumber);
        chipGroupProgramme = view.findViewById(R.id.chipGroupProgrammeChoice);
        chipGroupLabGroup = view.findViewById(R.id.chipGroupLabGroupChoice);
        textAddStudentError = view.findViewById(R.id.textAddStudentError);
        progressAddStudent = view.findViewById(R.id.progressAddStudent);
        buttonSubmit = view.findViewById(R.id.buttonSubmitAddStudent);

        view.findViewById(R.id.buttonCancelAddStudent).setOnClickListener(v -> dismiss());
        buttonSubmit.setOnClickListener(v -> attemptSubmit());

        loadGroupOccupancyForChips();
    }

    private void loadGroupOccupancyForChips() {
        if (!isAdded() || getContext() == null) return;
        new StudentRepository(requireContext()).getGroupOccupancy(new StudentRepository.GroupsCallback() {
            @Override
            public void onSuccess(List<GroupOccupancy> groups) {
                if (!isAdded()) return;
                Set<String> full = new HashSet<>();
                for (GroupOccupancy g : groups) {
                    if (g.isFull()) full.add(g.getGroup());
                }
                disableFullGroups(full);
            }

            @Override
            public void onError(String message) {
                // Non-fatal: chips just stay enabled and the server has final say anyway.
            }
        });
    }

    private void attemptSubmit() {
        if (!isAdded() || getContext() == null) return;
        hideError();

        String fullName = editFullName.getText() != null ? editFullName.getText().toString().trim() : "";
        String studentNumber = editStudentNumber.getText() != null ? editStudentNumber.getText().toString().trim() : "";

        boolean isValid = true;

        if (fullName.length() < 2 || fullName.length() > 100) {
            inputLayoutFullName.setError(getString(R.string.error_required));
            isValid = false;
        } else {
            inputLayoutFullName.setError(null);
        }

        if (!STUDENT_NUMBER_PATTERN.matcher(studentNumber).matches()) {
            inputLayoutStudentNumber.setError(getString(R.string.error_student_number_format));
            isValid = false;
        } else {
            inputLayoutStudentNumber.setError(null);
        }

        if (!isValid) return;

        String programme = selectedProgramme();
        String group = selectedGroup();

        setLoading(true);
        new StudentRepository(requireContext()).createStudent(fullName, studentNumber, programme, group,
                new StudentRepository.CreateCallback() {
                    @Override
                    public void onSuccess(Student student) {
                        if (!isAdded()) return;
                        setLoading(false);
                        if (listener != null) listener.onStudentAdded(student);
                        dismiss();
                    }

                    @Override
                    public void onApiError(ApiErrorBody error) {
                        if (!isAdded()) return;
                        setLoading(false);
                        showError(friendlyMessage(error));
                    }

                    @Override
                    public void onNetworkError(String message) {
                        if (!isAdded()) return;
                        setLoading(false);
                        showError(getString(R.string.login_error_network));
                    }
                });
    }

    private String friendlyMessage(ApiErrorBody error) {
        if (error.getCode() == null) return error.getMessage();
        switch (error.getCode()) {
            case "GROUP_FULL":
                return getString(R.string.error_group_full);
            case "DUPLICATE_STUDENT_NUMBER":
                return getString(R.string.error_duplicate_student_number);
            default:
                return error.getMessage();
        }
    }

    private String selectedProgramme() {
        int id = chipGroupProgramme.getCheckedChipId();
        if (id == R.id.chipChooseIt) return "IT";
        if (id == R.id.chipChooseDs) return "DS";
        return "CS";
    }

    private String selectedGroup() {
        int id = chipGroupLabGroup.getCheckedChipId();
        if (id == R.id.chipChooseG01) return "G01";
        if (id == R.id.chipChooseG02) return "G02";
        if (id == R.id.chipChooseG03) return "G03";
        if (id == R.id.chipChooseG04) return "G04";
        return "Unassigned";
    }

    public void disableFullGroups(Set<String> fullGroupCodes) {
        if (chipGroupLabGroup == null) return;
        setChipEnabled(R.id.chipChooseG01, "G01", fullGroupCodes);
        setChipEnabled(R.id.chipChooseG02, "G02", fullGroupCodes);
        setChipEnabled(R.id.chipChooseG03, "G03", fullGroupCodes);
        setChipEnabled(R.id.chipChooseG04, "G04", fullGroupCodes);
    }

    private void setChipEnabled(int chipId, String code, Set<String> fullGroupCodes) {
        Chip chip = chipGroupLabGroup.findViewById(chipId);
        if (chip == null) return;
        boolean full = fullGroupCodes.contains(code);
        chip.setEnabled(!full);
        chip.setText(full ? code + " · Full" : code);
    }

    private void setLoading(boolean loading) {
        progressAddStudent.setVisibility(loading ? View.VISIBLE : View.GONE);
        buttonSubmit.setEnabled(!loading);
    }

    private void showError(String message) {
        if (TextUtils.isEmpty(message)) return;
        ((TextView) textAddStudentError).setText(message);
        textAddStudentError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        textAddStudentError.setVisibility(View.GONE);
    }
}
