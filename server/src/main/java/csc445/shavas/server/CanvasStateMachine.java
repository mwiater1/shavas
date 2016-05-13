package csc445.shavas.server;

import csc445.shavas.core.*;
import io.atomix.catalyst.serializer.SerializableTypeResolver;
import io.atomix.copycat.Command;
import io.atomix.copycat.Operation;
import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.Snapshottable;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.session.ServerSession;
import io.atomix.copycat.server.session.SessionListener;
import io.atomix.copycat.server.storage.snapshot.SnapshotReader;
import io.atomix.copycat.server.storage.snapshot.SnapshotWriter;
import io.atomix.copycat.session.Event;
import io.atomix.copycat.session.Session;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CanvasStateMachine extends StateMachine implements Snapshottable, SessionListener
{
    private Canvas canvas;
    private Set<ServerSession> listeners = new HashSet<>();

    public CanvasStateMachine(int width, int height)
    {
        canvas = new Canvas(width, height);
    }

    public CanvasStateMachine()
    {
        canvas = new Canvas();
    }

    public void listen(Commit<JoinCommand> commit) {
        listeners.add(commit.session());
        commit.release();
    }

    public void update(Commit<UpdateCommand> commit)
    {
        List<Pixel> pixelDiffs = commit.command().pixelDiffs();

        System.err.println("CanvasStateMachine::update - commit [" + commit + "] and pixelDiffs " + pixelDiffs);

        listeners.forEach(session -> session.publish("change", pixelDiffs));
//        commit.session().publish("change");

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

    @Override
    public void register(ServerSession session) {

    }

    @Override
    public void unregister(ServerSession session) {

    }

    @Override
    public void expire(ServerSession session) {

    }

    @Override
    public void close(ServerSession session) {

    }
}