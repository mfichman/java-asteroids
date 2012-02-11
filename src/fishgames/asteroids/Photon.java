/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Photon extends OutlinedObject implements Projectile, Renderable, Collidable {
    
    private Body body;
    private static float SCALE = .2f;
    private static float SPEED = 40.f;
    
    public Photon() {
        this.polygon = getBodyPolygon();
        this.body = Asteroids.getBody(this.polygon, TYPE, MASK, SCALE);
        this.body.setBullet(true);
        this.body.setUserData(this);
        this.fillColor = new Vector3f(1.f, 1.f, 1.f);
        Asteroids.add(this);
    }

    @Override
    public void update(float delta) {        
        Asteroids.wrapTransform(this.body);
    }
    
    @Override
    public void render(float alpha) {
        glPushMatrix();
        Asteroids.setTransform(this.body, alpha);
        super.render(alpha);
        glPopMatrix();
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
         
    public static Photon getProjectile() {
        return new Photon();
    }
    
    public static Polygon getBodyPolygon() {
        if (bodyPolygon == null) {
            bodyPolygon = Polygon.getSquare(SCALE);
        }
        return bodyPolygon;
    }

    
    static Polygon bodyPolygon;

    @Override
    public void dispatch(Collidable other) {
        other.collide(this);
    }

    @Override
    public void collide(Projectile other) {
    }

    @Override
    public void collide(Rock other) {
    }

    @Override
    public void collide(Starship other) {
    }
    
}
