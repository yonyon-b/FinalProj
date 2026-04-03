package com.example.finalproj;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getPreferenceManager().setSharedPreferencesName("preferences_" + userId);
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        Preference logoutPreference = findPreference("log_out");
        logoutPreference.setOnPreferenceClickListener(preference -> {
            logOutUser();
            return true;
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("theme_preference".equals(key))
            requireActivity().recreate();
    }
    public void logOutUser(){
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // remove FCM token from database
        DatabaseReference userTokenRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserId)
                .child("fcmToken");

        userTokenRef.removeValue().addOnCompleteListener(task -> {
            // after removing the token, set user offline and sign out the user
            FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUserId)
                    .child("isOnline")
                    .setValue(false)
                    .addOnCompleteListener(t -> {
                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(getActivity(), Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        getActivity().finish();
                    });
        });
    }
}
