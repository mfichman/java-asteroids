/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.jbox2d.collision.shapes.PolygonShape;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

/**
 * Holder for a polygon (graphical and physical models). Since all the objects
 * in the game are polygons, most of them use this in some way.
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
    /**
     * Polygon shape for Box2D
     */
    private ArrayList<PolygonShape> shape;

    public Polygon(FloatBuffer vert, IntBuffer ind, boolean makeShape) {
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

    /**
     * Adds a new shape (doesn't affect the look of the polygon; it's just
     * convenient to have the physical model attached to the graphical one).
     *
     * @param shape
     */
    public void addShape(PolygonShape shape) {
        if (this.shape == null) {
            this.shape = new ArrayList<PolygonShape>();
        }
        this.shape.add(shape);
    }

    /**
     * Return a list of graphical models used to create fixtures for objects
     * that use this polygon for physical simulations.
     *
     * @return
     */
    public List<PolygonShape> getShapes() {
        return this.shape;
    }

    /**
     * Updates this object. Unused; no animation.
     */
    @Override
    public void update() {
    }

    /**
     * Renders this object with interpolation constant alpha.
     *
     * @param alpha
     */
    @Override
    public void render(float alpha) {
        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, this.vertices);
        glVertexPointer(2, GL_FLOAT, 0, 0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indices);
        glDrawElements(GL_TRIANGLES, this.numElements, GL_UNSIGNED_INT, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Releases this object (specifically, by freeing the hardware vertex and
     * index buffers).
     */
    public void release() {
        IntBuffer buf = BufferUtils.createIntBuffer(2);
        buf.put(0, this.vertices);
        buf.put(0, this.indices);
        glDeleteBuffers(buf);
        this.vertices = 0;
        this.indices = 0;
    }
}
