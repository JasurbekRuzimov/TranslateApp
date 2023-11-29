package uz.jasurbekruzimov.translatorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // 2 seconds delay with handler
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // after 2 seconds, go to MainActivity
                        startActivity(new android.content.Intent(SplashScreen.this, MainActivity.class));
                        finish();
                    }
                },
                2000);

    }
}