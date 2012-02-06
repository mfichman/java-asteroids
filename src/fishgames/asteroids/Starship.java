/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Starship extends OutlinedObject implements Renderable {


    /** Angle of rotation */
    private float rotation;
    
    /** Position */
    public float x = 200;
    private float y = 200;
    
    /** Velocity */
    public float dx = 200;
    public float dy = 200;
    
    private OutlinedObject mainThruster = new OutlinedObject();
    private OutlinedObject leftThruster = new OutlinedObject();
    private OutlinedObject rightThruster = new OutlinedObject();
    
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
    public void render() {    
        glPushMatrix();
        glTranslatef(this.x, this.y, 0.f);
        glRotatef(this.rotation, 0, 0, 1.f); // Rotate around z-axis
        
        if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
            this.y -= 0.2;
            mainThruster.render();
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
            this.y += 0.2;
        }

        super.render();
        
        glPopMatrix();
    }
    

    

}
