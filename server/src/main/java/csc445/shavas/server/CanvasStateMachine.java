package csc445.shavas.server;

import csc445.shavas.core.Canvas;
import csc445.shavas.core.GetQuery;
import csc445.shavas.core.Pixel;
import csc445.shavas.core.UpdateCommand;
import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.Snapshottable;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.storage.snapshot.SnapshotReader;
import io.atomix.copycat.server.storage.snapshot.SnapshotWriter;

import java.util.List;

public class CanvasStateMachine extends StateMachine implements Snapshottable
{
    private Canvas canvas;

    public CanvasStateMachine(int width, int height)
    {
        canvas = new Canvas(width, height);
    }

    public CanvasStateMachine()
    {
        canvas = new Canvas();
    }

    public void update(Commit<UpdateCommand> commit)
    {
        List<Pixel> pixelDiffs = commit.command().pixelDiffs();

        canvas.update(pixelDiffs);
        commit.close();
    }

    // TODO: look into using the query index to hold onto pixel diffs and deleting them when every client has caught up
    public void get(Commit<GetQuery> commit)
    {

    }

    @Override
    public void snapshot(SnapshotWriter writer)
    {
        writer.writeObject(canvas);
    }

    @Override
    public void install(SnapshotReader reader)
    {
        canvas = reader.readObject();
    }
}