package gabe.zabi.fitme_demo.ui.loginActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;


import gabe.zabi.fitme_demo.R;

/**
 * Created by Gabe on 2017-02-07.
 */

public class LoginActivity extends AppCompatActivity implements LoginFragment.Communicator {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null){
            // create fragment and add it to the activity using a fragment transaction.

            LoginFragment initialFragment = new LoginFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.login_container, initialFragment).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.login_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void sendUserInfo(Fragment fragment, boolean loggedInFromProvider) {
        Bundle args = new Bundle();
        args.putBoolean("KEY_FROM_PROVIDER", loggedInFromProvider);
        fragment.setArguments(args);
    }
}