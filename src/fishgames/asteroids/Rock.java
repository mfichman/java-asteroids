/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import org.lwjgl.util.vector.Vector3f;

/**
 * Procedurally-generated rock/asteroid constructed as a polygon, and modeled
 * physically as a circle.
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Rock extends OutlinedObject implements Renderable, Collidable {

    private Body body;
    private float radius;
    private static float DENSITY = 1.0f;
    private static float MARGIN = 0.05f; // Collision margin (to avoid gaps)
    public static int TYPE = 0x1;
    public static int MASK = Starship.TYPE | Projectile.TYPE;
    public static float SMALL = 2.0f;
    public static float MEDIUM = 4.0f;
    public static float LARGE = 6.0f;

    /**
     * Creates a new randomly-shaped Rock object with the given diameter.
     *
     * @param diameter size of the rock
     */
    public Rock(float radius, int segments) {
        this.outlineColor = new Vector3f(.6f, .6f, .6f);
        this.fillColor = new Vector3f(.2f, .2f, .2f);
        this.radius = radius;

        // Create one vertex per segment.  Each vertex has 2 coordinates.
        // Remember to save one vertex for the center point.
        FloatBuffer vert = BufferUtils.createFloatBuffer(2 * (segments + 1));
        double angle = 2. * Math.PI / segments;
        for (int i = 0; i < segments; i++) {
            // Skew the angle and diameter by up to 10% in either direction.
            // This is a bit of a 'magic' equation to make the rocks look right.
            double diameterSkew = (Math.random() - 0.5) * radius * 0.3;
            double px = (diameterSkew + radius) * Math.cos(angle * i);
            double py = (diameterSkew + radius) * Math.sin(angle * i);
            vert.put((i + 1) * 2 + 0, (float) px);
            vert.put((i + 1) * 2 + 1, (float) py);

        }
        vert.put(0, 0); // The center point is (0, 0, 0)
        vert.put(0, 1);

        // Create one triangle per segment by binding the correct vertices.
        IntBuffer ind = BufferUtils.createIntBuffer(3 * segments);
        for (int i = 0; i < segments; i++) {
            ind.put(3 * i + 0, 0); // Center
            ind.put(3 * i + 1, i + 1); // Top left
            if (i == (segments - 1)) {
                ind.put(3 * i + 2, 1);
            } else {
                ind.put(3 * i + 2, i + 2); // Top right
            }
        }
        this.polygon = new Polygon(vert, ind);
        this.body = Asteroids.getBody(radius - MARGIN, TYPE, MASK, DENSITY);
        this.body.setUserData(this);
    }

    /**
     * Updates the rock (and wraps the transform).
     *
     * @param delta
     */
    @Override
    public void update(float delta) {
        Asteroids.wrapTransform(this.body);
    }

    /**
     * Render the rock with interpolation value 'alpha'.
     *
     * @param alpha
     */
    @Override
    public void render(float alpha) {
        glPushMatrix();
        Asteroids.setTransform(this.body, alpha);
        super.render(alpha);
        glPopMatrix();
    }

    /**
     * Releases the rock and returns it to the pool of other rocks.
     */
    public void release() {
        if (this.body.isActive()) {
            this.body.setActive(false);
            Asteroids.remove(this);
            Queue<Rock> queue = released.get(this.radius);
            if (queue == null) {
                queue = new LinkedList<Rock>();
                released.put(this.radius, queue);
            }
            queue.add(this);
        }
    }

    @Override
    public void dispatch(Collidable other) {
        other.collide(this);
    }

    @Override
    public void collide(Projectile other) {
        destroy();
    }

    @Override
    public void collide(Rock other) {
    }

    @Override
    public void collide(Starship other) {
    }

    public void destroy() {
        if (this.body.isActive()) {
            this.release();
            int num = 0;
            float size = 0.f;
            if (this.radius == LARGE) {
                size = MEDIUM;
                num = Asteroids.random(2, 3);
            } else if (this.radius == MEDIUM) {
                size = SMALL;
                num = Asteroids.random(2, 3);
            }

            for (int i = 0; i < num; i++) {
                getRock(size, this.body.getPosition());
            }
            if (this.radius == SMALL) {
                for (int i = 0; i < 3; i++) {
                    Debris.getDebris(this.body.getPosition());
                }
            }
        }
    }

    /**
     * Gets a rock of the given size, or creates one.
     *
     * @param radius
     * @param position
     * @return
     */
    public static Rock getRock(float radius, Vec2 position) {
        Queue<Rock> queue = released.get(radius);
        Rock rock;
        if (queue != null && queue.size() > 0) {
            rock = queue.remove();
        } else {
            rock = new Rock(radius, 16);
        }

        rock.body.setTransform(position, 0.f);
        float minSpeed;
        float maxSpeed;
        if (radius == LARGE) {
            minSpeed = 4.f;
            maxSpeed = 6.f;
        } else if (radius == MEDIUM) {
            minSpeed = 6.f;
            maxSpeed = 8.f;
        } else {
            minSpeed = 6.f;
            maxSpeed = 9.f;
        }

        float speed = (float) (Math.random() * (maxSpeed - minSpeed)) + minSpeed;
        float angle = (float) (2 * Math.PI * Math.random());
        float dx = (float) (speed * Math.cos(angle));
        float dy = (float) (speed * Math.sin(angle));

        //rock.body.applyLinearImpulse(new Vec2(dx, dy), rock.body.getWorldCenter());

        rock.body.setLinearVelocity(new Vec2(dx, dy));
        rock.body.setActive(true);
        Asteroids.add(rock);

        return rock;
    }

    public static Rock getRock(float radius) {
        float x = (float) (Math.random() * Asteroids.getWorldSize().x);
        float y = (float) (Math.random() * Asteroids.getWorldSize().y);

        return getRock(radius, new Vec2(x, y));
    }
    private static Map<Float, Queue<Rock>> released = new HashMap<Float, Queue<Rock>>();
}
