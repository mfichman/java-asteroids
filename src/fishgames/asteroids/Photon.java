/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.util.LinkedList;
import java.util.Queue;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Photon implements Projectile, Object, Functor {
    private static Queue<Photon> released = new LinkedList<Photon>();
    private static Polygon photonPolygon;
    public static float SCALE = .2f;
    public static float SPEED = 40.f;
    public static float LIFE = 1.8f;
    private Body body;
    public float life;

    public Photon() {
        this.body = Asteroids.getBody(getPolygon(), TYPE, MASK, SCALE);
        this.body.setBullet(true);
        this.body.setUserData(this);
        this.life = LIFE;
        Asteroids.add(this);
    }

    @Override
    public void update(float delta) {
        Asteroids.wrapTransform(this.body);
        life = Math.max(0.f, life - delta);
        if (life <= 0.f) {
            destroy();
        }
    }

    @Override
    public void setLaunchVector(Vec2 direction, Vec2 velocity) {
        direction = direction.mul(1.f / direction.length()).mul(SPEED);
        this.body.setLinearVelocity(direction.add(velocity));
    }

    @Override
    public void setPosition(Vec2 position) {
        this.body.setTransform(position, this.body.getAngle());
    }

    @Override
    public float getDamage() {
        return 1.f;
    }

    @Override
    public float getRearmTime() {
        return .1f;
    }
   
    public Body getBody() {
        return this.body;
    }

    @Override
    public void dispatch(Functor func) {
        func.visit(this);
    }
    
    @Override
    public void dispatch(Object obj) {
        obj.dispatch(this);
    }

    @Override
    public void visit(Debris obj) {
    }

    @Override
    public void visit(Explosion obj) {
    }
    
    @Override
    public void visit(Photon other) {
    }

    @Override
    public void visit(Rock other) {
        destroy();
    }

    @Override
    public void visit(Starship other) {
        destroy();
    }

    public void destroy() {
        if (this.body.isActive()) {
            release();
            Explosion ex = Explosion.getExplosion(.1f, this.body.getPosition());
            ex.getColor().x = 1.f;
            ex.getColor().y = .85f;
            ex.getColor().z = .2f;
        }
    }

    public void release() {
        if (this.body.isActive()) {
            this.body.setActive(false);
            Asteroids.remove(this);
            released.add(this);
        }
    }

   public static Photon getProjectile() {
        Photon photon = released.isEmpty() ? new Photon() : released.remove();
        photon.body.setActive(true);
        photon.life = LIFE;
        Asteroids.add(photon);
        return photon;
    }
    
    public static Polygon getPolygon() {
        if (photonPolygon == null) {
            photonPolygon = Polygon.getSquare(Photon.SCALE);
        }
        return photonPolygon;
    }

}
