package com.example.finalproj;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getPreferenceManager().setSharedPreferencesName("preferences_" + userId);
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}
