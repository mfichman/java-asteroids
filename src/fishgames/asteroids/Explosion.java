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
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Explosion implements Object {
    private static Queue<Explosion> released = new LinkedList<Explosion>();
    private static Polygon explosionPolygon;
    public static float LIFE = .2f;
    private Body body;
    private float size;
    private float life;
    private Vector3f color;
    
    public Explosion() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.KINEMATIC;
        this.body = Asteroids.getWorld().createBody(bodyDef);
        this.color = new Vector3f(1.f, 0.f, 0.f);
    }

    @Override
    public void update(float delta) {
        Asteroids.wrapTransform(this.getBody());
        this.life = Math.max(0.f, this.life - delta);
        if (this.life <= 0.f) {
            destroy();
        }
    }    
    
    @Override
    public void dispatch(Functor func) {
        func.visit(this);
    }
    
    public Body getBody() {
        return this.body;
    }
    
    Vector3f getColor() {
        return this.color;
    }

    float getLife() {
        return this.life;
    }

    float getSize() {
        return this.size;
    }

    public void destroy() {
        release();
    }

    public void release() {
        if (this.getBody().isActive()) {
            this.getBody().setActive(false);
            Asteroids.remove(this);
            released.add(this);
        }
    }
    
    public static Explosion getExplosion(float size, Vec2 pos) {
        Explosion exp = released.isEmpty() ? new Explosion() : released.remove();
        exp.getBody().setActive(true);
        exp.life = LIFE;
        exp.size = size;
        exp.getBody().setTransform(pos, 0.f);
        Asteroids.add(exp);
        return exp;
    }  

    public static Polygon getPolygon() {
        if (explosionPolygon == null) {
            explosionPolygon = Polygon.getCircle(1.f, 32);
        }
        return explosionPolygon;
    }
}
