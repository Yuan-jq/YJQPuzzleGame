package com.swufe.puzzlegame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameView extends View {
    private List<Tile> tiles;
    private int colCount = 3;
    private int baseSize;
    private int displaySize;
    private Paint drawPaint;
    private Paint infoPaint;
    private Integer selectedIdx = null;
    private final float sizeMultiplier = 1.5f;
    private int moveCounter = 0;
    private long beginTime;
    private Handler timeHandler = new Handler();
    private boolean timerActive = false;

    // 计时器逻辑重构
    private final Runnable timeTracker = new Runnable() {
        @Override
        public void run() {
            if (timerActive) {
                long currentSeconds = (System.currentTimeMillis() - beginTime) / 1000;
                if (currentSeconds >= 120) {
                    finishGame("时间结束!");
                    return;
                }
                invalidate();
                timeHandler.postDelayed(this, 1000);
            }
        }
    };

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawingTools();  // 方法封装
        tiles = new ArrayList<>();
    }

    // 初始化绘图工具
    private void setupDrawingTools() {
        drawPaint = new Paint();
        infoPaint = new Paint();
        infoPaint.setColor(0xFF000000);
        infoPaint.setTextSize(50);
    }

    // 拼图初始化
    public void setupPuzzle(Bitmap source) {  // 方法重命名
        baseSize = source.getWidth() / colCount;
        displaySize = (int) (baseSize * sizeMultiplier);

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 4;
        source = BitmapFactory.decodeResource(getResources(), R.drawable.img, opts);

        baseSize = source.getWidth() / colCount;
        displaySize = (int) (baseSize * sizeMultiplier);

        // 拼图块生成
        for (int row = 0; row < colCount; row++) {
            for (int col = 0; col < colCount; col++) {
                Bitmap segment = Bitmap.createBitmap(
                        source, col * baseSize, row * baseSize,
                        baseSize, baseSize
                );
                segment = Bitmap.createScaledBitmap(
                        segment, displaySize, displaySize, true
                );
                tiles.add(new Tile(segment, row * colCount + col));
            }
        }
        randomizeTiles();  // 方法重命名
        invalidate();
    }

    // 打乱拼图
    private void randomizeTiles() {
        Collections.shuffle(tiles);
    }

    // 开始计时
    private void beginTimer() {
        beginTime = System.currentTimeMillis();
        timerActive = true;
        timeHandler.postDelayed(timeTracker, 1000);
    }

    // 结束游戏
    private void finishGame(String msg) {
        timerActive = false;
        timeHandler.removeCallbacks(timeTracker);

        long totalSeconds = (System.currentTimeMillis() - beginTime) / 1000;
        Toast.makeText(
                getContext(),
                msg + " 步数: " + moveCounter + ", 用时: " + totalSeconds + "秒",
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int totalWidth = displaySize * colCount;
        int totalHeight = displaySize * colCount;
        int offsetX = (getWidth() - totalWidth) / 2;
        int offsetY = (getHeight() - totalHeight) / 2;

        // 绘制拼图块
        for (int idx = 0; idx < tiles.size(); idx++) {
            Tile tile = tiles.get(idx);
            int xPos = offsetX + (idx % colCount) * displaySize;
            int yPos = offsetY + (idx / colCount) * displaySize;
            canvas.drawBitmap(tile.getBitmap(), xPos, yPos, drawPaint);
        }

        // 绘制游戏信息
        drawGameInfo(canvas, offsetX, offsetY);
    }

    // 绘制游戏信息
    private void drawGameInfo(Canvas canvas, int x, int y) {
        canvas.drawText("移动次数: " + moveCounter, x, y - 20, infoPaint);

        long elapsed = timerActive ? (System.currentTimeMillis() - beginTime) / 1000 : 0;
        String timeDisplay = String.format("%02d:%02d", elapsed / 60, elapsed % 60);
        canvas.drawText("用时: " + timeDisplay, x, y - 70, infoPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            handleTileSelection(event);  // 封装处理逻辑
        }
        return true;
    }

    // 处理拼图块选择
    private void handleTileSelection(MotionEvent event) {
        int totalWidth = displaySize * colCount;
        int totalHeight = displaySize * colCount;
        int boardStartX = (getWidth() - totalWidth) / 2;
        int boardStartY = (getHeight() - totalHeight) / 2;

        int tapX = (int) (event.getX() - boardStartX);
        int tapY = (int) (event.getY() - boardStartY);

        if (tapX < 0 || tapY < 0 ||
                tapX >= totalWidth || tapY >= totalHeight) {
            return;
        }

        int colIdx = tapX / displaySize;
        int rowIdx = tapY / displaySize;
        int tileIndex = rowIdx * colCount + colIdx;

        if (!timerActive) {
            beginTimer();
        }

        processTileTap(tileIndex);
        invalidate();
    }

    // 处理拼图块点击
    private void processTileTap(int index) {
        if (selectedIdx == null) {
            selectedIdx = index;
            return;
        }

        if (areTilesAdjacent(selectedIdx, index)) {
            swapTilePositions(selectedIdx, index);
            moveCounter++;
            verifySolution();  // 方法重命名
        }
        selectedIdx = null;
    }

    // 交换拼图块位置
    private void swapTilePositions(int idx1, int idx2) {
        Collections.swap(tiles, idx1, idx2);
    }

    // 检查相邻性
    private boolean areTilesAdjacent(int idx1, int idx2) {
        int row1 = idx1 / colCount;
        int col1 = idx1 % colCount;
        int row2 = idx2 / colCount;
        int col2 = idx2 % colCount;

        int rowDiff = Math.abs(row1 - row2);
        int colDiff = Math.abs(col1 - col2);

        return (rowDiff == 1 && colDiff == 0) ||
                (colDiff == 1 && rowDiff == 0);
    }

    // 验证是否完成
    private void verifySolution() {
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i).getPosition() != i) {
                return;
            }
        }
        finishGame("拼图完成!");
    }
}

