package csc445.shavas.core;

public enum Colors
{
    WHITE(0xFFFFFFFF),
    BLACK(0xFF000000),
    RED(0xFFFF0000),
    GREEN(0xFF00FF00),
    BLUE(0XFF0000FF),
    VIOLET(0xFFFF00FF),
    YELLOW(0xFF00FFFF),
    ORANGE(0xFFFFFF00);

    public final int color;

    Colors(int color)
    {
        this.color = color;
    }
}
