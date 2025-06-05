package com.swufe.puzzlegame;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MainActivity extends AppCompatActivity {
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameView = new GameView(this, null);
        setContentView(gameView);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img);
        gameView.setupPuzzle(bitmap);
    }
}