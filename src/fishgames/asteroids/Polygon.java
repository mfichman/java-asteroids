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

/**
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Polygon implements Renderable {

    /**
     * Vertex buffer object handle.
     */
    private int vertices;
    /**
     * Index buffer object handle.
     */
    private int indices;
    /**
     * Number of triangles
     */
    private int numElements;

    public Polygon(FloatBuffer vert, IntBuffer ind) {
        IntBuffer intBuf = BufferUtils.createIntBuffer(2);
        glGenBuffers(intBuf);
        this.vertices = intBuf.get(0);
        this.indices = intBuf.get(1);

        glBindBuffer(GL_ARRAY_BUFFER, this.vertices);
        glBufferData(GL_ARRAY_BUFFER, vert, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ind, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        this.numElements = ind.limit();
    }

    @Override
    public void render() {
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
