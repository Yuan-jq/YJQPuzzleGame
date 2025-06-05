package com.swufe.puzzlegame;

import android.graphics.Bitmap;

public class Tile {
    private final Bitmap tileImage;
    private final int originalPosition;

    public Tile(Bitmap image, int position) {
        this.tileImage = image;
        this.originalPosition = position;
    }

    public Bitmap getBitmap() {
        return tileImage;
    }

    public int getPosition() {
        return originalPosition;
    }
}