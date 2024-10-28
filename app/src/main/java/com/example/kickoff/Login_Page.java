package com.example.kickoff;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login_Page extends AppCompatActivity {
    private TextInputEditText editTxtMail, editTxtPassword;
    private Button loginButton;
    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), Main_Page.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        mAuth = FirebaseAuth.getInstance();
        editTxtMail = findViewById(R.id.email);
        editTxtPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.bt_login);

        verifLogin();
    }

    public void verifLogin() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = String.valueOf(editTxtMail.getText());
                String password = String.valueOf(editTxtPassword.getText());

                if(TextUtils.isEmpty(mail)){
                    generateShortToast("E-mail manquant");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    generateShortToast("Password manquant");
                    return;
                }

                mAuth.signInWithEmailAndPassword(mail, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    generateShortToast("La connexion a réussi");
                                    Intent intent = new Intent(getApplicationContext(), Main_Page.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    generateShortToast("La connexion a echoué");
                                }
                            }
                        });
            }
        });
    }

    public void generateShortToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}