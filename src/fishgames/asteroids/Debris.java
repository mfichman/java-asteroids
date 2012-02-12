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
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */


public class Debris extends OutlinedObject implements Renderable {
    
    private Body body;
    private static float DENSITY = 1.0f;
    private static float MARGIN = 0.05f;
    public static int TYPE = 0x8;
    public static int MASK = 0;
    public static float LIFE = 0.55f;
    public float life;
    
    public Debris() {
        this.outlineColor = new Vector3f(.8f, .8f, .8f);
        this.fillColor = new Vector3f(.2f, .2f, .2f);
        this.outlineScale = new Vector3f(1.12f, 1.12f, 1.12f);
        this.life = LIFE;
        
        int segments = 12;
        float radius = 1.0f;
        
        // Create one vertex per segment.  Each vertex has 2 coordinates.
        // Remember to save one vertex for the center point.
        FloatBuffer vert = BufferUtils.createFloatBuffer(2 * (segments + 1));
        double angle = 2. * Math.PI / segments;
        for (int i = 0; i < segments; i++) {
            // Skew the angle and diameter by up to 10% in either direction.
            // This is a bit of a 'magic' equation to make the rocks look right.
            double diameterSkew = (Math.random() - 0.5) * radius * 0.3;
            double angleSkew = 0; //(Math.random() - 0.5) * angle;
            double px = (diameterSkew + radius) * Math.cos(angleSkew + angle * i);
            double py = (diameterSkew + radius) * Math.sin(angleSkew + angle * i);
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
    }
    
    /**
     * Updates the rock (and wraps the transform).
     * 
     * @param delta
     */
    @Override
    public void update(float delta) {
        Asteroids.wrapTransform(this.body);
        this.alpha = life / LIFE;
        this.fillColor.x = 0.2f * life / LIFE;
        this.fillColor.y = 0.2f * life / LIFE;
        this.fillColor.z = 0.2f * life / LIFE;

        life = Math.max(0.f, life - delta);
        if (life <= 0.f) {
            release();
        }
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
     * Releases the object (so it won't render).
     */
    public void release() {
        if (this.body.isActive()) {
            this.body.setActive(false);
            Asteroids.remove(this);
            released.add(this);
        }
    }
    
    public static Debris getDebris(Vec2 position) {
        Debris debris = released.isEmpty() ? new Debris() : released.remove(); 
        
        float minSpeed = 9.f;
        float maxSpeed = 12.f;
        float speed = (float) (Math.random() * (maxSpeed - minSpeed)) + minSpeed;
        float angle = (float) (2 * Math.PI * Math.random());
        float dx = (float) (speed * Math.cos(angle));
        float dy = (float) (speed * Math.sin(angle));
        
        debris.body.setLinearVelocity(new Vec2(dx, dy));
        debris.body.setTransform(position, debris.body.getAngle());
        debris.body.setActive(true);
        debris.life = LIFE;
        Asteroids.add(debris);
        
        return debris;
        
    }
    private static Queue<Debris> released = new LinkedList<Debris>();
}
