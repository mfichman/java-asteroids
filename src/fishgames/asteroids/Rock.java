/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Rock extends OutlinedObject implements Renderable {

    public static int TYPE = 0x1;
    public static int MASK = Starship.TYPE; // | Projectile.TYPE;
    private Body body;
    private float radius;
    
    /**
     * Creates a new randomly-shaped Rock object with the given diameter.
     * @param diameter size of the rock
     */
    public Rock(float radius, int segments) {
        this.outlineColor = new Vector3f(.6f, .6f, .6f);
        this.fillColor = new Vector3f(.2f, .2f, .2f);
        this.outlineScale = new Vector3f(1.02f, 1.02f, 1.02f);
        this.radius = radius;
        
        // Create one vertex per segment.  Each vertex has 2 coordinates.
        // Remember to save one vertex for the center point.
        FloatBuffer vert = BufferUtils.createFloatBuffer(2 * (segments + 1));
        double angle = 2. * Math.PI / segments;
        for (int i = 0; i < segments; i++) {
            // Skew the angle and diameter by up to 10% in either direction.
            double diameterSkew = (Math.random() - 0.5) * radius * 0.3;
            double angleSkew = 0; //(Math.random() - 0.5) * angle;
            double px = (diameterSkew + radius) * Math.cos(angleSkew + angle * i);
            double py = (diameterSkew + radius) * Math.sin(angleSkew + angle * i);
            vert.put((i + 1) * 2 + 0, (float)px);
            vert.put((i + 1) * 2 + 1, (float)py);
            
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
        this.polygon = new Polygon(vert, ind, false);
        
        CircleShape shape = new CircleShape();
        shape.m_radius = radius - 0.02f;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        this.body = Asteroids.world.createBody(bodyDef);
        this.body.setTransform(new Vec2(25.f, 25.f), 0.f);
        this.body.setLinearDamping(0.f);
        Fixture fixture = this.body.createFixture(shape, 1.f);
        Filter filter = new Filter();
        filter.categoryBits = TYPE;
        filter.maskBits = MASK;
        fixture.setFilterData(filter);
    }
    
    public void update() {
        Asteroids.wrapTransform(this.body);
    }
    
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
        this.body.setActive(false);
        Queue<Rock> queue = released.get(this.radius);
        if (queue == null) {
            queue = new LinkedList<Rock>();
            released.put(this.radius, queue);
        }
        queue.add(this);
    }
    
    /**
     * Gets a rock of the given size, or creates one.
     * @param radius
     * @return 
     */
    public static Rock getRock(Float radius) {
        Queue<Rock> queue = released.get(radius);
        Rock rock;
        if (queue != null && queue.size() > 0) {
            rock = queue.remove();
        } else {
            rock = new Rock(radius, 16);
        }
        float x = (float)(Math.random() * Asteroids.getWorldSize().x);
        float y = (float)(Math.random() * Asteroids.getWorldSize().y);
        
        rock.body.setTransform(new Vec2(x, y), 0.f);
        return rock;
    }
    
    public static Map<Float, Queue<Rock>> released = new HashMap<Float, Queue<Rock>>();
}
