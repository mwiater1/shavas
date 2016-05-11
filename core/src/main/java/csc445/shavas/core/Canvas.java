package csc445.shavas.core;

import java.io.Serializable;
import java.util.List;

public final class Canvas implements Serializable
{
    public final int SERIALIZABLE_ID = 1;

    private final int[][] canvas;

    public Canvas()
    {
        this(Constants.CANVAS_WIDTH, Constants.CANVAS_HEIGHT);
    }

    public Canvas(int width, int height)
    {
        canvas = new int[width][height];
        colorWholeCanvas(Colors.WHITE);
    }

    private void colorWholeCanvas(int color)
    {
        for (int i = 0; i < canvas.length; i++)
        {
            for (int j = 0; j < canvas[0].length; j++)
            {
                canvas[i][j] = color;
            }
        }
    }

    private void colorWholeCanvas(Colors color)
    {
        colorWholeCanvas(color.color);
    }

    public void update(List<Pixel> pixelDiffs)
    {
        for (Pixel diff : pixelDiffs)
        {
            canvas[diff.x][diff.y] = diff.color;
        }
    }
}
