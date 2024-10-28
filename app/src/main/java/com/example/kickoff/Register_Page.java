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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register_Page extends AppCompatActivity {

    private TextInputEditText editTxtMail, editTxtPassword, editTxtRePassword, username;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userId;
    private FirebaseUser currentUser;


    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance(getResources().getString(R.string.link_db)).getReference();

        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), Main_Page.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);
        mAuth = FirebaseAuth.getInstance();
        username = findViewById(R.id.pseudo);
        editTxtMail = findViewById(R.id.email);
        editTxtPassword = findViewById(R.id.password);
        editTxtRePassword = findViewById(R.id.repassword);
        registerButton = findViewById(R.id.bt_register);

        verifRegister();
    }

    public void verifRegister(){
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mail = String.valueOf(editTxtMail.getText());
                String pseudo = String.valueOf(username.getText());
                String password = String.valueOf(editTxtPassword.getText());
                String repassword = String.valueOf(editTxtRePassword.getText());

                if(TextUtils.isEmpty(pseudo)){
                    generateShortToast("Username empty");
                    return;
                }
                if(TextUtils.isEmpty(mail)){
                    generateShortToast("E-mail empty");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    generateShortToast("Password empty");
                    return;
                }
                if(!password.equals(repassword)){
                    generateShortToast( "Password are not the same");
                    return;
                }
                mAuth.createUserWithEmailAndPassword(mail, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    User user = new User(pseudo, mail,null);
                                    userId = mAuth.getCurrentUser().getUid();
                                    mDatabase.child("users").child(userId).setValue(user);
                                    generateShortToast("Le compte a bien été crée");
                                    Intent intent = new Intent(getApplicationContext(), Login_Page.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    generateShortToast("La création a echoué");
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