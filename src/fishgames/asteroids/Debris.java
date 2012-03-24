/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.util.ArrayList;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */


public class Debris extends Entity {
    
    private static ArrayList<Polygon> polygon;
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
        this.setSerializable(false);
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
            setActive(false);
        }
    }
    
    @Override
    public void dispatch(Functor func) {
        func.visit(this);
    }
    
    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        this.body.setActive(active);
    }
    
    public Body getBody() {
        return this.body;
    }
    
    public float getLife() {
        return life;
    }

    public static Debris getDebris(Vec2 position) {
        Debris debris = Asteroids.newEntity(Debris.class);
        float minSpeed = 9.f;
        float maxSpeed = 12.f;
        debris.body.setLinearVelocity(Asteroids.getRandomVel(minSpeed, maxSpeed));
        debris.body.setTransform(position, debris.body.getAngle());
        debris.life = LIFE;
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
        if (polygon == null) {
            polygon = new ArrayList<Polygon>();
            for (int i = 0; i < 8; ++i) {
                polygon.add(getPolygon());
            }
        }
        int index = debris.hashCode() % polygon.size();
        return polygon.get(index);
    }


}
