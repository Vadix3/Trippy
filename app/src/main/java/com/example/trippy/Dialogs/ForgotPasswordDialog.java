package com.example.trippy.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.trippy.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordDialog extends Dialog {

    private static final String TAG = "pttt";
    private Context context;
    private FirebaseAuth mAuth;

    private EditText email;
    private TextView send;


    public ForgotPasswordDialog(@NonNull Context context, FirebaseAuth mAuth) {
        super(context);
        this.context = context;
        this.mAuth = mAuth;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_forgotpassword);
        email = findViewById(R.id.forgotpassword_EDT_email);
        send = findViewById(R.id.forgotpassword_BTN_sendEmail);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUserInput();
            }
        });
    }

    /**
     * A method to check user email
     */
    private void checkUserInput() {
        Log.d(TAG, "checkUserInput: Checking user email");
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            Log.d(TAG, "checkForValidInputs: Email invalid");
            email.setError("Please enter a valid email address");
            return;
        }

        mAuth.sendPasswordResetEmail(email.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: Sent email to: " + email.getText().toString());
                Toast.makeText(context, "Sent reset email!", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Exception: " + e.getMessage());
                if(e.getMessage().equals("The email address is badly formatted.")){
                    email.setError("Please enter a valid email address");
                }
                if(e.getMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted.")){
                    email.setError("Email does not exist!");
                }
            }
        });

    }
}
