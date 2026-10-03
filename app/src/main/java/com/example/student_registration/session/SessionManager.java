package com.example.student_registration.session;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Holds the signed-in account's token/role/display name.
 *
 * Only the session token lives here — never the password, per the spec's
 * "passwords must never be stored on the Android device" rule. clear() wipes
 * everything on sign-out so a second account can never see the first
 * account's session or receive its queued sync operations.
 */
public class SessionManager {

    private static final String PREFS_NAME = "campus_roster_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ROLE = "role";
    private static final String KEY_ACCOUNT_ID = "accountId";
    private static final String KEY_DISPLAY_NAME = "displayName";
    private static final String KEY_STUDENT_ID = "studentId";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void save(String token, String role, String accountId, String displayName, String studentId) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_ROLE, role)
                .putString(KEY_ACCOUNT_ID, accountId)
                .putString(KEY_DISPLAY_NAME, displayName)
                .putString(KEY_STUDENT_ID, studentId)
                .apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }

    public String getRole() { return prefs.getString(KEY_ROLE, null); }

    public String getAccountId() { return prefs.getString(KEY_ACCOUNT_ID, null); }

    public String getDisplayName() { return prefs.getString(KEY_DISPLAY_NAME, null); }

    public String getStudentId() { return prefs.getString(KEY_STUDENT_ID, null); }

    public boolean isLoggedIn() { return getToken() != null; }

    public boolean isLecturer() { return "lecturer".equals(getRole()); }

    /** Clears the session. Called on sign-out and on SESSION_EXPIRED. */
    public void clear() {
        prefs.edit().clear().apply();
    }
}