package akhil.alltrans;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    public static String TAG = "alltrans";
    SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.toReplace, new AppListFragment())
                .commit();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.toReplace, new AppListFragment())
                                .commit();
                        Context context = getApplicationContext();
                        CharSequence text = "Hello toast0!";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        break;
                    case 1:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.toReplace, new GlobalPreferencesFragment())
                                .commit();
                        Context context2 = getApplicationContext();
                        CharSequence text2 = "Hello toast1!";
                        int duration2 = Toast.LENGTH_SHORT;

                        Toast toast2 = Toast.makeText(context2, text2, duration2);
                        toast2.show();
                        break;

                    //startActivity(new Intent(getApplicationContext(), GlobalPreferenceActivity.class));
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

