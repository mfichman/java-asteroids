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
        fillColor = color;
        outlineColor = new Vector3f(1.f, 1.f, 1.f);
        outlineScale = new Vector3f(1.08f, 1.08f, 1.08f);
        FloatBuffer vert = BufferUtils.createFloatBuffer(2 * 4);
        vert.put(0, 0.f); // Aft # 0
        vert.put(1, 10.f);
        vert.put(2, -20.f); // Right wing # 1
        vert.put(3, 20.f);
        vert.put(4, 0.f); // Bow # 2
        vert.put(5, -20.f);
        vert.put(6, 20.f); // Left wing # 3
        vert.put(7, 20.f);
        setVertices(vert);
        
        Vec2[] boxVert = new Vec2[vert.limit()/2];
        for (int i = 0; i < boxVert.length; i++) {
            boxVert[i] = new Vec2(vert.get(2 * i), vert.get(2 * i + 1));
        }
        PolygonShape shape = new PolygonShape();
        //shape.set(boxVert, boxVert.length);
        shape.setAsBox(10, 10);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        this.body = Asteroids.world.createBody(bodyDef);
        this.body.createFixture(shape, 1.f);
        this.body.setTransform(new Vec2(1.f, 1.f), 0.f);
        
        IntBuffer ind = BufferUtils.createIntBuffer(2 * 3);
        ind.put(0, 0); // Right side
        ind.put(1, 1);
        ind.put(2, 2);
        ind.put(3, 0); // Left side
        ind.put(4, 3);
        ind.put(5, 2);   
        setIndices(ind);
        
        vert.put(0, 0.f);
        vert.put(1, 13.f);
        
        vert.put(2, -8.f);
        vert.put(3, 17.f);
        
        vert.put(4, 0.f);
        vert.put(5, 32.f);
        
        vert.put(6, 8.f);
        vert.put(7, 17.f);
        mainThruster.setVertices(vert);
        mainThruster.setIndices(ind);
        mainThruster.fillColor = new Vector3f(1.0f, .85f, 0.2f);
        //mainThruster.outlineColor = new Vector3f(1.0f, .2f, 0.0f);
        //mainThruster.outlineScale = new Vector3f(1.09f, 1.12f, 1.09f);
    }
    
    @Override
    public void render(float alpha) {  
        Vec2 forward = body.getWorldVector(new Vec2(0.f, 100.f));
        if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
            //this.y -= 0.2;
            body.applyLinearImpulse(forward.negate(), body.getWorldCenter());
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            //this.y += 0.2;
            body.applyLinearImpulse(forward, body.getWorldCenter());
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_J) && !Keyboard.isKeyDown(Keyboard.KEY_L)) {
            body.setAngularVelocity(-4.f);
        } else if (Keyboard.isKeyDown(Keyboard.KEY_L) && !Keyboard.isKeyDown(Keyboard.KEY_J)) {
            body.setAngularVelocity(4.f);
        } else {
            body.setAngularVelocity(0.f);
        }
        
        Vec2 pos = this.body.getPosition();
        Vec2 vel = this.body.getLinearVelocity();
               
        Vec2 pos1 = pos.mul(1 - alpha);
        Vec2 pos2 = (pos.add(vel.mul(1.f/60.f))).mul(alpha);
        Vec2 finalPos = pos1.add(pos2).mul(10);    
          
        glPushMatrix();
        glTranslatef(finalPos.x, finalPos.y, 0.f);
        glRotatef((float)(this.body.getAngle() * 180.f / Math.PI), 0, 0, 1.f); // Rotate around z-axis
        
        super.render();
        if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
            mainThruster.render();
        }
        
        glPopMatrix();
    }
    

    

}
