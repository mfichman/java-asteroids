/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.nio.ByteBuffer;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Deserializer implements Functor {

    private ByteBuffer buffer = ByteBuffer.allocateDirect(Asteroids.BUFSIZE);

    @Override
    public void dispatch(Entity obj) {
        obj.dispatch(this);
    }

    @Override
    public void visit(Debris obj) {
        visit(obj.getBody());
    }

    @Override
    public void visit(Explosion obj) {
        visit(obj.getBody());
    }

    @Override
    public void visit(Photon obj) {
        visit(obj.getBody());
    }

    @Override
    public void visit(Rock obj) {
        visit(obj.getBody());
    }

    @Override
    public void visit(Starship obj) {
        visit(obj.getBody());
    }

    /**
     * Reads a jBox2D body from the input buffer. The position, velocity, and
     * angle are written.
     *
     * @param body
     */
    private void visit(Body body) {
        Vec2 pos = body.getPosition();
        Vec2 vel = body.getLinearVelocity();
        pos.x = buffer.getFloat();
        pos.y = buffer.getFloat();
        vel.x = buffer.getFloat();
        vel.y = buffer.getFloat();
        body.setTransform(pos, buffer.getFloat());
    }
    
    public ByteBuffer getBuffer() {
        return buffer;
    }
}
