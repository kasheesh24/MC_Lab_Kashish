package com.example.drawing_canvas_app;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private SeekBar brushSizeSeekBar;
    private Button clearButton, saveButton;
    private View colorBlack, colorRed, colorGreen, colorBlue, colorYellow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawingView = findViewById(R.id.drawing_view);
        brushSizeSeekBar = findViewById(R.id.brush_size_seekbar);
        clearButton = findViewById(R.id.clear_button);
        saveButton = findViewById(R.id.save_button);

        colorBlack = findViewById(R.id.color_black);
        colorRed = findViewById(R.id.color_red);
        colorGreen = findViewById(R.id.color_green);
        colorBlue = findViewById(R.id.color_blue);
        colorYellow = findViewById(R.id.color_yellow);

        // Color Selection Logic
        colorBlack.setOnClickListener(v -> drawingView.setColor(Color.BLACK));
        colorRed.setOnClickListener(v -> drawingView.setColor(Color.RED));
        colorGreen.setOnClickListener(v -> drawingView.setColor(Color.GREEN));
        colorBlue.setOnClickListener(v -> drawingView.setColor(Color.BLUE));
        colorYellow.setOnClickListener(v -> drawingView.setColor(Color.YELLOW));

        // Brush Size Logic
        brushSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawingView.setBrushSize((float) progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Clear Canvas Logic
        clearButton.setOnClickListener(v -> drawingView.clearCanvas());

        // Save Drawing Logic
        saveButton.setOnClickListener(v -> saveDrawing());
    }

    private void saveDrawing() {
        Bitmap bitmap = drawingView.getBitmap();
        OutputStream fos;
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "Drawing_" + System.currentTimeMillis() + ".png");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DrawingCanvasApp");

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                fos = getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                if (fos != null) {
                    fos.close();
                }
                Toast.makeText(this, "Drawing Saved to Gallery", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save drawing", Toast.LENGTH_SHORT).show();
        }
    }
}
