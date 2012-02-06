/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Rock extends OutlinedObject implements Renderable {

    /** Angle of rotation */
    private float rotation;
    
    /** Position */
    public float x = 200;
    private float y = 200;
    
    /** Velocity */
    public float dx = 200;
    public float dy = 200;
    
    /**
     * Creates a new randomly-shaped Rock object with the given diameter.
     *
     * @param diameter size of the rock
     */
    public Rock(float diameter, int segments) {
        outlineColor = new Vector3f(.6f, .6f, .6f);
        fillColor = new Vector3f(.2f, .2f, .2f);
        outlineScale = new Vector3f(1.02f, 1.02f, 1.02f);
        
        // Create one vertex per segment.  Each vertex has 2 coordinates.
        // Remember to save one vertex for the center point.
        FloatBuffer vert = BufferUtils.createFloatBuffer(2 * (segments + 1));
        double angle = 2. * Math.PI / segments;
        for (int i = 0; i < segments; i++) {
            // Skew the angle and diameter by up to 10% in either direction.
            double diameterSkew = (Math.random() - 0.5) * diameter * 0.3;
            double angleSkew = 0; //(Math.random() - 0.5) * angle;
            double px = (diameterSkew + diameter) * Math.cos(angleSkew + angle * i);
            double py = (diameterSkew + diameter) * Math.sin(angleSkew + angle * i);
            vert.put((i + 1) * 2 + 0, (float)px);
            vert.put((i + 1) * 2 + 1, (float)py);
            
        }
        vert.put(0, 0); // The center point is (0, 0, 0)
        vert.put(0, 1);        
        setVertices(vert);
        
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
        setIndices(ind);
    }
    
    @Override
    public void render() {
        glPushMatrix();
        glTranslatef(this.x, this.y, 0.f);
        glRotatef(this.rotation, 0, 0, 1.f); // Rotate around z-axis
        this.rotation += 0.2;
        super.render();
        glPopMatrix();
    }
}
