package csc445.shavas.core;

import java.awt.Color;
import java.io.Serializable;

public final class Pixel implements Serializable
{
    public final int x;
    public final int y;
    public final int color;

    public Pixel(final int x, final int y, final int color)
    {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public Pixel(final int x, final int y, final Color color)
    {
        this.x = x;
        this.y = y;

        this.color = color.getRGB();
    }

    public Pixel(final int x, final int y, Colors color)
    {
        this.x = x;
        this.y = y;
        this.color = color.color;
    }

    public int getRed()
    {
        return getRed(color);
    }

    public int getGreen()
    {
        return getGreen(color);
    }

    public int getBlue()
    {
        return getBlue(color);
    }

    public int getAlpha()
    {
        return getAlpha(color);
    }

    public static int getRed(int color)
    {
        return (color >>> 16) & 0x00FF;
    }

    public static int getGreen(int color)
    {
        return (color >>> 8) & 0x0000FF;
    }

    public static int getBlue(int color)
    {
        return color & 0x000000FF;
    }

    public static int getAlpha(int color)
    {
        return color >>> 24;
    }
}
