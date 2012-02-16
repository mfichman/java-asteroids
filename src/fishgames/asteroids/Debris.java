/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */


public class Debris implements Object {
    
    private static Queue<Debris> released = new LinkedList<Debris>();
    private static ArrayList<Polygon> debrisPolygon;
    public static float DENSITY = 1.0f;
    public static float MARGIN = 0.05f;
    public static float RADIUS = 1.f;
    public static int TYPE = 0x8;
    public static int MASK = 0;
    public static float LIFE = 0.55f;
    private float life;
    private Body body;
    
    public Debris() {
        this.body = Asteroids.getBody(RADIUS - MARGIN, TYPE, MASK, DENSITY);
        this.life = LIFE;
    }
    
    /**
     * Updates the rock (and wraps the transform).
     * 
     * @param delta
     */
    @Override
    public void update(float delta) {
        Asteroids.wrapTransform(this.body);
        this.life = Math.max(0.f, this.life - delta);
        if (this.life <= 0.f) {
            release();
        }
    }
    
    @Override
    public void dispatch(Functor func) {
        func.visit(this);
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
    
    public float getLife() {
        return life;
    }

    public Body getBody() {
        return body;
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
    
    public static Polygon getPolygon() {
        // Create one vertex per segment.  Each vertex has 2 coordinates.
        // Remember to save one vertex for the center point.
        return Rock.getRockPolygon(Debris.RADIUS, 12);
    }

    public static Polygon getPolygon(Debris debris) {
        // Select a polygon to render a debris object given the debris object's
        // hash code.
        if (debrisPolygon == null) {
            debrisPolygon = new ArrayList<Polygon>();
            for (int i = 0; i < 8; ++i) {
                debrisPolygon.add(getPolygon());
            }
        }
        int index = debris.hashCode() % debrisPolygon.size();
        return debrisPolygon.get(index);
    }


}
