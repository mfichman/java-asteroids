/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.lwjgl.BufferUtils;

/**
 * Procedurally-generated rock/asteroid constructed as a polygon, and modeled
 * physically as a circle.
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public abstract class Rock extends Entity implements Functor {

    private static ArrayList<Polygon> largeRockPolygon;
    private static ArrayList<Polygon> mediumRockPolygon;
    private static ArrayList<Polygon> smallRockPolygon;
    public static float DENSITY = 1.0f;
    public static float MARGIN = 0.05f; // Collision margin (to avoid gaps)
    public static int TYPE = 0x1;
    public static int MASK = Starship.TYPE | Projectile.TYPE;
    public static float SMALL = 2.0f;
    public static float MEDIUM = 4.0f;
    public static float LARGE = 6.0f;
    protected Body body;
    private float radius;

    /**
     * Creates a new randomly-shaped Rock object with the given diameter.
     *
     * @param diameter size of the rock
     */
    public Rock(float radius) {

        this.body = Asteroids.getBody(radius - MARGIN, TYPE, MASK, DENSITY);
        this.body.setUserData(this);
        this.radius = radius;
    }

    public Body getBody() {
        return this.body;
    }

    public float getRadius() {
        return radius;
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
    public void visit(Photon obj) {
        destroy();
    }

    @Override
    public void visit(Rock obj) {
    }

    @Override
    public void visit(Starship obj) {
    }
    
    public abstract void destroy();

    public static Polygon getLargePolygon(Rock rock) {
        // Select a polygon to render a rock, using the rock's hash code
        if (largeRockPolygon == null) {
            largeRockPolygon = new ArrayList<Polygon>();
            for (int i = 0; i < 8; ++i) {
                largeRockPolygon.add(getRockPolygon(Rock.LARGE, 16));
            }
        }
        int index = rock.hashCode() % largeRockPolygon.size();
        return largeRockPolygon.get(index);
    }

    public static Polygon getMediumPolygon(Rock rock) {
        // Select a polygon to render a rock, using the rock's hash code
        if (mediumRockPolygon == null) {
            mediumRockPolygon = new ArrayList<Polygon>();
            for (int i = 0; i < 8; ++i) {
                mediumRockPolygon.add(getRockPolygon(Rock.MEDIUM, 16));
            }
        }
        int index = rock.hashCode() % mediumRockPolygon.size();
        return mediumRockPolygon.get(index);
    }

    public static Polygon getSmallPolygon(Rock rock) {
        // Select a polygon to render a rock, using the rock's hash code
        if (smallRockPolygon == null) {
            smallRockPolygon = new ArrayList<Polygon>();
            for (int i = 0; i < 8; ++i) {
                smallRockPolygon.add(getRockPolygon(Rock.SMALL, 16));
            }
        }
        int index = rock.hashCode() % smallRockPolygon.size();
        return smallRockPolygon.get(index);
    }

    public static Polygon getRockPolygon(float radius, int segments) {
        // Creates a rock polygon using getRandomInt offsets around a circle 
        // generated as an n-gon.

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
        return new Polygon(vert, ind);
    }

    public static class Small extends Rock {

        public Small() {
            super(SMALL);
        }

        @Override
        public void destroy() {
            if (this.body.isActive()) {
                setActive(false);
                for (int i = 0; i < 3; i++) {
                    Debris.getDebris(this.body.getPosition());
                }
            }
        }
        
        public static Small getRock(Vec2 position) {
            Small rock = Asteroids.newEntity(Small.class);
            rock.body.setTransform(position, 0.f);
            rock.body.setLinearVelocity(Asteroids.getRandomVel(6.f, 9.f));
            return rock;
        }
    }

    public static class Medium extends Rock {
        public Medium() {
            super(MEDIUM);
        }

        @Override
        public void destroy() {
            if (this.body.isActive()) {
                setActive(false);
                for (int i = 0; i < Asteroids.getRandomInt(2, 3); i++) {
                    Small.getRock(this.body.getPosition());
                }
            }
        }
        
        public static Medium getRock(Vec2 position) {
            Medium rock = Asteroids.newEntity(Medium.class);
            rock.body.setTransform(position, 0.f);
            rock.body.setLinearVelocity(Asteroids.getRandomVel(6.f, 8.f));
            return rock;
        }
    }

    public static class Large extends Rock {
        public Large() {
            super(LARGE);
        }

        @Override
        public void destroy() {
            if (this.body.isActive()) {
                setActive(false);
                for (int i = 0; i < Asteroids.getRandomInt(2, 3); i++) {
                    Medium.getRock(this.body.getPosition());
                }
            }
        }
        
        public static Large getRock(Vec2 position) {
            Large rock = Asteroids.newEntity(Large.class);
            rock.body.setTransform(position, 0.f);
            rock.body.setLinearVelocity(Asteroids.getRandomVel(4.f, 6.f));
            return rock;
        }
    }
}
