package gabe.zabi.fitme_demo.ui.mainActivity;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import gabe.zabi.fitme_demo.model.Plan;
import gabe.zabi.fitme_demo.model.UserActivity;
import gabe.zabi.fitme_demo.model.Workouts;
import gabe.zabi.fitme_demo.ui.detailPlanActivity.DetailPlanActivity;
import gabe.zabi.fitme_demo.ui.detailPlanActivity.WorkoutFragment;
import gabe.zabi.fitme_demo.ui.searchPlanActivity.SearchPlanActivity;
import gabe.zabi.fitme_demo.ui.loginActivity.LoginActivity;
import gabe.zabi.fitme_demo.utils.Constants;
import gabe.zabi.fitme_demo.ui.BaseActivity;
import gabe.zabi.fitme_demo.R;
import gabe.zabi.fitme_demo.utils.Utils;
import gabe.zabi.fitme_demo.widget.TodayWidgetProvider;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Gabe on 2017-01-31.
 */

public class MainActivity extends BaseActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static Firebase mUserActivityRef;
    private static ValueEventListener activityListener;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;

    private ListView mDrawerList;

    private ImageView mAddPlanImageView;

    private UserActivity userActivity;
    private String currentPlan;
    private int currentWorkoutDay;
    private ArrayList<Workouts> workouts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * Link layout elements from XML and setup the toolbar
         */
        initializeScreen();

        if (Utils.getSharedPreferenceCompletionStatus(getApplicationContext())) {
            // true -> today's workout is completed
            ProgressFragment fragment = new ProgressFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_container, fragment);
            transaction.commit();
        }
    }

    public void initializeScreen() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mAddPlanImageView = (ImageView) findViewById(R.id.add_a_plan_button);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        String[] navigationArray = getResources().getStringArray(R.array.navigation_drawer_list);
        List<String> navigationItems = Arrays.asList(navigationArray);

        mDrawerList.setAdapter(new DrawerAdapter(getApplicationContext(), navigationItems));

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // MyProfile
                        Intent profile_intent = new Intent(MainActivity.this, ProfileSettingActivity.class);
                        startActivity(profile_intent);
                        break;
                    case 1:
                        // MyPlan
                        Intent plan_intent = new Intent(MainActivity.this, DetailPlanActivity.class);
                        plan_intent.putExtra("KEY_PLAN_UID", currentPlan);
                        startActivity(plan_intent);
                        break;
                    case 2:
                        // Search Plan
                        Intent search_intent = new Intent(MainActivity.this, SearchPlanActivity.class);
                        startActivity(search_intent);
                        break;
                    case 3:
                        // Logout
                        FirebaseAuth.getInstance().signOut();

                        // Set trigger for new log-in. (At BaseActivity)
                        Utils.clearAllSharedPreferences(getApplicationContext());

                        /*
                         * Move user to LoginActivity and remove the back stack
                         */
                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();
                        break;
                }
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onAddPlanClicked(View view) {
        Intent intent = new Intent(MainActivity.this, SearchPlanActivity.class);
        startActivity(intent);
    }

    /*
     * Not needed
     */

//    @Override
//    public void onBackPressed() {
//        int count = getSupportFragmentManager().getBackStackEntryCount();
//
//        if (count == 0) {
//            new AlertDialog.Builder(this)
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .setTitle("Closing App")
//                    .setMessage("Are you sure you want to close the app?")
//                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intent = new Intent(Intent.ACTION_MAIN);
//                            intent.addCategory(Intent.CATEGORY_HOME);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            startActivity(intent);
//                            finish();
//                            System.exit(0);
//                        }
//
//                    })
//                    .setNegativeButton("No", null)
//                    .show();
//        } else {
//            getSupportFragmentManager().popBackStack();
//        }
//    }

    @Override
    protected void onStart() {
        super.onStart();

        mUserActivityRef = new Firebase(Constants.FIREBASE_URL_USER).child(mCreatedUid).child(Constants.FIREBASE_LOCATION_USER_ACTIVITY);

        /**
         * Add ValueEventListeners to Firebase references
         * to control get data and control behavior and visibility of elements
         */
        activityListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                userActivity = snapshot.getValue(UserActivity.class);

                if (userActivity == null) {
                    mAddPlanImageView.setVisibility(View.VISIBLE);
                } else {
                    currentPlan = userActivity.getCurrent_plan_uid();

                    // grab current plan
                    Firebase planRef = new Firebase(Constants.FIREBASE_URL_PLAN_LISTS).child(currentPlan);
                    planRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Plan plan = dataSnapshot.getValue(Plan.class);
                            workouts = plan.getWorkouts();
                            Utils.saveSharedPreferencePlanSize(getApplicationContext(), workouts.size());

                            currentWorkoutDay = Utils.getSharedPreferenceWorkoutDay(getApplicationContext());

                            if (workouts != null) {
                                WorkoutFragment fragment = new WorkoutFragment().newInstance(userActivity.getCurrent_workout_day(),
                                                workouts.get(currentWorkoutDay), true);
                                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.main_container, fragment);
                                transaction.commit();

                                // Update Widget Data
                                Intent intent = new Intent(getApplicationContext(), TodayWidgetProvider.class);
                                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                                int[] ids = AppWidgetManager.getInstance(getApplicationContext())
                                        .getAppWidgetIds(new ComponentName(getApplicationContext(), TodayWidgetProvider.class));
                                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                                sendBroadcast(intent);
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            Log.e(LOG_TAG, getString(R.string.log_error_the_read_failed) + firebaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_TAG, getString(R.string.log_error_the_read_failed) + firebaseError.getMessage());
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUserActivityRef.removeEventListener(activityListener);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        /**
         * Perform fragment transaction.
         */
        if (!Utils.getSharedPreferenceCompletionStatus(getApplicationContext())) {
            // false -> today's workout is not completed
            mUserActivityRef.addValueEventListener(activityListener);
            Log.v(LOG_TAG, "at on postresume - false");
        } else {
            // true -> today's workout is completed
            Log.v(LOG_TAG, "at on postresume - true");
        }
    }

}