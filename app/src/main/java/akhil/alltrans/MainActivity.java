/*
 * Copyright 2017 Akhil Kedia
 * This file is part of AllTrans.
 *
 * AllTrans is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AllTrans is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AllTrans. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package akhil.alltrans;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    //public static String TAG = "alltrans";
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = this.getSharedPreferences("AllTransPref", MODE_WORLD_READABLE);
        utils.Debug = settings.getBoolean("Debug", false);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        utils.debugLog("Is Debug Logging enabled" + settings.getBoolean("Anon", true));
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(settings.getBoolean("Anon", true));
        if (settings.getBoolean("Anon", true)) {
            Fabric.with(this, new Crashlytics());
        }

        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.toReplace, new AppListFragment())
                .commitAllowingStateLoss();

        Toolbar toolbar = (Toolbar) findViewById(R.id.titleBar);
        toolbar.setTitle(getString(R.string.app_name) + " - " + getString(R.string.translate_anywhere));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.toReplace, new AppListFragment())
                                .commitAllowingStateLoss();
                        break;
                    case 1:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.toReplace, new GlobalPreferencesFragment())
                                .commitAllowingStateLoss();
                        break;
                    case 2:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.toReplace, new BlankFragment())
                                .commitAllowingStateLoss();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public void onClick(View v) {

        //startActivity(new Intent(getApplicationContext(), GlobalPreferenceActivity.class));

    }

}

