/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Photon extends Entity implements Projectile, Functor {

    private static Polygon photonPolygon;
    public static float SCALE = .2f;
    public static float SPEED = 40.f;
    public static float LIFE = 1.8f;
    public float life;
    private Body body;

    public Photon() {
        this.body = Asteroids.getBody(getPolygon(), TYPE, MASK, SCALE);
        this.body.setBullet(true);
        this.body.setUserData(this);
        this.life = LIFE;
        Asteroids.addActiveEntity(this);
    }

    @Override
    public void update(float delta) {
        Asteroids.wrapTransform(this.body);
        life = Math.max(0.f, life - delta);
        if (life <= 0.f) {
            destroy();
        }
    }

    public void setLife(float life) {
        this.life = life;
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
    public void setActive(boolean active) {
        super.setActive(active);
        this.body.setActive(active);
    }
    
    public float getLife() {
        return this.life;
    }

    public Body getBody() {
        return this.body;
    }

    @Override
    public float getDamage() {
        return 1.f;
    }

    @Override
    public float getRearmTime() {
        return .1f;
    }

    @Override
    public void dispatch(Functor func) {
        func.visit(this);
    }

    @Override
    public void dispatch(Entity obj) {
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
    public void visit(Player other) {
    }

    @Override
    public void visit(Starship other) {
        destroy();
    }

    public void destroy() {
        if (this.body.isActive()) {
            setActive(false);
            if (this.isRemote()) {
                Explosion ex = Explosion.getExplosion(.1f, this.body.getPosition());
                ex.setSerializable(false);
                ex.getColor().x = 1.f;
                ex.getColor().y = .85f;
                ex.getColor().z = .2f;
            }
        }
    }

    public static Photon getProjectile() {
        Photon photon = Asteroids.newEntity(Photon.class);
        photon.life = LIFE;
        return photon;
    }

    public static Polygon getPolygon() {
        if (photonPolygon == null) {
            photonPolygon = Polygon.getSquare(Photon.SCALE);
        }
        return photonPolygon;
    }
}
