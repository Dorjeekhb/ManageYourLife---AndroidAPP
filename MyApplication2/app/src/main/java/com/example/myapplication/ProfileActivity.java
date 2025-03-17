package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvPoints = findViewById(R.id.tvPoints);
        updatePoints();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePoints();
    }

    private void updatePoints() {
        int totalPoints = GamificationManager.getPoints(this);
        int level = GamificationManager.getLevel(this);
        tvPoints.setText("Total Points: " + totalPoints + "\nNivel: " + level);
    }
}
