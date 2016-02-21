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
public class Serializer implements Functor {

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
        buffer.putFloat(obj.getLife());
    }

    @Override
    public void visit(Rock obj) {
        visit(obj.getBody());
    }

    @Override
    public void visit(Starship obj) {
        visit(obj.getBody());
        buffer.put((byte) (obj.isThrusterOn() ? 1 : 0));
    }
    
    @Override
    public void visit(Player obj) {
        buffer.put(obj.getInputFlags());
    }

    /**
     * Writes a jBox2D body to the output buffer. The position, velocity, and
     * angle are written.
     *
     * @param body
     */
    private void visit(Body body) {
        Vec2 pos = body.getPosition();
        Vec2 vel = body.getLinearVelocity();
        buffer.putFloat(pos.x);
        buffer.putFloat(pos.y);
        buffer.putFloat(vel.x);
        buffer.putFloat(vel.y);
        buffer.putFloat(body.getAngularVelocity());
        buffer.putFloat(body.getAngle());
    }
    
    public ByteBuffer getBuffer() {
        return buffer;
    }
}
