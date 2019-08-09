package com.example.lock_yapili;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.lock.AuthenticationCallback;
import com.auth0.android.lock.Lock;
import com.auth0.android.lock.LockCallback;
import com.auth0.android.lock.utils.LockException;
import com.auth0.android.result.Credentials;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import static com.example.lock_yapili.Constants.DELAY_MILLIS;
import static com.example.lock_yapili.Constants.FACEBOOK;
import static com.example.lock_yapili.Constants.GOOGLE;
import static com.example.lock_yapili.Constants.LINKEDIN;
import static com.example.lock_yapili.Constants.LOG_IN_UNSUCCESSFUL;
import static com.example.lock_yapili.Constants.LOG_IN_UNSUCCESSFUL_WITH_ERROR;

public class MainActivity extends AppCompatActivity {

    //class objects/variables initialization
    AuthenticationAPIClient authentication;
    TextView emailText;
    ImageView profilePicture;
    Button logoutButton;
    private Lock lock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //object binding
        emailText = findViewById(R.id.userEmail);
        profilePicture = findViewById(R.id.profilePicture);
        logoutButton = findViewById(R.id.logoutBtn);

        //creating Auth0 object
        Auth0 account = new Auth0(getString(
                R.string.com_auth0_client_id),
                getString(R.string.com_auth0_domain));

        //enabling certified OpenID Connect (OIDC) provider
        account.setOIDCConformant(true);

        authentication = new AuthenticationAPIClient(account);

        //lock UI activity build
        lock = Lock.newBuilder(account, callBack)//adding custom social login styles
                .withAuthStyle(FACEBOOK, R.style.Style_Facebook)
                .withAuthStyle(GOOGLE, R.style.Lock_Theme_AuthStyle_GoogleOAuth2)
                .withAuthStyle(LINKEDIN, R.style.Lock_Theme_AuthStyle_LinkedIn)
                .build(this);

        //sign in a user after signing up automatically
        lock.getOptions().loginAfterSignUp();

        //start lock activity
        startActivity(lock.newIntent(this));

        //user logout
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(lock.newIntent(getApplicationContext()));
            }
        });
    }

    //callback method for authentication
    private LockCallback callBack = new AuthenticationCallback() {
        @Override
        public void onAuthentication(Credentials credentials) {

            authentication.userInfo(credentials.getAccessToken())
                    .start(new BaseCallback<com.auth0.android.result.UserProfile, AuthenticationException>() {
                        @Override
                        public void onSuccess(com.auth0.android.result.UserProfile payload) {

                            //emailText.setText("Email: " + payload.getEmail());
                            setProfileImage(payload.getPictureURL());

                        }

                        @Override
                        public void onFailure(AuthenticationException error) {

                        }
                    });

        }

        //display user profile picture
        public void setProfileImage(final String url) {
            //Using Glide to load image on the ImageView asynchronously
            //Using Handler and runnable to achieve this off the main UI thread
            //Looper.getMainLooper() prepares the handler for the task
            Handler handler = new Handler(Looper.getMainLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Glide.with(getApplicationContext())
                            .load(url)
                            .apply(RequestOptions.circleCropTransform())
                            .into(profilePicture);

                }
            };
            handler.postDelayed(runnable, DELAY_MILLIS);

        }

        @Override
        public void onCanceled() {
            Toast.makeText(getApplicationContext(),
                    LOG_IN_UNSUCCESSFUL,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(LockException error) {
            Toast.makeText(getApplicationContext(),
                    LOG_IN_UNSUCCESSFUL_WITH_ERROR + error.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    };

    //destroy lock activity on activity destroy
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Your own Activity code
        lock.onDestroy(this);
        lock = null;
    }

}
