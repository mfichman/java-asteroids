/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Queue;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Explosion extends OutlinedObject implements Renderable {

    private Body body;
    private static float LIFE = .2f;
    private float size;
    private float life;
    
    public Explosion() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.KINEMATIC;
        this.body = Asteroids.getWorld().createBody(bodyDef);
        this.fillColor = new Vector3f(1.f, 0.f, 0.f);
        this.polygon = getPolygon();
    }

    @Override
    public void render(float alpha) {
        glPushMatrix();
        Asteroids.setTransform(this.getBody(), alpha);
        float scale = size + 1.2f * (LIFE - this.life) / LIFE;
        glScalef(scale, scale, scale);
        super.render(alpha);
        glPopMatrix();
    }

    @Override
    public void update(float delta) {
        Asteroids.wrapTransform(this.getBody());
        this.life = Math.max(0.f, this.life - delta);
        if (this.life <= 0.f) {
            destroy();
        }
        this.alpha = this.life/LIFE;
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

    /**
     * Creates a simple ring, which will be rendered using GL_LINE_LOOP.
     *
     * @return
     */
    public static Polygon getPolygon() {
        if (bodyPolygon == null) {
            bodyPolygon = Polygon.getCircle(1.f, 32);
        }
        return bodyPolygon;
    }
    
    private static Polygon bodyPolygon;
    private static Queue<Explosion> released = new LinkedList<Explosion>();

    /**
     * @return the body
     */
    public Body getBody() {
        return body;
    }
}
