package csc445.shavas.core;

import io.atomix.copycat.Query;

import java.io.Serializable;
import java.util.List;

public class GetQuery implements Query<List<Pixel>>, Serializable
{
    private final List<Pixel> pixelDiffs;

    public GetQuery(List<Pixel> pixelDiffs)
    {
        this.pixelDiffs = pixelDiffs;
    }

    public List<Pixel> pixelDiffs()
    {
        return pixelDiffs;
    }
}
