package com.example.trippy.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.trippy.Dialogs.ForgotPasswordDialog;
import com.example.trippy.Dialogs.NewAccountDialog;
import com.example.trippy.Interfaces.NewUserDetailsCallback;
import com.example.trippy.Objects.MyContainer;
import com.example.trippy.Objects.MyUser;
import com.example.trippy.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, NewUserDetailsCallback {

    private static final String TAG = "pttt";
    private static final String EMAIL = "email";
    private static int RC_SIGN_IN = 100;

    //TODO: Disable button to prevent opening again

    private Toast toast;

    /**
     * Views
     */
    private ImageView profilePicture;
    private ImageView wave;
    private ImageView facebookLoginImage;
    private ImageView googleLoginImage;
    private TextView loginButton;
    private TextView forgotPassword;
    private TextView signup;
    private EditText email;
    private EditText password;

    //Facebook
    private CallbackManager callbackManager;

    //Google
    private GoogleSignInClient mGoogleSignInClient;

    //Firebase
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    /**
     * Variables
     */

    private MyUser newUser;

    public LoginActivity() {
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Creating login activity");
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        AppEventsLogger.activateApp(getApplication());
        setContentView(R.layout.activity_login);
        initViews();
        initFacebookLogin();
        initGoogleLogin();
        initPasswordLogin();
    }

    /**
     * A method to init relevant info for facebook login
     */
    private void initFacebookLogin() {
        Log.d(TAG, "initFacebookLogin:");

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess: Facebook success: " + loginResult.toString());
                //Request the users info
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken()
                        , new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.d(TAG, "onCompleted: response: " + response.toString());
                                Log.d(TAG, "onCompleted: json: " + object.toString());
                                try {
                                    moveToMainWindow("" + object.get("name"), "" + object.get("email"));
                                } catch (JSONException e) {
                                    Log.d(TAG, "onCompleted: Exception: " + e.getMessage());
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel: Facebook cancel");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.d(TAG, "onError: Exception: " + exception.getMessage());
            }
        });
    }

    /**
     * A method to init neccessary stuff for email & password login
     */
    private void initPasswordLogin() {
        Log.d(TAG, "initPasswordLogin: Creating new stuff");
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        /**Google*/
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Log.d(TAG, "onStart GOOGLE: User already signed in: " + account.toString());


//            mGoogleSignInClient.signOut();


            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("loginCode", MainActivity.GOOGLE_LOGIN);
            startActivity(intent);
            finish();
        } else {
            Log.d(TAG, "onStart Google: User not signed in yet");
        }


        /**Firebase*/
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "onStart: FIREBASE: User not signed in yet");
        } else {
            Log.d(TAG, "onStart: FIREBASE: User logged in");
            int check = getIntent().getIntExtra("LOGGED_OUT", 0);
            if (check != 1) {
                Log.d(TAG, "onStart: Did not came from main, logging in");
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("loginCode", MainActivity.EMAIL_LOGIN);
                startActivity(intent);
                finish();
            } else {
                Log.d(TAG, "onStart: Came from main, not logging in");
                mAuth.signOut();
            }
        }
    }

    /**
     * A method to init relevant info for Google Account login
     */
    private void initGoogleLogin() {
        Log.d(TAG, "initGoogleLogin: Initing google");
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        // Build a GoogleSignInClient with the options specified by gso.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initViews() {
        Log.d(TAG, "initViews: initviews");

        /** Testing*/
        facebookLoginImage = findViewById(R.id.login_IMG_facebookLogin);
        googleLoginImage = findViewById(R.id.login_IMG_googleLogin);
        facebookLoginImage.setOnClickListener(this);
        googleLoginImage.setOnClickListener(this);
        /**End testing*/

        profilePicture = findViewById(R.id.login_IMG_profilePicture);
        wave = findViewById(R.id.login_IMG_wave);
        Glide.with(wave).load(getDrawable(R.drawable.wave_shadow_coolgreen)).into(wave);
        loginButton = findViewById(R.id.login_BTN_loginButton);
        forgotPassword = findViewById(R.id.login_LBL_forgotPassword);
        signup = findViewById(R.id.login_LBL_newAccount);
        email = findViewById(R.id.login_EDT_email);
        password = findViewById(R.id.login_EDT_password);
        loginButton.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        signup.setOnClickListener(this);
        Glide.with(profilePicture).load(getDrawable(R.drawable.icon)).into(profilePicture);
    }


    private void moveToMainWindow(String name, String email) {
        Log.d(TAG, "moveToMainWindow: FACEBOOK Moving to main activity with: " + name + " , " + email);
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("email", email);
        intent.putExtra("loginCode", MainActivity.FACEBOOK_LOGIN);
        startActivity(intent);
        finish();
        LoginManager.getInstance().logOut();
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: " + view.toString());
        switch (view.getId()) {
            case R.id.login_BTN_loginButton:
                Log.d(TAG, "onClick: email login");
                loginButton.setClickable(false);
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
                    Log.d(TAG, "checkForValidInputs: Email invalid");
                    loginButton.setClickable(true);
                    email.setError("Please enter a valid email address");
                    return;
                }
                if (password.getText().toString().equals("") || password.getText().toString().length() < 6) {
                    loginButton.setClickable(true);
                    if (password.getText().toString().length() < 6) {
                        Log.d(TAG, "checkForValidInputs: short password");
                        password.setError("Please enter at least 6 characters");
                        return;
                    } else {
                        Log.d(TAG, "checkForValidInputs: invalid password");
                        password.setError("Please enter a password");
                        return;
                    }
                }
                loginUserWithEmailAndPassword();
                break;
            case R.id.login_LBL_newAccount:
                createAnewAccount();
                break;
            case R.id.login_LBL_forgotPassword:
                forgotPassword.setEnabled(false);
                openForgotPasswordDialog();
                break;
            case R.id.login_IMG_facebookLogin:
                Log.d(TAG, "onClick: Facebook Login Selected");
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "user_friends"));
                break;
            case R.id.login_IMG_googleLogin:
                Log.d(TAG, "onClick: Google Login Selected");
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
        }
    }

    /**
     * A method to open forgot password dialog and send a reset email
     */
    private void openForgotPasswordDialog() {
        Log.d(TAG, "openForgotPasswordDialog: Opening forgot password dialog");
        ForgotPasswordDialog dialog = new ForgotPasswordDialog(this, mAuth);
        dialog.show();
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.6);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        dialog.getWindow().setLayout(width, RelativeLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        dialog.getWindow().setDimAmount(0.9f);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                forgotPassword.setEnabled(true);
            }
        });
    }

    /**
     * A Method to log in user with email and password
     */
    private void loginUserWithEmailAndPassword() {
        Log.d(TAG, "loginUserWithEmailAndPassword: Logging in user with email and password");
        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d(TAG, "onComplete: userDetails:" + user.toString());
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("loginCode", MainActivity.EMAIL_LOGIN);
                            startActivity(intent);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Exception e = task.getException();
                            String errorMsg = e.getMessage();
                            Log.w(TAG, "signInWithEmail:failure " + errorMsg);
                            if (e.getMessage().equalsIgnoreCase(
                                    "The password is invalid or the user does not have a password.")) {
                                password.setError("Wrong password");
                                loginButton.setClickable(true);
                            }
                            if (e.getMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted.")) {
                                email.setError("Email address does not exist!");
                                loginButton.setClickable(true);
                            }
                        }
                    }
                });
    }


    private void fireBasePost() {
        Log.d(TAG, "fireBasePost: Posting user to firebase");
        String emailID = newUser.getEmailAddress();
        MyContainer myContainer = new MyContainer(newUser, null);
        db.collection("users")
                .document(emailID)
                .set(myContainer)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: User saved successfully!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Exception: " + e.getMessage());
            }
        });
    }

    /**
     * A method to create a new account
     */
    private void createAnewAccount() {
        Log.d(TAG, "createAnewAccount: Creating a new account");
        NewAccountDialog newAccountDialog = new NewAccountDialog(this);
        newAccountDialog.show();
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.6);
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        newAccountDialog.getWindow().setLayout(width, RelativeLayout.LayoutParams.WRAP_CONTENT);
        newAccountDialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        newAccountDialog.getWindow().setDimAmount(0.9f);
    }

    /**
     * A method to display a toast
     */
    private void makeToast(String message) {
        Log.d(TAG, "makeToast:" + message);
        if (toast != null)
            toast.cancel();

        toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        callbackManager.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.d(TAG, "handleSignInResult: Sign in from google");
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            Log.d(TAG, "handleSignInResult: Sign in successful: " + account.toString());
            String displayName = account.getDisplayName();
            String email = account.getEmail();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("loginCode", MainActivity.GOOGLE_LOGIN);
            startActivity(intent);
            finish();

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d(TAG, "handleSignInResult: Exception: " + e.getMessage());
        }
    }

    @Override
    public void getNewUser(MyUser user) {
        Log.d(TAG, "getNewUser: got user from dialog: " + user.toString());
        newUser = user;
        afterGotNewUser();
    }

    /**
     * A method to deal with new user details
     */
    private void afterGotNewUser() {
        Log.d(TAG, "afterGotNewUser: Dealing with new user: " + newUser.toString());
        mAuth.createUserWithEmailAndPassword(newUser.getEmailAddress(), newUser.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            setFirebaseUserParams(user);
                            email.setText(newUser.getEmailAddress());
                            password.setText(newUser.getPassword());
                            Toast.makeText(LoginActivity.this, "User Created Successfully!"
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // If sign in fails, display a message to the user.
                Log.d(TAG, "createUserWithEmail:failure" + e.getMessage());
                email.setText("");
                password.setText("");
                if (e.getMessage().equalsIgnoreCase("The email address is already in use by another account.")) {
                    Toast.makeText(LoginActivity.this, "There is an account with that email!"
                            , Toast.LENGTH_SHORT).show();
                    email.setText(newUser.getEmailAddress());
                }
                if (e.getMessage().equalsIgnoreCase("The email address is badly formatted.")) {
                    Log.d(TAG, "createUserWithEmail: Failure: email badly formatted");
                    Toast.makeText(LoginActivity.this, "Please enter a valid email address"
                            , Toast.LENGTH_SHORT).show();
                    loginButton.setClickable(true);
                }

            }
        });
    }

    /**
     * A method to set the firebase users parameters
     */
    private void setFirebaseUserParams(FirebaseUser user) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUser.getFirstName() + " " + newUser.getLastName())
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated, Name: " + user.getDisplayName());
                            fireBasePost();
                        }
                    }
                });
    }
}
