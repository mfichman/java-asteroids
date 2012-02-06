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
import static org.lwjgl.opengl.GL15.*;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
class OutlinedObject {
    
    /** Vertex buffer object handle. */
    private int vertices;
    
    /** Index buffer object handle. */
    private int indices;
    
    /** Number of triangles */
    private int numElements; 
    
    protected Vector3f fillColor;
    protected Vector3f outlineColor;
    protected Vector3f outlineScale;
    
    public OutlinedObject() {
        IntBuffer intBuf = BufferUtils.createIntBuffer(2);
        glGenBuffers(intBuf);
        this.vertices = intBuf.get(0);
        this.indices = intBuf.get(1);
    }
    
    public void setVertices(FloatBuffer vert) {
        glBindBuffer(GL_ARRAY_BUFFER, this.vertices);
        glBufferData(GL_ARRAY_BUFFER, vert, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    public void setIndices(IntBuffer ind) {
        this.numElements = ind.limit();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ind, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    public void render() {
        if (outlineColor != null) {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(-20f, -20f);        
            glPushMatrix();
            glScalef(this.outlineScale.x, this.outlineScale.y, this.outlineScale.z);       
            glColor3f(this.outlineColor.x, this.outlineColor.y, this.outlineColor.z);
            renderVertices();
            glPopMatrix();
            glDisable(GL_POLYGON_OFFSET_FILL);
        }
        
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glColor3f(this.fillColor.x, this.fillColor.y, this.fillColor.z);
        renderVertices();
    }
    
    private void renderVertices() {
        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, this.vertices);
        glVertexPointer(2, GL_FLOAT, 0, 0);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indices);
        glDrawElements(GL_TRIANGLES, this.numElements, GL_UNSIGNED_INT, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    public void release() {
        IntBuffer buf = BufferUtils.createIntBuffer(2);
        buf.put(0, this.vertices);
        buf.put(0, this.indices);
        glDeleteBuffers(buf);
        this.vertices = 0;
        this.indices = 0;
    }
}
