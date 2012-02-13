/*
 * Copyright 2012 FishGames.
 * FishGames Proprietary and Confidential.
 *
 */
package fishgames.asteroids;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import org.lwjgl.util.vector.Vector3f;

/**
 * Object with a nice outline, created by drawing the primitives once using the
 * outline color (and scaled up slightly) and a second time with the fill color
 * (normal scaling). The scale factor requires tweaking depending on the object
 * size. Ideally, this would not be necessary, but it's easier just to tweak the
 * outline for each object (and it adds some 'approximation' to the look of the
 * game, so it's not so manufactured-looking).
 *
 * @author Matt Fichman <matt.fichman@gmail.com>
 */
public class OutlinedObject implements Renderable {

    protected Polygon polygon;
    protected Vector3f fillColor;
    protected Vector3f outlineColor;
    protected float alpha = 1.0f;

    /**
     * Updates the object (unused, no animation for this object type).
     *
     * @param delta
     */
    @Override
    public void update(float delta) {
    }

    /**
     * Renders this outlined object with interpolation constant alpha (unused,
     * no animation for this one).
     *
     * @param alpha
     */
    @Override
    public void render(float alpha) {
        if (this.alpha != 1.0f) {
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, this.polygon.getVertices());
        glVertexPointer(2, GL_FLOAT, 0, 0);
        if (this.getFillColor() != null) {
            glColor4f(this.getFillColor().x, this.getFillColor().y, this.getFillColor().z, this.alpha);
            if (this.polygon.getNumIndices() > 0) {
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.polygon.getIndices());
                glDrawElements(GL_TRIANGLES, this.polygon.getNumIndices() * 1000, GL_UNSIGNED_INT, 0);
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            } else {
                glDrawArrays(GL_POLYGON, 0, this.polygon.getNumVertices());
            }
        }

        if (this.getOutlineColor() != null) {
            glLineWidth(1.5f);
            glColor4f(this.getOutlineColor().x, this.getOutlineColor().y, this.getOutlineColor().z, this.alpha);
            if (this.polygon.getNumIndices() > 0) {
                glDrawArrays(GL_LINE_LOOP, 1, this.polygon.getNumVertices()-1);
            } else {
                glDrawArrays(GL_LINE_LOOP, 0, this.polygon.getNumVertices());
            }
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDisableClientState(GL_VERTEX_ARRAY);

        if (this.alpha != 1.0f) {
            glDisable(GL_BLEND);
        }
    }

    /**
     * @return the fillColor
     */
    public Vector3f getFillColor() {
        return fillColor;
    }

    /**
     * @return the outlineColor
     */
    public Vector3f getOutlineColor() {
        return outlineColor;
    }
}
