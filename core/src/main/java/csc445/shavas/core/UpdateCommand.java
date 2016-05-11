package csc445.shavas.core;

import io.atomix.copycat.Command;

import java.io.Serializable;
import java.util.List;

public class UpdateCommand implements Command<List<Pixel>>, Serializable
{
    private final List<Pixel> pixelDiffs;

    public UpdateCommand(List<Pixel> pixelDiffs)
    {
        this.pixelDiffs = pixelDiffs;
    }

    public List<Pixel> pixelDiffs()
    {
        return pixelDiffs;
    }
}
