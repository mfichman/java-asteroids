/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

/**
 * Procedurally-generated starship (constructed as a quadrilateral); modeled as
 * a pair of triangles.
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Starship extends Entity implements Functor {
    private static Polygon hullPolygon;
    private static Polygon mainThrusterPolygon;
    private static Polygon shieldPolygon;
    public static float SCALE = .85f;
    public static float MAXSPEED = 30.f;
    public static float SPIN = 4.f;
    public static float DENSITY = 1.f;
    public static float SHIELD_LIFE = 0.2f;
    public static Vec2 THRUST = new Vec2(0, 1.0f);
    public static int TYPE = 0x2;
    public static int MASK = Starship.TYPE | Rock.TYPE | Projectile.TYPE;// | Upgrade.TYPE
    private Body body;
    private boolean flickerOn = true;
    private float shieldLife;
    private String weapon = "Photon";
    private Vector3f color;
    
    public Task flickerTask = new Task(0.07f) {

        @Override
        public boolean update() {
            Starship.this.flickerOn = !Starship.this.flickerOn;
            return true;
        }
    };
    
    public Task hyperspaceTask = new Task(2.f) {
        @Override
        public boolean update() {
            return false;
        }
    };
    public Task rearmTask = new Task(0.2f) {
        @Override
        public boolean update() {
            return false;
        }
    };

    /**
     * Creates a new starship with the given color.
     *
     * @param color
     */
    public Starship(Vector3f color) {
        this.color = color;
        this.body = Asteroids.getBody(getHullPolygon(), TYPE, MASK, DENSITY);
        this.body.setTransform(Asteroids.getWorldSize().mul(.5f), .0f);
        this.body.setUserData(this);
        this.body.setLinearDamping(.4f);
    }

    /**
     * Applies forces to the starship, and updates the transform.
     *
     * @param delta
     */
    @Override
    public void update(float delta) {
        this.shieldLife = Math.max(0.f, this.shieldLife - delta);
        
        float speed = this.body.getLinearVelocity().length();
        float capped = Math.min(speed, MAXSPEED);
        if (speed > 0) {
            this.body.getLinearVelocity().x *= capped / speed;
            this.body.getLinearVelocity().y *= capped / speed;
        }
        
        Vec2 forward = this.body.getWorldVector(THRUST);
        if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
            this.body.applyLinearImpulse(forward.negate(), this.body.getWorldCenter());
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            this.body.applyLinearImpulse(forward, this.body.getWorldCenter());
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_J) && !Keyboard.isKeyDown(Keyboard.KEY_L)) {
            this.body.setAngularVelocity(-SPIN);
        } else if (Keyboard.isKeyDown(Keyboard.KEY_L) && !Keyboard.isKeyDown(Keyboard.KEY_J)) {
            this.body.setAngularVelocity(SPIN);
        } else {
            this.body.setAngularVelocity(0.f);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_H)) {
            hyperjump();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            fire();
        }
        Asteroids.wrapTransform(this.body);
    }

    public void hyperjump() {
        if (this.hyperspaceTask.isActive()) {
            return;
        }
        this.hyperspaceTask.setActive(true);
        this.body.setTransform(Asteroids.getRandomPos(), this.body.getAngle());
    }

    public void fire() {
        if (this.rearmTask.isActive()) {
            return;
        }
        try {
            Vec2 launchVec = this.body.getWorldVector(new Vec2(0.f, -2.2f));
            Class weaponClass = Class.forName("fishgames.asteroids." + this.weapon);
            Method weaponMethod = weaponClass.getMethod("getProjectile");
            Projectile projectile = (Projectile) weaponMethod.invoke(null);
            projectile.setPosition(this.body.getPosition().add(launchVec));
            projectile.setLaunchVector(launchVec, this.body.getLinearVelocity());
            this.rearmTask.setTimeout(projectile.getRearmTime());
            this.rearmTask.setActive(true);
        } catch (NoSuchMethodException ex) {
        } catch (SecurityException ex) {
        } catch (ClassNotFoundException ex) {
        } catch (IllegalAccessException ex) {
        } catch (IllegalArgumentException ex) {
        } catch (InvocationTargetException ex) {
        }
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
        this.shieldLife = SHIELD_LIFE;
    }

    @Override
    public void visit(Rock other) {
        //destroy();
        this.shieldLife = SHIELD_LIFE;
    }

    @Override
    public void visit(Starship other) {
        this.shieldLife = SHIELD_LIFE;
    }
    
    public Vector3f getColor() {
        return this.color;
    }
    
    public Body getBody() {
        return this.body;
    }
    
    public boolean isShieldVisible() {
        return this.shieldLife > 0.f;
    }

    public boolean isThrusterOn() {
        return Keyboard.isKeyDown(Keyboard.KEY_I);
    }
    
    public boolean isFlickerOn() {
        return this.flickerOn || !this.flickerTask.isActive();
    }
    
    public float getShieldLife() {
        return this.shieldLife;
    }
    
    public void destroy() {
    }
    
    /**
     * Returns the polygon shape for the hull of the starship.
     *
     * @return
     */
    public static Polygon getHullPolygon() {
        if (hullPolygon == null) {
            FloatBuffer vert = BufferUtils.createFloatBuffer(2 * 5);
            vert.put(0, 0);
            vert.put(1, 0);
            // These zeros is here because an offset of 1 is added to account
            // for the 'center' point needed for most polygons in the game, 
            // which is skipped in line-rendering mode.

            vert.put(2, Starship.SCALE * 0.f); // Aft # 0
            vert.put(3, Starship.SCALE * .8f);
            vert.put(4, Starship.SCALE * -1.8f); // Right wing # 1
            vert.put(5, Starship.SCALE * 1.8f);
            vert.put(6, Starship.SCALE * 0.f); // Bow # 2
            vert.put(7, Starship.SCALE * -2.2f);
            vert.put(8, Starship.SCALE * 1.8f); // Left wing # 3
            vert.put(9, Starship.SCALE * 1.8f);

            IntBuffer ind = BufferUtils.createIntBuffer(2 * 3);
            ind.put(0, 1);
            ind.put(1, 2);
            ind.put(2, 3);
            ind.put(3, 1);
            ind.put(4, 3);
            ind.put(5, 4);
            hullPolygon = new Polygon(vert, ind);

            Vec2[] triangle1 = new Vec2[3];
            triangle1[0] = new Vec2(vert.get(0), vert.get(1));
            triangle1[1] = new Vec2(vert.get(2), vert.get(3));
            triangle1[2] = new Vec2(vert.get(4), vert.get(5));
            PolygonShape shape1 = new PolygonShape();
            shape1.set(triangle1, triangle1.length);
            hullPolygon.addShape(shape1);

            Vec2[] triangle2 = new Vec2[3];
            triangle2[0] = new Vec2(vert.get(0), vert.get(1));
            triangle2[1] = new Vec2(vert.get(4), vert.get(5));
            triangle2[2] = new Vec2(vert.get(6), vert.get(7));
            PolygonShape shape2 = new PolygonShape();
            shape2.set(triangle2, triangle2.length);
            hullPolygon.addShape(shape2);
        }
        return hullPolygon;
    }

    /**
     * Returns the shape for the main thruster of the starship.
     *
     * @return
     */
    public static Polygon getMainThrusterPolygon() {
        if (mainThrusterPolygon == null) {
            FloatBuffer vert = BufferUtils.createFloatBuffer(2 * 4);
            vert.put(0, Starship.SCALE * 0.f); // Top
            vert.put(1, Starship.SCALE * 1.1f);
            vert.put(2, Starship.SCALE * -0.8f); // Left
            vert.put(3, Starship.SCALE * 1.7f);
            vert.put(4, Starship.SCALE * 0.f); // Bottom
            vert.put(5, Starship.SCALE * 3.0f);
            vert.put(6, Starship.SCALE * .8f); // Right
            vert.put(7, Starship.SCALE * 1.7f);
            mainThrusterPolygon = new Polygon(vert, null);
        }
        return mainThrusterPolygon;
    }

    public static Polygon getShieldPolygon() {
        if (shieldPolygon == null) {
            shieldPolygon = Polygon.getCircle(3.5f, 32);
        }
        return shieldPolygon;
    }
}
