/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Explosion implements Renderable {

    private Body body;
    private static float LIFE = .2f;
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
    public void render(float alpha) {
        glPushMatrix();
        Asteroids.setTransform(this.getBody(), alpha);
        
        float scale = size + 1.2f * (LIFE - this.life) / LIFE;
        glScalef(scale, scale, scale);
        glColor4f(this.getColor().x, this.getColor().y, this.getColor().z, this.life/LIFE);
        
        glLineWidth(2.f);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Polygon polygon = getPolygon();
        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, polygon.getIndices());
        glBindBuffer(GL_ARRAY_BUFFER, polygon.getVertices());
        glVertexPointer(2, GL_FLOAT, 0, 0);
        
        glDrawArrays(GL_POLYGON, 0, polygon.getNumElements());
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glDisable(GL_BLEND);
        glPopMatrix();
    }

    @Override
    public void update(float delta) {
        Asteroids.wrapTransform(this.getBody());
        life = Math.max(0.f, life - delta);
        if (life <= 0.f) {
            destroy();
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
            int segments = 32;
            float radius = 1.f;

            // Create one vertex per segment.  Each vertex has 2 coordinates.
            FloatBuffer vert = BufferUtils.createFloatBuffer(2 * segments);
            double angle = 2. * Math.PI / segments;
            for (int i = 0; i < segments; i++) {
                double px = (radius) * Math.cos(angle * i);
                double py = (radius) * Math.sin(angle * i);
                vert.put(i * 2 + 0, (float) px);
                vert.put(i * 2 + 1, (float) py);

            }

            // Create one triangle per segment by binding the correct vertices.
            IntBuffer ind = BufferUtils.createIntBuffer(segments);
            for (int i = 0; i < segments; i++) {
                ind.put(i, i);
            }
            CircleShape shape = new CircleShape();
            shape.m_radius = radius;
            bodyPolygon = new Polygon(vert, ind);
            bodyPolygon.addShape(shape);
        }
        return bodyPolygon;
    }
    
    private static Polygon bodyPolygon;
    private static Queue<Explosion> released = new LinkedList<Explosion>();

    /**
     * @return the color
     */
    public Vector3f getColor() {
        return color;
    }

    /**
     * @return the body
     */
    public Body getBody() {
        return body;
    }
}
