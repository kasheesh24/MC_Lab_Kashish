package com.example.cellular_frequency_reuse_game_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;
    private TextView tvScore;
    private Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);
        tvScore = findViewById(R.id.tvScore);
        btnReset = findViewById(R.id.btnReset);

        gameView.setOnScoreChangeListener(new GameView.OnScoreChangeListener() {
            @Override
            public void onScoreChanged(int score) {
                tvScore.setText(getString(R.string.score_label, score));
            }
        });

        btnReset.setOnClickListener(v -> gameView.resetGame());
        
        // Initial score update
        tvScore.setText(getString(R.string.score_label, 0));
    }
}
