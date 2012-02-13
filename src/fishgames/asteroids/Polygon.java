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
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL15.*;

/**
 * Holder for a polygon (graphical and physical models). Since all the objects
 * in the game are polygons, most of them use this in some way.
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class Polygon {

    /**
     * Vertex buffer object handle.
     */
    private int vertices;
    /**
     * Index buffer object handle.
     */
    private int indices;
    private int numVertices;
    private int numIndices;
    /**
     * Polygon shape for Box2D
     */
    private ArrayList<Shape> shape;

    public Polygon(FloatBuffer vert, IntBuffer ind) {
        IntBuffer intBuf = BufferUtils.createIntBuffer(1);
        glGenBuffers(intBuf);

        this.vertices = intBuf.get(0);
        this.numVertices = vert.limit() / 2;
        glBindBuffer(GL_ARRAY_BUFFER, this.vertices);
        glBufferData(GL_ARRAY_BUFFER, vert, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        if (ind != null) {
            glGenBuffers(intBuf);
            this.indices = intBuf.get(0);
            this.numIndices = ind.limit();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.indices);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, ind, GL_STATIC_DRAW);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    /**
     * Adds a new shape (doesn't affect the look of the polygon; it's just
     * convenient to have the physical model attached to the graphical one).
     *
     * @param shape
     */
    public void addShape(Shape shape) {
        if (this.shape == null) {
            this.shape = new ArrayList<Shape>();
        }
        this.shape.add(shape);
    }

    /**
     * Return a list of graphical models used to create fixtures for objects
     * that use this polygon for physical simulations.
     *
     * @return
     */
    public List<Shape> getShapes() {
        return this.shape;
    }

    /**
     * Releases this object (specifically, by freeing the hardware vertex and
     * index buffers).
     */
    public void release() {
        IntBuffer buf = BufferUtils.createIntBuffer(2);
        buf.put(0, this.getVertices());
        buf.put(1, this.getIndices());
        glDeleteBuffers(buf);
        this.vertices = 0;
        this.indices = 0;
    }

    public static Polygon getCircle(float radius, int segments) {
        // Create one vertex per segment.  Each vertex has 2 coordinates.
        FloatBuffer vert = BufferUtils.createFloatBuffer(2 * segments);
        double angle = 2. * Math.PI / segments;
        for (int i = 0; i < segments; i++) {
            double px = (radius) * Math.cos(angle * i);
            double py = (radius) * Math.sin(angle * i);
            vert.put(i * 2 + 0, (float) px);
            vert.put(i * 2 + 1, (float) py);
        }

        CircleShape shape = new CircleShape();
        shape.m_radius = radius;
        Polygon polygon = new Polygon(vert, null);
        polygon.addShape(shape);
        return polygon;
    }

    public static Polygon getSquare(float size) {
        FloatBuffer vert = BufferUtils.createFloatBuffer(8);

        vert.put(0, size); // Upper left
        vert.put(1, size);

        vert.put(2, size); // Upper right
        vert.put(3, -size);

        vert.put(4, -size); // Lower right
        vert.put(5, -size);

        vert.put(6, -size); // Lower left
        vert.put(7, size);

        Polygon polygon = new Polygon(vert, null);
        Vec2[] triangle1 = new Vec2[3];
        triangle1[0] = new Vec2(vert.get(0), vert.get(1));
        triangle1[1] = new Vec2(vert.get(2), vert.get(3));
        triangle1[2] = new Vec2(vert.get(4), vert.get(5));
        PolygonShape shape1 = new PolygonShape();
        shape1.set(triangle1, triangle1.length);
        polygon.addShape(shape1);

        Vec2[] triangle2 = new Vec2[3];
        triangle2[0] = new Vec2(vert.get(0), vert.get(1));
        triangle2[1] = new Vec2(vert.get(4), vert.get(5));
        triangle2[2] = new Vec2(vert.get(6), vert.get(7));
        PolygonShape shape2 = new PolygonShape();
        shape2.set(triangle2, triangle2.length);
        polygon.addShape(shape2);
        return polygon;
    }

    /**
     * @return the vertices
     */
    public int getVertices() {
        return vertices;
    }

    /**
     * @return the vertices
     */
    public int getIndices() {
        return indices;
    }

    /**
     * @return the numElements
     */
    public int getNumIndices() {
        return numIndices;
    }

    public int getNumVertices() {
        return numVertices;
    }
}
