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
        visit(obj.getBody(), obj.isInitialized());
        obj.setInitialized(true);
    }

    @Override
    public void visit(Explosion obj) {
        visit(obj.getBody(), obj.isInitialized());
        obj.setInitialized(true);
    }

    @Override
    public void visit(Photon obj) {
        visit(obj.getBody(), obj.isInitialized());
        obj.setLife(buffer.getFloat());
        obj.setInitialized(true);
    }

    @Override
    public void visit(Rock obj) {
        visit(obj.getBody(), obj.isInitialized());
        obj.setInitialized(true);
    }

    @Override
    public void visit(Starship obj) {
        visit(obj.getBody(), obj.isInitialized());
        obj.setThrusterOn(buffer.get() != 0);
        obj.setInitialized(true);
    }

    @Override
    public void visit(Player obj) {
        obj.setInputFlags(buffer.get());
    }

    /**
     * Reads a jBox2D body from the input buffer. The position, velocity, and
     * angle are written.
     *
     * @param body
     */
    private void visit(Body body, boolean initialized) {
        float alpha = .0f;

        float x = buffer.getFloat();
        float y = buffer.getFloat();
        float dx = buffer.getFloat();
        float dy = buffer.getFloat();
        
        Vec2 pos = body.getPosition();
        Vec2 vel = body.getLinearVelocity();

        double dist = Math.sqrt((pos.x - x) * (pos.x - x) + (pos.y - y) * (pos.y - y));

        if (initialized && dist < 0.2f) {
            //System.out.printf("Net: %f, %f\n", x, y);
            //System.out.printf("Local: %f, %f\n", pos.x, pos.y);
            pos.x = (1 - alpha) * pos.x + (alpha) * x;
            pos.y = (1 - alpha) * pos.y + (alpha) * y;
        } else {
            pos.x = x;
            pos.y = y;
        }
        // Smooth the position vector

            vel.x = dx;
            vel.y = dy;

        body.setAngularVelocity(buffer.getFloat());
        float angle = buffer.getFloat();
        
        if (initialized) {
            angle = Asteroids.slerp(body.getAngle(), angle, 0.2f);
        }
        
        body.setLinearVelocity(vel);
        body.setTransform(pos, angle);
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}
