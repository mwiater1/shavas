package csc445.shavas.server;

import csc445.shavas.core.Canvas;
import csc445.shavas.core.GetQuery;
import csc445.shavas.core.Pixel;
import csc445.shavas.core.UpdateCommand;
import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.Snapshottable;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.session.ServerSession;
import io.atomix.copycat.server.session.SessionListener;
import io.atomix.copycat.server.storage.snapshot.SnapshotReader;
import io.atomix.copycat.server.storage.snapshot.SnapshotWriter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CanvasStateMachine extends StateMachine implements SessionListener, Snapshottable
{
    private final Set<ServerSession> sessions = new HashSet<>();
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

        System.err.println("CanvasStateMachine::update - commit [" + commit + "] and pixelDiffs " + pixelDiffs);

        canvas.update(pixelDiffs);
        commit.close();

        for (ServerSession session : sessions)
        {
            session.publish("change");
        }
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

    @Override
    public void register(ServerSession session)
    {
        sessions.add(session);
    }

    @Override
    public void unregister(ServerSession session)
    {

    }

    @Override
    public void expire(ServerSession session)
    {

    }

    @Override
    public void close(ServerSession session)
    {
        sessions.remove(session);
    }
}