/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Starship extends OutlinedObject implements Renderable {
    
    private OutlinedObject mainThruster = new OutlinedObject();
    private OutlinedObject leftThruster = new OutlinedObject();
    private OutlinedObject rightThruster = new OutlinedObject();
    private Body body;
    
    public Starship(Vector3f color) {
        
            /*    
        Vec2[] boxVert = new Vec2[ind.limit()];
        for (int i = 0; i < ind.limit(); i++) {
            float px = .1f * vert.get(2 * ind.get(i));
            float py = .1f * vert.get(2 * ind.get(i) + 1);
            boxVert[i] = new Vec2(px, py);
            System.out.printf("%f %f\n", px, py);
        }*/
        this.fillColor = color;
        this.outlineColor = new Vector3f(1.f, 1.f, 1.f);
        this.outlineScale = new Vector3f(1.08f, 1.08f, 1.08f);
        this.polygon = getHullPolygon();

        this.mainThruster.polygon = getMainThrusterPolygon();
        this.mainThruster.fillColor = new Vector3f(1.0f, .85f, 0.2f);

       
        PolygonShape shape = new PolygonShape();
        //shape.set(boxVert, boxVert.length);
        shape.setAsBox(10, 10);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        this.body = Asteroids.world.createBody(bodyDef);
        this.body.createFixture(shape, 1.f);
        this.body.setTransform(new Vec2(10.f, 10.f), 0.f);
        
    }
    
    @Override
    public void render(float alpha) {
        Vec2 forward = this.body.getWorldVector(new Vec2(0.f, 4.f));
        if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
            this.body.applyLinearImpulse(forward.negate(), this.body.getWorldCenter());
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            this.body.applyLinearImpulse(forward, this.body.getWorldCenter());
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_J) && !Keyboard.isKeyDown(Keyboard.KEY_L)) {
            this.body.setAngularVelocity(-4.f);
        } else if (Keyboard.isKeyDown(Keyboard.KEY_L) && !Keyboard.isKeyDown(Keyboard.KEY_J)) {
            this.body.setAngularVelocity(4.f);
        } else {
            this.body.setAngularVelocity(0.f);
        }
        
        Vec2 pos = this.body.getPosition();
        Vec2 vel = this.body.getLinearVelocity();
        Vec2 pos1 = pos.mul(1 - alpha);
        Vec2 pos2 = (pos.add(vel.mul(1.f/60.f))).mul(alpha);
        Vec2 finalPos = pos1.add(pos2).mul(10);    
          
        glPushMatrix();
        glTranslatef(finalPos.x, finalPos.y, 0.f);
        glRotatef((float)(this.body.getAngle() * 180.f / Math.PI), 0, 0, 1.f); 
        // Rotate around z-axis
        
        super.render(alpha);
        if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
            this.mainThruster.render(alpha);
        }
        glPopMatrix();
    }
    
    static Polygon getHullPolygon() {
        if (hullPolygon == null) {
            FloatBuffer vert = BufferUtils.createFloatBuffer(2 * 4);
            vert.put(0, 0.f); // Aft # 0
            vert.put(1, 10.f);
            vert.put(2, -20.f); // Right wing # 1
            vert.put(3, 20.f);
            vert.put(4, 0.f); // Bow # 2
            vert.put(5, -20.f);
            vert.put(6, 20.f); // Left wing # 3
            vert.put(7, 20.f);

            IntBuffer ind = BufferUtils.createIntBuffer(2 * 3);
            ind.put(0, 0); // Right side
            ind.put(1, 1);
            ind.put(2, 2);
            ind.put(3, 0); // Left side
            ind.put(4, 3);
            ind.put(5, 2);
            hullPolygon = new Polygon(vert, ind);
        }
        return hullPolygon;
    }
    
    static Polygon getMainThrusterPolygon() {
        if (mainThrusterPolygon == null) {
            FloatBuffer vert = BufferUtils.createFloatBuffer(2 * 4);
            vert.put(0, 0.f);
            vert.put(1, 13.f);
            vert.put(2, -8.f);
            vert.put(3, 18.f);
            vert.put(4, 0.f);
            vert.put(5, 32.f);
            vert.put(6, 8.f);
            vert.put(7, 18.f);

            IntBuffer ind = BufferUtils.createIntBuffer(2 * 3);
            ind.put(0, 0); // Right side
            ind.put(1, 1);
            ind.put(2, 2);
            ind.put(3, 0); // Left side
            ind.put(4, 3);
            ind.put(5, 2);
            mainThrusterPolygon = new Polygon(vert, ind);
        }
        return mainThrusterPolygon;
    }
    
    static Polygon hullPolygon;
    static Polygon mainThrusterPolygon;
}
